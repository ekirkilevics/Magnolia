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
package info.magnolia.cms.core;

import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.DelegateNodeWrapper;
import info.magnolia.cms.util.JCRPropertiesFilteringNodeWrapper;
import info.magnolia.cms.util.Rule;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.logging.AuditLoggingUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default, JCR-based, implementation of {@link Content}.
 *
 * @author Sameer Charles
 * @version $Revision:2719 $ ($Author:scharles $)
 * @deprecated since 5.0 use jcr.Node instead.
 */
@Deprecated
public class DefaultContent extends AbstractContent {
    private static final Logger log = LoggerFactory.getLogger(DefaultContent.class);

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
        Access.tryPermission(rootNode.getSession(), Path.getAbsolutePath(rootNode.getPath(), path), Session.ACTION_READ);
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
        // TODO: this check seems bit pointless ... we wouldn't have got the node if we were not allowed to read it
        Access.tryPermission(elem.getSession(), Path.getAbsolutePath(elem.getPath()), Session.ACTION_READ);
        this.setNode((Node) elem);
        this.setPath(this.getHandle());
    }

    /**
     * Creates contentNode of type <b>contentType </b> contentType must be defined in item type definition of Magnolia
     * as well as JCR implementation.
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
        Access.tryPermission(rootNode.getSession(), Path.getAbsolutePath(rootNode.getPath(), path), Session.ACTION_SET_PROPERTY + "," + Session.ACTION_ADD_NODE + "," + Session.ACTION_REMOVE);
        this.setPath(path);
        this.setRootNode(rootNode);
        this.setNode(this.rootNode.addNode(this.path, contentType));
        // add mix:lockable as default for all nodes created using this manager
        // for version 3.5 we cannot change node type definitions because of compatibility reasons
        // MAGNOLIA-1518
        this.addMixin(ItemType.MIX_LOCKABLE);
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_CREATE, hierarchyManager.getName(), this.getItemType(), Path.getAbsolutePath(node.getPath()));
    }

    /**
     * @param node
     */
    protected void setNode(Node node) {
        if (node instanceof DelegateNodeWrapper) {
            // Default content takes care of filtering jcr properties on its own
            this.node = ((DelegateNodeWrapper) node).deepUnwrap(JCRPropertiesFilteringNodeWrapper.class);
        } else {
            this.node = node;
        }
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

    @Override
    public Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new DefaultContent(this.node, name, this.hierarchyManager));
    }

    @Override
    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException,
    AccessDeniedException {
        Content content = new DefaultContent(this.node, name, contentType, this.hierarchyManager);
        MetaData metaData = content.getMetaData();
        metaData.setCreationDate();
        return content;
    }

    @Override
    public boolean hasNodeData(String name) throws RepositoryException {
        if (this.node.hasProperty(name)) {
            return true;
        }
        else { // check for mgnl:resource node
            if (this.node.hasNode(name) && (this.node.getNode(name).isNodeType(ItemType.NT_RESOURCE) || (this.node.hasProperty("jcr:frozenPrimaryType") && this.node.getNode(name).getProperty("jcr:frozenPrimaryType").getValue().getString().equals(ItemType.NT_RESOURCE)))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NodeData newNodeDataInstance(String name, int type, boolean createIfNotExisting) throws AccessDeniedException, RepositoryException {
        try {
            Access.tryPermission(node.getSession(), Path.getAbsolutePath(getHandle(), name), Session.ACTION_READ);
        }
        // FIXME: should be thrown but return a dummy node data
        catch (AccessDeniedException e) {
            return new NonExistingNodeData(this, name);
        }

        // create an empty dummy
        if(!hasNodeData(name) && !createIfNotExisting){
            return new NonExistingNodeData(this, name);
        }

        if(type == PropertyType.UNDEFINED){
            type = determineNodeDataType(name);
        }

        if(type == PropertyType.BINARY){
            return new BinaryNodeData(this, name);
        }
        else{
            return new DefaultNodeData(this, name);
        }
    }

    protected int determineNodeDataType(String name) {
        // FIXME: maybe delegate to NodeDataImplementations?
        try {
            if (this.node.hasProperty(name)) {
                return this.node.getProperty(name).getType();
            }
            else { // check for mgnl:resource node
                if (this.node.hasNode(name) && (this.node.getNode(name).isNodeType(ItemType.NT_RESOURCE) || (this.node.getNode(name).hasProperty("jcr:frozenPrimaryType") && this.node.getNode(name).getProperty("jcr:frozenPrimaryType").getValue().getString().equals(ItemType.NT_RESOURCE)))) {
                    return PropertyType.BINARY;
                }
            }
        }
        catch (RepositoryException e) {
            throw new IllegalStateException("Can't determine property type of [" + getHandle() + "/" + name + "]", e);
        }
        return PropertyType.UNDEFINED;
    }


    @Override
    public MetaData getMetaData() {
        if (this.metaData == null) {
            try {
                this.metaData = new MetaData(this.node, this.node.getSession());
            } catch (RepositoryException e) {
                throw new UnhandledException(e);
            }
        }
        return this.metaData;
    }

    @Override
    public String getName() {
        try {
            return this.node.getName();
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    @Override
    public Collection<Content> getChildren(ContentFilter filter, String namePattern, Comparator<Content> orderCriteria) {
        List<Content> children;
        children = new ArrayList<Content>();

        try {
            final NodeIterator nodeIterator;
            if (namePattern == null) {
                nodeIterator = this.node.getNodes();
            } else {
                nodeIterator = this.node.getNodes(namePattern);
            }

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

        if (orderCriteria != null) {
            // stable sort - do not reorder or remove equal items
            Collections.sort(children, orderCriteria);
        }
        return children;
    }

    @Override
    public Collection<NodeData> getNodeDataCollection(String namePattern) {
        final ArrayList<NodeData> all = new ArrayList<NodeData>();
        try {
            all.addAll(getPrimitiveNodeDatas(namePattern));
            all.addAll(getBinaryNodeDatas(namePattern));
        }
        catch (RepositoryException e) {
            throw new IllegalStateException("Can't read node datas of " + toString(), e);
        }
        return all;
    }

    protected Collection<NodeData> getPrimitiveNodeDatas(String namePattern) throws RepositoryException {
        final Collection<NodeData> nodeDatas = new ArrayList<NodeData>();
        final PropertyIterator propertyIterator;
        if (namePattern == null) {
            propertyIterator = this.node.getProperties();
        } else {
            propertyIterator = this.node.getProperties(namePattern);
        }
        while (propertyIterator.hasNext()) {
            Property property = (Property) propertyIterator.next();
            try {
                if (!property.getName().startsWith("jcr:") && !property.getName().startsWith("mgnl:")) { //$NON-NLS-1$ //$NON-NLS-2$
                    nodeDatas.add(getNodeData(property.getName()));
                }
            }
            catch (PathNotFoundException e) {
                log.error("Exception caught", e);
            }
            catch (AccessDeniedException e) {
                // ignore, simply wont add content in a list
            }
        }
        return nodeDatas;
    }


    @Override
    public boolean hasContent(String name) throws RepositoryException {
        return this.node.hasNode(name);
    }

    @Override
    public String getHandle() {
        try {
            return this.node.getPath();
        }
        catch (RepositoryException e) {
            log.error("Failed to get handle: " + e.getMessage(), e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        }
    }

    @Override
    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new DefaultContent(this.node.getParent(), this.hierarchyManager));
    }

    @Override
    public Content getAncestor(int level) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (level > this.getLevel()) {
            throw new PathNotFoundException();
        }
        return (new DefaultContent(this.node.getAncestor(level), this.hierarchyManager));
    }

    @Override
    public Collection<Content> getAncestors() throws PathNotFoundException, RepositoryException {
        List<Content> allAncestors = new ArrayList<Content>();
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

    @Override
    public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.node.getDepth();
    }

    @Override
    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        this.node.orderBefore(srcName, beforeName);
    }

    @Override
    public int getIndex() throws RepositoryException {
        return this.node.getIndex();
    }

    @Override
    public Node getJCRNode() {
        return this.node;
    }

    @Override
    public boolean isNodeType(String type) {
        return isNodeType(this.node, type);
    }

    /**
     * private Helper method to evaluate primary node type of the given node.
     * @param node
     * @param type
     */
    protected boolean isNodeType(Node node, String type) {
        try {
            return NodeUtil.isNodeType(node, type);
        } catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re.getMessage(), re);
            return false;
        }
    }

    @Override
    public NodeType getNodeType() throws RepositoryException {
        return this.node.getPrimaryNodeType();
    }

    @Override
    public String getNodeTypeName() throws RepositoryException {

        if (this.node.hasProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE)) {
            return this.node.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE).getString();
        }
        return this.node.getProperty(ItemType.JCR_PRIMARY_TYPE).getString();
    }

    @Override
    public ItemType getItemType() throws RepositoryException {
        return new ItemType(getNodeTypeName());
    }

    @Override
    public void restore(String versionName, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        Access.tryPermission(this.node.getSession(), this.getHandle(), Session.ACTION_ADD_NODE + "," + Session.ACTION_REMOVE + "," + Session.ACTION_SET_PROPERTY);
        Version version = this.getVersionHistory().getVersion(versionName);
        this.restore(version, removeExisting);
    }

    @Override
    public void restore(Version version, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        Access.tryPermission(this.node.getSession(), this.getHandle(), Session.ACTION_ADD_NODE + "," + Session.ACTION_REMOVE + "," + Session.ACTION_SET_PROPERTY);
        VersionManager.getInstance().restore(this, version, removeExisting);
    }

    @Override
    public void restore(Version version, String relPath, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Not implemented since 3.0 Beta");
    }

    @Override
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        // FIXME: !!! why does it do something and then throws exception anyway?
        this.node.restoreByLabel(versionLabel, removeExisting);
        throw new UnsupportedRepositoryOperationException("Not implemented since 3.0 Beta");
    }

    @Override
    public Version addVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().addVersion(this.getJCRNode());
    }

    @Override
    public Version addVersion(Rule rule) throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().addVersion(this.getJCRNode(), rule);
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

    @Override
    public boolean isModified() {
        return this.node.isModified();
    }

    @Override
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().getVersionHistory(this.getJCRNode());
    }

    @Override
    public VersionIterator getAllVersions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().getAllVersions(this.getJCRNode());
    }

    @Override
    public ContentVersion getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new ContentVersion(VersionManager.getInstance().getBaseVersion(this.getJCRNode()), this);
    }

    @Override
    public ContentVersion getVersionedContent(Version version) throws RepositoryException {
        return new ContentVersion(version, this);
    }

    @Override
    public ContentVersion getVersionedContent(String versionName) throws RepositoryException {
        return new ContentVersion(VersionManager.getInstance().getVersion(this.getJCRNode(), versionName), this);
    }

    @Override
    public void removeVersionHistory() throws AccessDeniedException, RepositoryException {
        Access.tryPermission(this.node.getSession(), Path.getAbsolutePath(node.getPath()), Session.ACTION_ADD_NODE + "," + Session.ACTION_REMOVE + "," + Session.ACTION_SET_PROPERTY);
        VersionManager.getInstance().removeVersionHistory(this.node.getUUID());
    }

    @Override
    public void save() throws RepositoryException {
        this.node.save();
    }

    @Override
    public void delete() throws RepositoryException {
        Access.tryPermission(this.node.getSession(), Path.getAbsolutePath(node.getPath()), Session.ACTION_REMOVE);
        String nodePath = Path.getAbsolutePath(this.node.getPath());
        ItemType nodeType = this.getItemType();
        this.node.remove();
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_DELETE, hierarchyManager.getName(), nodeType, nodePath);
    }


    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.node.refresh(keepChanges);
    }

    @Override
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

    @Override
    public void addMixin(String type) throws RepositoryException {
        // TODO: was Permission.SET, is this OK?
        Access.tryPermission(node.getSession(), Path.getAbsolutePath(node.getPath()), Session.ACTION_SET_PROPERTY);
        // TODO: there seems to be bug somewhere as we are able to add mixins even when the method below returns false
        if (!this.node.canAddMixin(type)) {
            log.debug("Node - " + this.node.getPath() + " does not allow mixin type - " + type); //$NON-NLS-1$ //$NON-NLS-2$
        }
        try {
            this.node.addMixin(type);
        } catch (Exception e) {
            log.error("Failed to add  mixin type - " + type + " to a node " + this.node.getPath()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public void removeMixin(String type) throws RepositoryException {
        // TODO: was Permission.SET, is this OK?
        Access.tryPermission(node.getSession(), Path.getAbsolutePath(node.getPath()), Session.ACTION_SET_PROPERTY);
        this.node.removeMixin(type);
    }

    @Override
    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return this.node.getMixinNodeTypes();
    }

    @Override
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws LockException, RepositoryException {
        return this.node.lock(isDeep, isSessionScoped);
    }

    @Override
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

    @Override
    public Lock getLock() throws LockException, RepositoryException {
        return this.node.getLock();
    }

    @Override
    public void unlock() throws LockException, RepositoryException {
        this.node.unlock();
    }

    @Override
    public boolean holdsLock() throws RepositoryException {
        return this.node.holdsLock();
    }

    @Override
    public boolean isLocked() throws RepositoryException {
        return this.node.isLocked();
    }

    @Override
    public boolean hasMetaData() {
        try {
            return this.node.hasNode("MetaData");
        }
        catch (RepositoryException re) {
            log.debug(re.getMessage(), re);
        }
        return false;
    }

    @Override
    public boolean hasMixin(String mixinName) throws RepositoryException {
        if (StringUtils.isBlank(mixinName)) {
            throw new IllegalArgumentException("Mixin name can't be empty.");
        }
        for (NodeType type : getMixinNodeTypes()) {
            if (mixinName.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }
}
