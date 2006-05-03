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

import info.magnolia.cms.core.*;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.beans.runtime.MgnlContext;

import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;

/**
 * @author Sameer Charles
 * $Id$
 */
public class VersionManager {

    /**
     * version data base
     * */
    public static final String VERSION_WORKSPACE = "mgnlVersion";

    /**
     * version workspace system path
     * */
    protected static final String TMP_REFERENCED_NODES = "mgnl:tmpReferencedNodes";

    /**
     *  property name for collection rule
     *
     * */
    protected static final String PROPERTY_RULE = "Rule";

    /**
     * jcr root version
     * */
    protected static final String ROOT_VERSION = "jcr:rootVersion";

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(VersionManager.class);

    /**
     * singleton
     * */
    private static final VersionManager thisInstance = new VersionManager();

    /**
     * do not instanciate
     * */
    private VersionManager() {
        try {
            this.createInitialStructure();
        } catch (RepositoryException re) {
            log.error("Failed to initialize VersionManager");
            log.error(re.getMessage(), re);
        }
    }

    /**
     * create structure needed for version store workspace
     * @throws RepositoryException if unable to create magnolia system structure
     * */
    private void createInitialStructure() throws RepositoryException {
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(VERSION_WORKSPACE);
        if (!hm.isExist("/"+VersionManager.TMP_REFERENCED_NODES)) {
            hm.createContent("",VersionManager.TMP_REFERENCED_NODES, ItemType.SYSTEM.getSystemName());
        }
    }

    /**
     * get instance
     * */
    public static VersionManager getInstance() {
        return thisInstance;
    }

    /**
     * add version of the specified node and all child nodes while ignoring the same node type
     *
     * @param node to be versioned
     * @return newly created version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized Version addVersion(Content node) throws UnsupportedRepositoryOperationException, RepositoryException {
        Rule rule = new Rule(new String[] {node.getNodeType().getName()});
        rule.reverse();
        return this.addVersion(node, rule);
    }

    /**
     * add version of the specified node and all child nodes while ignoring the same node type
     *
     * @param node to be versioned
     * @return newly created version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized Version addVersion(Content node, Rule rule)
            throws UnsupportedRepositoryOperationException, RepositoryException {
        List permissions = this.getAccessManegerPermissions();
        this.impersonateAccessManager(null);
        try {
            return this.createVersion(node, rule);
        } catch (RepositoryException re) {
            // since add version is synchronized on a singleton object, its safe to revert all changes made in
            // the session attached to workspace - mgnlVersion
            log.error("failed to copy versionable node to version store, reverting all changes made in this session");
            getHierarchyManager().refresh(false);
            throw re;
        } finally {
            this.revertAccessManager(permissions);
        }
    }

    /**
     * create version of the specified node and all child nodes based on the given <code>Rule</code>
     *
     * @param node to be versioned
     * @param rule
     * @return newly created version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    private Version createVersion(Content node, Rule rule)
            throws UnsupportedRepositoryOperationException, RepositoryException {
        CopyUtil.getInstance().copyToversion(node, new RuleBasedContentFilter(rule));
        Content versionedNode = this.getVersionedNode(node);
        // add serialized rule which was used to create this version
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ObjectOutput objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(rule);
            objectOut.flush();
            objectOut.close();
            NodeData nodeData;
            // PROPERTY_RULE is not a part of MetaData to allow versioning of node types which does support MetaData
            if (!versionedNode.hasNodeData(PROPERTY_RULE))
                nodeData = versionedNode.createNodeData(PROPERTY_RULE);
            else
                nodeData = versionedNode.getNodeData(PROPERTY_RULE);
            nodeData.setValue(out.toString());
        } catch (IOException e) {
            throw new RepositoryException("Unable to add serialized Rule to the versioned content");
        }
        if (versionedNode.hasMetaData()) {
            versionedNode.getMetaData().setProperty(MetaData.VERSION_USER, MgnlContext.getUser().getName());
            versionedNode.getMetaData().setProperty(MetaData.NAME, node.getName());
        }
        versionedNode.save();
        // add version
        Version newVersion = versionedNode.getJCRNode().checkin();
        versionedNode.getJCRNode().checkout();
        return newVersion;
    }

    /***
     * get node from version store
     * @param node
     * */
    protected synchronized Content getVersionedNode(Content node) throws RepositoryException {
        List permissions = this.getAccessManegerPermissions();
        this.impersonateAccessManager(null);
        try {
            return getHierarchyManager().getContent(node.getUUID());
            //return MgnlContext.getHierarchyManager(VERSION_WORKSPACE).getContentByUUID(node.getUUID());
        } catch (PathNotFoundException e) {
            return null;
        } catch (RepositoryException re) {
            throw re;
        } finally {
            this.revertAccessManager(permissions);
        }
    }

    /**
     * get history of this node as recorded in the version store
     *
     * @param node
     * @return version history of the given node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized VersionHistory getVersionHistory(Content node)
            throws UnsupportedRepositoryOperationException, RepositoryException {
        Content versionedNode = this.getVersionedNode(node);
        if (versionedNode == null) {
            // node does not exist in version store so no version history
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
     * */
    public synchronized Version getVersion(Content node, String name)
            throws UnsupportedRepositoryOperationException, RepositoryException {
        VersionHistory history = this.getVersionHistory(node);
        if (history != null) {
            return history.getVersion(name);
        }
        log.error("Node "+node.getHandle()+" was never versioned");
        return null;
    }

    /**
     * Returns the current base version of given node
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException
     * */
    public Version getBaseVersion(Content node)
            throws UnsupportedOperationException, RepositoryException {
        Content versionedNode = this.getVersionedNode(node).getBaseVersion();
        if (versionedNode != null) {
            return versionedNode.getJCRNode().getBaseVersion();
        } else {
            throw new RepositoryException("Node "+node.getHandle()+" was never versioned");
        }
    }

    /**
     * get all versions
     *
     * @param node
     * @return Version iterator retreived from version history
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized VersionIterator getAllVersions(Content node)
            throws UnsupportedRepositoryOperationException, RepositoryException {
        Content versionedNode = this.getVersionedNode(node);
        if (versionedNode == null) {
            // node does not exist in version store so no versions
            return null;
        }
        return versionedNode.getJCRNode().getVersionHistory().getAllVersions();
    }

    /**
     * restore specified version
     *
     * @param node to be restored
     * @param version to be used
     * @param removeExisting
     * @throws javax.jcr.version.VersionException if the specified <code>versionName</code> does not exist in this
     * node's version history
     * @throws javax.jcr.RepositoryException if an error occurs
     * @throws javax.jcr.version.VersionException
     */
    public synchronized void restore(Content node, Version version, boolean removeExisting)
            throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        // get the cloned node from version store
        Content versionedNode = this.getVersionedNode(node);
        versionedNode.getJCRNode().restore(version, removeExisting);
        versionedNode.getJCRNode().checkout();
        List permissions = this.getAccessManegerPermissions();
        this.impersonateAccessManager(null);
        try {
            // if restored, update original node with the restored node and its subtree
            Rule rule = this.getUsedFilter(versionedNode);
            try {
                synchronized(ExclusiveWrite.getInstance()) {
                    CopyUtil.getInstance().copyFromVersion(versionedNode, node, new RuleBasedContentFilter(rule));
                    node.save();
                }
            } catch (RepositoryException re) {
                log.error("failed to restore versioned node, reverting all changes make to this node");
                node.refresh(false);
                throw re;
            }
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (ClassNotFoundException e) {
            throw new RepositoryException(e);
        } catch (RepositoryException e) {
            throw e;
        } finally {
            this.revertAccessManager(permissions);
        }
    }

    /**
     * get Rule used for this version
     * @param versionedNode
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws RepositoryException
     * */
    protected Rule getUsedFilter(Content versionedNode)
            throws IOException, ClassNotFoundException, RepositoryException {
        // if restored, update original node with the restored node and its subtree
        ByteArrayInputStream inStream = null;
        try {
            String ruleString = versionedNode.getNodeData(PROPERTY_RULE).getString();
            inStream = new ByteArrayInputStream(ruleString.getBytes());
            ObjectInput objectInput = new ObjectInputStream(inStream);
            return (Rule) objectInput.readObject();
        } catch (IOException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(inStream);
        }
    }

    /**
     * impersonate to be access manager with system rights
     * @param permissions
     * */
    private void impersonateAccessManager(List permissions) {
        this.getHierarchyManager().getAccessManager().setPermissionList(permissions);
    }

    /**
     * revert access manager permissions
     * @param permissions
     * */
    private void revertAccessManager(List permissions) {
        this.getHierarchyManager().getAccessManager().setPermissionList(permissions);
    }

    /**
     * get access manager permission list
     * */
    private List getAccessManegerPermissions() {
        return this.getHierarchyManager().getAccessManager().getPermissionList();
    }

    /**
     * get version store hierarchy manager
     * */
    private HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(VersionManager.VERSION_WORKSPACE);
    }

}
