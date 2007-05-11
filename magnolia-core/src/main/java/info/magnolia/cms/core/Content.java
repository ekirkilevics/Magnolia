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

import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.i18n.I18NSupportFactory;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision:2719 $ ($Author:scharles $)
 */
public class Content extends ContentHandler implements Cloneable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Content.class);

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
        this.setAccessManager(manager);
        this.addMixin(ItemType.MIX_VERSIONABLE);
        // add mix:lockable as defualt for all nodes created using this manager
        // for version 3.1 we cannot change node type definitions because of compatibility reasons
        // MAGNOLIA-1518
        this.addMixin(ItemType.MIX_LOCKABLE);
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
        return new Content(this.node, path, this.accessManager);
    }

    /**
     * create ContentNode node under the current node with the specified name
     * @param name of the node to be created as <code>ContentNode</code>
     * @return newly created <node>ContentNode </node>
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @deprecated use createContent(String name, String contentType) instead
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
     * Like getContent but creates the node if not existing.
     * @param name
     * @param create true if the node is created
     * @param contentType the type of the created node
     * @return
     * @throws AccessDeniedException
     * @throws RepositoryException
     * @deprecated use the ContentUtil instead
     */
    public Content getContent(String name, boolean create, ItemType contentType) throws AccessDeniedException,
        RepositoryException {
        Content node;
        try {
            node = this.getContent(name);
        }
        catch (PathNotFoundException e) {
            if (create) {
                node = this.createContent(name, contentType);
            }
            else {
                throw e;
            }
        }
        return node;
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
        return I18NSupportFactory.getI18nSupport().getNodeData(this, "title").getString();
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
     * get top level NodeData
     * @return NodeData requested <code>NodeData</code> object
     */

    public NodeData getNodeData(String name) {
        NodeData nodeData = null;
        try {
            nodeData = new NodeData(this.node, name, this.accessManager);
        }
        catch (PathNotFoundException e) {
            if (log.isDebugEnabled()) {
                String nodepath = null;
                try {
                    nodepath = this.node.getPath();
                }
                catch (RepositoryException e1) {
                    // ignore, debug only
                }
                if (log.isDebugEnabled()) {
                    log.debug("Path not found for property [{}] in node {}", name, nodepath); //$NON-NLS-1$
                }
            }
        }
        catch (AccessDeniedException e) {
            if (log.isDebugEnabled()) {
                String nodepath = null;
                try {
                    nodepath = this.node.getPath();
                }
                catch (RepositoryException e1) {
                    // ignore, debug only
                }
                log.debug("Access denied while trying to read property [{}] in node {}", name, nodepath); //$NON-NLS-1$
            }
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
            nodeData = new NodeData();
        }

        return (nodeData != null) ? nodeData : new NodeData();
    }

    /**
     * @param name
     * @param create
     * @deprecated use NodeDataUtil.getOrCreate(name)
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
        return (new NodeData(this.node, name, PropertyType.STRING, true, this.accessManager));
    }

    /**
     * create top level NodeData object
     * @param name to be created
     * @param type propertyType
     * @return NodeData requested <code>NodeData</code> object
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public NodeData createNodeData(String name, int type) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new NodeData(this.node, name, type, true, this.accessManager));
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
     * Creates a property and set its value immediately, according to the type of the
     * passed instance, hiding the complexity of using JCR's ValueFactory and providing
     * a sensible default behaviour.
     */
    public NodeData createNodeData(String name, Object obj) throws RepositoryException {
        final ValueFactory valueFactory = node.getSession().getValueFactory();
        final Value value;
        if (obj instanceof String) {
            value = valueFactory.createValue((String) obj);
        } else if (obj instanceof Boolean) {
            value = valueFactory.createValue(((Boolean) obj).booleanValue());
        } else if (obj instanceof Long) {
            value = valueFactory.createValue(((Long) obj).longValue());
        } else if (obj instanceof Integer) {
            value = valueFactory.createValue(((Integer) obj).longValue());
        } else if (obj instanceof Double) {
            value = valueFactory.createValue(((Double) obj).doubleValue());
        } else if (obj instanceof Calendar) {
            value = valueFactory.createValue((Calendar) obj);
        } else if (obj instanceof InputStream) {
            value = valueFactory.createValue((InputStream) obj);
        } else if (obj instanceof Content) {
            value = valueFactory.createValue(((Content) obj).getJCRNode());
        } else {
            value = valueFactory.createValue(obj.toString());
        }

        return createNodeData(name, value);
    }

    /**
     * Set NodeData value.
     * @param name to be created
     * @param value to be set initially
     * @return NodeData requested <code>NodeData</code> object
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    public NodeData setNodeData(String name, Value value) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        NodeData nodeData;
        try {
            nodeData = new NodeData(this.node, name, this.accessManager);
            nodeData.setValue(value);
        }
        catch (PathNotFoundException e) {
            nodeData = new NodeData(this.node, name, value, this.accessManager);
        }
        return nodeData;
    }

    /**
     * delete NodeData with the specified name
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     */
    public void deleteNodeData(String name) throws PathNotFoundException, RepositoryException {
        if (this.node.hasNode(name)) {
            this.node.getNode(name).remove();
        }
        else {
            this.node.getProperty(name).remove();
        }
    }

    /**
     * you could call this method anytime to update working page properties - Modification date & Author ID
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     */
    public void updateMetaData() throws RepositoryException, AccessDeniedException {
        MetaData md = this.getMetaData();
        md.setModificationDate();
        md.setAuthorId(MgnlContext.getUser().getName());
    }

    /**
     * you could call this method anytime to update working page properties - Modification date & Author ID
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     * @deprecated use the the method without a request object passed
     */
    public void updateMetaData(HttpServletRequest request) throws RepositoryException, AccessDeniedException {
        MetaData md = this.getMetaData();
        md.setModificationDate();
        md.setAuthorId(Authenticator.getUserId(request));
    }

    /**
     * Get a collection containing child nodes which satisfies the given filter
     * @param filter
     * @return Collection of content objects
     */
    public Collection getChildren(ContentFilter filter) {
        return getChildren(filter, null);
    }

    /**
     * Get a collection containing child nodes which satisfies the given filter. The returned collection is ordered
     * according to the passed in criteria.
     * @param filter filter for the child nodes
     * @param orderCriteria ordering for the selected child nodes; if <tt>null</tt> than no particular order of the
     * child nodes
     * @return Collection of content objects
     */
    public Collection getChildren(ContentFilter filter, Comparator orderCriteria) {
        Collection children;
        if (orderCriteria == null) {
            children = new ArrayList();
        }
        else {
            children = new TreeSet(orderCriteria);
        }

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
                    log.error("Exception caught", e);
                }
                catch (AccessDeniedException e) {
                    // ignore, simply wont add content in a list
                }
            }
        }
        catch (RepositoryException re) {
            log.error("Exception caught", re);
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
            type = this.getNodeTypeName();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re.getMessage(), re);
        }
        // fix all getChildren calls from the root node
        if ("rep:root".equalsIgnoreCase(type)) { //$NON-NLS-1$
            type = ItemType.CONTENT.getSystemName();
        }
        // --------------------------------------------------
        return this.getChildren(type);
    }

    /**
     * @param contentType
     * @param sortCriteria
     * @return Collection of content nodes
     * @deprecated use JCR ordering
     */
    public Collection getChildren(String contentType, int sortCriteria) {
        return this.getChildren(contentType);
    }

    /**
     * @param contentType
     * @param sortCriteria
     * @return Collection of content nodes
     * @deprecated use JCR ordering
     */
    public Collection getChildren(ItemType contentType, int sortCriteria) {
        return this.getChildren(contentType);
    }

    /**
     * Get collection of specified content type
     * @param contentType JCR node type as configured
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType) {
        return this.getChildren(contentType, "*"); //$NON-NLS-1$
    }

    /**
     * Get collection of specified content type
     * @param contentType ItemType
     * @return Collection of content nodes
     */
    public Collection getChildren(ItemType contentType) {
        return this.getChildren(contentType != null ? contentType.getSystemName() : (String) null);
    }

    /**
     * Get collection of specified content type.
     * @param contentType JCR node type as configured
     * @param namePattern
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType, String namePattern) {
        Collection children = new ArrayList();
        try {
            children = this.getChildContent(contentType, namePattern);
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return children;
    }

    /**
     * Returns the first child with the given name, any node type
     * @param namePattern child node name
     * @return first found node with the given name or <code>null</code> if not found
     */
    public Content getChildByName(String namePattern) {
        Collection children = null;
        try {
            children = getChildContent(null, namePattern);
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        if (children != null && !children.isEmpty()) {
            return (Content) children.iterator().next();
        }
        return null;
    }

    /**
     * @param contentType JCR node type as configured, <code>null</code> means no filter
     * @param namePattern, <code>null</code> means no filter
     * @return COllection of <code>Content</code> objects
     * @throws RepositoryException if an error occurs
     */
    private Collection getChildContent(String contentType, String namePattern) throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = namePattern != null ? this.node.getNodes(namePattern) : this.node.getNodes();
        while (nodeIterator.hasNext()) {
            Node subNode = (Node) nodeIterator.next();
            try {
                if (contentType == null || this.isNodeType(subNode, contentType)) {
                    children.add(new Content(subNode, this.accessManager));
                }
            }
            catch (PathNotFoundException e) {
                log.error("Exception caught", e);
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
                    log.error("Exception caught", e);
                }
                catch (AccessDeniedException e) {
                    // ignore, simply wont add content in a list
                }
            }
            // add nt:resource nodes
            children.addAll(this.getBinaryProperties("*"));
        }
        catch (RepositoryException re) {
            log.error("Exception caught", re);
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
                    log.error("Exception caught", e);
                }
                catch (AccessDeniedException e) {
                    // ignore, simply wont add content in a list
                }
            }
            // add nt:resource nodes
            children.addAll(this.getBinaryProperties(namePattern));
        }
        catch (RepositoryException re) {
            log.error("Exception caught", re);
        }
        return children;
    }

    /**
     * @param namePattern
     * @return nodeData collection
     * @throws RepositoryException if an error occurs
     */
    private Collection getBinaryProperties(String namePattern) throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = this.node.getNodes(namePattern);
        while (nodeIterator.hasNext()) {
            Node subNode = (Node) nodeIterator.next();
            try {
                if (this.isNodeType(subNode, ItemType.NT_RESOURCE)) {
                    children.add(new NodeData(subNode, this.accessManager));
                }
            }
            catch (PathNotFoundException e) {
                log.error(e.getMessage(), e);
            }
            catch (AccessDeniedException e) {
                // ignore, simply wont add content in a list
            }
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
        if (this.node.hasProperty(name)) {
            return true;
        }
        else { // check for mgnl:resource node
            if (getNodeData(name).getType() == PropertyType.BINARY) {
                return true;
            }
        }
        return false;
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
     * get parent content object
     * @return Content representing parent node
     * @throws javax.jcr.PathNotFoundException
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
     * get node level from the ROOT node
     * @return level at which current node exist, relative to the ROOT node
     * @throws javax.jcr.PathNotFoundException
     * @throws RepositoryException if an error occurs
     */
    public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.node.getDepth(); //$NON-NLS-1$
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
     * @param type
     */
    public boolean isNodeType(String type) {
        try {
            return this.getNodeTypeName().equalsIgnoreCase(type);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re.getMessage(), re);
        }
        return false;
    }

    /**
     * private Helper method to evaluate primary node type of the given node
     * @param node
     * @param type
     */
    protected boolean isNodeType(Node node, String type) {
        try {
            final String actualType = node.getProperty(ItemType.JCR_PRIMARY_TYPE).getString();
            // if the node is frozen, and we're not looking specifically for frozen nodes, then we compare with the original node type
            if (ItemType.NT_FROZENNODE.equals(actualType) && !(ItemType.NT_FROZENNODE.equals(type))) {
                final Property p = node.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE);
                final String s = p.getString();
                return s.equalsIgnoreCase(type);
            } else {
                return actualType.equalsIgnoreCase(type);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re.getMessage(), re);
            return false;
        }
    }

    /**
     * returns primary node type definition of the associated Node of this object
     * @throws RepositoryException if an error occurs
     */
    public NodeType getNodeType() throws RepositoryException {
        return this.node.getPrimaryNodeType();
    }

    /**
     * returns primary node type name of the associated Node of this object
     * @throws RepositoryException if an error occurs
     */
    public String getNodeTypeName() throws RepositoryException {

        if (this.node.hasProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE)) {
            return this.node.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE).getString();
        }
        return this.node.getProperty(ItemType.JCR_PRIMARY_TYPE).getString();
    }

    /**
     * Get the magnolia ItemType.
     * @return the type
     * @throws RepositoryException
     */
    public ItemType getItemType() throws RepositoryException {
        return new ItemType(getNodeTypeName());
    }

    /**
     * Restores this node to the state defined by the version with the specified versionName.
     * @param versionName
     * @param removeExisting
     * @throws VersionException if the specified <code>versionName</code> does not exist in this node's version
     * history
     * @throws RepositoryException if an error occurs
     */
    public void restore(String versionName, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        Access.isGranted(this.getAccessManager(), this.getHandle(), Permission.WRITE);
        Version version = this.getVersionHistory().getVersion(versionName);
        this.restore(version, removeExisting);
    }

    /**
     * Restores this node to the state defined by the specified version.
     * @param version
     * @param removeExisting
     * @throws VersionException if the specified <code>version</code> is not part of this node's version history
     * @throws RepositoryException if an error occurs
     */
    public void restore(Version version, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        Access.isGranted(this.getAccessManager(), this.getHandle(), Permission.WRITE);
        VersionManager.getInstance().restore(this, version, removeExisting);
    }

    /**
     * Restores the specified version to relPath, relative to this node.
     * @param version
     * @param relPath
     * @param removeExisting
     * @throws VersionException if the specified <code>version</code> is not part of this node's version history
     * @throws RepositoryException if an error occurs
     */
    public void restore(Version version, String relPath, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Not implemented in 3.0 Beta");
    }

    /**
     * Restores this node to the state recorded in the version specified by versionLabel.
     * @param versionLabel
     * @param removeExisting
     * @throws VersionException if the specified <code>versionLabel</code> does not exist in this node's version
     * history
     * @throws RepositoryException if an error occurs
     */
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restoreByLabel(versionLabel, removeExisting);
        throw new UnsupportedRepositoryOperationException("Not implemented in 3.0 Beta");
    }

    /**
     * add version leaving the node checked out
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException if an error occurs
     */
    public Version addVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().addVersion(this);
    }

    /**
     * add version leaving the node checked out
     * @param rule to be used to collect content
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException if an error occurs
     * @see info.magnolia.cms.util.Rule
     */
    public Version addVersion(Rule rule) throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().addVersion(this, rule);
    }

    /**
     * Returns true if this node is either
     * <ul>
     * <li/>versionable and currently checked-out, <li/>non-versionable and its nearest versionable ancestor is
     * checked-out or <li/>non-versionable and it has no versionable ancestor.
     * </ul>
     * Returns false if this node is either
     * <ul>
     * <li/>versionable and currently checked-in or <li/>non-versionable and its nearest versionable ancestor is
     * checked-in.
     * </ul>
     * @return true if the node is checked out
     * @throws RepositoryException
     */
    protected boolean isCheckedOut() throws RepositoryException {
        return this.node.isCheckedOut();
    }

    /**
     * Returns <code>true</code> if this <code>Item</code> has been saved but has subsequently been modified through
     * the current session and therefore the state of this item as recorded in the session differs from the state of
     * this item as saved. Within a transaction, <code>isModified</code> on an <code>Item</code> may return
     * <code>false</code> (because the <code>Item</code> has been saved since the modification) even if the
     * modification in question is not in persistent storage (because the transaction has not yet been committed). <p/>
     * Note that in level 1 (that is, read-only) implementations, this method will always return <code>false</code>.
     * @return <code>true</code> if this item is modified; <code>false</code> otherwise.
     */
    public boolean isModified() {
        return this.node.isModified();
    }

    /**
     * @return version history
     * @throws RepositoryException if an error occurs
     */
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().getVersionHistory(this);
    }

    /**
     * @return Version iterator retreived from version history
     * @throws RepositoryException if an error occurs
     */
    public VersionIterator getAllVersions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().getAllVersions(this);
    }

    /**
     * get the current base version of this node
     * @return base ContentVersion
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException
     */
    public ContentVersion getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new ContentVersion(VersionManager.getInstance().getBaseVersion(this), this);
    }

    /**
     * get content view over the jcr version object
     * @param version
     * @return version object wrapped in ContentVersion
     * @see ContentVersion
     */
    public ContentVersion getVersionedContent(Version version) throws RepositoryException {
        return new ContentVersion(version, this);
    }

    /**
     * get content view over the jcr version object
     * @param versionName
     * @return version object wrapped in ContentVersion
     * @see ContentVersion
     */
    public ContentVersion getVersionedContent(String versionName) throws RepositoryException {
        return new ContentVersion(VersionManager.getInstance().getVersion(this, versionName), this);
    }

    /**
     * removes all versions of this node and associated version graph
     * @throws AccessDeniedException If not allowed to do write operations on this node
     * @throws RepositoryException if unable to remove versions from version store
     */
    public void removeVersionHistory() throws AccessDeniedException, RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(node.getPath()), Permission.WRITE);
        VersionManager.getInstance().removeVersionHistory(this.node.getUUID());
    }

    /**
     * Persists all changes to the repository if validation succeds
     * @throws RepositoryException if an error occurs
     */
    public void save() throws RepositoryException {
        this.node.save();
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
            if (log.isDebugEnabled()) {
                log.debug(this.getHandle() + " says: no access"); //$NON-NLS-1$
            }
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
            this.getNodeData(path).delete();
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
        boolean result = false;
        try {
            result = this.node.hasProperty(path);
            if (!result) {
                // check if its a nt:resource
                result = this.node.hasProperty(path + "/" + ItemType.JCR_DATA);
            }
        }
        catch (RepositoryException e) {
            if (log.isDebugEnabled()) {
                log.debug("isNodeData(): " + e.getMessage()); //$NON-NLS-1$
            }
        }
        return result;
    }

    /**
     * If keepChanges is false, this method discards all pending changes recorded in this session.
     * @throws RepositoryException if an error occurs
     * @see javax.jcr.Node#refresh(boolean)
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.node.refresh(keepChanges);
    }

    /**
     * UUID of the node refrenced by this object
     * @return uuid
     */
    public String getUUID() {
        try {
            return this.node.getUUID();
        }
        catch (UnsupportedOperationException e) {
            log.error(e.getMessage());
        }
        catch (RepositoryException re) {
            log.error("Exception caught", re);
        }
        return StringUtils.EMPTY;
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
     * places a lock on this object
     * @param isDeep if true this lock will apply to this node and all its descendants; if false, it applies only to
     * this node.
     * @param isSessionScoped if true, this lock expires with the current session; if false it expires when explicitly
     * or automatically unlocked for some other reason.
     * @param yieldFor number of milliseconds for which this method will try to get a lock
     * @return A Lock object containing a lock token.
     * @throws LockException if this node is already locked or <code>isDeep</code> is true and a descendant node of
     * this node already holds a lock.
     * @throws RepositoryException if an error occurs
     * @see javax.jcr.Node#lock(boolean, boolean)
     */
    public Lock lock(boolean isDeep, boolean isSessionScoped, long yieldFor) throws LockException, RepositoryException {
        long finalTime = System.currentTimeMillis() + yieldFor;
        LockException lockException = null;
        while (System.currentTimeMillis() <= finalTime) {
            try {
                return this.node.lock(isDeep, isSessionScoped);
            }
            catch (LockException e) {
                // its not an exception yet, still got time
                lockException = e;
            }
            Thread.yield();
        }
        // could not get lock
        throw lockException;
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
     * @return a boolean
     * @throws RepositoryException if an error occurs
     */
    public boolean holdsLock() throws RepositoryException {
        return this.node.holdsLock();
    }

    /**
     * Returns true if this node is locked either as a result of a lock held by this node or by a deep lock on a node
     * above this node; otherwise returns false.
     * @return a boolean
     * @throws RepositoryException if an error occurs
     */
    public boolean isLocked() throws RepositoryException {
        return this.node.isLocked();
    }

    /**
     * get workspace to which this node attached to
     * @throws RepositoryException if unable to get this node session
     */
    public Workspace getWorkspace() throws RepositoryException {
        return this.node.getSession().getWorkspace();
    }

    /**
     * checks if this node has a sub node with name MetaData
     * @return true if MetaData exists
     */
    public boolean hasMetaData() {
        try {
            return this.node.hasNode("MetaData");
        }
        catch (RepositoryException re) {
            log.debug(re.getMessage(), re);
        }
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append("[");
        if (this.node != null) {
            buffer.append(getHandle());
        }
        buffer.append("]");

        return buffer.toString();
    }

    /**
     * Implement this interface to be used as node filter by getChildren()
     */
    public interface ContentFilter {

        /**
         * Test if this content should be included in a resultant collection
         * @param content
         * @return if true this will be a part of collection
         */
        public boolean accept(Content content);

    }
}
