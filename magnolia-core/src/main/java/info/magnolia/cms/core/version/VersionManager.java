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
package info.magnolia.cms.core.version;

import info.magnolia.cms.beans.config.VersionConfig;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;
import info.magnolia.context.MgnlContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Singleton class which should be used for any operation related to versioning VersionManager synchronizes all
 * operations like add version, restore version and remove version but it does not synchronize between operations
 * @author Sameer Charles $Id$
 */
public final class VersionManager {

    /**
     * version data base
     */
    public static final String VERSION_WORKSPACE = "mgnlVersion";

    /**
     * version workspace system path
     */
    public static final String TMP_REFERENCED_NODES = "mgnl:tmpReferencedNodes";

    /**
     * version system node, holds this node version specific data
     */
    private static final String SYSTEM_NODE = "mgnl:versionMetaData";

    /**
     * property name for collection rule
     */
    protected static final String PROPERTY_RULE = "Rule";

    /**
     * jcr root version
     */
    public static final String ROOT_VERSION = "jcr:rootVersion";

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(VersionManager.class);

    /**
     * singleton
     */
    private static final VersionManager thisInstance = new VersionManager();

    /**
     * do not instanciate
     */
    private VersionManager() {
        try {
            this.createInitialStructure();
        }
        catch (RepositoryException re) {
            log.error("Failed to initialize VersionManager");
            log.error(re.getMessage(), re);
        }
    }

    /**
     * create structure needed for version store workspace
     * @throws RepositoryException if unable to create magnolia system structure
     */
    private void createInitialStructure() throws RepositoryException {
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(VERSION_WORKSPACE);
        try {
            Content tmp = hm.getContent("/" + VersionManager.TMP_REFERENCED_NODES);
            // remove nodes if they are no longer referenced within this workspace
            NodeIterator children = tmp.getJCRNode().getNodes();
            while (children.hasNext()) {
                Node child = children.nextNode();
                if (child.getReferences().getSize() < 1) {
                    child.remove();
                }
            }
        }
        catch (PathNotFoundException e) {
            hm.createContent("", VersionManager.TMP_REFERENCED_NODES, ItemType.SYSTEM.getSystemName());
        }
        hm.save();
    }

    /**
     * get instance
     */
    public static VersionManager getInstance() {
        return thisInstance;
    }

    /**
     * add version of the specified node and all child nodes while ignoring the same node type
     * @param node to be versioned
     * @return newly created version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized Version addVersion(Content node) throws UnsupportedRepositoryOperationException,
        RepositoryException {
        // Rule rule = new Rule(new String[] {node.getNodeType().getName(), ItemType.SYSTEM.getSystemName()});
        Rule rule = new Rule(node.getNodeTypeName() + "," + ItemType.SYSTEM.getSystemName(), ",");
        rule.reverse();
        return this.addVersion(node, rule);
    }

    /**
     * add version of the specified node and all child nodes while ignoring the same node type
     * @param node to be versioned
     * @return newly created version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized Version addVersion(Content node, Rule rule) throws UnsupportedRepositoryOperationException,
        RepositoryException {
        List permissions = this.getAccessManagerPermissions();
        this.impersonateAccessManager(null);
        try {
            return this.createVersion(node, rule);
        }
        catch (RepositoryException re) {
            // since add version is synchronized on a singleton object, its safe to revert all changes made in
            // the session attached to workspace - mgnlVersion
            log.error("failed to copy versionable node to version store, reverting all changes made in this session");
            getHierarchyManager().refresh(false);
            throw re;
        }
        finally {
            this.revertAccessManager(permissions);
        }
    }

    /**
     * create version of the specified node and all child nodes based on the given <code>Rule</code>
     * @param node to be versioned
     * @param rule
     * @return newly created version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    private Version createVersion(Content node, Rule rule) throws UnsupportedRepositoryOperationException,
        RepositoryException {
        if (VersionConfig.getMaxVersionIndex() < 1) {
            log.debug("Ignore create version, MaxVersionIndex < 1");
            log.debug("Returning root version of the source node");
            return node.getJCRNode().getVersionHistory().getRootVersion();
        }
        CopyUtil.getInstance().copyToversion(node, new RuleBasedContentFilter(rule));
        Content versionedNode = this.getVersionedNode(node);
        Content systemInfo = this.getSystemNode(versionedNode);
        // add serialized rule which was used to create this version
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ObjectOutput objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(rule);
            objectOut.flush();
            objectOut.close();
            NodeData nodeData;
            // PROPERTY_RULE is not a part of MetaData to allow versioning of node types which does NOT support MetaData
            if (!systemInfo.hasNodeData(PROPERTY_RULE)) {
                nodeData = systemInfo.createNodeData(PROPERTY_RULE);
            }
            else {
                nodeData = systemInfo.getNodeData(PROPERTY_RULE);
            }
            nodeData.setValue(out.toString());
        }
        catch (IOException e) {
            throw new RepositoryException("Unable to add serialized Rule to the versioned content");
        }
        // temp fix, MgnlContext should always have user either logged-in or anonymous
        String userName = "";
        if (MgnlContext.getUser() != null) {
            userName = MgnlContext.getUser().getName();
        }
        // add all system properties for this version
        if (!systemInfo.hasNodeData(ContentVersion.VERSION_USER)) {
            systemInfo.createNodeData(ContentVersion.VERSION_USER).setValue(userName);
        }
        else {
            systemInfo.getNodeData(ContentVersion.VERSION_USER).setValue(userName);
        }
        if (!systemInfo.hasNodeData(ContentVersion.NAME)) {
            systemInfo.createNodeData(ContentVersion.NAME).setValue(node.getName());
        }
        else {
            systemInfo.getNodeData(ContentVersion.NAME).setValue(node.getName());
        }

        versionedNode.save();
        // add version
        Version newVersion = versionedNode.getJCRNode().checkin();
        versionedNode.getJCRNode().checkout();
        try {
            this.setMaxVersionHistory(versionedNode);
        }
        catch (RepositoryException re) {
            log.error("Failed to limit version history to the maximum configured", re);
            log.error("New version has already been created");
        }

        return newVersion;
    }

    /*******************************************************************************************************************
     * get node from version store
     * @param node
     */
    protected synchronized Content getVersionedNode(Content node) throws RepositoryException {
        return getVersionedNode(node.getUUID());
    }

    /*******************************************************************************************************************
     * get node from version store
     * @param uuid
     */
    protected synchronized Content getVersionedNode(String uuid) throws RepositoryException {
        List permissions = this.getAccessManagerPermissions();
        this.impersonateAccessManager(null);
        try {
            return getHierarchyManager().getContentByUUID(uuid);
        }
        catch (ItemNotFoundException e) {
            return null;
        }
        catch (RepositoryException re) {
            throw re;
        }
        finally {
            this.revertAccessManager(permissions);
        }
    }

    /**
     * set version history to max version possible
     * @param node
     * @throws RepositoryException if failed to get VersionHistory or fail to remove
     */
    private void setMaxVersionHistory(Content node) throws RepositoryException {
        VersionHistory history = node.getJCRNode().getVersionHistory();
        VersionIterator versions = history.getAllVersions();
        long indexToRemove = (versions.getSize() - 1) - VersionConfig.getMaxVersionIndex();
        if (indexToRemove > 0) {
            // skip root version
            versions.nextVersion();
            // remove the version after rootVersion
            for (; indexToRemove > 0; indexToRemove--) {
                history.removeVersion(versions.nextVersion().getName());
            }
        }
    }

    /**
     * get history of this node as recorded in the version store
     * @param node
     * @return version history of the given node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized VersionHistory getVersionHistory(Content node) throws UnsupportedRepositoryOperationException,
        RepositoryException {
        Content versionedNode = this.getVersionedNode(node);
        if (versionedNode == null) {
            // node does not exist in version store so no version history
            log.info("No VersionHistory found for this node");
            return null;
        }
        return versionedNode.getJCRNode().getVersionHistory();
    }

    /**
     * get named version
     * @param node
     * @param name
     * @return version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized Version getVersion(Content node, String name) throws UnsupportedRepositoryOperationException,
        RepositoryException {
        VersionHistory history = this.getVersionHistory(node);
        if (history != null) {
            return history.getVersion(name);
        }
        log.error("Node " + node.getHandle() + " was never versioned");
        return null;
    }

    /**
     * Returns the current base version of given node
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException
     */
    public Version getBaseVersion(Content node) throws UnsupportedOperationException, RepositoryException {
        Content versionedNode = this.getVersionedNode(node);
        if (versionedNode != null) {
            return versionedNode.getJCRNode().getBaseVersion();
        }

        throw new RepositoryException("Node " + node.getHandle() + " was never versioned");
    }

    /**
     * get all versions
     * @param node
     * @return Version iterator retreived from version history
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized VersionIterator getAllVersions(Content node) throws UnsupportedRepositoryOperationException,
        RepositoryException {
        Content versionedNode = this.getVersionedNode(node);
        if (versionedNode == null) {
            // node does not exist in version store so no versions
            return null;
        }
        return versionedNode.getJCRNode().getVersionHistory().getAllVersions();
    }

    /**
     * restore specified version
     * @param node to be restored
     * @param version to be used
     * @param removeExisting
     * @throws javax.jcr.version.VersionException if the specified <code>versionName</code> does not exist in this
     * node's version history
     * @throws javax.jcr.RepositoryException if an error occurs
     * @throws javax.jcr.version.VersionException
     */
    public synchronized void restore(Content node, Version version, boolean removeExisting) throws VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        // get the cloned node from version store
        Content versionedNode = this.getVersionedNode(node);
        versionedNode.getJCRNode().restore(version, removeExisting);
        versionedNode.getJCRNode().checkout();
        List permissions = this.getAccessManagerPermissions();
        this.impersonateAccessManager(null);
        try {
            // if restored, update original node with the restored node and its subtree
            Rule rule = this.getUsedFilter(versionedNode);
            try {
                synchronized (ExclusiveWrite.getInstance()) {
                    CopyUtil.getInstance().copyFromVersion(versionedNode, node, new RuleBasedContentFilter(rule));
                    node.save();
                }
            }
            catch (RepositoryException re) {
                log.error("failed to restore versioned node, reverting all changes make to this node");
                node.refresh(false);
                throw re;
            }
        }
        catch (IOException e) {
            throw new RepositoryException(e);
        }
        catch (ClassNotFoundException e) {
            throw new RepositoryException(e);
        }
        catch (RepositoryException e) {
            throw e;
        }
        finally {
            this.revertAccessManager(permissions);
        }
    }

    /**
     * Removes all versions of the node associated with given UUID
     * @param uuid
     * @throws RepositoryException if fails to remove versioned node from the version store
     */
    public synchronized void removeVersionHistory(String uuid) throws RepositoryException {
        List permissions = this.getAccessManagerPermissions();
        this.impersonateAccessManager(null);
        try {
            Content node = this.getVersionedNode(uuid);
            if (node != null) {
                VersionHistory history = node.getVersionHistory();
                VersionIterator versions = node.getAllVersions();
                if (versions != null) {
                    // skip root version
                    versions.nextVersion();
                    while (versions.hasNext()) {
                        history.removeVersion(versions.nextVersion().getName());
                    }
                }
                if (node.getJCRNode().getReferences().getSize() < 1) {
                    // remove node from the version store only if its not referenced
                    node.delete();
                }
            }
        }
        catch (RepositoryException re) {
            throw re;
        }
        finally {
            this.revertAccessManager(permissions);
        }
        getHierarchyManager().save();
    }

    /**
     * get Rule used for this version
     * @param versionedNode
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws RepositoryException
     */
    protected Rule getUsedFilter(Content versionedNode) throws IOException, ClassNotFoundException, RepositoryException {
        // if restored, update original node with the restored node and its subtree
        ByteArrayInputStream inStream = null;
        try {
            String ruleString = this.getSystemNode(versionedNode).getNodeData(PROPERTY_RULE).getString();
            inStream = new ByteArrayInputStream(ruleString.getBytes());
            ObjectInput objectInput = new ObjectInputStream(inStream);
            return (Rule) objectInput.readObject();
        }
        catch (IOException e) {
            throw e;
        }
        catch (ClassNotFoundException e) {
            throw e;
        }
        finally {
            IOUtils.closeQuietly(inStream);
        }
    }

    /**
     * get magnolia system node created under the given node
     * @param node
     * @throws RepositoryException if failed to create system node
     */
    protected synchronized Content getSystemNode(Content node) throws RepositoryException {
        try {
            return node.getContent(SYSTEM_NODE);
        }
        catch (PathNotFoundException e) {
            return node.createContent(SYSTEM_NODE, ItemType.SYSTEM);
        }
    }

    /**
     * impersonate to be access manager with system rights
     * @param permissions
     */
    private void impersonateAccessManager(List permissions) {
        this.getHierarchyManager().getAccessManager().setPermissionList(permissions);
    }

    /**
     * revert access manager permissions
     * @param permissions
     */
    private void revertAccessManager(List permissions) {
        this.getHierarchyManager().getAccessManager().setPermissionList(permissions);
    }

    /**
     * get access manager permission list
     */
    private List getAccessManagerPermissions() {
        return this.getHierarchyManager().getAccessManager().getPermissionList();
    }

    /**
     * get version store hierarchy manager
     */
    private HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(VersionManager.VERSION_WORKSPACE);
    }

}
