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
package info.magnolia.cms.core.version;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Rule;

import java.util.Calendar;
import java.util.Collection;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * $Id$
 */
public class ContentVersion extends DefaultContent {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentVersion.class);

    /**
     * user who created this version
     */
    public static final String VERSION_USER = "versionUser"; //$NON-NLS-1$

    /**
     * name of the base node
     */
    public static final String NAME = "name";

    /**
     * version node (nt:version)
     */
    private Version state;

    /**
     * base content
     */
    private DefaultContent base;

    /**
     * Rule used to create this version
     */
    private Rule rule;

    /**
     * package private constructor
     * @param thisVersion
     * @param base content on which this version is based on
     * @throws RepositoryException
     */
    public ContentVersion(Version thisVersion, DefaultContent base) throws RepositoryException {
        if (thisVersion == null) {
            throw new RepositoryException("Failed to get ContentVersion, version does not exist");
        }
        this.state = thisVersion;
        this.base = base;
        this.init();
    }

    /**
     * Set frozen node of this version as working node
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
     * Get creation date of this version
     * @throws RepositoryException
     * @return creation date as calendar
     */
    public Calendar getCreated() throws RepositoryException {
        return this.state.getCreated();
    }

    /**
     * Return the name of the version represented by this object
     * @return the versions name
     * @throws RepositoryException
     */
    public String getVersionLabel() throws RepositoryException {
        return this.state.getName();
    }

    /**
     * Get containing version history
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
     * The name of the user who created this version
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
     * get original path of this versioned content
     */
    public String getHandle() {
        return this.base.getHandle();
    }

    /**
     * create Content node under the current node with the specified name
     * @param name of the node to be created as <code>Content</code>
     * @return newly created <node>Content </node>
     * @throws info.magnolia.cms.security.AccessDeniedException if the current session does not have sufficient access
     * rights to complete the operation
     */
    public Content createContent(String name) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * create Content node under the current node with the specified name
     * @param name of the node to be created as <code>Content</code>
     * @param contentType JCR node type as configured
     * @return newly created <node>Content </node>
     * @throws info.magnolia.cms.security.AccessDeniedException if the current session does not have sufficient access
     * rights to complete the operation
     */
    public Content createContent(String name, String contentType) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Create Content node under the current node with the specified name.
     * @param name of the node to be created as <code>Content</code>
     * @param contentType ItemType
     * @return newly created <node>Content </node>
     * @throws info.magnolia.cms.security.AccessDeniedException if the current session does not have sufficient access
     * rights to complete the operation
     */
    public Content createContent(String name, ItemType contentType) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * create top level NodeData object
     * @param name to be created
     * @return NodeData requested <code>NodeData</code> object
     * @throws info.magnolia.cms.security.AccessDeniedException if the current session does not have sufficient access
     * rights to complete the operation
     */
    public NodeData createNodeData(String name) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Create NodeData with the given value and type.
     * @param name to be created
     * @param value to be set initially
     * @param type propertyType
     * @return NodeData requested <code>NodeData</code> object
     * @throws info.magnolia.cms.security.AccessDeniedException if the current session does not have sufficient access
     * rights to complete the operation
     */
    public NodeData createNodeData(String name, Value value, int type) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Create NodeData with the given value and type.
     * @param name to be created
     * @param value to be set initially
     * @return NodeData requested <code>NodeData</code> object
     * @throws info.magnolia.cms.security.AccessDeniedException if the current session does not have sufficient access
     * rights to complete the operation
     */
    public NodeData createNodeData(String name, Value value) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * create top level NodeData object
     * @param name to be created
     * @param type propertyType
     * @return NodeData requested <code>NodeData</code> object
     * @throws info.magnolia.cms.security.AccessDeniedException if the current session does not have sufficient access
     * rights to complete the operation
     */
    public NodeData createNodeData(String name, int type) throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * delete NodeData with the specified name
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void deleteNodeData(String name) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * you could call this method anytime to update working page properties - Modification date & Author ID
     * @throws info.magnolia.cms.security.AccessDeniedException if the current session does not have sufficient access
     * rights to complete the operation
     */
    public void updateMetaData() throws AccessDeniedException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * gets a Collection containing all child nodes of the same NodeType as "this" object.
     * @return Collection of content objects
     */
    public Collection getChildren() {
        try {
            if (this.rule.isAllowed(this.base.getNodeTypeName())) {
                return super.getChildren();
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return this.base.getChildren();
    }

    /**
     * Get collection of specified content type
     * @param contentType JCR node type as configured
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType) {
        if (this.rule.isAllowed(contentType)) {
            return super.getChildren(contentType);
        }
        return this.base.getChildren(contentType);
    }

    /**
     * Get collection of specified content type
     * @param contentType ItemType
     * @return Collection of content nodes
     */
    public Collection getChildren(ItemType contentType) {
        return this.getChildren(contentType.getSystemName());
    }

    /**
     * Get collection of specified content type.
     * @param contentType JCR node type as configured
     * @param namePattern
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType, String namePattern) {
        if (this.rule.isAllowed(contentType)) {
            return super.getChildren(contentType, namePattern);
        }
        return this.base.getChildren(contentType, namePattern);
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
     * get parent content object
     * @return Content representing parent node
     * @throws javax.jcr.PathNotFoundException
     * @throws info.magnolia.cms.security.AccessDeniedException if the current session does not have sufficient access
     * rights to complete the operation
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.base.getParent();
    }

    /**
     * get absolute parent object starting from the root node
     * @param digree level at which the requested node exist, relative to the ROOT node
     * @return Content representing parent node
     * @throws info.magnolia.cms.security.AccessDeniedException if the current session does not have sufficient access
     * rights to complete the operation
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public Content getAncestor(int digree) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.base.getAncestor(digree);
    }

    /**
     * Convenience method for taglib
     * @return Content representing node on level 0
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public Collection getAncestors() throws PathNotFoundException, RepositoryException {
        return this.base.getAncestors();
    }

    /**
     * get node level from the ROOT node : FIXME implement getDepth in javax.jcr
     * @return level at which current node exist, relative to the ROOT node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.base.getLevel();
    }

    /**
     * move current node to the specified location above the named <code>beforename</code>
     * @param srcName where current node has to be moved
     * @param beforeName name of the node before the current node has to be placed
     * @throws javax.jcr.RepositoryException if an error occurs
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
     * returns primary node type definition of the associated Node of this object
     * @throws RepositoryException if an error occurs
     */
    public NodeType getNodeType() throws RepositoryException {
        log.warn("This is a Version node, it will always return NT_FROZEN as node type.");
        log.warn("Use getNodeTypeName to retrieve base node primary type");
        return super.getNodeType();
    }

    /**
     * Restores this node to the state defined by the version with the specified versionName.
     * @param versionName
     * @param removeExisting
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void restore(String versionName, boolean removeExisting) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Restores this node to the state defined by the specified version.
     * @param version
     * @param removeExisting
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void restore(Version version, boolean removeExisting) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Restores the specified version to relPath, relative to this node.
     * @param version
     * @param relPath
     * @param removeExisting
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void restore(Version version, String relPath, boolean removeExisting) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Restores this node to the state recorded in the version specified by versionLabel.
     * @param versionLabel
     * @param removeExisting
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * add version leaving the node checked out
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public Version addVersion() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to add version on version preview");
    }

    /**
     * add version leaving the node checked out
     * @param rule to be used to collect content
     * @throws javax.jcr.RepositoryException if an error occurs
     * @see info.magnolia.cms.util.Rule
     */
    public Version addVersion(Rule rule) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to add version on version preview");
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
        log.error("Not valid for version");
        return false;
    }

    /**
     * @return version history
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public VersionHistory getVersionHistory() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to read VersionHistory of Version");
    }

    /**
     * @return Version iterator retreived from version history
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public VersionIterator getAllVersions() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to get VersionIterator of Version");
    }

    /**
     * get the current base version of this node
     * @return base ContentVersion
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     * @throws javax.jcr.RepositoryException
     */
    public ContentVersion getBaseVersion() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to get base version of Version");
    }

    /**
     * get content view over the jcr version object
     * @param version
     * @return version object wrapped in ContentVersion
     * @see info.magnolia.cms.core.version.ContentVersion
     */
    public ContentVersion getVersionedContent(Version version) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to get preview of Version itself");
    }

    /**
     * get content view over the jcr version object
     * @param versionName
     * @return version object wrapped in ContentVersion
     * @see info.magnolia.cms.core.version.ContentVersion
     */
    public ContentVersion getVersionedContent(String versionName) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to get preview of Version itself");
    }

    /**
     * Persists all changes to the repository if validation succeds
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void save() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * checks for the allowed access rights
     * @param permissions as defined in javax.jcr.Permission
     * @return true is the current user has specified access on this node.
     */
    public boolean isGranted(long permissions) {
        return (permissions & Permission.READ) == permissions;
    }

    /**
     * Remove this path
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void delete() throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Remove specified path
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void delete(String path) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * UUID of the node refrenced by this object
     * @return uuid
     */
    public String getUUID() {
        return this.base.getUUID();
    }

    /**
     * add specified mixin type if allowed
     * @param type mixin type to be added
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void addMixin(String type) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * Removes the specified mixin node type from this node. Also removes mixinName from this node's jcr:mixinTypes
     * property. <b>The mixin node type removal takes effect on save</b>.
     * @param type , mixin type to be removed
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void removeMixin(String type) throws RepositoryException {
        throw new AccessDeniedException("Not allowed to write on version preview");
    }

    /**
     * places a lock on this object
     * @param isDeep if true this lock will apply to this node and all its descendants; if false, it applies only to
     * this node.
     * @param isSessionScoped if true, this lock expires with the current session; if false it expires when explicitly
     * or automatically unlocked for some other reason.
     * @return A Lock object containing a lock token.
     * @throws javax.jcr.lock.LockException if this node is already locked or <code>isDeep</code> is true and a
     * descendant node of this node already holds a lock.
     * @throws javax.jcr.RepositoryException if an error occurs
     * @see javax.jcr.Node#lock(boolean, boolean)
     */
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws LockException, RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * places a lock on this object
     * @param isDeep if true this lock will apply to this node and all its descendants; if false, it applies only to
     * this node.
     * @param isSessionScoped if true, this lock expires with the current session; if false it expires when explicitly
     * or automatically unlocked for some other reason.
     * @param yieldFor number of milliseconds for which this method will try to get a lock
     * @return A Lock object containing a lock token.
     * @throws javax.jcr.lock.LockException if this node is already locked or <code>isDeep</code> is true and a
     * descendant node of this node already holds a lock.
     * @throws javax.jcr.RepositoryException if an error occurs
     * @see javax.jcr.Node#lock(boolean, boolean)
     */
    public Lock lock(boolean isDeep, boolean isSessionScoped, long yieldFor) throws LockException, RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Returns the Lock object that applies to this node. This may be either a lock on this node itself or a deep lock
     * on a node above this node.
     * @throws javax.jcr.lock.LockException If no lock applies to this node, a LockException is thrown.
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public Lock getLock() throws LockException, RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Removes the lock on this node. Also removes the properties jcr:lockOwner and jcr:lockIsDeep from this node. These
     * changes are persisted automatically; <b>there is no need to call save</b>.
     * @throws javax.jcr.lock.LockException if either does not currently hold a lock, or holds a lock for which this
     * Session does not have the correct lock token
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public void unlock() throws LockException, RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Returns true if this node holds a lock; otherwise returns false. To hold a lock means that this node has actually
     * had a lock placed on it specifically, as opposed to just having a lock apply to it due to a deep lock held by a
     * node above.
     * @return a boolean
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public boolean holdsLock() throws RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Returns true if this node is locked either as a result of a lock held by this node or by a deep lock on a node
     * above this node; otherwise returns false.
     * @return a boolean
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    public boolean isLocked() throws RepositoryException {
        throw new AccessDeniedException("Lock not supported on version preview");
    }

    /**
     * Set access manager for this object
     * @param manager
     */
    public void setAccessManager(AccessManager manager) {
        log.error("Not allowed to set access manager on Version preview");
    }

    /**
     * Get access manager if previously set for this object
     * @return AccessManager
     */
    public AccessManager getAccessManager() {
        return this.base.getAccessManager();
    }

}
