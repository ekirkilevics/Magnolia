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
import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.core.util.Access;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @version 2.01
 */
public abstract class ContentHandler {

    public static final String SORT_BY_DATE = "date";

    public static final String SORT_BY_NAME = "name";

    public static final String SORT_BY_SEQUENCE = "sequence";

    private static Logger log = Logger.getLogger(ContentHandler.class);

    protected Node node;

    protected AccessManager accessManager;

    /**
     * package private constructor
     */
    ContentHandler() {
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

    public void setAccessManager(AccessManager manager) {
        this.accessManager = manager;
    }

    /**
     * <p>
     * get a handle representing path relative to the content repository
     * </p>
     * @return String representing path (handle) of the content
     */
    public String getHandle() {
        try {
            return this.node.getPath();
        }
        catch (RepositoryException e) {
            log.error("Failed to get handle");
            log.error(e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * <p>
     * get a handle representing path relative to the content repository with the default extension
     * </p>
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @return String representing path (handle) of the content
     */
    public String getHandleWithDefaultExtension() throws PathNotFoundException, RepositoryException {
        return (this.node.getPath() + "." + Server.getDefaultExtension());
    }

    /**
     * <p>
     * get parent content object
     * </p>
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @return Content representing parent node
     */
    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new Content(this.node.getParent(), this.accessManager));
    }

    /**
     * <p>
     * get absolute parent object starting from the root node
     * </p>
     * @param digree level at which the requested node exist, relative to the ROOT node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @return Content representing parent node
     */
    public Content getAncestor(int digree) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (digree > this.getLevel()) {
            throw new PathNotFoundException();
        }
        return (new Content(this.node.getAncestor(digree), this.accessManager));
    }

    /**
     * <p>
     * Convenience method for taglib
     * </p>
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @return Content representing node on level 0
     */
    public Collection getAncestors() throws PathNotFoundException, RepositoryException {
        List allAncestors = new ArrayList();
        int level = this.getLevel();
        while (level != 0) {
            try {
                allAncestors.add(new Content(this.node.getAncestor(--level), this.accessManager));
            }
            catch (AccessDeniedException e) {
                log.info(e.getMessage());
            }
        }
        return allAncestors;
    }

    /**
     * <p>
     * get node level from the ROOT node : FIXME implement getDepth in javax.jcr
     * </p>
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @return level at which current node exist, relative to the ROOT node
     */
    public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.node.getPath().split("/").length - 1;
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
     * This method returns the index of this node within the ordered set of its same-name  sibling nodes.
     * This index is the one used to address same-name siblings using the  square-bracket notation,
     * e.g., /a[3]/b[4]. Note that the index always starts  at 1 (not 0), for compatibility with XPath.
     * As a result, for nodes that do not have  same-name-siblings, this method will always return 1.
     * </p>
     *
     * @return The index of this node within the ordered set of its same-name sibling nodes.
     * */
    public int getIndex() throws RepositoryException {
        return this.node.getIndex();
    }

    /**
     * <p>
     * utility method to get Node object used to create current content object
     * </p>
     * @return Node
     */
    public Node getJCRNode() {
        return this.node;
    }

    /**
     * <p>
     * evaluate primary node type of the associated Node of this object
     * </p>
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
     * <p>
     * returns primary node type definition of the associated Node of this object
     * </p>
     */
    public NodeType getNodeType() throws RepositoryException {
        return this.node.getPrimaryNodeType();
    }

    /**
     * <p>
     * Restores this node to the state defined by the version with the specified versionName.
     * </p>
     * @param versionName
     * @param removeExisting
     * @see javax.jcr.Node#restore(String, boolean)
     */
    public void restore(String versionName, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restore(versionName, removeExisting);
    }

    /**
     * <p>
     * Restores this node to the state defined by the specified version.
     * </p>
     * @param version
     * @param removeExisting
     * @see javax.jcr.Node#restore(javax.jcr.version.Version, boolean)
     */
    public void restore(Version version, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restore(version, removeExisting);
    }

    /**
     * <p>
     * Restores the specified version to relPath, relative to this node.
     * </p>
     * @param version
     * @param relPath
     * @param removeExisting
     * @see javax.jcr.Node#restore(javax.jcr.version.Version, String, boolean)
     */
    public void restore(Version version, String relPath, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restore(version, relPath, removeExisting);
    }

    /**
     * <p>
     * Restores this node to the state recorded in the version specified by versionLabel.
     * </p>
     * @param versionLabel
     * @param removeExisting
     * @see javax.jcr.Node#restoreByLabel(String, boolean)
     */
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restoreByLabel(versionLabel, removeExisting);
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
        this.node.getSession().save();
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

    /**
     * <p>
     * Remove this path
     * </p>
     * @throws RepositoryException
     */
    public void delete() throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath()), Permission.REMOVE);
        this.node.remove();
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
     * Refreses current node keeping all changes
     * </p>
     * @see javax.jcr.Node#refresh(boolean)
     * @throws RepositoryException
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.node.refresh(keepChanges);
    }

    /**
     * <p>
     * UUID of the node refrenced by this object
     * </p>
     * @throws RepositoryException
     * */
    public String getUUID() throws RepositoryException {
        return this.node.getUUID();
    }

    /**
     * <p>
     * add specified mixin type if allowed
     * </p>
     *
     * @param type , mixin type to be added
     * */
    public void addMixin(String type) throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath()), Permission.SET);
        if (this.node.canAddMixin(type)) {
            this.node.addMixin(type);
        } else {
            log.error("Node - "+this.node.getPath()+" does not allow mixin type - "+type);
        }
    }

    /**
     * <p>
     * Removes the specified mixin node type from this node.
     * Also removes mixinName from this node's jcr:mixinTypes property.
     * <b>The mixin node type removal  takes effect on save</b>.
     * </p>
     *
     * @param type , mixin type to be removed
     * */
    public void removeMixin(String type) throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.node.getPath()), Permission.SET);
        this.node.removeMixin(type);
    }

    /**
     * <p>
     * Returns an array of NodeType objects representing the mixin node types  assigned to this node.
     * This includes only those mixin types explicitly  assigned to this node,
     * and therefore listed in the property jcr:mixinTypes. It does not include mixin types inherited
     * through the additon of supertypes to the primary type hierarchy.
     * </p>
     *
     * @return an array of mixin NodeType objects.
     * */
    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return this.node.getMixinNodeTypes();
    }

    /**
     * <p>
     * places a lock on this object
     * </p>
     *
     * @param isDeep if true this lock will apply to this node and all its descendants; if  false,
     * it applies only to this node.
     * @param isSessionScoped if true, this lock expires with the current session; if false it  expires when
     * explicitly or automatically unlocked for some other reason.
     * @return A Lock object containing a lock token.
     * @see javax.jcr.Node#lock(boolean, boolean)
     * */
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws LockException, RepositoryException {
        return this.node.lock(isDeep, isSessionScoped);
    }

    /**
     * <p>
     * Returns the Lock object that applies to this node.
     * This may be either a lock on this node itself  or a deep lock on a node above this node.
     * </p>
     *
     * @throws LockException If no lock applies to this node, a LockException is thrown.
     * @throws RepositoryException
     * */
    public Lock getLock() throws LockException, RepositoryException {
        return this.node.getLock();
    }

    /**
     * <p>
     * Removes the lock on this node. Also removes the properties jcr:lockOwner and  jcr:lockIsDeep from this node.
     * These changes are persisted automatically; <b>there is no need to call  save</b>.
     * </p>
     *
     * @throws LockException if either does not currently hold a lock,
     * or holds a lock for which this Session does not have the correct lock token
     * @throws RepositoryException
     * */
    public void unlock() throws LockException, RepositoryException {
        this.node.unlock();
    }

    /**
     * <p>
     * Returns true if this node holds a lock; otherwise returns false.
     * To hold a  lock means that this node has actually had a lock placed on it specifically,
     * as opposed to just having a lock  apply to it due to a deep lock held by a node above.
     * </p>
     * */
    public boolean holdsLock() throws RepositoryException {
        return this.node.holdsLock();
    }
}
