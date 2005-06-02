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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Permission;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public class HierarchyManager {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(HierarchyManager.class);

    /**
     * Used by this Hierarchy as a "root" node of a workspace
     * */
    private Node startPage;

    /**
     * Workspace represented by this hierarchy
     * */
    private Workspace workSpace;

    /**
     * user who created this hierarchy manager
     * */
    private String userID;

    /**
     * Access manager used by this hierarchy
     * */
    private AccessManager accessManager;

    /**
     * constructor
     */
    public HierarchyManager() {
        this.userID = "anonymous";
    }

    /**
     * Construct this object with the specified user id
     * @param userID a string representing the owner
     * */
    public HierarchyManager(String userID) {
        this.userID = userID;
    }

    /**
     * constructor
     * @param request
     */
    public HierarchyManager(HttpServletRequest request) {
        this.userID = Authenticator.getUserId(request);
    }

    /**
     * sets start page of the current working repository
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @deprecated instead use init(Node rootNode)
     * @see HierarchyManager#init(javax.jcr.Node)
     */
    public void setStartPage(Node rootNode) throws PathNotFoundException, RepositoryException {
        this.startPage = rootNode;
        this.workSpace = this.startPage.getSession().getWorkspace();
    }

    /**
     * Initialize hierarchy manager and sets default workspace
     * @param rootNode JCR node to be set as root for this workspace represented by this hierarchy
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void init(Node rootNode) throws PathNotFoundException, RepositoryException {
        this.startPage = rootNode;
        this.workSpace = this.startPage.getSession().getWorkspace();
    }

    /**
     * Initialize hierarchy manager and sets default workspace
     * @param rootNode JCR node to be set as root for this workspace represented by this hierarchy
     * @param manager access manager to be used by this hierarchy
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void init(Node rootNode, AccessManager manager) throws PathNotFoundException, RepositoryException {
        this.startPage = rootNode;
        this.workSpace = this.startPage.getSession().getWorkspace();
        this.accessManager = manager;
    }

    /**
     * Set access manager for this hierarchy.
     * @param manager
     */
    public void setAccessManager(AccessManager manager) {
        this.accessManager = manager;
    }

    /**
     * Creates a new content page
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
     * Creates contentNode of type <b>contentType</b> contentType must be registered to the JCR used.
     * @param path absolute (primary) path to this <code>Node</code>
     * @param label page name
     * @param contentType , JCR node type as configured
     * @throws PathNotFoundException if the given path does not exist
     * @throws RepositoryException if fail to create new node
     * @throws AccessDeniedException if the AccessManager associated to this object does not allow writing to this path
     * @return newly created content object
     */
    public Content createContent(String path, String label, String contentType) throws PathNotFoundException,
        RepositoryException, AccessDeniedException {
        try {
            Content newPage = new Content(
                this.startPage,
                this.getNodePath(path, label),
                contentType,
                this.accessManager);
            setMetaData(newPage.getMetaData());
            return newPage;
        }
        catch (ItemExistsException ee) {
            Content page = this.getContent(this.getNodePath(path, label));
            return page;
        }
    }

    /**
     * Concatinate goven path and label
     * @param path
     * @param label
     * @return concatinated path
     * */
    private String getNodePath(String path, String label) {
        if (StringUtils.isEmpty(path) || (path.equals("/"))) {
            return label;
        }
        return getNodePath(path + "/" + label);
    }

    /**
     * Removes any trailing slashes for the given path
     * @param path
     * @return path
     * */
    private String getNodePath(String path) {
        if (path.startsWith("/")) {
            path = path.replaceFirst("/", "");
        }
        return path;
    }

    /**
     * Helper method to set object properties, create page calls this method. you could call this method anytime to
     * create working page properties
     * @param md meta data for this object
     * @param template to be set as meta data property
     * @throws RepositoryException
     * @throws AccessDeniedException if not allowed to set meta data
     */
    public void setMetaData(MetaData md, String template) throws RepositoryException, AccessDeniedException {
        md.setTemplate(template);
        setMetaData(md);
    }

    /**
     * Helper method to set object properties, create page calls this method. you could call this method anytime to create
     * working page properties
     * @param md meta data for this object
     * @throws RepositoryException
     * @throws AccessDeniedException if not allowed to set meta data
     */
    public void setMetaData(MetaData md) throws RepositoryException, AccessDeniedException {
        md.setCreationDate();
        md.setModificationDate();
        md.setAuthorId(this.userID);
        md.setTitle("");
        md.setSequencePosition();
    }

    /**
     * Helper method to set object properties, get page calls this method. you could call this method anytime to update
     * working page properties
     * @param md meta data for this object
     * @throws RepositoryException
     * @throws AccessDeniedException if not allowed to update meta data
     */
    public void updateMetaData(MetaData md) throws RepositoryException, AccessDeniedException {
        md.setModificationDate();
        md.setAuthorId(this.userID);
    }

    /**
     * Returns the page specified by path in the parameter
     * @param path handle of the page to be initialized
     * @return Content hierarchy node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException
     * @deprecated use getContent(String path) instead
     */
    public Content getPage(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getContent(path);
    }

    /**
     * Get content object of the requested URI
     * @param path of the content to be initialized
     * @return Content
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException if not allowed to read this path
     */
    public Content getContent(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        // todo remove this.. caller should take care of this
        if (path.equals("/")) {
            return this.getRoot();
        }
        Content content = (new Content(this.startPage, getNodePath(path), this.accessManager));
        return content;
    }

    /**
     * Get content node object of the requested URI
     * @param path of the content (container / containerlist) to be initialized
     * @return ContentNode
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException
     * @deprecated use getContent(String path) instead
     */
    public Content getContentNode(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content content = new Content(this.startPage, getNodePath(path), this.accessManager);
        return content;
    }

    /**
     * Get NodeData object of the requested URI
     * @param path of the property to be initialized
     * @return NodeData
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException if not alloed to read this path
     */
    public NodeData getNodeData(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        String nodePath = getNodePath(path);
        if (StringUtils.isEmpty(nodePath)) {
            return null;
        }

        NodeData nodeData = new NodeData(this.startPage, nodePath, this.accessManager);
        return nodeData;
    }

    /**
     * Returns the first page with a given template name that is found in tree that starts from the page given py the
     * path (including this page)
     * @param path handle of the page from where the search should start
     * @param templateName template name to search for
     * @return first Content hierarchy node that has the specified template name assigned
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException if not allowed to read this path
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
                Collection children = page.getChildren(ItemType.CONTENT.getSystemName(), ContentHandler.SORT_BY_NAME);
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
            log.error("Failed to get - " + path);
            log.error(e.getMessage(), e);
        }
        return pageToBeFound;
    }

    /**
     * Removes specified path, it can be either node or aproperty
     * @param path to be removed
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException if not allowed to remove this path
     */
    public void delete(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (this.isNodeData(path)) {
            this.startPage.getProperty(makeRelative(path)).remove();
        }
        else {
            this.startPage.getNode(makeRelative(path)).remove();
        }

    }

    /**
     * Helper method to convert the given path in to a relative path
     * @param path
     * @return path with no trailing slashes
     * */
    private String makeRelative(String path) {
        return StringUtils.stripStart(path, "/");
    }

    /**
     * Get content object representing workspace root node
     * @return root of the current working workspace
     * @throws RepositoryException
     * @throws AccessDeniedException if not allowed to read this workspace
     */
    public Content getRoot() throws RepositoryException, AccessDeniedException {
        Content content = (new Content(this.startPage, this.accessManager));
        return content;
    }

    /**
     * Checks if the requested resource is a page (hierarchy Node).
     * @param path of the requested content
     * @return boolean true if the requested content is a Hierarchy Node
     * @throws AccessDeniedException if not allowed to read this path
     */
    public boolean isPage(String path) throws AccessDeniedException {
        Access.isGranted(this.accessManager, path, Permission.READ);

        String nodePath = getNodePath(path);
        if (StringUtils.isEmpty(nodePath)) {
            return false;
        }

        try {
            Node n = this.startPage.getNode(nodePath);
            return (n.isNodeType(ItemType.CONTENT.getSystemName()));
        }
        catch (RepositoryException re) {
        }
        return false;
    }

    /**
     * Check is either the node or property exists with the specified path
     * @param path to be checked
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
            log.error(re);
        }
        return isExist;
    }

    /**
     * Evaluate primary node type of the node at the given path.
     * @param path to be evaluated
     * @param type node type
     * @return true if the node at the given path is of specified type
     */
    public boolean isNodeType(String path, String type) {
        try {
            Node n = this.startPage.getNode(getNodePath(path));
            return n.isNodeType(type);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re);
        }
        return false;
    }

    /**
     * Evaluate primary node type of the node at the given path.
     * @param path to be evaluated
     * @param type ItemType
     * @return true if the requested content is an NodeData
     */
    public boolean isNodeType(String path, ItemType type) {
        return isNodeType(path, type.getSystemName());
    }

    /**
     * Checks if the requested resource is a NodeData (Property)
     * @param path of the requested NodeData
     * @return true if the requested content is an NodeData
     * @throws AccessDeniedException if not allowed to read this path
     */
    public boolean isNodeData(String path) throws AccessDeniedException {
        Access.isGranted(this.accessManager, path, Permission.READ);

        String nodePath = getNodePath(path);
        if (StringUtils.isEmpty(nodePath)) {
            return false;
        }

        try {
            return this.startPage.hasProperty(nodePath);
        }
        catch (RepositoryException e) {
        }
        return false;
    }

    /**
     * This method can be used to retrieve Content which has UUID assigned to it, in other words only those nodes which
     * has mixin type mix:referenceable.
     * @param uuid
     * @return content
     * @throws ItemNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException if not allowed to read the node found with the given UUID
     */
    public Content getContentByUUID(String uuid) throws ItemNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new Content(this.startPage.getSession().getNodeByUUID(uuid), this.accessManager));
    }

    /**
     * Gets currently used workspace for this hierarchy manager
     * @return workspace
     */
    public Workspace getWorkspace() {
        return this.workSpace;
    }

    /**
     * Move content to the specified location
     * @param source source node path
     * @param destination node where the node has to be moved
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException if either not allowed to remove the source or write on destonation
     */
    public void moveTo(String source, String destination) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Access.isGranted(this.accessManager, source, Permission.REMOVE);
        Access.isGranted(this.accessManager, destination, Permission.WRITE);
        this.workSpace.move(source, destination);
    }

    /**
     * Copy content to the specified location
     * @param source source node path
     * @param destination node where the node has to be copied
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException if either not allowed to read the source or write on destonation
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
     * Check if current session to which this hierarchy is associated has some pending changes to be persisted
     * @return true if the session has pending (unsaved) changes.
     */
    public boolean hasPendingChanges() throws RepositoryException {
        return this.startPage.getSession().hasPendingChanges();
    }

}
