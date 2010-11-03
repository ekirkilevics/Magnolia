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
package info.magnolia.cms.core.version;

import info.magnolia.cms.core.AbstractContent;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.ContentWrapper;
import info.magnolia.cms.util.HierarchyManagerWrapper;
import info.magnolia.cms.util.NodeDataWrapper;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.SimpleUrlPattern;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wraps a versioned node (frozen node) and allows traversing the hierarchy as if the node where in the original place.
 * @author Sameer Charles
 * $Id$
 */
public class ContentVersion extends DefaultContent {

    /**
     * Makes sure that the handle hides the fact that the nodes live in the version store.
     * @version $Id$
     *
     */
    private final class ContentVersionChildWrapper extends ContentWrapper {
        private final Content parent;

        private ContentVersionChildWrapper(Content wrappedContent, Content parent) {
            super(wrappedContent);
            this.parent = parent;
        }

        @Override
        public Content getParent() throws RepositoryException {
            return parent;
        }

        /**
         * Show the original path not the one from the version store.
         */
        @Override
        public String getHandle() {
            try {
                return getParent().getHandle() + "/" + getName();
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't create handle for versioned node.", e);
            }
        }

        /**
         * We have to wrap the node data to make sure that the handle is correct.
         */
        @Override
        public NodeData newNodeDataInstance(String name, int type, boolean createIfNotExisting) throws AccessDeniedException, RepositoryException {
            return new NodeDataWrapper(super.newNodeDataInstance(name, type, createIfNotExisting)) {
                @Override
                public String getHandle() {
                    return ContentVersionChildWrapper.this.getHandle() + "/" + getName();
                }
            };
        }

        @Override
        protected Content wrap(Content node) {
            return new ContentVersionChildWrapper(node, this);
        }
    }

    private static Logger log = LoggerFactory.getLogger(ContentVersion.class);

    /**
     * User who created this version.
     */
    public static final String VERSION_USER = "versionUser"; //$NON-NLS-1$

    /**
     * Name of the base node.
     */
    public static final String NAME = "name";

    /**
     * Version node (nt:version).
     */
    private Version state;

    /**
     * The node as existing in the workspace. Not the version node.
     */
    private AbstractContent base;

    /**
     * Rule used to create this version.
     */
    private Rule rule;

    public ContentVersion(Version thisVersion, AbstractContent base) throws RepositoryException {
        if (thisVersion == null) {
            throw new RepositoryException("Failed to get ContentVersion, version does not exist");
        }
        this.state = thisVersion;
        this.base = base;

        this.hierarchyManager = new HierarchyManagerWrapper(base.getHierarchyManager()) {
            private AccessManagerImpl accessManager;

            {
                // child nodes (and metaData if nothing else) depends on this to have access when root access is restricted for given user
                List<Permission> permissions = new ArrayList<Permission>(getWrappedHierarchyManager().getAccessManager().getPermissionList());
                PermissionImpl p = new PermissionImpl();
                p.setPattern(new SimpleUrlPattern("/jcr:system/jcr:versionStorage/*"));
                // read only
                p.setPermissions(8);
                permissions.add(p);
                // use dedicated AM and not the one base share with its parent
                accessManager = new AccessManagerImpl();
                accessManager.setPermissionList(permissions);
            }

            @Override
            public AccessManager getAccessManager() {
                return accessManager;
            }
        };
        this.init();
    }

    /**
     * Set frozen node of this version as working node.
     * @throws RepositoryException
     */
    private void init() throws RepositoryException {
        this.setNode(this.state.getNode(ItemType.JCR_FROZENNODE));
        try {
            if (!StringUtils.equalsIgnoreCase(this.state.getName(), VersionManager.ROOT_VERSION)) {
                this.rule = VersionManager.getInstance().getUsedFilter(this);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (this.rule == null) {
            log.info("failed to get filter used for creating this version, use open filter");
            this.rule = new Rule();
        }
    }

    /**
     * Get creation date of this version.
     * @throws RepositoryException
     * @return creation date as calendar
     */
    public Calendar getCreated() throws RepositoryException {
        return this.state.getCreated();
    }

    /**
     * Return the name of the version represented by this object.
     * @return the versions name
     * @throws RepositoryException
     */
    public String getVersionLabel() throws RepositoryException {
        return this.state.getName();
    }

    /**
     * Get containing version history.
     * @throws RepositoryException
     * @return version history associated to this version
     */
    public VersionHistory getContainingHistory() throws RepositoryException {
        return this.state.getContainingHistory();
    }

    /**
     * The original name of the node.
     */
    public String getName() {
        try {
            return VersionManager.getInstance().getSystemNode(this).getNodeData(NAME).getString();
        }
        catch (RepositoryException re) {
            log.error("Failed to retrieve name from version system node", re);
            return "";
        }
    }

    /**
     * The name of the user who created this version.
     */
    public String getUserName() {
        try {
            return VersionManager.getInstance().getSystemNode(this).getNodeData(VERSION_USER).getString();
        }
        catch (RepositoryException re) {
            log.error("Failed to retrieve user from version system node", re);
            return "";
        }
    }

    /**
     * Get original path of this versioned content.
     */
    public String getHandle() {
        return this.base.getHandle();
    }

    /**
     * Returns a direct child if it was included in the version. Otherwise it tries to get the child from the original place.
     * The versioning rule is respected.
     */
    public Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        //first we have to check if this is a direct child
        if(super.hasContent(name)){
            return new ContentVersionChildWrapper(super.getContent(name), this);
        }
        else{
            Content content = base.getContent(name);
            // only return the node if it was excluded from the versioning, otherwise the node is new
            if(!rule.isAllowed(content.getNodeTypeName())){
                return content;
            }
            else{
                throw new PathNotFoundException(base.getHandle() + "/" + name);
            }
        }
    }

    /**
     * Uses the same approach as {@link #getContent(String)}.
     */
    @Override
    public boolean hasContent(String name) throws RepositoryException {
        if(super.hasContent(name)){
            return true;
        }
        else if(base.hasContent(name)){
            Content content = base.getContent(name);
            // only return the node if it was excluded from the versioning, otherwise the node is new
            if(!rule.isAllowed(content.getNodeTypeName())){
                return true;
            }
        }
        return false;
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public Content createContent(String name) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public Content createContent(String name, String contentType) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public Content createContent(String name, ItemType contentType) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public NodeData createNodeData(String name) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public NodeData createNodeData(String name, Value value, int type) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public NodeData createNodeData(String name, Value value) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     *  Throws an {@link AccessDeniedException} as versions are read only.
     */
    public NodeData createNodeData(String name, int type) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void deleteNodeData(String name) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void updateMetaData() throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * All {@link #getChildren()} methods delegate to this method. We combine the direct children and children
     * from the current node which were not included by the version rule.
     */
    @Override
    public Collection<Content> getChildren(ContentFilter filter, String namePattern, Comparator<Content> orderCriteria) {
        ArrayList<Content> result = new ArrayList<Content>();
        result.addAll(wrap(super.getChildren(filter, namePattern, orderCriteria)));

        Collection<Content> transientChildren = this.base.getChildren(filter, namePattern, orderCriteria);
        for (Content transientChild : transientChildren) {
            try {
                if(!rule.isAllowed(transientChild.getNodeTypeName())){
                    result.add(transientChild);
                }
            }
            catch (RepositoryException e) {
                throw new RuntimeException("Can't determine node type of " + transientChild, e);
            }
        }

        return result;
    }

    private Collection<Content> wrap(Collection<Content> children) {
        List<Content> transformed = new ArrayList<Content>();
        for (Content child : children) {
            transformed.add(new ContentVersionChildWrapper(child, this));
        }
        return transformed;
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
     * Returns the parent of the base node.
     */
    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.base.getParent();
    }

    public Content getAncestor(int level) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.base.getAncestor(level);
    }

    /**
     * Convenience method for taglib.
     * @return Content representing node on level 0
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public Collection<Content> getAncestors() throws PathNotFoundException, RepositoryException {
        return this.base.getAncestors();
    }

    /**
     * Get node level from the ROOT node : FIXME implement getDepth in javax.jcr.
     * @return level at which current node exist, relative to the ROOT node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.base.getLevel();
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * This method returns the index of this node within the ordered set of its same-name sibling nodes. This index is
     * the one used to address same-name siblings using the square-bracket notation, e.g., /a[3]/b[4]. Note that the
     * index always starts at 1 (not 0), for compatibility with XPath. As a result, for nodes that do not have
     * same-name-siblings, this method will always return 1.
     * @return The index of this node within the ordered set of its same-name sibling nodes.
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public int getIndex() throws RepositoryException {
        return this.base.getIndex();
    }

    /**
     * Returns primary node type definition of the associated Node of this object.
     * @throws RepositoryException if an error occurs
     */
    public NodeType getNodeType() throws RepositoryException {
        log.warn("This is a Version node, it will always return NT_FROZEN as node type.");
        log.warn("Use getNodeTypeName to retrieve base node primary type");
        return super.getNodeType();
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void restore(String versionName, boolean removeExisting) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void restore(Version version, boolean removeExisting) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void restore(Version version, String relPath, boolean removeExisting) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public Version addVersion() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to add version on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public Version addVersion(Rule rule) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to add version on version preview");
    }

    /**
     * Returns always false as verions are read only.
     */
    public boolean isModified() {
        log.error("Not valid for version");
        return false;
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public VersionHistory getVersionHistory() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to read VersionHistory of Version");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public VersionIterator getAllVersions() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to get VersionIterator of Version");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public ContentVersion getBaseVersion() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to get base version of Version");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public ContentVersion getVersionedContent(Version version) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to get preview of Version itself");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public ContentVersion getVersionedContent(String versionName) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to get preview of Version itself");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void save() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Checks for the allowed access rights.
     * @param permissions as defined in javax.jcr.Permission
     * @return true is the current user has specified access on this node.
     */
    public boolean isGranted(long permissions) {
        return (permissions & Permission.READ) == permissions;
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void delete() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void delete(String path) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * UUID of the node refrenced by this object.
     * @return uuid
     */
    public String getUUID() {
        return this.base.getUUID();
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void addMixin(String type) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void removeMixin(String type) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws LockException, RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public Lock lock(boolean isDeep, boolean isSessionScoped, long yieldFor) throws LockException, RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public Lock getLock() throws LockException, RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public void unlock() throws LockException, RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public boolean holdsLock() throws RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Throws an {@link AccessDeniedException} as versions are read only.
     */
    public boolean isLocked() throws RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Get hierarchy manager if previously set for this object.
     * @return HierarchyManager
     */
    public HierarchyManager getHierarchyManager() {
        return this.base.getHierarchyManager();
    }

    /**
     * Get access manager if previously set for this object.
     * @return AccessManager
     * @deprecated use getHierarchyManager instead
     */
    public AccessManager getAccessManager() {
        return this.base.getAccessManager();
    }

    @Override
    public Workspace getWorkspace() throws RepositoryException {
        return this.base.getWorkspace();
    }

    @Override
    public boolean hasNodeData(String name) throws RepositoryException {
        if (this.node.hasProperty(name)) {
            return true;
        }
        else { // check for mgnl:resource node
            if (this.node.hasNode(name) && this.node.getNode(name).getProperty("jcr:frozenPrimaryType").getValue().getString().equals(ItemType.NT_RESOURCE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int determineNodeDataType(String name) {
        // FIXME: maybe delegate to NodeDataImplementations?
        try {
            if (this.node.hasProperty(name)) {
                return this.node.getProperty(name).getType();
            }
            else { // check for mgnl:resource node
                if (this.node.hasNode(name) && this.node.getNode(name).getProperty("jcr:frozenPrimaryType").getValue().getString().equals(ItemType.NT_RESOURCE)) {
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
    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        Value[] vals = this.node.getProperty("jcr:frozenMixinTypes").getValues();
        NodeTypeManager typeMan = this.hierarchyManager.getWorkspace().getNodeTypeManager();
        NodeType[] types = new NodeType[vals.length];
        int i = 0;
        for (Value val : vals) {
            types[i++] = typeMan.getNodeType(val.getString());
        }
        return types;
    }

}
