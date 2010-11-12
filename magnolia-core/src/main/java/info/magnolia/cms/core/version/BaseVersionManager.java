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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.context.MgnlContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This version manager uses an extra workspace to manage the versions. The
 * workspace maintains a flat hierarchy. The content is then finally versioned
 * using JCR versioning which also copies the sub-nodes.
 *
 * The mix:versionable is only added on the top level nodes.
 *
 * @author Sameer Charles
 */
public abstract class BaseVersionManager {

    /**
     * Name of the workspace.
     */
    public static final String VERSION_WORKSPACE = "mgnlVersion";

    /**
     * Node which contains stubs for referenced nodes. We have to copy them to the workspace as well.
     */
    public static final String TMP_REFERENCED_NODES = "mgnl:tmpReferencedNodes";

    /**
     * Sub-node containing the data used for the version/restore process.
     */
    protected static final String SYSTEM_NODE = "mgnl:versionMetaData";

    /**
     * Property name for collection rule. The rule defines which sub-nodes belong to a node: page and paragraphs.
     */
    public static final String PROPERTY_RULE = "Rule";

    /**
     * JCR version store root.
     */
    protected static final String ROOT_VERSION = "jcr:rootVersion";

    private static Logger log = LoggerFactory.getLogger(BaseVersionManager.class);

    /**
     * Create structure needed for version store workspace.
     * @throws RepositoryException if unable to create magnolia system structure
     */
    protected void createInitialStructure() throws RepositoryException {
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
     * Add version of the specified node and all child nodes while ignoring the same node type.
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
     * Add version of the specified node and all child nodes while ignoring the same node type.
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
     * Create version of the specified node and all child nodes based on the given <code>Rule</code>.
     * @param node to be versioned
     * @param rule
     * @return newly created version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    protected Version createVersion(Content node, Rule rule) throws UnsupportedRepositoryOperationException,
    RepositoryException {
        if (isInvalidMaxVersions()) {
            log.debug("Ignore create version, MaxVersionIndex < 1");
            log.debug("Returning root version of the source node");
            return node.getJCRNode().getVersionHistory().getRootVersion();
        }

        CopyUtil.getInstance().copyToversion(node, new RuleBasedContentFilter(rule));
        Content versionedNode = this.getVersionedNode(node);

        checkAndAddMixin(versionedNode);
        Content systemInfo = this.getSystemNode(versionedNode);
        // add serialized rule which was used to create this version
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ObjectOutput objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(rule);
            objectOut.flush();
            objectOut.close();
            // PROPERTY_RULE is not a part of MetaData to allow versioning of node types which does NOT support MetaData
            systemInfo.setNodeData(PROPERTY_RULE, new String(Base64.encodeBase64(out.toByteArray())));
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
        systemInfo.setNodeData(ContentVersion.VERSION_USER, userName);
        systemInfo.setNodeData(ContentVersion.NAME, node.getName());

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

    /**
     * Check if version index is set to negative number.
     */
    public abstract boolean isInvalidMaxVersions();

    /**
     * Get node from version store.
     */
    public synchronized Content getVersionedNode(Content node) throws RepositoryException {
        return getVersionedNode(node.getUUID());
    }

    /**
     * Get node from version store.
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
     * Set version history to max version possible.
     * @throws RepositoryException if failed to get VersionHistory or fail to remove
     */
    public abstract void setMaxVersionHistory(Content node) throws RepositoryException;

    /**
     * Get history of this node as recorded in the version store.
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
     * Get named version.
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
     * Returns the current base version of given node.
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
     * Get all versions.
     * @param node
     * @return Version iterator retrieved from version history
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized VersionIterator getAllVersions(Content node) throws UnsupportedRepositoryOperationException, RepositoryException {
        Content versionedNode = this.getVersionedNode(node);
        if (versionedNode == null) {
            // node does not exist in version store so no versions
            return null;
        }
        return versionedNode.getJCRNode().getVersionHistory().getAllVersions();
    }

    /**
     * Restore specified version.
     * @param node to be restored
     * @param version to be used
     * @param removeExisting
     * @throws javax.jcr.version.VersionException if the specified <code>versionName</code> does not exist in this
     * node's version history
     * @throws javax.jcr.RepositoryException if an error occurs
     * @throws javax.jcr.version.VersionException
     */
    public synchronized void restore(Content node, Version version, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        // get the cloned node from version store
        Content versionedNode = this.getVersionedNode(node);

        versionedNode.getJCRNode().restore(version, removeExisting);
        versionedNode.getJCRNode().checkout();
        //mixins are NOT restored automatically
        List<String> mixins = new ArrayList<String>();
        for (Value v: version.getNode("jcr:frozenNode").getProperty("jcr:frozenMixinTypes").getValues()) {
            mixins.add(v.getString());
        }
        final Content systemVersionedNode = MgnlContext.getSystemContext().getHierarchyManager(versionedNode.getHierarchyManager().getName()).getContentByUUID(versionedNode.getUUID());
        for (NodeType nt : versionedNode.getMixinNodeTypes()) {
            if (!mixins.remove(nt.getName())) {
                systemVersionedNode.removeMixin(nt.getName());
            }
        }
        for (String mix : mixins) {
            systemVersionedNode.addMixin(mix);
        }
        systemVersionedNode.save();

        List permissions = this.getAccessManagerPermissions();
        this.impersonateAccessManager(null);
        try {
            // if restored, update original node with the restored node and its subtree
            Rule rule = this.getUsedFilter(versionedNode);
            try {
                synchronized (ExclusiveWrite.getInstance()) {
                    CopyUtil.getInstance().copyFromVersion(versionedNode, node, new RuleBasedContentFilter(rule));
                    if (node.hasMixin(ItemType.DELETED_NODE_MIXIN)) {
                        node.removeMixin(ItemType.DELETED_NODE_MIXIN);
                    }
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
     * Removes all versions of the node associated with given UUID.
     * @param uuid
     * @throws RepositoryException if fails to remove versioned node from the version store
     */
    public synchronized void removeVersionHistory(String uuid) throws RepositoryException {
        List permissions = this.getAccessManagerPermissions();
        this.impersonateAccessManager(null);
        try {
            Content node = this.getVersionedNode(uuid);
            if (node != null) {
                if (node.getJCRNode().getReferences().getSize() < 1) {
                    // remove node from the version store only if its not referenced
                    node.delete();
                } else { // remove all associated versions
                    VersionHistory history = node.getVersionHistory();
                    VersionIterator versions = node.getAllVersions();
                    if (versions != null) {
                        // skip root version
                        versions.nextVersion();
                        while (versions.hasNext()) {
                            history.removeVersion(versions.nextVersion().getName());
                        }
                    }
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
     * Verifies the existence of the mix:versionable and adds it if not.
     */
    protected void checkAndAddMixin(Content node) throws RepositoryException {
        if(!node.getJCRNode().isNodeType("mix:versionable")){
            log.debug("Add mix:versionable");
            node.addMixin("mix:versionable");
        }
    }

    /**
     * Get Rule used for this version.
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
            inStream = new ByteArrayInputStream(Base64.decodeBase64(ruleString.getBytes()));
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
     * Get the Magnolia system node created under the given node.
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
     * Impersonate to be access manager with system rights.
     * @param permissions
     */
    protected void impersonateAccessManager(List permissions) {
        // FIXME: this is a very ugly hack but it needs the least change in the code
        // see MAGNOLIA-1753
        if(permissions == null){
            Permission permission = new PermissionImpl();
            permission.setPermissions(Permission.ALL);
            permission.setPattern(UrlPattern.MATCH_ALL);
            permissions = Collections.singletonList(permission);
        }
        this.getHierarchyManager().getAccessManager().setPermissionList(permissions);
    }

    /**
     * Revert access manager permissions.
     * @param permissions
     */
    protected void revertAccessManager(List permissions) {
        this.getHierarchyManager().getAccessManager().setPermissionList(permissions);
    }

    /**
     * Get access manager permission list.
     */
    protected List getAccessManagerPermissions() {
        return this.getHierarchyManager().getAccessManager().getPermissionList();
    }

    /**
     * Get version store hierarchy manager.
     */
    protected HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(VersionManager.VERSION_WORKSPACE);
    }
}
