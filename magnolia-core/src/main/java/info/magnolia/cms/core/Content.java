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

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.doomdark.uuid.UUIDGenerator;


/**
 * todo  - refactor all getChildren methods, use getChildren(ContentFilter)
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class Content extends ContentHandler implements Cloneable {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Content.class);

    /**
     * UUID property added on creation of object
     */
    private static final String PROPERTY_UUID = "mgnl:uuid"; //$NON-NLS-1$

    /**
     * Wrapped jcr node.
     */
    protected Node node;

    /**
     * Path for the jcr node.
     */
    private String path;

    /**
     * root node.
     */
    private Node rootNode;

    /**
     * node metadata.
     */
    private MetaData metaData;

    /**
     * Empty constructor. Should NEVER be used for standard use, test only.
     */
    protected Content() {
    }

    /**
     * Constructor to get existing node.
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @param manager AccessManager instance
     * @throws PathNotFoundException if the node at <code>path</code> does not exist
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
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
     * Constructor to get existing node.
     * @param elem initialized node object
     * @param manager AccessManager instance
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     */
    public Content(Item elem, AccessManager manager) throws RepositoryException, AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(elem.getPath()), Permission.READ);
        this.setNode((Node) elem);
        this.setPath(this.getHandle());
        this.setAccessManager(manager);
    }

    /**
     * creates contentNode of type <b>contentType </b> contentType must be defined in item type definition of magnolia
     * as well as JCR implementation
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @param contentType JCR node type as configured
     * @param manager AccessManager instance
     * @throws PathNotFoundException if the node at <code>path</code> does not exist
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public Content(Node rootNode, String path, String contentType, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(rootNode.getPath(), path), Permission.WRITE);
        this.setPath(path);
        this.setRootNode(rootNode);
        this.node = this.rootNode.addNode(this.path, contentType);
        this.addUUID();
        this.setAccessManager(manager);
        this.addMixin(ItemType.MIX_VERSIONABLE);
    }

    /**
     * bit by bit copy of the current object. Warning: this doesn't clone wrapped jcr nodes.
     * @return Object cloned object
     */
    public Object clone() { // don't add throws CloneNotSupportedException! the super class ContentHandler doesn't throw
        // it anymore, so it will not compile
        return super.clone();
    }

    /**
     * @param node
     */
    protected void setNode(Node node) {
        this.node = node;
    }

    /**
     * @param node
     */
    protected void setRootNode(Node node) {
        this.rootNode = node;
    }

    /**
     * @param path
     */
    protected void setPath(String path) {
        this.path = path;
    }

    /**
     * get ContentNode node of the current node with the specified name
     * @param path of the node acting as <code>ContentNode</code>
     * @return ContentNode
     * @throws PathNotFoundException if the node at <code>path</code> does not exist
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @deprecated use getContent(String name) instead
     */
    public Content getContentNode(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content content = new Content(this.node, path, this.accessManager);
        return content;
    }

    /**
     * create ContentNode node under the current node with the specified name
     * @param name of the node to be created as <code>ContentNode</code>
     * @return newly created <node>ContentNode </node>
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @deprecated use createContent(String name, String contentType) instead
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public Content createContentNode(String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return this.createContent(name, ItemType.CONTENTNODE);
    }

    /**
     * get Content node of the current node with the specified name
     * @param name of the node acting as <code>Content</code>
     * @return <node>Content </node>
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new Content(this.node, name, this.accessManager));
    }

    /**
     * create Content node under the current node with the specified name
     * @param name of the node to be created as <code>Content</code>
     * @return newly created <node>Content </node>
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public Content createContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.createContent(name, ItemType.CONTENT);
    }

    /**
     * create Content node under the current node with the specified name
     * @param name of the node to be created as <code>Content</code>
     * @param contentType JCR node type as configured
     * @return newly created <node>Content </node>
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Content content = (new Content(this.node, name, contentType, this.accessManager));
        MetaData metaData = content.getMetaData();
        metaData.setCreationDate();
        return content;
    }

    /**
     * Create Content node under the current node with the specified name.
     * @param name of the node to be created as <code>Content</code>
     * @param contentType ItemType
     * @return newly created <node>Content </node>
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public Content createContent(String name, ItemType contentType) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Content content = (new Content(this.node, name, contentType.getSystemName(), this.accessManager));
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
        return this.getNodeData("title").getString(); //$NON-NLS-1$
    }

    /**
     * get meta data of the current node
     * @return MetaData meta information of the content <code>Node</code>
     */
    public MetaData getMetaData() {
        if (this.metaData == null) {
            this.metaData = new MetaData(this.node, this.accessManager);
        }
        return this.metaData;
    }

    /**
     * get meta data of the current node
     * @return MetaData meta information of the context under the content <code>Node</code>
     */
    public MetaData getMetaData(String context) {
        return new MetaData(this.node, context, this.accessManager);
    }

    /**
     * get top level NodeData
     * @return NodeData requested <code>NodeData</code> object
     */

    public NodeData getNodeData(String name) {
        return getNodeData(name, false);
    }

    /**
     * @param name
     * @param create
     */
    public NodeData getNodeData(String name, boolean create) {
        try {
            return (new NodeData(this.node, name, this.accessManager));
        }
        catch (PathNotFoundException e) {
            if (create) {
                try {
                    return this.createNodeData(name);
                }
                catch (Exception e1) {
                    log.error("can't create property [" + name + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            if (log.isDebugEnabled()) {
                String nodepath = null;
                try {
                    nodepath = this.node.getPath();
                }
                catch (RepositoryException e1) {
                    // ignore, debug only
                }
                if (log.isDebugEnabled()) {
                    log.debug("Path not found for property [" + name + "] in node " + nodepath); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            return (new NodeData());
        }
        catch (RepositoryException re) {
            String nodepath = null;
            try {
                nodepath = this.node.getPath();
            }
            catch (RepositoryException e1) {
                // ignore, debug only
            }
            log.warn("Repository exception while trying to read property [" + name + "] for node " + nodepath, re); //$NON-NLS-1$ //$NON-NLS-2$
            return (new NodeData());
        }
    }

    /**
     * get node name
     * @return String name of the current <code>Node</code>
     */
    public String getName() {
        try {
            return this.node.getName();
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * create top level NodeData object
     * @param name to be created
     * @return NodeData requested <code>NodeData</code> object
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public NodeData createNodeData(String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new NodeData(this.node, name, true, this.accessManager));
    }

    /**
     * Create NodeData with the given value and type.
     * @param name to be created
     * @param value to be set initially
     * @return NodeData requested <code>NodeData</code> object
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public NodeData createNodeData(String name, Value value) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new NodeData(this.node, name, value, this.accessManager));
    }

    /**
     * Create NodeData with the given value and type.
     * @param name to be created
     * @param value to be set initially
     * @param type propertyType
     * @return NodeData requested <code>NodeData</code> object
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public NodeData createNodeData(String name, Value value, int type) throws PathNotFoundException,
        RepositoryException, AccessDeniedException {
        return createNodeData(name, value);
    }

    /**
     * delete NodeData with the specified name
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     */
    public void deleteNodeData(String name) throws PathNotFoundException, RepositoryException {
        this.node.getProperty(name).remove();
    }

    /**
     * you could call this method anytime to update working page properties - Modification date & Author ID
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     */
    public void updateMetaData(HttpServletRequest request) throws RepositoryException, AccessDeniedException {
        MetaData md = this.getMetaData();
        md.setModificationDate();
        md.setAuthorId(Authenticator.getUserId(request));
        md = null;
    }

    /**
     * Get a collection containing child nodes which satisfies the given filter
     * @param filter
     * @return Collection of content objects
     * */
    public Collection getChildren(ContentFilter filter) {
        Collection children = new ArrayList();
        try {
            NodeIterator nodeIterator = this.node.getNodes();
            while (nodeIterator.hasNext()) {
                Node subNode = (Node) nodeIterator.next();
                try {
                    Content content = new Content(subNode, this.accessManager);
                    if (filter.accept(content)) {
                        children.add(content);
                    }
                }
                catch (PathNotFoundException e) {
                    log.error(e);
                }
                catch (AccessDeniedException e) {
                    // ignore, simply wont add content in a list
                }
            }
        } catch (RepositoryException re) {
            log.error(re);
        }
        return children;
    }

    /**
     * gets a Collection containing all child nodes of the same NodeType as "this" object.
     * @return Collection of content objects
     */
    public Collection getChildren() {
        String type = null;

        try {
            type = this.getNodeType().getName();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re);
        }
        // @todo workaround
        // fix all getChildren calls from the root node
        if ("rep:root".equalsIgnoreCase(type)) { //$NON-NLS-1$
            type = ItemType.CONTENT.getSystemName();
        }
        // --------------------------------------------------
        return this.getChildren(StringUtils.defaultString(type));
    }

    // collects all the children nodes, at any level
    public List collectAllChildren() {
        List nodes = new ArrayList();
        return collectAllChildren(nodes, this);
    }

    // collects all the nodes under a same path
    private List collectAllChildren(List nodes, Content node) {
        Collection children = node.getChildren();
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            nodes.add(child);
            collectAllChildren(nodes, child);
        }
        return nodes;
    }

    /**
     * Get collection of specified content type.
     * @param contentType JCR node type as configured
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType) {
        return this.getChildren(contentType, ContentHandler.IGNORE_SORT);
    }

    /**
     * Get collection of specified content type.
     * @param contentType ItemType
     * @return Collection of content nodes
     */
    public Collection getChildren(ItemType contentType) {
        return this.getChildren(contentType.getSystemName(), ContentHandler.IGNORE_SORT);
    }

    /**
     * Get collection of specified content type.
     * @param contentType JCR node type as configured
     * @param namePattern
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType, String namePattern) {
        return this.getChildren(contentType, namePattern, ContentHandler.IGNORE_SORT);
    }

    /**
     * Get collection of specified content type
     * @param contentType JCR node type as configured
     * @param sortCriteria which can be either ContentHandler.SORT_BY_SEQUENCE , ContentHandler.SORT_BY_DATE or
     * ContentHandler.SORT_BY_NAME
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType, int sortCriteria) {
        return this.getChildren(contentType, "*", sortCriteria); //$NON-NLS-1$
    }

    /**
     * Get collection of specified content type
     * @param contentType ItemType
     * @param sortCriteria which can be either ContentHandler.SORT_BY_SEQUENCE , ContentHandler.SORT_BY_DATE or
     * ContentHandler.SORT_BY_NAME
     * @return Collection of content nodes
     */
    public Collection getChildren(ItemType contentType, int sortCriteria) {
        return this.getChildren(contentType.getSystemName(), sortCriteria);
    }

    /**
     * Get collection of specified content type.
     * @param contentType JCR node type as configured
     * @param namePattern
     * @param sortCriteria which can be either ContentHandler.SORT_BY_SEQUENCE , ContentHandler.SORT_BY_DATE or
     * ContentHandler.SORT_BY_NAME
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType, String namePattern, int sortCriteria) {
        Collection children = new ArrayList();
        try {
            children = this.getChildContent(contentType, namePattern);
            children = sort(children, sortCriteria);
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return children;
    }

    /**
     * @param collection Collection of content nodes
     * @param sortCriteria
     * @return
     */
    private Collection sort(Collection collection, int sortCriteria) {
        if (sortCriteria == ContentHandler.SORT_BY_DATE) {
            return sortByDate(collection);
        }
        else if (sortCriteria == ContentHandler.SORT_BY_SEQUENCE) {
            return sortBySequence(collection);
        }
        return collection;
    }

    /**
     * @param contentType JCR node type as configured
     * @param namePattern
     * @return
     * @throws RepositoryException if an error occurs
     */
    private Collection getChildContent(String contentType, String namePattern) throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = this.node.getNodes(namePattern);
        while (nodeIterator.hasNext()) {
            Node subNode = (Node) nodeIterator.next();
            try {
                if (subNode.isNodeType(contentType)) {
                    children.add(new Content(subNode, this.accessManager));
                }
            }
            catch (PathNotFoundException e) {
                log.error(e);
            }
            catch (AccessDeniedException e) {
                // ignore, simply wont add content in a list
            }
        }
        return children;
    }

    /**
     * Gets all properties bind in NodeData object excluding JCR system properties
     */
    public Collection getNodeDataCollection() {
        Collection children = new ArrayList();
        try {
            PropertyIterator propertyIterator = this.node.getProperties();
            while (propertyIterator.hasNext()) {
                Property property = (Property) propertyIterator.next();
                try {
                    if (!property.getName().startsWith("jcr:") && !property.getName().startsWith("mgnl:")) { //$NON-NLS-1$ //$NON-NLS-2$
                        children.add(new NodeData(property, this.accessManager));
                    }
                }
                catch (PathNotFoundException e) {
                    log.error(e);
                }
                catch (AccessDeniedException e) {
                    // ignore, simply wont add content in a list
                }
            }
        }
        catch (RepositoryException re) {
            log.error(re);
        }
        return children;
    }

    /**
     * Gets all properties bind in NodeData object which qualify the given namePattern
     * @param namePattern
     */
    public Collection getNodeDataCollection(String namePattern) {
        Collection children = new ArrayList();
        try {
            PropertyIterator propertyIterator = this.node.getProperties(namePattern);
            if (propertyIterator == null) {
                return children;
            }
            while (propertyIterator.hasNext()) {
                Property property = (Property) propertyIterator.next();
                try {
                    children.add(new NodeData(property, this.accessManager));
                }
                catch (PathNotFoundException e) {
                    log.error(e);
                }
                catch (AccessDeniedException e) {
                    // ignore, simply wont add content in a list
                }
            }
        }
        catch (RepositoryException re) {
            log.error(re);
        }
        return children;
    }

    /**
     * @return Boolean, if sub node(s) exists
     */
    public boolean hasChildren() {
        return (this.getChildren().size() > 0);
    }

    /**
     * @param contentType JCR node type as configured
     * @return Boolean, if sub <code>collectionType</code> exists
     */
    public boolean hasChildren(String contentType) {
        return (this.getChildren(contentType).size() > 0);
    }

    /**
     * @param name
     * @throws RepositoryException if an error occurs
     */
    public boolean hasContent(String name) throws RepositoryException {
        return this.node.hasNode(name);
    }

    /**
     * @param name
     * @throws RepositoryException if an error occurs
     */
    public boolean hasNodeData(String name) throws RepositoryException {
        return this.node.hasProperty(name);
    }

    /**
     * Gets a Collection containing all child nodes at the current level+1 level
     * @param contentCollection collection of content nodes
     * @return sorted collection
     */
    public Collection sortByDate(Collection contentCollection) {
        try {
            if (contentCollection == null) {
                return contentCollection;
            }
            Collections.sort((List) contentCollection, new Comparator() {

                public int compare(Object o1, Object o2) {
                    Date date1 = ((Content) o1).getMetaData().getCreationDate().getTime();
                    Date date2 = ((Content) o2).getMetaData().getCreationDate().getTime();
                    return date1.compareTo(date2);
                }
            });
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return contentCollection;
    }

    /**
     * Gets a Collection containing all child nodes at the current level+1 level
     * @param contentCollection collection of content nodes
     * @return sorted collection
     */
    public Collection sortBySequence(Collection contentCollection) {
        try {
            if (contentCollection == null) {
                return contentCollection;
            }
            Collections.sort((List) contentCollection, new Comparator() {

                public int compare(Object o0, Object o1) {
                    try {
                        long pos0 = (((Content) o0).getMetaData().getSequencePosition());
                        long pos1 = (((Content) o1).getMetaData().getSequencePosition());
                        String s0 = "0"; //$NON-NLS-1$
                        String s1 = "0"; //$NON-NLS-1$
                        if (pos0 > pos1) {
                            s0 = "1"; //$NON-NLS-1$
                        }
                        else if (pos0 < pos1) {
                            s1 = "1"; //$NON-NLS-1$
                        }
                        return s0.compareTo(s1);
                    }
                    catch (Exception e) {
                        return 0;
                    }
                }

            });
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return contentCollection;
    }

    /**
     * get a handle representing path relative to the content repository
     * @return String representing path (handle) of the content
     */
    public String getHandle() {
        try {
            return this.node.getPath();
        }
        catch (RepositoryException e) {
            log.error("Failed to get handle: " + e.getMessage(), e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        }
    }

    /**
     * get a handle representing path relative to the content repository with the default extension
     * @return String representing path (handle) of the content
     * @throws RepositoryException if an error occurs
     */
    public String getHandleWithDefaultExtension() throws PathNotFoundException, RepositoryException {
        return (this.node.getPath() + "." + Server.getDefaultExtension()); //$NON-NLS-1$
    }

    /**
     * get parent content object
     * @throws javax.jcr.PathNotFoundException
     * @return Content representing parent node
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     */
    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new Content(this.node.getParent(), this.accessManager));
    }

    /**
     * get absolute parent object starting from the root node
     * @param digree level at which the requested node exist, relative to the ROOT node
     * @return Content representing parent node
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     */
    public Content getAncestor(int digree) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (digree > this.getLevel()) {
            throw new PathNotFoundException();
        }
        return (new Content(this.node.getAncestor(digree), this.accessManager));
    }

    /**
     * Convenience method for taglib
     * @return Content representing node on level 0
     * @throws RepositoryException if an error occurs
     */
    public Collection getAncestors() throws PathNotFoundException, RepositoryException {
        List allAncestors = new ArrayList();
        int level = this.getLevel();
        while (level != 0) {
            try {
                allAncestors.add(new Content(this.node.getAncestor(--level), this.accessManager));
            }
            catch (AccessDeniedException e) {
                // valid
            }
        }
        return allAncestors;
    }

    /**
     * get node level from the ROOT node : FIXME implement getDepth in javax.jcr
     * @throws javax.jcr.PathNotFoundException
     * @return level at which current node exist, relative to the ROOT node
     * @throws RepositoryException if an error occurs
     */
    public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.node.getPath().split("/").length - 1; //$NON-NLS-1$
    }

    /**
     * move current node to the specified location above the named <code>beforename</code>
     * @param srcName where current node has to be moved
     * @param beforeName name of the node before the current node has to be placed
     * @throws RepositoryException if an error occurs
     */
    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        this.node.orderBefore(srcName, beforeName);
    }

    /**
     * This method returns the index of this node within the ordered set of its same-name sibling nodes. This index is
     * the one used to address same-name siblings using the square-bracket notation, e.g., /a[3]/b[4]. Note that the
     * index always starts at 1 (not 0), for compatibility with XPath. As a result, for nodes that do not have
     * same-name-siblings, this method will always return 1.
     * @return The index of this node within the ordered set of its same-name sibling nodes.
     * @throws RepositoryException if an error occurs
     */
    public int getIndex() throws RepositoryException {
        return this.node.getIndex();
    }

    /**
     * utility method to get Node object used to create current content object
     * @return Node
     */
    public Node getJCRNode() {
        return this.node;
    }

    /**
     * evaluate primary node type of the associated Node of this object
     */
    public boolean isNodeType(String type) {
        try {
            return this.node.isNodeType(type);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re);
        }
        return false;
    }

    /**
     * returns primary node type definition of the associated Node of this object
     * @throws RepositoryException if an error occurs
     */
    public NodeType getNodeType() throws RepositoryException {
        return this.node.getPrimaryNodeType();
    }

    /**
     * Restores this node to the state defined by the version with the specified versionName.
     * @param versionName
     * @param removeExisting
     * @throws VersionException if the specified <code>versionName</code> does not exist in this node's version
     * history
     * @throws RepositoryException if an error occurs
     * @see javax.jcr.Node#restore(String, boolean)
     */
    public void restore(String versionName, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restore(versionName, removeExisting);
    }

    /**
     * Restores this node to the state defined by the specified version.
     * @param version
     * @param removeExisting
     * @throws VersionException if the specified <code>version</code> is not part of this node's version history
     * @throws RepositoryException if an error occurs
     * @see javax.jcr.Node#restore(javax.jcr.version.Version, boolean)
     */
    public void restore(Version version, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restore(version, removeExisting);
    }

    /**
     * Restores the specified version to relPath, relative to this node.
     * @param version
     * @param relPath
     * @param removeExisting
     * @throws VersionException if the specified <code>version</code> is not part of this node's version history
     * @throws RepositoryException if an error occurs
     * @see javax.jcr.Node#restore(javax.jcr.version.Version, String, boolean)
     */
    public void restore(Version version, String relPath, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restore(version, relPath, removeExisting);
    }

    /**
     * Restores this node to the state recorded in the version specified by versionLabel.
     * @param versionLabel
     * @param removeExisting
     * @throws VersionException if the specified <code>versionLabel</code> does not exist in this node's version
     * history
     * @throws RepositoryException if an error occurs
     * @see javax.jcr.Node#restoreByLabel(String, boolean)
     */
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restoreByLabel(versionLabel, removeExisting);
    }

    /**
     * add version leaving the node checked out
     * @throws RepositoryException if an error occurs
     */
    public Version addVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        Version version = this.checkIn();
        this.checkOut();
        return version;
    }

    /**
     * @return checked in version
     * @throws RepositoryException if an error occurs
     */
    public Version checkIn() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.node.checkin();
    }

    /**
     * check out for further write operations
     * @throws RepositoryException if an error occurs
     */
    public void checkOut() throws UnsupportedRepositoryOperationException, RepositoryException {
        this.node.checkout();
    }

    /**
     * @return version history
     * @throws RepositoryException if an error occurs
     */
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.node.getVersionHistory();
    }

    /**
     * @return Version iterator retreived from version history
     * @throws RepositoryException if an error occurs
     */
    public VersionIterator getAllVersions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.getVersionHistory().getAllVersions();
    }

    /**
     * Persists all changes to the repository if validation succeds
     * @throws RepositoryException if an error occurs
     */
    public void save() throws RepositoryException {
        this.node.getSession().save();
    }

    /**
     * checks for the allowed access rights
     * @param permissions as defined in javax.jcr.Permission
     * @return true is the current user has specified access on this node.
     */
    public boolean isGranted(long permissions) {
        try {
            Access.isGranted(this.accessManager, Path.getAbsolutePath(node.getPath()), permissions);
            return true;
        }
        catch (RepositoryException re) {
            log.debug(this.getHandle() + " says: no access"); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * Remove this path
     * @throws RepositoryException if an error occurs
     */
    public void delete() throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath()), Permission.REMOVE);
        this.node.remove();
    }

    /**
     * Remove specified path
     * @throws RepositoryException if an error occurs
     */
    public void delete(String path) throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath(), path), Permission.REMOVE);
        if (this.isNodeData(path)) {
            this.node.getProperty(path).remove();
        }
        else {
            this.node.getNode(path).remove();
        }
    }

    /**
     * checks if the requested resource is an NodeData (Property)
     * @param path of the requested NodeData
     * @return boolean true is the requested content is an NodeData
     * @throws AccessDeniedException
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     */
    public boolean isNodeData(String path) throws AccessDeniedException, RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath(), path), Permission.READ);
        try {
            return this.node.hasProperty(path);
        }
        catch (RepositoryException e) {
            log.debug("isNodeData(): " + e.getMessage()); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * If keepChanges is false, this method discards all pending changes recorded in this session.
     * @see javax.jcr.Node#refresh(boolean)
     * @throws RepositoryException if an error occurs
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.node.refresh(keepChanges);
    }

    /**
     * UUID of the node refrenced by this object
     * @return uuid
     */
    public String getUUID() {
        return this.getNodeData(PROPERTY_UUID).getString();
    }

    /**
     * add specified mixin type if allowed
     * @param type mixin type to be added
     * @throws RepositoryException if an error occurs
     */
    public void addMixin(String type) throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath()), Permission.SET);
        if (this.node.canAddMixin(type)) {
            this.node.addMixin(type);
        }
        else {
            log.error("Node - " + this.node.getPath() + " does not allow mixin type - " + type); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Removes the specified mixin node type from this node. Also removes mixinName from this node's jcr:mixinTypes
     * property. <b>The mixin node type removal takes effect on save</b>.
     * @param type , mixin type to be removed
     * @throws RepositoryException if an error occurs
     */
    public void removeMixin(String type) throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath()), Permission.SET);
        this.node.removeMixin(type);
    }

    /**
     * Returns an array of NodeType objects representing the mixin node types assigned to this node. This includes only
     * those mixin types explicitly assigned to this node, and therefore listed in the property jcr:mixinTypes. It does
     * not include mixin types inherited through the additon of supertypes to the primary type hierarchy.
     * @return an array of mixin NodeType objects.
     * @throws RepositoryException if an error occurs
     */
    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return this.node.getMixinNodeTypes();
    }

    /**
     * places a lock on this object
     * @param isDeep if true this lock will apply to this node and all its descendants; if false, it applies only to
     * this node.
     * @param isSessionScoped if true, this lock expires with the current session; if false it expires when explicitly
     * or automatically unlocked for some other reason.
     * @return A Lock object containing a lock token.
     * @throws LockException if this node is already locked or <code>isDeep</code> is true and a descendant node of
     * this node already holds a lock.
     * @throws RepositoryException if an error occurs
     * @see javax.jcr.Node#lock(boolean, boolean)
     */
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws LockException, RepositoryException {
        return this.node.lock(isDeep, isSessionScoped);
    }

    /**
     * Returns the Lock object that applies to this node. This may be either a lock on this node itself or a deep lock
     * on a node above this node.
     * @throws LockException If no lock applies to this node, a LockException is thrown.
     * @throws RepositoryException if an error occurs
     */
    public Lock getLock() throws LockException, RepositoryException {
        return this.node.getLock();
    }

    /**
     * Removes the lock on this node. Also removes the properties jcr:lockOwner and jcr:lockIsDeep from this node. These
     * changes are persisted automatically; <b>there is no need to call save</b>.
     * @throws LockException if either does not currently hold a lock, or holds a lock for which this Session does not
     * have the correct lock token
     * @throws RepositoryException if an error occurs
     */
    public void unlock() throws LockException, RepositoryException {
        this.node.unlock();
    }

    /**
     * Returns true if this node holds a lock; otherwise returns false. To hold a lock means that this node has actually
     * had a lock placed on it specifically, as opposed to just having a lock apply to it due to a deep lock held by a
     * node above.
     * @throws RepositoryException if an error occurs
     */
    public boolean holdsLock() throws RepositoryException {
        return this.node.holdsLock();
    }

    /**
     * Add a UUID property to the existing node
     * @throws RepositoryException
     */
    private void addUUID() throws RepositoryException {
        this.node.setProperty(PROPERTY_UUID, UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
    }


    /**
     * Implement this interface to be used as node filter by getChildren()
     * */
    public interface ContentFilter {

        /**
         * Test if this content should be included in a resultant collection
         * @param content
         * @return if true this will be a part of collection
         * */
        public boolean accept(Content content);

    }
}
