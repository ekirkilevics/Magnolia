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
 * User: sameercharles Date: Sept 23, 2004 Time: 1:42:48 PM
 * @author Sameer Charles
 * @version 2.01
 */
public class HierarchyManager {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(HierarchyManager.class);

    private Node startPage;

    private Workspace workSpace;

    private String userID;

    private AccessManager accessManager;

    /**
     * constructor
     */
    public HierarchyManager() {
        this.userID = "anonymous";
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
     * @deprecated instead use init(Node rootNode)
     * @see HierarchyManager#init(javax.jcr.Node)
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
     * Set access manager for this hierarchy.
     */
    public void setAccessManager(AccessManager manager) {
        this.accessManager = manager;
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

    private String getNodePath(String path, String label) {
        if (StringUtils.isEmpty(path) || (path.equals("/"))) {
            return label;
        }
        return getNodePath(path + "/" + label);
    }

    private String getNodePath(String path) {
        if (path.startsWith("/")) {
            path = path.replaceFirst("/", "");
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
        md.setTitle("");
        md.setSequencePosition();
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
        // todo remove this.. caller should take care of this
        if (path.equals("/")) {
            return this.getRoot();
        }
        Content content = (new Content(this.startPage, getNodePath(path), this.accessManager));
        return content;
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
        Content content = new Content(this.startPage, getNodePath(path), this.accessManager);
        return content;
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

        NodeData nodeData = new NodeData(this.startPage, nodePath, this.accessManager);
        return nodeData;
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
     * removes specified path, it can be either node or property
     * @param path to be removed
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException
     */
    public void delete(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (this.isNodeData(path)) {
            this.startPage.getProperty(makeRelative(path)).remove();
        }
        else {
            this.startPage.getNode(makeRelative(path)).remove();
        }

    }

    private String makeRelative(String path) {
        return StringUtils.stripStart(path, "/");
    }

    /**
     * @return startPage of the current working repository-workspace
     */
    public Content getRoot() throws RepositoryException, AccessDeniedException {
        Content content = (new Content(this.startPage, this.accessManager));
        return content;
    }

    /**
     * Checks if the requested resource is a page (hierarchy Node).
     * @param path of the requested content
     * @return boolean true is the requested content is a Hierarchy Node
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
            log.error(re);
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
            log.debug(re);
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
     */
    public Content getContentByUUID(String uuid) throws ItemNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new Content(this.startPage.getSession().getNodeByUUID(uuid), this.accessManager));
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

}
