/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.MgnlKeyPair;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.safehaus.uuid.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * This filter receives activation requests from another instance and applies them.
 *
 * @author Sameer Charles
 * $Id$
 */
public class ReceiveFilter extends AbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(ReceiveFilter.class);

    private int unlockRetries = 10;

    private int retryWait = 2;

    private final ExchangeSimpleModule module;
    private final MessageDigest md;


    @Inject
    public ReceiveFilter(ExchangeSimpleModule module) {
        this.module = module;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("In order to proceed with activation please run Magnolia CMS using Java version with MD5 support.", e);
        }
    }

    public int getUnlockRetries() {
        return unlockRetries;
    }

    public void setUnlockRetries(int unlockRetries) {
        this.unlockRetries = unlockRetries;
    }

    public long getRetryWait() {
        return retryWait;
    }

    public void setRetryWait(int retryWait) {
        this.retryWait = retryWait;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String statusMessage = "";
        String status = "";
        String result = null;
        try {
            final String utf8AuthorStatus = request.getHeader(BaseSyndicatorImpl.UTF8_STATUS);
            // null check first to make sure we do not break activation from older versions w/o this flag
            if (utf8AuthorStatus != null && (Boolean.parseBoolean(utf8AuthorStatus) != SystemProperty.getBooleanProperty(SystemProperty.MAGNOLIA_UTF8_ENABLED))) {
                throw new UnsupportedOperationException("Activation between instances with different UTF-8 setting is not supported.");
            }
            final String action = request.getHeader(BaseSyndicatorImpl.ACTION);
            if (action == null) {
                throw new InvalidParameterException("Activation action must be set for each activation request.");
            }

            // verify the author ... if not trusted yet, but no exception thrown, then we attempt to establish trust
            if (!isAuthorAuthenticated(request, response)) {
                status = BaseSyndicatorImpl.ACTIVATION_HANDSHAKE;
                setResponseHeaders(response, statusMessage, status, result);
                return;
            }
            // we do not lock the content on handshake requests
            applyLock(request);
        }catch (Throwable e) {
            log.error(e.getMessage(), e);
            // we can only rely on the exception's actual message to give something back to the user here.
            statusMessage = StringUtils.defaultIfEmpty(e.getMessage(), e.getClass().getSimpleName());
            status = BaseSyndicatorImpl.ACTIVATION_FAILED;
            setResponseHeaders(response, statusMessage, status, result);
            return;
        }

        try{
            result = receive(request);
            status = BaseSyndicatorImpl.ACTIVATION_SUCCESSFUL;
        }
        catch (OutOfMemoryError e) {
            Runtime rt = Runtime.getRuntime();
            log.error("---------\nOutOfMemoryError caught during activation. Total memory = "
                    + rt.totalMemory()
                    + ", free memory = "
                    + rt.freeMemory()
                    + "\n---------");
            statusMessage = e.getMessage();
            status = BaseSyndicatorImpl.ACTIVATION_FAILED;
        }
        catch (PathNotFoundException e) {
            // this should not happen. PNFE should be already caught and wrapped in ExchangeEx
            log.error(e.getMessage(), e);
            statusMessage = "Parent not found (not yet activated): " + e.getMessage();
            status = BaseSyndicatorImpl.ACTIVATION_FAILED;
        } catch (ExchangeException e) {
            log.debug(e.getMessage(), e);
            statusMessage = e.getMessage();
            status = BaseSyndicatorImpl.ACTIVATION_FAILED;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            // we can only rely on the exception's actual message to give something back to the user here.
            statusMessage = StringUtils.defaultIfEmpty(e.getMessage(), e.getClass().getSimpleName());
            status = BaseSyndicatorImpl.ACTIVATION_FAILED;
        }
        finally {
            cleanUp(request, status);
            setResponseHeaders(response, statusMessage, status, result);
        }
    }

    protected boolean isAuthorAuthenticated(HttpServletRequest request, HttpServletResponse response) throws NoSuchAlgorithmException, ExchangeException {
        if (SecurityUtil.getPublicKey() == null) {
            if (module.getTempKeys() == null) {
                // no temp keys set or module reset wiped them out
                MgnlKeyPair tempKeys = SecurityUtil.generateKeyPair(module.getActivationKeyLength());
                // we use a temp key to encrypt authors public key for transport ... intercepting this key will not anyone allow to decrypt public key sent by the author
                response.addHeader(BaseSyndicatorImpl.ACTIVATION_AUTH, tempKeys.getPublicKey());
                module.setTempKeys(tempKeys);
                return false;
            } else {
                try {
                    // we have temp keys so we expect that this time around we are getting the public key to store
                    String authorsPublicKeyEncryptedByTempPublicKey = request.getHeader(BaseSyndicatorImpl.ACTIVATION_AUTH_KEY);
                    // use our private key to decrypt
                    String publicKey = SecurityUtil.decrypt(authorsPublicKeyEncryptedByTempPublicKey, module.getTempKeys().getPrivateKey());
                    if (StringUtils.isNotBlank(publicKey)) {
                        String authString = SecurityUtil.decrypt(request.getHeader(BaseSyndicatorImpl.ACTIVATION_AUTH), publicKey);
                        String[] auth = authString.split(";");
                        checkTimestamp(auth);
                        // no private key for public
                        // TODO: what about chain of instances? should we not store 2 sets - one generated by the instance (and possibly passed on) and one (public key only) received by the instance?
                        SecurityUtil.updateKeys(new MgnlKeyPair(null, publicKey));
                    }
                } finally {
                    // cleanup temp keys no matter what
                    module.setTempKeys(null);
                }
                if (SecurityUtil.getPublicKey() == null) {
                    // we are too fast and trying before observation had a chance to kick in
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    if (SecurityUtil.getPublicKey() == null) {
                        throw new ExchangeException("Failed to negotiate encryption key between author and public instance. Please try again later or contact admin if error persists.");
                    }
                }
            }
        }
        return true;
    }

    protected void setResponseHeaders(HttpServletResponse response, String statusMessage, String status, String result) {
        response.setHeader(BaseSyndicatorImpl.ACTIVATION_ATTRIBUTE_STATUS, status);
        response.setHeader(BaseSyndicatorImpl.ACTIVATION_ATTRIBUTE_MESSAGE, statusMessage);
    }

    /**
     * handle activate or deactivate request.
     * @param request
     * @throws Exception if fails to update
     */
    protected synchronized String receive(HttpServletRequest request) throws Exception {
        String action = request.getHeader(BaseSyndicatorImpl.ACTION);
        log.debug("action: " + action);

        String[] auth = checkAuthenticated(request);

        String user = auth[1];

        String resourcesmd5 = auth[2];

        // TODO : this variable is used in log messages to identify the instance - but its content is really the folder name into which Magnolia was deployed, which is irrelevant.
        String webapp = getWebappName();

        if (action.equalsIgnoreCase(BaseSyndicatorImpl.ACTIVATE)) {
            String name = update(request, resourcesmd5);
            // Everything went well
            log.info("User {} successfuly activated {} on {}.", new Object[] { user, name, webapp });
        }
        else if (action.equalsIgnoreCase(BaseSyndicatorImpl.DEACTIVATE)) {
            String name = remove(request, resourcesmd5);
            // Everything went well
            log.info("User {} succeessfuly deactivated {} on {}.", new Object[] { user, name, webapp });
        }
        else {
            throw new UnsupportedOperationException("Method not supported : " + action);
        }
        return null;
    }

    protected String[] checkAuthenticated(HttpServletRequest request) throws ExchangeException {
        String encrypted = request.getHeader(BaseSyndicatorImpl.ACTIVATION_AUTH);
        if (StringUtils.isBlank(encrypted)) {
            log.debug("Attempt to access activation URL w/o proper information in request. Ignoring silently.");
            throw new ExchangeException();
        }

        String decrypted = SecurityUtil.decrypt(encrypted);
        if (StringUtils.isBlank(decrypted)) {
            throw new SecurityException("Handshake information for activation was incorrect. Someone attempted to impersonate author instance. Incoming request was from " + request.getRemoteAddr());
        }

        String[] auth = decrypted.split(";");

        // timestamp;user;resourcemd;optional_encrypted_public_key
        if (auth.length != 3) {
            throw new SecurityException("Handshake information for activation was incorrect. Someone attempted to impersonate author instance. Incoming request was from " + request.getRemoteAddr());
        }
        // first part is a timestamp
        checkTimestamp(auth);
        return auth;
    }

    private void checkTimestamp(String[] auth) {
        long timestamp = System.currentTimeMillis();
        long authorTimestamp = 0;
        try {
            authorTimestamp = Long.parseLong(auth[0]);
        } catch (NumberFormatException e) {
            throw new SecurityException("Handshake information for activation was incorrect. This might be an attempt to replay earlier activation request.");
        }
        if (Math.abs(timestamp - authorTimestamp) > module.getActivationDelayTolerance()) {
            throw new SecurityException("Activation refused due to request arriving too late or time not synched between author and public instance. Please contact your administrator to ensure server times are synced or the tolerance is set high enough to counter the differences.");
        }
    }

    protected String getWebappName() {
        return SystemProperty.getProperty(SystemProperty.MAGNOLIA_WEBAPP);
    }

    /**
     * @deprecated since 4.5. This method is not used anymore and there is no replacement. Authentication of activation is now handled by exchange of info encrypted by PPKey.
     */
    @Deprecated
    protected String getUser(HttpServletRequest request) {
        return null;
    }

    /**
     * handle update (activate) request.
     * 
     * @param request
     *            incoming reuqest
     * @param resourcesmd5
     *            signature confirming validity of resource file
     * @throws Exception
     *             if fails to update
     */
    protected synchronized String update(HttpServletRequest request, String resourcesmd5) throws Exception {
        MultipartForm data = MgnlContext.getPostedForm();
        if (null != data) {
            String newParentPath = this.getParentPath(request);
            String resourceFileName = request.getHeader(BaseSyndicatorImpl.RESOURCE_MAPPING_FILE);
            HierarchyManager hm = getHierarchyManager(request);
            Element rootElement = getImportedContentRoot(data, resourceFileName, resourcesmd5);
            Element topContentElement = rootElement.getChild(BaseSyndicatorImpl.RESOURCE_MAPPING_FILE_ELEMENT);
            Content content = null;
            try {
                String uuid = topContentElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_UUID_ATTRIBUTE);
                content = hm.getContentByUUID(uuid);
                // move content to new location if necessary.
                newParentPath = handleMovedContent(newParentPath, hm, topContentElement, content);
                handleChildren(request, content);
                this.importOnExisting(topContentElement, data, hm, content);
            }
            catch (ItemNotFoundException e) {
                // new content
                importFresh(topContentElement, data, hm, newParentPath);
            }

            return orderImportedNode(newParentPath, hm, rootElement, topContentElement);
        }
        return null;
    }

    protected Element getImportedContentRoot(MultipartForm data, String resourceFileName, String resourcesmd5) throws JDOMException, IOException {
        Document resourceDocument = data.getDocument(resourceFileName);
        SAXBuilder builder = new SAXBuilder();
        InputStream documentInputStream = new DigestInputStream(resourceDocument.getStream(), md);
        org.jdom.Document jdomDocument = builder.build(documentInputStream);
        IOUtils.closeQuietly(documentInputStream);
        String sign = SecurityUtil.byteArrayToHex(md.digest());
        if (!resourcesmd5.equals(sign)) {
            throw new SecurityException("Signature of received resource (" + sign + ") doesn't match expected signature (" + resourcesmd5 + "). This might mean that the activation operation have been intercepted by a third party and content have been modified during transfer.");
        }

        return jdomDocument.getRootElement();
    }

    protected void handleChildren(HttpServletRequest request, Content content) {
        String ruleString = request.getHeader(BaseSyndicatorImpl.CONTENT_FILTER_RULE);
        Rule rule = new Rule(ruleString, ",");
        RuleBasedContentFilter filter = new RuleBasedContentFilter(rule);
        // remove all child nodes
        this.removeChildren(content, filter);
    }

    protected String handleMovedContent(String newParentPath, HierarchyManager hm, Element topContentElement, Content content) throws RepositoryException {
        String currentParentPath = content.getHandle();
        currentParentPath = currentParentPath.substring(0, currentParentPath.lastIndexOf('/'));
        String newName = topContentElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_NAME_ATTRIBUTE);
        if (!newParentPath.endsWith("/")) {
            newParentPath += "/";
        }
        if (!currentParentPath.endsWith("/")) {
            currentParentPath += "/";
        }
        if (!newParentPath.equals(currentParentPath) || !content.getName().equals(newName)) {
            log.info("Moving content from {} to {} due to activation request.", new Object[] { content.getHandle(), newParentPath  + newName});
            hm.moveTo(content.getHandle(), newParentPath + newName);
        }
        return newParentPath;
    }

    protected String orderImportedNode(String newParentPath, HierarchyManager hm, Element rootElement, Element topContentElement) throws RepositoryException {
        String name;
        // order imported node
        name = topContentElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_NAME_ATTRIBUTE);
        Content parent = hm.getContent(newParentPath);
        List siblings = rootElement.getChild(BaseSyndicatorImpl.SIBLINGS_ROOT_ELEMENT).getChildren(BaseSyndicatorImpl.SIBLINGS_ELEMENT);
        Iterator siblingsIterator = siblings.iterator();
        while (siblingsIterator.hasNext()) {
            Element sibling = (Element) siblingsIterator.next();
            // check for existence and order
            try {
                String siblingUUID = sibling.getAttributeValue(BaseSyndicatorImpl.SIBLING_UUID);
                Content beforeContent = hm.getContentByUUID(siblingUUID);
                log.debug("Ordering {} before {}", name, beforeContent.getName());
                order(parent, name, beforeContent.getName());
                break;
            } catch (ItemNotFoundException e) {
                // ignore
            } catch (RepositoryException re) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to order node", re);
                } else {
                    log.warn("Failed to order node");
                }
            }
        }

        // ensure the no sibling nodes are at the end ... since move is not activated immediately it is sometimes necessary to preserve right order
        if (siblings.isEmpty()) {
            order(parent, name, null);
        }
        return name;
    }


    protected void order(Content parent, String name, String orderBefore) throws RepositoryException {
        try {
            parent.orderBefore(name, orderBefore);
        } catch (UnsupportedRepositoryOperationException e) {
            // since not all types support ordering we should not enforce it, but only log the error
            log.warn("Failed to order unorderable content {} at {} due to {}", new Object[] {name, parent.getHandle(), e.getMessage()});
        }
        parent.save();
    }

    /**
     * Copy all properties from source to destination (by cleaning the old properties).
     * @param source the content node to be copied
     * @param destination the destination node
     */
    protected synchronized void copyProperties(Content source, Content destination) throws RepositoryException {
        // first remove all existing properties at the destination
        // will be different with incremental activation
        Iterator nodeDataIterator = destination.getNodeDataCollection().iterator();
        while (nodeDataIterator.hasNext()) {
            NodeData nodeData = (NodeData) nodeDataIterator.next();
            // Ignore binary types, since these are sub nodes and already taken care of while
            // importing sub resources
            if (nodeData.getType() != PropertyType.BINARY) {
                nodeData.delete();
            }
        }

        // copy all properties
        Node destinationNode = destination.getJCRNode();
        nodeDataIterator = source.getNodeDataCollection().iterator();
        while (nodeDataIterator.hasNext()) {
            NodeData nodeData = (NodeData) nodeDataIterator.next();
            Property property = nodeData.getJCRProperty();
            if (property.getDefinition().isMultiple()) {
                if (destination.isGranted(Permission.WRITE)) {
                    destinationNode.setProperty(nodeData.getName(), property.getValues());
                }
                else {
                    throw new AccessDeniedException("User not allowed to " + Permission.PERMISSION_NAME_WRITE + " at [" + nodeData.getHandle() + "]");
                }
            }
            else {
                destination.createNodeData(nodeData.getName(), nodeData.getValue());
            }
        }
    }

    /**
     * remove children.
     * @param content whose children to be deleted
     * @param filter content filter
     */
    protected synchronized void removeChildren(Content content, Content.ContentFilter filter) {
        Iterator children = content.getChildren(filter).iterator();
        // remove sub nodes using the same filter used by the sender to collect
        // this will make sure there is no existing nodes of the same type
        while (children.hasNext()) {
            Content child = (Content) children.next();
            try {
                child.delete();
            }
            catch (Exception e) {
                log.error("Failed to remove " + child.getHandle() + " | " + e.getMessage());
            }
        }
    }

    /**
     * import on non existing tree.
     * @param topContentElement
     * @param data
     * @param hierarchyManager
     * @param parentPath
     * @throws ExchangeException
     * @throws RepositoryException
     */
    protected synchronized void importFresh(Element topContentElement, MultipartForm data, HierarchyManager hierarchyManager, String parentPath) throws ExchangeException, RepositoryException {
        // content might still exists under different uuid if it was auto generated. Check the path first, if exists, then remove it before activating new content into same path
        // TODO: handle same name siblings!
        String path = parentPath + (parentPath.endsWith("/") ? "" : "/") + topContentElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_NAME_ATTRIBUTE);
        if (hierarchyManager.isExist(path)) {
            log.warn("Replacing {} due to name collision (but different UUIDs.). This operation could not be rolled back in case of failure and you need to reactivate the page manually.", path);
            hierarchyManager.delete(path);
        }
        try {
            importResource(data, topContentElement, hierarchyManager, parentPath);
            hierarchyManager.save();
        } catch (PathNotFoundException e) {
            final String  message = "Parent content " + parentPath + " is not yet activated or you do not have write access to it. Please activate the parent content before activating children and ensure you have appropriate rights"; // .. on XXX will be appended to the error message by syndicator on the author instance
            // this is not a system error so there should not be a need to log the exception all the time.
            log.debug(message, e);
            hierarchyManager.refresh(false); // revert all transient changes made in this session till now.
            throw new ExchangeException(message);
        } catch (Exception e) {
            final String message = "Activation failed | " + e.getMessage();
            log.error("Exception caught", e);
            hierarchyManager.refresh(false); // revert all transient changes made in this session till now.
            throw new ExchangeException(message);
        }
    }

    /**
     * import on existing content, making sure that content which is not sent stays as is.
     * @param topContentElement
     * @param data
     * @param hierarchyManager
     * @param existingContent
     * @throws ExchangeException
     * @throws RepositoryException
     */
    protected synchronized void importOnExisting(Element topContentElement, MultipartForm data,
            final HierarchyManager hierarchyManager, Content existingContent) throws ExchangeException, RepositoryException {
        final Iterator<Content> fileListIterator = topContentElement.getChildren(BaseSyndicatorImpl.RESOURCE_MAPPING_FILE_ELEMENT).iterator();
        final String uuid = UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
        final String handle = existingContent.getHandle();
        // Can't execute in system context here just get hm from SC and use it for temp node handling
        final HierarchyManager systemHM = MgnlContext.getSystemContext().getHierarchyManager("mgnlSystem");
        try {
            while (fileListIterator.hasNext()) {
                Element fileElement = (Element) fileListIterator.next();
                importResource(data, fileElement, hierarchyManager, handle);
            }
            // use temporary node in mgnlSystem workspace to extract the top level node and copy its properties
            Content activationTmp = ContentUtil.getOrCreateContent(systemHM.getRoot(), "activation-tmp", ItemType.FOLDER, true);
            final Content transientNode = activationTmp.createContent(uuid, MgnlNodeType.NT_PAGE);
            final String transientStoreHandle = transientNode.getHandle();
            // import properties into transientStore
            final String fileName = topContentElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_ID_ATTRIBUTE);
            final String md5 = topContentElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_MD_ATTRIBUTE);
            final InputStream inputStream = new DigestInputStream(new GZIPInputStream(data.getDocument(fileName).getStream()), md);
            // need to import in system context
            systemHM.getWorkspace().getSession().importXML(transientStoreHandle, inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
            IOUtils.closeQuietly(inputStream);
            final String calculatedMD5 = SecurityUtil.byteArrayToHex(md.digest());
            if (!calculatedMD5.equals(md5)) {
                throw new SecurityException(fileName + " signature is not valid. Resource might have been modified in transit.");
            }
            // copy properties from transient store to existing content
            Content tmpContent = transientNode.getContent(topContentElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_NAME_ATTRIBUTE));
            copyProperties(tmpContent, existingContent);
            systemHM.delete(transientStoreHandle);
            hierarchyManager.save();
            systemHM.save();
        } catch (Exception e) {
            // revert all transient changes made in this session till now.
            hierarchyManager.refresh(false);
            systemHM.refresh(false);

            log.error("Exception caught", e);
            throw new ExchangeException("Activation failed : " + e.getMessage());
        }
    }

    /**
     * import documents.
     * @param data as sent
     * @param resourceElement parent file element
     * @param hm
     * @param parentPath Path to the node parent
     * @throws Exception
     */
    protected synchronized void importResource(MultipartForm data, Element resourceElement, HierarchyManager hm, String parentPath) throws Exception {

        // throws an exception in case you don't have the permission
        PermissionUtil.isGranted(hm.getWorkspace().getSession(), parentPath, Session.ACTION_ADD_NODE);

        final String name = resourceElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_NAME_ATTRIBUTE);
        final String fileName = resourceElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_ID_ATTRIBUTE);
        final String md5 = resourceElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_MD_ATTRIBUTE);
        // do actual import
        final InputStream inputStream = new DigestInputStream(new GZIPInputStream(data.getDocument(fileName).getStream()), md);
        log.debug("Importing {} into parent path {}", new Object[] {name, parentPath});
        hm.getWorkspace().getSession().importXML(parentPath, inputStream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
        IOUtils.closeQuietly(inputStream);
        final String calculatedMD5 = SecurityUtil.byteArrayToHex(md.digest());
        if (!calculatedMD5.equals(md5)) {
            throw new SecurityException(fileName + " signature is not valid. Resource might have been modified in transit. Expected signature:" + md5 + ", actual signature found: " + calculatedMD5);
        }
        Iterator fileListIterator = resourceElement.getChildren(BaseSyndicatorImpl.RESOURCE_MAPPING_FILE_ELEMENT).iterator();
        // parent path
        try {
            parentPath = hm.getContentByUUID(resourceElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_UUID_ATTRIBUTE)).getHandle();
        } catch (ItemNotFoundException e) {
            // non referencable content like meta data ...
            // FYI: if we ever have non referencable same name sibling content the trouble will be here with child content being mixed
            parentPath = StringUtils.removeEnd(parentPath, "/") + "/" + name;
        }
        while (fileListIterator.hasNext()) {
            Element fileElement = (Element) fileListIterator.next();
            importResource(data, fileElement, hm, parentPath);
        }
    }

    /**
     * Deletes (de-activate) the content specified by the request.
     * @param request
     * @throws Exception if fails to update
     */
    protected synchronized String remove(HttpServletRequest request, String md5) throws Exception {

        if (!md5.equals(SecurityUtil.byteArrayToHex(md.digest(request.getHeader(BaseSyndicatorImpl.NODE_UUID).getBytes())))) {
            throw new SecurityException("Signature of resource doesn't match. This seems like malicious attempt to delete content. Request was issued from " + request.getRemoteAddr());
        }
        HierarchyManager hm = getHierarchyManager(request);
        String handle = null;
        try {
            Content node = this.getNode(request);
            handle = node.getHandle();
            hm.delete(handle);
            hm.save();
        } catch (ItemNotFoundException e) {
            log.debug("Unable to delete node", e);
        }
        return handle;
    }

    /**
     * get hierarchy manager.
     * @param request
     * @throws ExchangeException
     */
    protected HierarchyManager getHierarchyManager(HttpServletRequest request) throws ExchangeException {
        String workspaceName = request.getHeader(BaseSyndicatorImpl.WORKSPACE_NAME);

        if (StringUtils.isEmpty(workspaceName)) {
            throw new ExchangeException("Repository or workspace name not sent, unable to activate. Workspace: " + workspaceName) ;
        }
        SystemContext sysCtx = MgnlContext.getSystemContext();
        return sysCtx.getHierarchyManager(workspaceName);
    }

    /**
     * cleans temporary store and removes any locks set.
     *
     * @param request
     * @param status
     */
    protected void cleanUp(HttpServletRequest request, String status) {
        if (!BaseSyndicatorImpl.DEACTIVATE.equalsIgnoreCase(request.getHeader(BaseSyndicatorImpl.ACTION))) {
            MultipartForm data = MgnlContext.getPostedForm();
            if (null != data) {
                Iterator keys = data.getDocuments().keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    data.getDocument(key).delete();
                }
            }
            try {
                final String parentPath = getParentPath(request);
                if (StringUtils.isEmpty(parentPath) || this.getHierarchyManager(request).isExist(parentPath)) {
                    try {
                        Content content = this.getNode(request);
                        if (content.isLocked()) {
                            content.unlock();
                        }
                    }catch (ItemNotFoundException e) {
                        // ignore - commit of deactivation
                    }
                }
            } catch (LockException le) {
                // either repository does not support locking OR this node never locked
                log.debug(le.getMessage(), le);
            } catch (RepositoryException re) {
                // should never come here
                log.warn("Exception caught", re);
            } catch (ExchangeException e) {
                // should never come here
                log.warn("Exception caught", e);
            }
        }

        // TODO : why is this here ? as far as I can tell, http sessions are never created when reaching this
        try {
            HttpSession httpSession = request.getSession(false);
            if (httpSession != null) {
                httpSession.invalidate();
            }
        } catch (Throwable t) {
            // its only a test so just dump
            log.error("failed to invalidate session", t);
        }
    }

    /**
     * apply lock.
     * @param request
     */
    protected void applyLock(HttpServletRequest request) throws ExchangeException {
        try {
            Content parent = waitForLock(request);
            // get a new deep lock
            parent.lock(true, true);
        } catch (LockException le) {
            // either repository does not support locking OR this node never locked
            log.debug(le.getMessage());
        } catch (ItemNotFoundException e) {
            // - when deleting new piece of content on the author and mgnl tries to deactivate it on public automatically
            log.warn("Attempt to lock non existing content {} during (de)activation.",getUUID(request));
        } catch (PathNotFoundException e) {
            // - when attempting to activate the content for which parent content have not been yet activated
            log.debug("Attempt to lock non existing content {}:{} during (de)activation.",getHierarchyManager(request).getName(), getParentPath(request));
        } catch (RepositoryException re) {
            // will blow fully at later stage
            log.warn("Exception caught", re);
        }
    }

    protected Content waitForLock(HttpServletRequest request) throws ExchangeException, RepositoryException {
        int retries = getUnlockRetries();
        long retryWait = getRetryWait() * 1000;
        Content content = this.getNode(request);
        while (content.isLocked() && retries > 0) {
            log.info("Content " + content.getHandle() + " is locked. Will retry " + retries + " more times.");
            try {
                Thread.sleep(retryWait);
            } catch (InterruptedException e) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
            retries--;
            content = this.getNode(request);
        }
        if (content.isLocked()) {
            throw new ExchangeException("Failed to lock content with 'Operation not permitted, " + content.getHandle() + " is locked while activating " + request.getHeader(BaseSyndicatorImpl.NODE_UUID) + "'");
        }
        return content;
    }

    protected Content getNode(HttpServletRequest request) throws ExchangeException, RepositoryException {
        if (request.getHeader(BaseSyndicatorImpl.PARENT_PATH) != null) {
            String parentPath = this.getParentPath(request);
            log.debug("parent path:" + parentPath);
            return this.getHierarchyManager(request).getContent(parentPath);
        } else if (!StringUtils.isEmpty(getUUID(request))){
            log.debug("node uuid:" + request.getHeader(BaseSyndicatorImpl.NODE_UUID));
            return this.getHierarchyManager(request).getContentByUUID(request.getHeader(BaseSyndicatorImpl.NODE_UUID));
        } else {
            throw new ExchangeException("Request is missing mandatory content identifier.");
        }
    }

    protected String getParentPath(HttpServletRequest request) {
        String parentPath = request.getHeader(BaseSyndicatorImpl.PARENT_PATH);
        if (StringUtils.isNotEmpty(parentPath)) {
            return parentPath;
        }
        return "";
    }

    protected String getUUID(HttpServletRequest request) {
        String parentPath = request.getHeader(BaseSyndicatorImpl.NODE_UUID);
        if (StringUtils.isNotEmpty(parentPath)) {
            return parentPath;
        }
        return "";
    }


}
