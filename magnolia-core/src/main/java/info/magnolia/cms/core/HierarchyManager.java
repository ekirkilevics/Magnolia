/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Permission;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * User: sameercharles Date: Sept 23, 2004 Time: 1:42:48 PM
 * @author Sameer Charles
 * $Id:HierarchyManager.java 2719 2006-04-27 14:38:44Z scharles $
 */
public class HierarchyManager {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(HierarchyManager.class);

    /**
     * root of this hierarchy
     */
    private Node startPage;

    /**
     * workspacer for this hierarchy
     */
    private Workspace workSpace;

    /**
     * user who created this hierarchy
     */
    private String userID;

    /**
     * access manager for this hierarchy
     */
    private AccessManager accessManager;

    /**
     * query manager for this hierarchy
     */
    private QueryManager queryManager;

    /**
     * constructor
     */
    public HierarchyManager() {
        this.userID = "anonymous"; //$NON-NLS-1$
    }

    public HierarchyManager(String userID) {
        this.userID = userID;
    }

    /**
     * constructor
     */
    public HierarchyManager(HttpServletRequest request) {
        this.userID = Authenticator.getUserId(request);
    }

    /**
     * sets start page of the current working repository
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @see HierarchyManager#init(javax.jcr.Node)
     * @deprecated instead use init(Node rootNode)
     */
    public void setStartPage(Node rootNode) throws PathNotFoundException, RepositoryException {
        this.startPage = rootNode;
        this.workSpace = this.startPage.getSession().getWorkspace();
    }

    /**
     * initialize hierarchy manager and sets default workspace
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void init(Node rootNode) throws PathNotFoundException, RepositoryException {
        this.startPage = rootNode;
        this.workSpace = this.startPage.getSession().getWorkspace();
    }

    /**
     * initialize hierarchy manager and sets default workspace
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void init(Node rootNode, AccessManager manager) throws PathNotFoundException, RepositoryException {
        this.startPage = rootNode;
        this.workSpace = this.startPage.getSession().getWorkspace();
        this.accessManager = manager;
    }

    /**
     * Set access manager for this hierarchy
     * @param accessManager
     */
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    /**
     * Get access manager
     * @return accessmanager attached to this hierarchy
     * */
    public AccessManager getAccessManager() {
        return this.accessManager;
    }

    /**
     * Set query manager for this hierarchy
     * @param queryManager
     */
    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public QueryManager getQueryManager() {
        return this.queryManager;
    }

    /**
     * creates a new content page
     * @param path parent handle under which new page has to be created
     * @param label page name to be created
     * @return Content newly created hierarchy node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public Content createPage(String path, String label) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Content newPage = (new Content(
            this.startPage,
            this.getNodePath(path, label),
            ItemType.CONTENT.getSystemName(),
            this.accessManager));
        this.setMetaData(newPage.getMetaData());
        return newPage;
    }

    /**
     * creates contentNode of type <b>contentType </b> contentType must be defined in item type definition of magnolia
     * as well as JCR implementation
     * @param path absolute (primary) path to this <code>Node</code>
     * @param label page name
     * @param contentType , JCR node type as configured
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    public Content createContent(String path, String label, String contentType) throws PathNotFoundException,
        RepositoryException, AccessDeniedException {
        Content newPage = new Content(
            this.startPage,
            this.getNodePath(path, label),
            contentType,
            this.accessManager);
        setMetaData(newPage.getMetaData());
        return newPage;
    }

    private String getNodePath(String path, String label) {
        if (StringUtils.isEmpty(path) || (path.equals("/"))) { //$NON-NLS-1$
            return label;
        }
        return getNodePath(path + "/" + label); //$NON-NLS-1$
    }

    private String getNodePath(String path) {
        if (path.startsWith("/")) { //$NON-NLS-1$
            return path.replaceFirst("/", StringUtils.EMPTY); //$NON-NLS-1$
        }
        return path;
    }

    /**
     * Helper method to set page properties, create page calls this method. you could call this method anytime to create
     * working page properties
     */
    public void setMetaData(MetaData md, String template) throws RepositoryException, AccessDeniedException {
        md.setTemplate(template);
        setMetaData(md);
    }

    /**
     * Helper method to set page properties, create page calls this method. you could call this method anytime to create
     * working page properties
     */
    public void setMetaData(MetaData md) throws RepositoryException, AccessDeniedException {
        md.setCreationDate();
        md.setModificationDate();
        md.setAuthorId(this.userID);
        md.setTitle(StringUtils.EMPTY);
    }

    /**
     * Helper method to set page properties, get page calls this method. you could call this method anytime to update
     * working page properties
     */
    public void updateMetaData(MetaData md) throws RepositoryException, AccessDeniedException {
        md.setModificationDate();
        md.setAuthorId(this.userID);
    }

    /**
     * returns the page specified by path in the parameter
     * @param path handle of the page to be initialized
     * @return Content hierarchy node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @deprecated use getContent(String path) instead
     */
    public Content getPage(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getContent(path);
    }

    /**
     * get content object of the requested URI
     * @param path of the content to be initialized
     * @return Content
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public Content getContent(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (path.equals("/")) { //$NON-NLS-1$
            return this.getRoot();
        }
        return (new Content(this.startPage, getNodePath(path), this.accessManager));
    }

    /**
     * Like getContent() but creates the node if not yet existing. Attention save is not called!
     * @param path the path of the node
     * @param create true if the node should get created
     * @param type the node type of the created node
     * @return the node
     * @throws AccessDeniedException
     * @throws RepositoryException
     */
    public Content getContent(String path, boolean create, ItemType type) throws AccessDeniedException,
        RepositoryException {
        Content node;
        try {
            node = getContent(path);
        }
        catch (PathNotFoundException e) {
            if (create) {
                node = this.createContent(StringUtils.substringBeforeLast(path, "/"), StringUtils.substringAfterLast(
                    path,
                    "/"), type.toString());
            }
            else {
                throw e;
            }
        }
        return node;
    }

    /**
     * get content node object of the requested URI
     * @param path of the content (container / containerlist) to be initialized
     * @return ContentNode
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @deprecated use getContent(String path) instead
     */
    public Content getContentNode(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return new Content(this.startPage, getNodePath(path), this.accessManager);
    }

    /**
     * get NodeData object of the requested URI
     * @param path of the atom to be initialized
     * @return NodeData
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public NodeData getNodeData(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        String nodePath = getNodePath(path);
        if (StringUtils.isEmpty(nodePath)) {
            return null;
        }

        return new NodeData(this.startPage, nodePath, this.accessManager);
    }

    /**
     * returns the first page with a given template name that is found in tree that starts from the page given py the
     * path (including this page)
     * @param path handle of the page from where the search should start
     * @param templateName template name to search for
     * @return first Content hierarchy node that has the specified template name assigned
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public Content getPage(String path, String templateName) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Content page = getContent(path);
        if (page.getTemplate().equals(templateName)) {
            return page;
        }
        Content pageToBeFound = null;
        try {
            if (page.hasChildren()) {
                Collection children = page.getChildren(ItemType.CONTENT.getSystemName());
                Iterator iterator = children.iterator();
                while (iterator.hasNext()) {
                    Content child = (Content) iterator.next();
                    if (child.getTemplate().equals(templateName)) {
                        return child;
                    }
                    if (child.hasChildren()) {
                        pageToBeFound = getPage(child.getHandle(), templateName);
                    }
                    if (pageToBeFound != null) {
                        return pageToBeFound;
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to get - " + path); //$NON-NLS-1$
            log.error(e.getMessage(), e);
        }
        return pageToBeFound;
    }

    /**
     * removes specified path, it can be either node or property
     * @param path to be removed
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException
     */
    public void delete(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (this.isNodeData(path)) {
            this.getNodeData(makeRelative(path)).delete();
        }
        else {
            this.startPage.getNode(makeRelative(path)).remove();
        }

    }

    private String makeRelative(String path) {
        return StringUtils.stripStart(path, "/"); //$NON-NLS-1$
    }

    /**
     * @return startPage of the current working repository-workspace
     */
    public Content getRoot() throws RepositoryException, AccessDeniedException {
        return (new Content(this.startPage, this.accessManager));
    }

    /**
     * Checks if the requested resource is a page (hierarchy Node).
     * @param path of the requested content
     * @return boolean true is the requested content is a Hierarchy Node
     * todo remove this method, instead use (getContent(PATH) is NodeType)
     */
    public boolean isPage(String path) throws AccessDeniedException {
        Access.isGranted(this.accessManager, path, Permission.READ);

        String nodePath = getNodePath(path);
        if (StringUtils.isEmpty(nodePath)) return false;

        try {
            Node n = this.startPage.getNode(nodePath);
            return (n.isNodeType(ItemType.CONTENT.getSystemName()));
        }
        catch (RepositoryException re) {
            // ignore, not existing?
        }
        return false;
    }

    /**
     * check is either the node or property exists with the specified path
     * @param path
     */
    public boolean isExist(String path) {
        try {
            Access.isGranted(this.accessManager, path, Permission.READ);
        }
        catch (AccessDeniedException e) {
            log.error(e.getMessage());
            return false;
        }
        boolean isExist = false;
        try {
            isExist = this.workSpace.getSession().itemExists(path);
        }
        catch (RepositoryException re) {
            log.error("Exception caught", re);
        }
        return isExist;
    }

    /**
     * Evaluate primary node type of the node at the given path.
     */
    public boolean isNodeType(String path, String type) {
        try {
            Node n = this.startPage.getNode(getNodePath(path));
            return n.isNodeType(type);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re.getMessage(), re);
        }
        return false;
    }

    /**
     * Evaluate primary node type of the node at the given path.
     */
    public boolean isNodeType(String path, ItemType type) {
        return isNodeType(path, type.getSystemName());
    }

    /**
     * checks if the requested resource is an NodeData (Property)
     * @param path of the requested NodeData
     * @return boolean true is the requested content is an NodeData
     */
    public boolean isNodeData(String path) throws AccessDeniedException {
        Access.isGranted(this.accessManager, path, Permission.READ);
        boolean result = false;
        String nodePath = getNodePath(path);
        if (StringUtils.isEmpty(nodePath)) {
            return false;
        }
        try {
            result = this.startPage.hasProperty(nodePath);
            if (!result) {
                // check if its a nt:resource
                result = this.startPage.hasProperty(nodePath + "/" + ItemType.JCR_DATA);
            }
        }
        catch (RepositoryException e) {
            // ignore, no property
        }
        return result;
    }

    /**
     * This method can be used to retrieve Content which has UUID assigned to it, in other words only those nodes which
     * has mixin type mix:referenceable.
     * @param uuid
     */
    public Content getContentByUUID(String uuid) throws ItemNotFoundException, RepositoryException,
        AccessDeniedException {
        return new Content(this.startPage.getSession().getNodeByUUID(uuid), this.accessManager);
    }

    /**
     * gets currently used workspace for this hierarchy manager
     */
    public Workspace getWorkspace() {
        return this.workSpace;
    }

    /**
     * move content to the specified location
     * @param source source node path
     * @param destination node where the node has to be moved
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void moveTo(String source, String destination) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Access.isGranted(this.accessManager, source, Permission.REMOVE);
        Access.isGranted(this.accessManager, destination, Permission.WRITE);
        this.workSpace.move(source, destination);
    }

    /**
     * copy content to the specified location
     * @param source source node path
     * @param destination node where the node has to be copied
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void copyTo(String source, String destination) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Access.isGranted(this.accessManager, source, Permission.READ);
        Access.isGranted(this.accessManager, destination, Permission.WRITE);
        this.workSpace.copy(source, destination);
    }

    /**
     * Persists all changes to the repository if validation succeds
     * @throws RepositoryException
     */
    public void save() throws RepositoryException {
        try {
            this.startPage.getSession().save();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            throw re;
        }
    }

    /**
     * Returns true if the session has pending (unsaved) changes.
     */
    public boolean hasPendingChanges() throws RepositoryException {
        return this.startPage.getSession().hasPendingChanges();
    }

    /**
     * Refreshes this session
     * @param keepChanges
     * @throws RepositoryException
     * @see javax.jcr.Session#refresh(boolean)
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.workSpace.getSession().refresh(keepChanges);
    }

}
