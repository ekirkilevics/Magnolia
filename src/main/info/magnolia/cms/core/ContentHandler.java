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
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

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
            return "";
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
     * utility method to get Node object used to create current content object
     * </p>
     * @return Node
     */
    public Node getJCRNode() {
        return this.node;
    }

    /**
     * <p>
     * evaluate primary node type of the associated Node of this Content object
     * </p>
     */
    public boolean isContentType(String type) {
        try {
            return this.node.isNodeType(type);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return false;
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
        this.node.save();
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

}
