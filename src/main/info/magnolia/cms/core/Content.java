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

import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.core.util.Access;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Path;
import java.io.IOException;
import java.io.OutputStream;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;


/**
 * User: sameercharles Date: May 27, 2004 Time: 04:55:10 PM
 * @author Sameer Charles
 * @version 2.0
 */
public class Content extends ContentHandler implements Cloneable {

    private static Logger log = Logger.getLogger(Content.class);

    private String path;

    private Node rootNode;

    private MetaData metaData;

    /**
     * constructor
     */
    Content() {
    }

    /**
     * constructor to get existing node
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content(Node rootNode, String path, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(rootNode.getPath(), path), Permission.READ);
        this.setPath(path);
        this.setRootNode(rootNode);
        this.setNode(this.rootNode.getNode(this.path));
        this.setAccessManager(manager);
    }

    /**
     * constructor to get existing node
     * @param elem , initialized node object
     */
    public Content(Item elem, AccessManager manager) throws RepositoryException, AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(elem.getPath()), Permission.READ);
        this.setNode((Node) elem);
        this.setPath(this.getHandle());
        this.setAccessManager(manager);
    }

    /**
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @param isHierarchyNode create a new node as hierarchy node, else node
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content(Node rootNode, String path, boolean isHierarchyNode, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(rootNode.getPath(), path), Permission.WRITE);
        this.setPath(path);
        this.setRootNode(rootNode);
        if (isHierarchyNode)
            this.node = this.rootNode.addNode(this.path, ItemType.getSystemName(ItemType.NT_CONTENT));
        else
            this.node = this.rootNode.addNode(this.path, ItemType.getSystemName(ItemType.NT_CONTENTNODE));
        this.setAccessManager(manager);
        if (this.node.canAddMixin(ItemType.getSystemName(ItemType.MIX_Versionable))) {
            this.node.addMixin(ItemType.getSystemName(ItemType.MIX_Versionable));
        }
    }

    /**
     * <p>
     * creates contentNode of type <b>contentType </b> contentType must be defined in item type definition of magnolia
     * as well as JCR implementation
     * </p>
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @param contentType , JCR node type as configured
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content(Node rootNode, String path, String contentType, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(rootNode.getPath(), path), Permission.WRITE);
        this.setPath(path);
        this.setRootNode(rootNode);
        this.node = this.rootNode.addNode(this.path, ItemType.getSystemName(contentType));
        this.setAccessManager(manager);
        if (this.node.canAddMixin(ItemType.getSystemName(ItemType.MIX_Versionable))) {
            this.node.addMixin(ItemType.getSystemName(ItemType.MIX_Versionable));
        }
    }

    /**
     * <p>
     * checks if the requested resource is an NodeData (Property)
     * </p>
     * @param contentType of the requested NodeData
     * @return boolean true is the requested content is an NodeData
     */
    public boolean isType(String contentType) {
        try {
            return (this.node.isNodeType(ItemType.getSystemName(contentType)));
        }
        catch (RepositoryException e) {
            log.error(e);
        }
        return false;
    }

    /**
     * <p>
     * bit by bit copy of the current object
     * </p>
     * @return Object cloned object
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    protected void setNode(Node node) {
        this.node = node;
    }

    protected void setRootNode(Node node) {
        this.rootNode = node;
    }

    protected void setPath(String path) {
        this.path = path;
    }

    /**
     * <p>
     * get ContentNode node of the current node with the specified name
     * </p>
     * @param name of the node acting as <code>ContentNode</code>
     * @return <node>ContentNode </node>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public ContentNode getContentNode(String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        ContentNode contentNode = new ContentNode(this.node, name, this.accessManager);
        return contentNode;
    }

    /**
     * <p>
     * create ContentNode node under the current node with the specified name
     * </p>
     * @param name of the node to be created as <code>ContentNode</code>
     * @return newly created <node>ContentNode </node>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public ContentNode createContentNode(String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        ContentNode contentNode = (new ContentNode(this.node, name, true, this.accessManager));
        MetaData metaData = contentNode.getMetaData();
        metaData.setCreationDate();
        return contentNode;
    }

    /**
     * <p>
     * get Content node of the current node with the specified name
     * </p>
     * @param name of the node acting as <code>Content</code>
     * @return <node>Content </node>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new Content(this.node, name, this.accessManager));
    }

    /**
     * <p>
     * create Content node under the current node with the specified name
     * </p>
     * @param name of the node to be created as <code>Content</code>
     * @return newly created <node>Content </node>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content createContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content content = (new Content(this.node, name, true, this.accessManager));
        MetaData metaData = content.getMetaData();
        metaData.setCreationDate();
        return content;
    }

    /**
     * @return String, template name
     */
    public String getTemplate() {
        return this.getMetaData().getTemplate();
    }

    /**
     * @return String, title
     */
    public String getTitle() {
        try {
            return this.getNodeData("title").getString();
        }
        catch (AccessDeniedException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    /**
     * <p>
     * get meta data of the current node
     * </p>
     * @return MetaData meta information of the content <code>Node</code>
     */
    public MetaData getMetaData() {
        if (this.metaData == null)
            this.metaData = new MetaData(this.node, this.accessManager);
        return this.metaData;
    }

    /**
     * <p>
     * get meta data of the current node
     * </p>
     * @return MetaData meta information of the context under the content <code>Node</code>
     */
    public MetaData getMetaData(String context) {
        return new MetaData(this.node, context, this.accessManager);
    }

    /**
     * <p>
     * get top level NodeData
     * </p>
     * @return NodeData requested <code>NodeData</code> object
     */
    public NodeData getNodeData(String name) throws AccessDeniedException {
        try {
            return (new NodeData(this.node, name, this.accessManager));
        }
        catch (RepositoryException re) {
            return (new NodeData());
        }
    }

    /**
     * <p>
     * get node name
     * </p>
     * @return String name of the current <code>Node</code>
     */
    public String getName() {
        try {
            return this.node.getName();
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * <p>
     * create top level NodeData object
     * </p>
     * @param name to be created
     * @return NodeData requested <code>NodeData</code> object
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public NodeData createNodeData(String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new NodeData(this.node, name, true, this.accessManager));
    }

    /**
     * <p>
     * create NodeData with the given value and type
     * </p>
     * @param name to be created
     * @param value to be set initially
     * @param type propertyType
     * @return NodeData requested <code>NodeData</code> object
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public NodeData createNodeData(String name, Value value, int type) throws PathNotFoundException,
        RepositoryException, AccessDeniedException {
        return (new NodeData(this.node, name, value, this.accessManager));
    }

    /**
     * <p>
     * delete NodeData with the specified name
     * </p>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public void deleteNodeData(String name) throws PathNotFoundException, RepositoryException {
        this.delete(name);
    }

    /**
     * <p>
     * delete Content node with the specified name from the current node
     * </p>
     * @param name of the Content to be deleted
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void deleteContent(String name) throws PathNotFoundException, RepositoryException {
        this.delete(name);
    }

    /**
     * <p>
     * delete ContentNode with the specified name from the current node
     * </p>
     * @param name of the ContentNode to be deleted
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void deleteContentNode(String name) throws PathNotFoundException, RepositoryException {
        this.delete(name);
    }

    /**
     * <p>
     * Remove specified path
     * </p>
     * @throws RepositoryException
     */
    public void delete(String path) throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath(), path), Permission.REMOVE);
        this.node.getNode(path).remove();
    }

    /**
     * <p>
     * Remove this Content object
     * </p>
     * @throws RepositoryException
     */
    public void delete() throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath()), Permission.REMOVE);
        this.node.remove();
    }

    /**
     * <p>
     * move content to the specified location
     * </p>
     * @param path where current node has to be moved
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @deprecated as on magnolia 2.0
     */
    public void moveTo(String path) throws PathNotFoundException, RepositoryException {
        log.error("Not supported - use [ HierarchyManager.moveTo ]");
    }

    /**
     * <p>
     * copy content to the specified location
     * </p>
     * @param path where current node has to be copied
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @deprecated as on magnolia 2.0
     */
    public void copyTo(String path) throws PathNotFoundException, RepositoryException {
        log.error("Not supported - use [ HierarchyManager.copyTo ]");
    }

    /**
     * <p>
     * move current node to the specified location above the named <code>beforename</code>
     * </p>
     * @param srcName where current node has to be moved
     * @param beforeName name of the node before the current node has to be placed
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void orderBefore(String srcName, String beforeName) throws PathNotFoundException, RepositoryException {
        this.node.orderBefore(srcName, beforeName);
    }

    /**
     * <p>
     * get xml data as bite stream <br>
     * </p>
     * @param out OutputStream to which xml bite stream will be written
     * @param onlyThis boolean saying weather to stream only current node or the entire sub tree.
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @deprecated as on magnolia 2.0
     */
    public void toStream(OutputStream out, boolean onlyThis) throws IOException, RepositoryException {
        log.error("moved to HierarchyManager-Workspace");
    }

    /**
     * <p>
     * you could call this method anytime to update working page properties - Modification date & Author ID
     * </p>
     */
    public void updateMetaData(HttpServletRequest request) throws RepositoryException, AccessDeniedException {
        MetaData md = this.getMetaData();
        md.setModificationDate();
        md.setAuthorId(Authenticator.getUserId(request));
        md = null;
    }

    /**
     * <p>
     * Restores this node to the state recorded in the specified versionName
     * </p>
     * @param versionName
     */
    public void restore(String versionName) throws VersionException, UnsupportedRepositoryOperationException,
        RepositoryException {
        this.node.restore(versionName);
    }

    /**
     * <p>
     * Restores this node to the state recorded in the specified version
     * </p>
     * @param version
     */
    public void restore(Version version) throws VersionException, UnsupportedRepositoryOperationException,
        RepositoryException {
        this.node.restore(version);
    }

    /**
     * <p>
     * Restores this node to the state recorded in the version specified by versionLabel.
     * </p>
     * @param versionLabel
     */
    public void restoreByLabel(String versionLabel) throws VersionException, UnsupportedRepositoryOperationException,
        RepositoryException {
        this.node.restoreByLabel(versionLabel);
    }

    /**
     * add version leaving the node checked out
     */
    public Version addVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        Version version = this.checkIn();
        this.checkOut();
        return version;
    }

    /**
     * @return checked in version
     */
    public Version checkIn() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.node.checkin();
    }

    /**
     * check out for further write operations
     */
    public void checkOut() throws UnsupportedRepositoryOperationException, RepositoryException {
        this.node.checkout();
    }

    /**
     * @return version history
     */
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.node.getVersionHistory();
    }

    /**
     * @return Version iterator retreived from version history
     */
    public VersionIterator getAllVersions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.getVersionHistory().getAllVersions();
    }

    /**
     * <p>
     * Persists all changes to the repository if valiation succeds
     * </p>
     * @throws RepositoryException
     */
    public void save() throws RepositoryException {
        this.node.save();
    }

    /**
     * <p>
     * Refreses current node keeping all changes
     * </p>
     * @throws RepositoryException
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.node.refresh(keepChanges);
    }

    /**
     * <p>
     * checks for the allowed access rights
     * </p>
     * @param permissions as defined in javax.jcr.Permission
     * @return true is the current user has specified access on this node.
     */
    public boolean isGranted(long permissions) {
        try {
            Access.isGranted(this.accessManager, Path.getAbsolutePath(node.getPath()), permissions);
            return true;
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return false;
    }
}
