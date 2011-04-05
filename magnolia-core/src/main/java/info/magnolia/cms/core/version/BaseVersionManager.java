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
package info.magnolia.cms.core.version;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.JCRUtil;
import info.magnolia.cms.security.JCRSessionOp;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedNodePredicate;
import info.magnolia.context.MgnlContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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
        MgnlContext.doInSystemContext(new JCRSessionOp<Void>(VERSION_WORKSPACE) {

            @Override
            public Void exec(Session session) throws RepositoryException {
                try {
                    Node tmp = session.getNode("/" + VersionManager.TMP_REFERENCED_NODES);
                    // remove nodes if they are no longer referenced within this workspace
                    NodeIterator children = tmp.getNodes();
                    while (children.hasNext()) {
                        Node child = children.nextNode();
                        if (child.getReferences().getSize() < 1) {
                            child.remove();
                        }
                    }
                }
                catch (PathNotFoundException e) {
                    session.getRootNode().addNode(VersionManager.TMP_REFERENCED_NODES, ItemType.SYSTEM.getSystemName());
                }
                session.save();

                return null;
            }
        });

    }

    /**
     * Add version of the specified node and all child nodes while ignoring the same node type.
     * @param node to be versioned
     * @return newly created version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized Version addVersion(Node node) throws UnsupportedRepositoryOperationException,
    RepositoryException {
        // Rule rule = new Rule(new String[] {node.getNodeType().getName(), ItemType.SYSTEM.getSystemName()});
        Rule rule = new Rule(JCRUtil.getNodeTypeName(node) + "," + ItemType.SYSTEM.getSystemName(), ",");
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
    public synchronized Version addVersion(final Node node, final Rule rule) throws UnsupportedRepositoryOperationException,
    RepositoryException {
        MgnlContext.doInSystemContext(new JCRSessionOp<Version>(node.getSession().getWorkspace().getName()) {

            @Override
            public Version exec(Session session) throws RepositoryException {
                try {
                    return createVersion(session.getNodeByIdentifier(node.getUUID()), rule);
                }
                catch (RepositoryException re) {
                    // since add version is synchronized on a singleton object, its safe to revert all changes made in
                    // the session attached to workspace - mgnlVersion
                    log.error("failed to copy versionable node to version store, reverting all changes made in this session");
                    getSession().refresh(false);
                    throw re;
                }

            }});
        try {
            return this.createVersion(node, rule);
        }
        catch (RepositoryException re) {
            // since add version is synchronized on a singleton object, its safe to revert all changes made in
            // the session attached to workspace - mgnlVersion
            log.error("failed to copy versionable node to version store, reverting all changes made in this session");
            getSession().refresh(false);
            throw re;
        }
    }

    /**
     * Create version of the specified node and all child nodes based on the given <code>Rule</code>.
     * @param node to be versioned
     * @param rule
     * @return newly created version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     * @deprecated since 5.0 use {@link #createVersion(Node, Rule)} instead
     */
    @Deprecated
    protected Version createVersion(Content node, Rule rule) throws UnsupportedRepositoryOperationException, RepositoryException {
        return createVersion(node.getJCRNode(), rule);
    }

    /**
     * Create version of the specified node and all child nodes based on the given <code>Rule</code>.
     * @param node to be versioned
     * @param rule
     * @return newly created version node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    protected Version createVersion(Node node, Rule rule) throws UnsupportedRepositoryOperationException,
    RepositoryException {
        if (isInvalidMaxVersions()) {
            log.debug("Ignore create version, MaxVersionIndex < 1");
            log.debug("Returning root version of the source node");
            return node.getVersionHistory().getRootVersion();
        }

        CopyUtil.getInstance().copyToversion(node, new RuleBasedNodePredicate(rule));
        Node versionedNode = this.getVersionedNode(node);

        checkAndAddMixin(versionedNode);
        Node systemInfo = this.getSystemNode(versionedNode);
        // add serialized rule which was used to create this version
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ObjectOutput objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(rule);
            objectOut.flush();
            objectOut.close();
            // PROPERTY_RULE is not a part of MetaData to allow versioning of node types which does NOT support MetaData
            systemInfo.setProperty(PROPERTY_RULE, new String(Base64.encodeBase64(out.toByteArray())));
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
        systemInfo.setProperty(ContentVersion.VERSION_USER, userName);
        systemInfo.setProperty(ContentVersion.NAME, node.getName());

        versionedNode.save();
        // add version
        Version newVersion = versionedNode.checkin();
        versionedNode.checkout();

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
    public synchronized Node getVersionedNode(Node node) throws RepositoryException {
        return getVersionedNode(node.getIdentifier());
    }

    /**
     * Get node from version store.
     */
    protected Node getVersionedNode(String uuid) throws RepositoryException {
        return getSession().getNodeByIdentifier(uuid);
    }

    /**
     * Set version history to max version possible.
     * @throws RepositoryException if failed to get VersionHistory or fail to remove
     */
    public abstract void setMaxVersionHistory(Node node) throws RepositoryException;

    /**
     * Get history of this node as recorded in the version store.
     * @param node
     * @return version history of the given node
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized VersionHistory getVersionHistory(Node node) throws UnsupportedRepositoryOperationException,
    RepositoryException {
        Node versionedNode = this.getVersionedNode(node);
        if (versionedNode == null) {
            // node does not exist in version store so no version history
            log.info("No VersionHistory found for this node");
            return null;
        }
        return versionedNode.getVersionHistory();
    }

    /**
     * Get named version.
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized Version getVersion(Node node, String name) throws UnsupportedRepositoryOperationException,
    RepositoryException {
        VersionHistory history = this.getVersionHistory(node);
        if (history != null) {
            return new VersionedNode(history.getVersion(name));
        }
        log.error("Node " + node.getPath() + " was never versioned");
        return null;
    }

    /**
     * Returns the current base version of given node.
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException
     */
    public Version getBaseVersion(Node node) throws UnsupportedOperationException, RepositoryException {
        Node versionedNode = this.getVersionedNode(node);
        if (versionedNode != null) {
            return versionedNode.getBaseVersion();
        }

        throw new RepositoryException("Node " + node.getPath() + " was never versioned");
    }

    /**
     * Get all versions.
     * @param node
     * @return Version iterator retrieved from version history
     * @throws UnsupportedOperationException if repository implementation does not support Versions API
     * @throws javax.jcr.RepositoryException if any repository error occurs
     */
    public synchronized VersionIterator getAllVersions(Node node) throws UnsupportedRepositoryOperationException, RepositoryException {
        Node versionedNode = this.getVersionedNode(node);
        if (versionedNode == null) {
            // node does not exist in version store so no versions
            return null;
        }
        return versionedNode.getVersionHistory().getAllVersions();
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
     * @deprecated since 5.0 use {@link #restore(Node, Version, boolean)} instead
     */
    @Deprecated
    public synchronized void restore(Content node, Version version, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        restore(node.getJCRNode(), version, removeExisting);
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
    public synchronized void restore(final Node node, Version version, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        // get the cloned node from version store
        final Node versionedNode = this.getVersionedNode(node);

        final Version unwrappedVersion;
        if (version instanceof VersionedNode) {
            unwrappedVersion = ((VersionedNode) version).unwrap();
        } else {
            unwrappedVersion = version;
        }

        versionedNode.restore(unwrappedVersion, removeExisting);
        versionedNode.checkout();
        MgnlContext.doInSystemContext(new JCRSessionOp<Void>(versionedNode.getSession().getWorkspace().getName()) {

            @Override
            public Void exec(Session session) throws RepositoryException {
                //mixins are NOT restored automatically
                List<String> mixins = new ArrayList<String>();
                for (Value v: unwrappedVersion.getNode("jcr:frozenNode").getProperty("jcr:frozenMixinTypes").getValues()) {
                    mixins.add(v.getString());
                }

                final Node systemVersionedNode = session.getNodeByIdentifier(versionedNode.getUUID());
                for (NodeType nt : versionedNode.getMixinNodeTypes()) {
                    if (!mixins.remove(nt.getName())) {
                        systemVersionedNode.removeMixin(nt.getName());
                    }
                }
                for (String mix : mixins) {
                    systemVersionedNode.addMixin(mix);
                }
                systemVersionedNode.save();

                try {
                    // if restored, update original node with the restored node and its subtree
                    Rule rule = getUsedFilter(versionedNode);
                    try {
                        synchronized (ExclusiveWrite.getInstance()) {
                            CopyUtil.getInstance().copyFromVersion(versionedNode, node, new RuleBasedNodePredicate(rule));
                            if (JCRUtil.hasMixin(node, ItemType.DELETED_NODE_MIXIN)) {
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
                return null;
            }
        });
    }

    /**
     * Removes all versions of the node associated with given UUID.
     * @param uuid
     * @throws RepositoryException if fails to remove versioned node from the version store
     */
    public synchronized void removeVersionHistory(final String uuid) throws RepositoryException {
        MgnlContext.doInSystemContext(new JCRSessionOp<Void>(VersionManager.VERSION_WORKSPACE) {

            @Override
            public Void exec(Session session) throws RepositoryException {
                Node node = getVersionedNode(uuid);
                if (node != null) {
                    if (node.getReferences().getSize() < 1) {
                        // remove node from the version store only if its not referenced
                        node.remove();
                    } else { // remove all associated versions
                        VersionHistory history = node.getVersionHistory();
                        VersionIterator versions = history.getAllVersions();
                        if (versions != null) {
                            // skip root version
                            versions.nextVersion();
                            while (versions.hasNext()) {
                                history.removeVersion(versions.nextVersion().getName());
                            }
                        }
                    }
                }
                session.save();
                return null;
            }
        });
    }

    /**
     * Verifies the existence of the mix:versionable and adds it if not.
     */
    protected void checkAndAddMixin(Node node) throws RepositoryException {
        if(!node.isNodeType("mix:versionable")){
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
    protected Rule getUsedFilter(Node versionedNode) throws IOException, ClassNotFoundException, RepositoryException {
        // if restored, update original node with the restored node and its subtree
        ByteArrayInputStream inStream = null;
        try {
            String ruleString = this.getSystemNode(versionedNode).getProperty(PROPERTY_RULE).getString();
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
    protected synchronized Node getSystemNode(Node node) throws RepositoryException {
        if (node.hasNode(SYSTEM_NODE)) {
            return node.getNode(SYSTEM_NODE);
        }
        return node.addNode(SYSTEM_NODE, ItemType.SYSTEM.getSystemName());
    }

    /**
     * Get version store hierarchy manager.
     * @throws RepositoryException
     * @throws LoginException
     */
    protected Session getSession() throws LoginException, RepositoryException {
        return MgnlContext.getJCRSession(VersionManager.VERSION_WORKSPACE);
    }
}
