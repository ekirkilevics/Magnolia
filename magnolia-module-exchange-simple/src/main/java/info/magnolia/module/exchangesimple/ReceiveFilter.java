/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
import info.magnolia.cms.core.Access;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;
import info.magnolia.context.MgnlContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.safehaus.uuid.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * This filter receives activation requests from another instance and applies them.
 *
 * @author Sameer Charles
 * $Id$
 */
public class ReceiveFilter extends AbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(ReceiveFilter.class);

    /**
     * @deprecated since 3.5. This is the attribute name that was used in 3.0, so we keep to be able to activate from 3.0.
     */
    private static final String SIBLING_UUID_3_0 = "UUID";

    private int unlockRetries = 10;

    private int retryWait = 2;

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

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String statusMessage = "";
        String status = "";
        String result = null;
        try {
            String action = request.getHeader(BaseSyndicatorImpl.ACTION);
            if (action == null) {
                throw new InvalidParameterException("Activation action must be set for each activation request.");
            }

            applyLock(request);
            result = receive(request);
            status = BaseSyndicatorImpl.ACTIVATION_SUCCESSFUL;
        }
        catch (OutOfMemoryError e) {
            Runtime rt = Runtime.getRuntime();
            log.error("---------\nOutOfMemoryError caught during activation. Total memory = " //$NON-NLS-1$
                + rt.totalMemory()
                + ", free memory = " //$NON-NLS-1$
                + rt.freeMemory()
                + "\n---------"); //$NON-NLS-1$
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
            cleanUp(request);
            setResponseHeaders(response, statusMessage, status, result);
        }
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
         String authorization = getUser(request);
         String webapp = getWebappName();

         if (action.equalsIgnoreCase(BaseSyndicatorImpl.ACTIVATE)) {
             String name = update(request);
             // Everything went well
             log.info("User {} successfuly activated {} on {}.", new Object[]{authorization, name, webapp});
         }
         else if (action.equalsIgnoreCase(BaseSyndicatorImpl.DEACTIVATE)) {
             String name = remove(request);
             // Everything went well
             log.info("User {} succeessfuly deactivated {} on {}.", new Object[] {authorization, name, webapp});
         }
         else {
             throw new UnsupportedOperationException("Method not supported : " + action);
         }
         return null;
     }

    protected String getWebappName() {
        return SystemProperty.getProperty(SystemProperty.MAGNOLIA_WEBAPP);
    }

    protected String getUser(HttpServletRequest request) {
        // get the user who authorized this request.
         String user = request.getHeader(BaseSyndicatorImpl.AUTHORIZATION);
         if (StringUtils.isEmpty(user)) {
             user = request.getParameter(BaseSyndicatorImpl.AUTH_USER);
         } else {
            user = new String(Base64.decodeBase64(user.substring(6).getBytes())); //Basic uname:pwd
            user = user.substring(0, user.indexOf(":"));
         }
        return user;
    }

     /**
      * handle update (activate) request.
      * @param request
      * @throws Exception if fails to update
      */
     protected synchronized String update(HttpServletRequest request) throws Exception {
         MultipartForm data = MgnlContext.getPostedForm();
         if (null != data) {
             String newParentPath = this.getParentPath(request);
             String resourceFileName = request.getHeader(BaseSyndicatorImpl.RESOURCE_MAPPING_FILE);
             HierarchyManager hm = getHierarchyManager(request);
             Element rootElement = getImportedContentRoot(data, resourceFileName);
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

    protected Element getImportedContentRoot(MultipartForm data, String resourceFileName) throws JDOMException, IOException {
        Document resourceDocument = data.getDocument(resourceFileName);
        SAXBuilder builder = new SAXBuilder();
        InputStream documentInputStream = resourceDocument.getStream();
        org.jdom.Document jdomDocument = builder.build(documentInputStream);
        IOUtils.closeQuietly(documentInputStream);
        return jdomDocument.getRootElement();
    }

    protected void handleChildren(HttpServletRequest request, Content content) {
        String ruleString = request.getHeader(BaseSyndicatorImpl.CONTENT_FILTER_RULE);
         Rule rule = new Rule(ruleString, ",");
         RuleBasedContentFilter filter = new RuleBasedContentFilter(rule);
         // remove all child nodes
         this.removeChildren(content, filter);
    }

    protected String handleMovedContent(String newParentPath,
            HierarchyManager hm, Element topContentElement, Content content)
            throws RepositoryException {
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
                // be compatible with 3.0 (MAGNOLIA-2016)
                if (StringUtils.isEmpty(siblingUUID)) {
                    log.debug("Activating from a Magnolia 3.0 instance");
                    siblingUUID = sibling.getAttributeValue(SIBLING_UUID_3_0);
                }
                Content beforeContent = hm.getContentByUUID(siblingUUID);
                log.debug("Ordering {} before {}", name, beforeContent.getName());
                order(parent, name, beforeContent.getName());
                break;
            } catch (ItemNotFoundException e) {
                // ignore
            } catch (RepositoryException re) {
                log.warn("Failed to order node");
                log.debug("Failed to order node", re);
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
                     throw new AccessDeniedException("User not allowed to " + Permission.PERMISSION_NAME_WRITE + " at [" + nodeData.getHandle() + "]"); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
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
             log.warn("Replacing {} due to name collision (but different UUIDs.)", path);
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
            final Content transientNode = activationTmp.createContent(uuid, ItemType.CONTENTNODE.toString());
            final String transientStoreHandle = transientNode.getHandle();
            // import properties into transientStore
            final String fileName = topContentElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_ID_ATTRIBUTE);
            final GZIPInputStream inputStream = new GZIPInputStream(data.getDocument(fileName).getStream());
            // need to import in system context
            systemHM.getWorkspace().getSession().importXML(transientStoreHandle, inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
            IOUtils.closeQuietly(inputStream);
            // copy properties from transient store to existing content
            Content tmpContent = transientNode.getChildByName(topContentElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_NAME_ATTRIBUTE));
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
         Access.isGranted(hm.getAccessManager(), parentPath, Permission.WRITE);

         final String name = resourceElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_NAME_ATTRIBUTE);
         final String fileName = resourceElement.getAttributeValue(BaseSyndicatorImpl.RESOURCE_MAPPING_ID_ATTRIBUTE);
         // do actual import
         final GZIPInputStream inputStream = new GZIPInputStream(data.getDocument(fileName).getStream());
         log.debug("Importing {} into parent path {}", new Object[] {name, parentPath});
         hm.getWorkspace().getSession().importXML(parentPath, inputStream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
         IOUtils.closeQuietly(inputStream);
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
     protected synchronized String remove(HttpServletRequest request) throws Exception {
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
         String repositoryName = request.getHeader(BaseSyndicatorImpl.REPOSITORY_NAME);
         String workspaceName = request.getHeader(BaseSyndicatorImpl.WORKSPACE_NAME);

         if (StringUtils.isEmpty(repositoryName) || StringUtils.isEmpty(workspaceName)) {
             throw new ExchangeException("Repository or workspace name not sent, unable to activate. Repository: " + repositoryName + ", workspace: " + workspaceName) ;
         }

         return MgnlContext.getHierarchyManager(repositoryName, workspaceName);
     }

     /**
      * cleans temporary store and removes any locks set.
      * @param request
      */
     protected void cleanUp(HttpServletRequest request) {
         if (BaseSyndicatorImpl.ACTIVATE.equalsIgnoreCase(request.getHeader(BaseSyndicatorImpl.ACTION))) {
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
                     Content content = this.getNode(request);
                     if (content.isLocked()) {
                         content.unlock();
                     }
                 }
             } catch (LockException le) {
                 // either repository does not support locking OR this node never locked
                 log.debug(le.getMessage());
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
             if (httpSession != null) httpSession.invalidate();
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
                 throw new ExchangeException("Operation not permitted, " + content.getHandle() + " is locked");
             }
             // get a new deep lock
             content.lock(true, true);
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

     protected Content getNode(HttpServletRequest request) throws ExchangeException, RepositoryException {
         if (request.getHeader(BaseSyndicatorImpl.PARENT_PATH) != null) {
             String parentPath = this.getParentPath(request);
             log.debug("parent path:" + parentPath);
             return this.getHierarchyManager(request).getContent(parentPath);
         } else if (!StringUtils.isEmpty(getUUID(request))){
             log.debug("node uuid:" + request.getHeader(BaseSyndicatorImpl.NODE_UUID));
             return this.getHierarchyManager(request).getContentByUUID(request.getHeader(BaseSyndicatorImpl.NODE_UUID));
         }
         // 3.0 protocol
         else {
             log.debug("path: {}", request.getHeader(BaseSyndicatorImpl.PATH));
             return this.getHierarchyManager(request).getContent(request.getHeader(BaseSyndicatorImpl.PATH));
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
