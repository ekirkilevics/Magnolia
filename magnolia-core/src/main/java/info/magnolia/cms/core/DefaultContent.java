/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.LifeTimeJCRSessionUtil;
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
import javax.jcr.Session;
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
public class DefaultContent extends ContentHandler implements Content {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DefaultContent.class);

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
    protected DefaultContent() {
    }

    /**
     * Constructor to get existing node.
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @param hierarchyManager HierarchyManager instance
     * @throws PathNotFoundException if the node at <code>path</code> does not exist
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     */
    DefaultContent(Node rootNode, String path, HierarchyManager hierarchyManager) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        this.setHierarchyManager(hierarchyManager);
        this.setAccessManager(hierarchyManager.getAccessManager());
        Access.isGranted(hierarchyManager.getAccessManager(), Path.getAbsolutePath(rootNode.getPath(), path), Permission.READ);
        this.setPath(path);
        this.setRootNode(rootNode);
        this.setNode(this.rootNode.getNode(this.path));
    }

    /**
     * Constructor to get existing node.
     * @param elem initialized node object
     * @param hierarchyManager HierarchyManager instance
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     */
    public DefaultContent(Item elem,HierarchyManager hierarchyManager) throws RepositoryException, AccessDeniedException {
        this.setHierarchyManager(hierarchyManager);
        this.setAccessManager(hierarchyManager.getAccessManager());
        Access.isGranted(hierarchyManager.getAccessManager(), Path.getAbsolutePath(elem.getPath()), Permission.READ);
        this.setNode((Node) elem);
        this.setPath(this.getHandle());
    }

    /**
     * creates contentNode of type <b>contentType </b> contentType must be defined in item type definition of magnolia
     * as well as JCR implementation
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @param contentType JCR node type as configured
     * @param hierarchyManager HierarchyManager instance
     * @throws PathNotFoundException if the node at <code>path</code> does not exist
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    DefaultContent(Node rootNode, String path, String contentType, HierarchyManager hierarchyManager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        this.setHierarchyManager(hierarchyManager);
        this.setAccessManager(hierarchyManager.getAccessManager());
        Access.isGranted(hierarchyManager.getAccessManager(), Path.getAbsolutePath(rootNode.getPath(), path), Permission.WRITE);
        this.setPath(path);
        this.setRootNode(rootNode);
        this.node = this.rootNode.addNode(this.path, contentType);
        // add mix:lockable as default for all nodes created using this manager
        // for version 3.5 we cannot change node type definitions because of compatibility reasons
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

    public Content getContentNode(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return new DefaultContent(this.node, path, this.hierarchyManager);
    }

    public Content createContentNode(String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return this.createContent(name, ItemType.CONTENTNODE);
    }

    public Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new DefaultContent(this.node, name, this.hierarchyManager));
    }

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

    public Content createContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.createContent(name, ItemType.CONTENT);
    }

    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Content content = (new DefaultContent(this.node, name, contentType, this.hierarchyManager));
        MetaData metaData = content.getMetaData();
        metaData.setCreationDate();
        return content;
    }

    public Content createContent(String name, ItemType contentType) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Content content = (new DefaultContent(this.node, name, contentType.getSystemName(), this.hierarchyManager));
        MetaData metaData = content.getMetaData();
        metaData.setCreationDate();
        return content;
    }

    public String getTemplate() {
        return this.getMetaData().getTemplate();
    }

    public String getTitle() {
        return I18nContentSupportFactory.getI18nSupport().getNodeData(this, "title").getString();
    }

    public MetaData getMetaData() {
        if (this.metaData == null) {
            this.metaData = new MetaData(this.node, this.hierarchyManager.getAccessManager());
        }
        return this.metaData;
    }

    public NodeData getNodeData(String name) {
        NodeData nodeData = null;
        try {
            nodeData = new DefaultNodeData(this.node, name, this.hierarchyManager, this);
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
            nodeData = new DefaultNodeData();
        }

        return (nodeData != null) ? nodeData : new DefaultNodeData();
    }

    public NodeData getNodeData(String name, boolean create) {
        try {
            return (new DefaultNodeData(this.node, name, this.hierarchyManager, this));
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

            return (new DefaultNodeData());
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
            return (new DefaultNodeData());
        }
    }

    public String getName() {
        try {
            return this.node.getName();
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    public NodeData createNodeData(String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new DefaultNodeData(this.node, name, PropertyType.STRING, true, this.hierarchyManager, this));
    }

    public NodeData createNodeData(String name, int type) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new DefaultNodeData(this.node, name, type, true, this.hierarchyManager, this));
    }

    public NodeData createNodeData(String name, Value value) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new DefaultNodeData(this.node, name, value, this.hierarchyManager, this));
    }

    public NodeData createNodeData(String name, Value[] value) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new DefaultNodeData(this.node, name, value, this.hierarchyManager, this));
    }

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

    public NodeData setNodeData(String name, Value value) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        NodeData nodeData;
        try {
            nodeData = new DefaultNodeData(this.node, name, this.hierarchyManager, this);
            nodeData.setValue(value);
        }
        catch (PathNotFoundException e) {
            nodeData = new DefaultNodeData(this.node, name, value, this.hierarchyManager, this);
        }
        return nodeData;
    }

    public NodeData setNodeData(String name, Value[] value) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        NodeData nodeData;
        try {
            nodeData = new DefaultNodeData(this.node, name, this.hierarchyManager, this);
            nodeData.setValue(value);
        }
        catch (PathNotFoundException e) {
            nodeData = new DefaultNodeData(this.node, name, value, this.hierarchyManager, this);
        }
        return nodeData;
    }

    public void deleteNodeData(String name) throws PathNotFoundException, RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(this.node.getPath()), Permission.REMOVE);
        if (this.node.hasNode(name)) {
            this.node.getNode(name).remove();
        }
        else {
            this.node.getProperty(name).remove();
        }
    }

    public void updateMetaData() throws RepositoryException, AccessDeniedException {
        MetaData md = this.getMetaData();
        md.setModificationDate();
        md.setAuthorId(MgnlContext.getUser().getName());
    }

    public void updateMetaData(HttpServletRequest request) throws RepositoryException, AccessDeniedException {
        MetaData md = this.getMetaData();
        md.setModificationDate();
        md.setAuthorId(Authenticator.getUserId(request));
    }

    public Collection getChildren(ContentFilter filter) {
        return getChildren(filter, null);
    }

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
                    Content content = new DefaultContent(subNode, this.hierarchyManager);
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

    public Collection getChildren(String contentType, int sortCriteria) {
        return this.getChildren(contentType);
    }

    public Collection getChildren(ItemType contentType, int sortCriteria) {
        return this.getChildren(contentType);
    }

    public Collection getChildren(String contentType) {
        return this.getChildren(contentType, "*"); //$NON-NLS-1$
    }

    public Collection getChildren(ItemType contentType) {
        return this.getChildren(contentType != null ? contentType.getSystemName() : (String) null);
    }

    public Collection getChildren(String contentType, String namePattern) {
        Collection children = null;
        try {
            children = this.getChildContent(contentType, namePattern);
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            children = new ArrayList();
        }
        return children;
    }

    public Content getChildByName(String namePattern) {
        Collection children = null;
        try {
            children = getChildContent(null, namePattern);
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        if (!children.isEmpty()) {
            return (Content) children.iterator().next();
        }
        return null;
    }

    /**
     * @param contentType JCR node type as configured, <code>null</code> means no filter
     * @param namePattern, <code>null</code> means no filter
     * @return Collection of <code>Content</code> objects or empty collection when no children are found.
     * @throws RepositoryException if an error occurs
     */
    private Collection getChildContent(String contentType, String namePattern) throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = namePattern != null ? this.node.getNodes(namePattern) : this.node.getNodes();
        while (nodeIterator.hasNext()) {
            Node subNode = (Node) nodeIterator.next();
            try {
                if (contentType == null || this.isNodeType(subNode, contentType)) {
                    children.add(new DefaultContent(subNode, this.hierarchyManager));
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

    public Collection getNodeDataCollection() {
        Collection children = new ArrayList();
        try {
            PropertyIterator propertyIterator = this.node.getProperties();
            while (propertyIterator.hasNext()) {
                Property property = (Property) propertyIterator.next();
                try {
                    if (!property.getName().startsWith("jcr:") && !property.getName().startsWith("mgnl:")) { //$NON-NLS-1$ //$NON-NLS-2$
                        children.add(new DefaultNodeData(property, this.hierarchyManager, this));
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
                    children.add(new DefaultNodeData(property, this.hierarchyManager, this));
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
                    children.add(new DefaultNodeData(subNode, this.hierarchyManager, this));
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

    public boolean hasChildren() {
        return (this.getChildren().size() > 0);
    }

    public boolean hasChildren(String contentType) {
        return (this.getChildren(contentType).size() > 0);
    }

    public boolean hasContent(String name) throws RepositoryException {
        return this.node.hasNode(name);
    }

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

    public String getHandle() {
        try {
            return this.node.getPath();
        }
        catch (RepositoryException e) {
            log.error("Failed to get handle: " + e.getMessage(), e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        }
    }

    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new DefaultContent(this.node.getParent(), this.hierarchyManager));
    }

    public Content getAncestor(int digree) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (digree > this.getLevel()) {
            throw new PathNotFoundException();
        }
        return (new DefaultContent(this.node.getAncestor(digree), this.hierarchyManager));
    }

    public Collection getAncestors() throws PathNotFoundException, RepositoryException {
        List allAncestors = new ArrayList();
        int level = this.getLevel();
        while (level != 0) {
            try {
                allAncestors.add(getAncestor(--level));
            }
            catch (AccessDeniedException e) {
                // valid
            }
        }
        return allAncestors;
    }

    public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.node.getDepth(); //$NON-NLS-1$
    }

    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        this.node.orderBefore(srcName, beforeName);
    }

    public int getIndex() throws RepositoryException {
        return this.node.getIndex();
    }

    public Node getJCRNode() {
        return this.node;
    }

    public boolean isNodeType(String type) {
        try {
            return this.node.isNodeType(type);
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

    public NodeType getNodeType() throws RepositoryException {
        return this.node.getPrimaryNodeType();
    }

    public String getNodeTypeName() throws RepositoryException {

        if (this.node.hasProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE)) {
            return this.node.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE).getString();
        }
        return this.node.getProperty(ItemType.JCR_PRIMARY_TYPE).getString();
    }

    public ItemType getItemType() throws RepositoryException {
        return new ItemType(getNodeTypeName());
    }

    public void restore(String versionName, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), this.getHandle(), Permission.WRITE);
        Version version = this.getVersionHistory().getVersion(versionName);
        this.restore(version, removeExisting);
    }

    public void restore(Version version, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), this.getHandle(), Permission.WRITE);
        VersionManager.getInstance().restore(this, version, removeExisting);
    }

    public void restore(Version version, String relPath, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Not implemented in 3.0 Beta");
    }

    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restoreByLabel(versionLabel, removeExisting);
        throw new UnsupportedRepositoryOperationException("Not implemented in 3.0 Beta");
    }

    public Version addVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().addVersion(this);
    }

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

    public boolean isModified() {
        return this.node.isModified();
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().getVersionHistory(this);
    }

    public VersionIterator getAllVersions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().getAllVersions(this);
    }

    public ContentVersion getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new ContentVersion(VersionManager.getInstance().getBaseVersion(this), this);
    }

    public ContentVersion getVersionedContent(Version version) throws RepositoryException {
        return new ContentVersion(version, this);
    }

    public ContentVersion getVersionedContent(String versionName) throws RepositoryException {
        return new ContentVersion(VersionManager.getInstance().getVersion(this, versionName), this);
    }

    public void removeVersionHistory() throws AccessDeniedException, RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(node.getPath()), Permission.WRITE);
        VersionManager.getInstance().removeVersionHistory(this.node.getUUID());
    }

    public void save() throws RepositoryException {
        this.node.save();
    }

    public boolean isGranted(long permissions) {
        try {
            return hierarchyManager.getAccessManager().isGranted(Path.getAbsolutePath(node.getPath()), permissions);
        }
        catch (RepositoryException re) {
            log.error("Could not get node path: " + re, re);
            return false;
        }
    }

    public void delete() throws RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(this.node.getPath()), Permission.REMOVE);
        this.node.remove();
    }

    public void delete(String path) throws RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(this.node.getPath(), path), Permission.REMOVE);
        if (this.isNodeData(path)) {
            this.getNodeData(path).delete();
        }
        else {
            this.node.getNode(path).remove();
        }
    }

    public boolean isNodeData(String path) throws AccessDeniedException, RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(this.node.getPath(), path), Permission.READ);
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

    public void refresh(boolean keepChanges) throws RepositoryException {
        this.node.refresh(keepChanges);
    }

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

    public void addMixin(String type) throws RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(this.node.getPath()), Permission.SET);
        if (this.node.canAddMixin(type)) {
            this.node.addMixin(type);
        }
        else {
            log.error("Node - " + this.node.getPath() + " does not allow mixin type - " + type); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void removeMixin(String type) throws RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(this.node.getPath()), Permission.SET);
        this.node.removeMixin(type);
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return this.node.getMixinNodeTypes();
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped) throws LockException, RepositoryException {
        return this.node.lock(isDeep, isSessionScoped);
    }

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

    public Lock getLock() throws LockException, RepositoryException {
        return this.node.getLock();
    }

    public void unlock() throws LockException, RepositoryException {
        this.node.unlock();
    }

    public boolean holdsLock() throws RepositoryException {
        return this.node.holdsLock();
    }

    public boolean isLocked() throws RepositoryException {
        return this.node.isLocked();
    }

    public Workspace getWorkspace() throws RepositoryException {
        return this.node.getSession().getWorkspace();
    }

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
        if (this.node !=  null) {
            buffer.append(getHandle());
        }
        buffer.append("]");

        return buffer.toString();
    }

}
