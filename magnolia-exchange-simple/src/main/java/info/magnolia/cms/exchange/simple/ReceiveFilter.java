/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.filters.AbstractMagnoliaFilter;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.util.Resource;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.*;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.jcr.*;
import javax.jcr.lock.LockException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.safehaus.uuid.UUIDGenerator;

/**
 * @author Sameer Charles
 * $Id$
 */
public class ReceiveFilter extends AbstractMagnoliaFilter {

    private static Logger log = LoggerFactory.getLogger(ReceiveFilter.class);

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String statusMessage = "";
        String status = "";
        try {
            applyLock(request);
            receive(request);
            status = SimpleSyndicator.ACTIVATION_SUCCESSFUL;
        }
        catch (OutOfMemoryError e) {
            Runtime rt = Runtime.getRuntime();
            log.error("---------\nOutOfMemoryError caught during activation. Total memory = " //$NON-NLS-1$
                + rt.totalMemory()
                + ", free memory = " //$NON-NLS-1$
                + rt.freeMemory()
                + "\n---------"); //$NON-NLS-1$
            statusMessage = e.getMessage();
            status = SimpleSyndicator.ACTIVATION_FAILED;
        }
        catch (PathNotFoundException e) {
            log.error(e.getMessage(), e);
            statusMessage = "Parent not found (not yet activated): " + e.getMessage();
            status = SimpleSyndicator.ACTIVATION_FAILED;
        }
        catch (Throwable e) {
            log.error(e.getMessage(), e);
            statusMessage = e.getMessage();
            status = SimpleSyndicator.ACTIVATION_FAILED;
        }
        finally {
            cleanUp(request);
            response.setHeader(SimpleSyndicator.ACTIVATION_ATTRIBUTE_STATUS, status);
            response.setHeader(SimpleSyndicator.ACTIVATION_ATTRIBUTE_MESSAGE, statusMessage);
        }
    }

    /**
      * handle activate or deactivate request
      * @param request
      * @throws Exception if fails to update
      */
     private synchronized void receive(HttpServletRequest request) throws Exception {
         String action = request.getHeader(SimpleSyndicator.ACTION);
         if (action.equalsIgnoreCase(SimpleSyndicator.ACTIVATE)) {
             update(request);
         }
         else if (action.equalsIgnoreCase(SimpleSyndicator.DE_ACTIVATE)) {
             remove(request);
         }
         else {
             throw new UnsupportedOperationException("Method not supported : " + action);
         }
         // Everything went well
         log.info("Activation succeeded");
     }

     /**
      * handle update (activate) request
      * @param request
      * @throws Exception if fails to update
      */
     private synchronized void update(HttpServletRequest request) throws Exception {
         MultipartForm data = Resource.getPostedForm();
         if (null != data) {
             String parentPath = this.getParentPath(request);
             String resourceFileName = request.getHeader(SimpleSyndicator.RESOURCE_MAPPING_FILE);
             HierarchyManager hm = getHierarchyManager(request);
             Document resourceDocument = data.getDocument(resourceFileName);
             SAXBuilder builder = new SAXBuilder();
             InputStream documentInputStream = resourceDocument.getStream();
             org.jdom.Document jdomDocument = builder.build(documentInputStream);
             IOUtils.closeQuietly(documentInputStream);
             Element rootElement = jdomDocument.getRootElement();
             Element topContentElement = rootElement.getChild(SimpleSyndicator.RESOURCE_MAPPING_FILE_ELEMENT);
             try {
                 String uuid = topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_UUID_ATTRIBUTE);
                 Content content = hm.getContentByUUID(uuid);
                 String ruleString = request.getHeader(SimpleSyndicator.CONTENT_FILTER_RULE);
                 Rule rule = new Rule(ruleString, ",");
                 RuleBasedContentFilter filter = new RuleBasedContentFilter(rule);
                 // remove all child nodes
                 this.removeChildren(content, filter);
                 // import all child nodes
                 this.importOnExisting(topContentElement, data, hm, content);
             }
             catch (ItemNotFoundException e) {
                 importFresh(topContentElement, data, hm, parentPath);
             }

             // order imported node
             String name = topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_NAME_ATTRIBUTE);
             Content parent = hm.getContent(parentPath);
             List siblings = rootElement.getChild(SimpleSyndicator.SIBLINGS_ROOT_ELEMENT)
                     .getChildren(SimpleSyndicator.SIBLINGS_ELEMENT);
             Iterator siblingsIterator = siblings.iterator();
             while (siblingsIterator.hasNext()) {
                 Element sibling = (Element) siblingsIterator.next();
                 // check for existence and order
                 try {
                     Content beforeContent = hm.getContentByUUID(sibling.getAttributeValue(SimpleSyndicator.SIBLING_UUID));
                     parent.orderBefore(name, beforeContent.getName());
                     parent.save();
                     break;
                 } catch (ItemNotFoundException e) {
                     // ignore
                 } catch (RepositoryException re) {
                     log.warn("Failed to order node");
                     log.debug("Failed to order node", re);
                 }
             }
         }
     }

     /**
      * Copy all properties from source to destination (by cleaning the old properties).
      * @param source the content node to be copied
      * @param destination the destination node
      */
     private synchronized void copyProperties(Content source, Content destination) throws RepositoryException {
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
                     throw new AccessDeniedException(
                         "User not allowed to " + Permission.PERMISSION_NAME_WRITE + " at [" + nodeData.getHandle() + "]"); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
                 }
             }
             else {
                 destination.createNodeData(nodeData.getName(), nodeData.getValue());
             }
         }
     }

     /**
      * remove children
      * @param content whose children to be deleted
      * @param filter content filter
      */
     private synchronized void removeChildren(Content content, Content.ContentFilter filter) {
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
      * import on non existing tree
      * @param topContentElement
      * @param data
      * @param hierarchyManager
      * @param parentPath
      * @throws ExchangeException
      * @throws RepositoryException
      */
     private synchronized void importFresh(Element topContentElement, MultipartForm data,
         HierarchyManager hierarchyManager, String parentPath) throws ExchangeException, RepositoryException {
         try {
             importResource(data, topContentElement, hierarchyManager, parentPath);
             hierarchyManager.save();
         }
         catch (Exception e) {
             hierarchyManager.refresh(false); // revert all transient changes made in this session till now.
             log.error("Exception caught", e);
             throw new ExchangeException("Activation failed | " + e.getMessage());
         }
     }

     /**
      * import on existing content, making sure that content which is not sent stays as is
      * @param topContentElement
      * @param data
      * @param hierarchyManager
      * @param existingContent
      * @throws ExchangeException
      * @throws RepositoryException
      */
     private synchronized void importOnExisting(Element topContentElement, MultipartForm data,
         HierarchyManager hierarchyManager, Content existingContent) throws ExchangeException, RepositoryException {
         Iterator fileListIterator = topContentElement
             .getChildren(SimpleSyndicator.RESOURCE_MAPPING_FILE_ELEMENT)
             .iterator();
         String uuid = UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
         String transientStore = existingContent.getHandle() + "/" + uuid;
         try {
             while (fileListIterator.hasNext()) {
                 Element fileElement = (Element) fileListIterator.next();
                 importResource(data, fileElement, hierarchyManager, existingContent.getHandle());
             }
             // use temporary transient store to extract top level node and copy properties
             existingContent.createContent(uuid, ItemType.CONTENTNODE.toString());
             //hierarchyManager.createContent("/", uuid, ItemType.CONTENTNODE.toString());
             String fileName = topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_ID_ATTRIBUTE);
             GZIPInputStream inputStream = new GZIPInputStream(data.getDocument(fileName).getStream());
             hierarchyManager.getWorkspace().getSession().importXML(
                 transientStore,
                 inputStream,
                 ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
             IOUtils.closeQuietly(inputStream);
             StringBuffer newPath = new StringBuffer();
             newPath.append(transientStore);
             newPath.append("/");
             newPath.append(topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_NAME_ATTRIBUTE));
             Content tmpContent = hierarchyManager.getContent(newPath.toString());
             copyProperties(tmpContent, existingContent);
             hierarchyManager.delete(transientStore);
             hierarchyManager.save();
         }
         catch (Exception e) {
             hierarchyManager.refresh(false); // revert all transient changes made in this session till now.
             log.error("Exception caught", e);
             throw new ExchangeException("Activation failed | " + e.getMessage());
         }
     }

     /**
      * import documents
      * @param data as sent
      * @param resourceElement parent file element
      * @param hm
      * @param parentPath
      * @throws Exception
      */
     private synchronized void importResource(MultipartForm data, Element resourceElement, HierarchyManager hm,
         String parentPath) throws Exception {

         String name = resourceElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_NAME_ATTRIBUTE);
         String fileName = resourceElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_ID_ATTRIBUTE);
         // do actual import
         GZIPInputStream inputStream = new GZIPInputStream(data.getDocument(fileName).getStream());
         hm.getWorkspace().getSession().importXML(
             parentPath,
             inputStream,
             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
         IOUtils.closeQuietly(inputStream);
         Iterator fileListIterator = resourceElement
             .getChildren(SimpleSyndicator.RESOURCE_MAPPING_FILE_ELEMENT)
             .iterator();
         // parent path
         if (parentPath.equals("/")) {
             parentPath = ""; // remove / if its a root
         }
         parentPath += ("/" + name);
         while (fileListIterator.hasNext()) {
             Element fileElement = (Element) fileListIterator.next();
             importResource(data, fileElement, hm, parentPath);
         }
     }

     /**
      * handle remove (de-activate) request
      * @param request
      * @throws Exception if fails to update
      */
     private synchronized void remove(HttpServletRequest request) throws Exception {
         String uuid = request.getHeader(SimpleSyndicator.NODE_UUID);
         HierarchyManager hm = getHierarchyManager(request);
         try {
             hm.delete(hm.getContentByUUID(uuid).getHandle());
             hm.save();
         }
         catch (ItemNotFoundException e) {
             if (log.isDebugEnabled()) {
                 log.debug("Unable to delete node", e);
             }
         }
     }

     /**
      * get hierarchy manager
      * @param request
      * @throws ExchangeException
      */
     private HierarchyManager getHierarchyManager(HttpServletRequest request) throws ExchangeException {
         String repositoryName = request.getHeader(SimpleSyndicator.REPOSITORY_NAME);
         String workspaceName = request.getHeader(SimpleSyndicator.WORKSPACE_NAME);

         if (StringUtils.isEmpty(repositoryName) || StringUtils.isEmpty(workspaceName)) {
             throw new ExchangeException("Repository or workspace name not sent, unable to activate.");
         }

         if (ConfigLoader.isConfigured()) {
             return MgnlContext.getHierarchyManager(repositoryName, workspaceName);
         }

         return ContentRepository.getHierarchyManager(repositoryName, workspaceName);

     }

     /**
      * cleans temporary store and removes any locks set
      * @param request
      */
     private void cleanUp(HttpServletRequest request) {
         MultipartForm data = Resource.getPostedForm();
         if (null != data) {
             Iterator keys = data.getDocuments().keySet().iterator();
             while (keys.hasNext()) {
                 String key = (String) keys.next();
                 data.getDocument(key).delete();
             }
         }
         try {
             getHierarchyManager(request).getWorkspace().getSession().logout();
             HttpSession httpSession = request.getSession(false);
             if (httpSession != null) httpSession.invalidate();
         } catch (Throwable t) {
             // its only a test so just dump
             log.error("failed to invalidate session", t);
         }
         try {
             Content content = this.getNode(request);
             if (content.isLocked()) {
                 content.unlock();
             }
         }
         catch (LockException le) {
             // either repository does not support locking OR this node never locked
             if (log.isDebugEnabled()) {
                 log.debug(le.getMessage());
             }
         }
         catch (RepositoryException re) {
             // should never come here
             log.debug("Exception caught", re);
         }
         catch (ExchangeException e) {
             // should never come here
             log.debug("Exception caught", e);
         }
     }

     /**
      * apply lock
      * @param request
      */
     private void applyLock(HttpServletRequest request) throws ExchangeException {
         try {
             Content content = this.getNode(request);
             if (content.isLocked()) {
                 throw new ExchangeException("Operation not permitted, " + content.getHandle() + " is locked");
             }
             // get a new deep lock
             content.lock(true, true);
         }
         catch (LockException le) {
             // either repository does not support locking OR this node never locked
             if (log.isDebugEnabled()) {
                 log.debug(le.getMessage());
             }
         }
         catch (RepositoryException re) {
             // should never come here
             if (log.isDebugEnabled()) {
                 log.debug("Exception caught", re);
             }
         }
     }

     private Content getNode(HttpServletRequest request)
             throws ExchangeException, RepositoryException {

         String action = request.getHeader(SimpleSyndicator.ACTION);
         if (SimpleSyndicator.ACTIVATE.equalsIgnoreCase(action)) {
             return this.getHierarchyManager(request).getContent(this.getParentPath(request));
         }
         else if (SimpleSyndicator.DE_ACTIVATE.equalsIgnoreCase(action)) {
             return this.getHierarchyManager(request).getContent(request.getHeader(SimpleSyndicator.NODE_UUID));
         }
         throw new ExchangeException("Node not found");
     }

     private String getParentPath(HttpServletRequest request) {
         String parentPath = request.getHeader(SimpleSyndicator.PARENT_PATH);
         if (StringUtils.isNotEmpty(parentPath)) {
             return parentPath;
         }
         return "";
     }



}
