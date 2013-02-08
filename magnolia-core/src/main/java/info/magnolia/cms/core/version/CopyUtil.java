/**
 * This file Copyright (c) 2003-2012 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.Path;
import info.magnolia.context.MgnlContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.FilteringNodeIterator;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to copy nodes and hierarchies between workspaces. A {@link Content.ContentFilter} defines what such a copy process includes.
 * This is used to copy pages to the version workspace. While the paragraph nodes have to be copied the sub-pages should not.
 */
public final class CopyUtil {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CopyUtil.class);

    private static final CopyUtil thisInstance = new CopyUtil();

    private CopyUtil() {
    }

    public static CopyUtil getInstance() {
        return thisInstance;
    }

    /**
     * Copy given node to the version store using specified filter.
     * @param source
     * @param filter
     */
    void copyToversion(Node source, Predicate filter) throws RepositoryException {
        // first check if the node already exist
        Node root = null;
        try {
            root = getVersionableRoot(source);

            if (root != null) {
                updateVersionableRoot(source, root);
                root.getSession().save();
            }
        }
        catch (ItemNotFoundException e) {
            // root is null, will import it below
        }
        if (root == null) {
            // create root for this versionable node
            try {
                this.importNode(this.getSession().getRootNode(), source);
            }
            catch (IOException ioe) {
                throw new RepositoryException("Failed to import node in magnolia version store : " + ioe.getMessage());
            }
            // we have imported versionable root above so it can't be null now
            root = getVersionableRoot(source);
            // and there's no need to update its properties

            // persist everything (just to be sure)
            getSession().save();

        }
        // copy all child nodes that need to be versioned under this root
        NodeIterator children = new FilteringNodeIterator(source.getNodes(), filter);
        while (children.hasNext()) {
            Node child = children.nextNode();
            this.clone(child, root, filter, true);
        }
        this.removeNonExistingChildNodes(source, root, filter);
    }

    private void updateVersionableRoot(Node source, Node root) throws RepositoryException, ItemNotFoundException, AccessDeniedException, ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException {
        // if root exists already, but only as a temporary node, we might have to move it up first
        if (root.getParent().getName().equalsIgnoreCase(VersionManager.TMP_REFERENCED_NODES)) {
            root.getSession().move(root.getPath(), "/" + root.getName());
        }
        // and make sure it has correct set of props
        this.removeProperties(root);
        this.updateProperties(source, root);
        // and correct node types
        this.updateNodeTypes(source, root);
    }

    private Node getVersionableRoot(Node source) throws RepositoryException, ItemNotFoundException, LoginException, UnsupportedRepositoryOperationException, PathNotFoundException {
        Node root = null;
        if (source.isNodeType(MgnlNodeType.MIX_REFERENCEABLE)) {
            root = this.getSession().getNodeByIdentifier(source.getIdentifier());
        } else {
            Session session = this.getSession();
            if (session.itemExists("/" + source.getName())) {
                root = session.getNode("/" + source.getName());
            } else if (session.itemExists("/" + VersionManager.TMP_REFERENCED_NODES + "/" + source.getName())) {
                root = session.getNode("/" + VersionManager.TMP_REFERENCED_NODES + "/" + source.getName());
            }
        }
        return root;
    }

    private void updateNodeTypes(Node source, Node root) throws RepositoryException {
        List<String> targetNodeTypes = new ArrayList<String>();
        for (NodeType t : root.getMixinNodeTypes()) {
            targetNodeTypes.add(t.getName());
        }
        NodeType[] nodeTypes = source.getMixinNodeTypes();
        for (NodeType type : nodeTypes) {
            root.addMixin(type.getName());
            targetNodeTypes.remove(type.getName());
        }
        // remove all mixins not found in the original except MIX_VERSIONABLE
        for (String nodeType : targetNodeTypes) {
            if (MgnlNodeType.MIX_VERSIONABLE.equals(nodeType)) {
                continue;
            }
            root.removeMixin(nodeType);
        }
    }

    /**
     * Copy source to destination using the provided filter.
     * @param source node in version store
     * @param destination which needs to be restored
     * @param filter this must be the same filter as used while creating this version
     */
    void copyFromVersion(Node source, Node destination, Predicate filter) throws RepositoryException {
        // merge top node properties
        this.removeProperties(destination);
        this.updateProperties(source, destination);
        // copy all nodes from version store
        this.copyAllChildNodes(source, destination, filter);
        // remove all non existing nodes
        this.removeNonExistingChildNodes(source, destination, filter);

        this.removeNonExistingMixins(source, destination);
    }

    private void removeNonExistingMixins(Node source, Node destination) throws RepositoryException {
        List<String> destNodeTypes = new ArrayList<String>();
        // has to match mixin names as mixin instances to not equal()
        for (NodeType nt : destination.getMixinNodeTypes()) {
            destNodeTypes.add(nt.getName());
        }
        // remove all that still exist in source
        for (NodeType nt :source.getMixinNodeTypes()) {
            destNodeTypes.remove(nt.getName());
        }
        // un-mix the rest
        for (String type : destNodeTypes) {
            destination.removeMixin(type);
        }
    }

    /**
     * Recursively removes all child nodes from node using specified filter.
     */
    private void removeNonExistingChildNodes(Node source, Node destination, Predicate filter)
            throws RepositoryException {
        // collect all identifiers from the source node hierarchy using the given filter
        NodeIterator children = new FilteringNodeIterator(destination.getNodes(), filter);
        while (children.hasNext()) {
            Node child = children.nextNode();
            // check if this child exist in source, if not remove it
            if (child.getDefinition().isAutoCreated()) {
                continue;
            }
            try {
                if (child.isNodeType(JcrConstants.MIX_REFERENCEABLE)) {
                    source.getSession().getNodeByIdentifier(child.getIdentifier());
                } else {
                    source.getNode(child.getName());
                }
                // if exist its ok, recursively remove all sub nodes
                this.removeNonExistingChildNodes(source, child, filter);
            }
            catch (ItemNotFoundException e) {
                PropertyIterator referencedProperties = child.getReferences();
                if (referencedProperties.getSize() > 0) {
                    // remove all referenced properties, its safe since source workspace cannot have these
                    // properties if node with this UUID does not exist
                    while (referencedProperties.hasNext()) {
                        referencedProperties.nextProperty().remove();
                    }
                }
                child.remove();
            } catch (PathNotFoundException e) {
                PropertyIterator referencedProperties = child.getReferences();
                if (referencedProperties.getSize() > 0) {
                    // remove all referenced properties, its safe since source workspace cannot have
                    // these
                    // properties if node with this UUID does not exist
                    while (referencedProperties.hasNext()) {
                        referencedProperties.nextProperty().remove();
                    }
                }
                child.remove();
            }
        }
    }

    /**
     * Copy all child nodes from source to destination.
     */
    private void copyAllChildNodes(Node source, Node destination, Predicate filter) throws RepositoryException {
        NodeIterator children = new FilteringNodeIterator(source.getNodes(), filter);
        while (children.hasNext()) {
            Node child = children.nextNode();
            this.clone(child, destination, filter, false);
        }
    }

    public void clone(Node sourceNode, Node destinationParent, Predicate filter, boolean removeExisting) throws RepositoryException {
        try {
            // it seems to be a bug in jackrabbit - cloning does not work if the node with the same identifier
            // exist, "removeExisting" has no effect if node exist with the same UUID, simply update non protected properties
            String workspaceName = ContentRepository.getInternalWorkspaceName(destinationParent.getSession().getWorkspace().getName());
            Node existingNodeAtDestination = null;
            if (sourceNode.isNodeType(MgnlNodeType.MIX_REFERENCEABLE)) {
                existingNodeAtDestination = getSession(workspaceName).getNodeByIdentifier(sourceNode.getIdentifier());
                // ok, we found matching node at destination ... have to deal with it below
            }
            if (removeExisting) {
                if (existingNodeAtDestination != null) {
                    // remove matching node at destination
                    existingNodeAtDestination.remove();
                    // persist removal at destination
                    destinationParent.getSession().save();
                }
                this.clone(sourceNode, destinationParent);
                return;
            }
            if (existingNodeAtDestination == null) {
                // Will end up w/ PNFE in case node doesn't exist. Using exception for flow handling
                // is not great, but we can't avoid it since same situation can occur also when
                // checking by UUID and there's no test for existing UUID :(
                existingNodeAtDestination = destinationParent.getNode(sourceNode.getName());
            }
            // when we get here, we are not removing matching child at destination but updating it's props and children instead
            // this is the case for restoring versions as we do not want to accidentally remove any non versioned sub nodes (e.g. sub pages or sub types in case of data module)
            this.removeProperties(existingNodeAtDestination);
            this.updateProperties(sourceNode, existingNodeAtDestination);
            //persist update of existing child at destination
            destinationParent.getSession().save();
            NodeIterator children = new FilteringNodeIterator(sourceNode.getNodes(), filter);
            while (children.hasNext()) {
                this.clone(children.nextNode(), existingNodeAtDestination, filter, removeExisting);
            }
        }
        catch (ItemNotFoundException e) {
            // clone the node later
        } catch (PathNotFoundException e) {
            // clone the node later
        }
        // its safe to clone if UUID does not exist in this workspace
        this.clone(sourceNode, destinationParent);
    }

    private void clone(Node node, Node parent) throws RepositoryException {
        if (node.getDefinition().isAutoCreated()) {
            Node destination = parent.getNode(node.getName());
            this.removeProperties(destination);
            this.updateProperties(node, destination);
        }
        else {
            String parH = parent.getPath();
            log.debug("workspace level clone from {}:{} to {}:{}", new Object[] { node.getSession().getWorkspace().getName(), node.getPath(), parent.getSession().getWorkspace().getName(), parent.getPath() });
            parent.getSession().getWorkspace().clone(
                    node.getSession().getWorkspace().getName(),
                    node.getPath(),
                    parH + (parH != null && parH.endsWith("/") ? "" :"/") + node.getName(),
                    true);
        }
    }

    /**
     * Remove all properties under the given node.
     */
    private void removeProperties(Node node) throws RepositoryException {
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            if (property.getDefinition().isProtected() || property.getDefinition().isMandatory()) {
                continue;
            }
            try {
                property.remove();
            }
            catch (ConstraintViolationException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Property " + property.getName() + " is a reserved property");
                }
            }
        }
    }

    /**
     * Import while preserving UUID, parameters supplied must be from separate workspaces.
     * @param parent under which the specified node will be imported
     * @param node
     * @throws RepositoryException
     * @throws IOException if failed to import or export
     */
    private void importNode(Node parent, Node node) throws RepositoryException, IOException {
        File file = File.createTempFile("mgnl", null, Path.getTempDirectory());
        FileOutputStream outStream = new FileOutputStream(file);
        node.getSession().exportSystemView(node.getPath(), outStream, false, true);
        outStream.flush();
        IOUtils.closeQuietly(outStream);
        FileInputStream inStream = new FileInputStream(file);
        parent.getSession().getWorkspace().getSession().importXML(
                parent.getPath(),
                inStream,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
        IOUtils.closeQuietly(inStream);
        file.delete();
    }

    /**
     * Merge all non reserved properties.
     */
    private void updateProperties(Node source, Node destination) throws RepositoryException {
        PropertyIterator properties = source.getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            // exclude system property Rule and Version specific properties which were created on version
            if (property.getName().equalsIgnoreCase(VersionManager.PROPERTY_RULE)) {
                continue;
            }
            try {
                if (property.getDefinition().isProtected()) {
                    continue;
                }
                if ("jcr:isCheckedOut".equals(property.getName())) {
                    // do not attempt to restore isCheckedOut property as it makes no sense to restore versioned node with
                    // checkedOut status and value for this property might not be set even though the property itself is set.
                    // Since JCR-1272 attempt to restore the property with no value will end up with RepositoryException instead
                    // of ConstraintViolationException and hence will not be caught by the catch{} block below.
                    continue;
                }
                if (property.getType() == PropertyType.REFERENCE) {
                    // first check for the referenced node existence
                    try {
                        getSession(destination.getSession().getWorkspace().getName())
                        .getNodeByIdentifier(property.getString());
                    }
                    catch (ItemNotFoundException e) {
                        if (!StringUtils.equalsIgnoreCase(
                                destination.getSession().getWorkspace().getName(),
                                VersionManager.VERSION_WORKSPACE)) {
                            throw e;
                        }
                        // get referenced node under temporary store
                        // use jcr import, there is no other way to get a node without sub hierarchy
                        Node referencedNode = getSession(source.getSession().getWorkspace().getName()).getNodeByIdentifier(
                                property.getString());
                        try {
                            this.importNode(getTemporaryPath(), referencedNode);
                            this.removeProperties(getSession().getNodeByIdentifier(property.getString()));
                            getTemporaryPath().getSession().save();
                        }
                        catch (IOException ioe) {
                            log.error("Failed to import referenced node", ioe);
                        }
                    }
                }
                if (property.getDefinition().isMultiple()) {
                    destination.setProperty(property.getName(), property.getValues());
                }
                else {
                    destination.setProperty(property.getName(), property.getValue());
                }
            }
            catch (ConstraintViolationException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Property " + property.getName() + " is a reserved property");
                }
            }
        }
    }

    /**
     * Get version store session.
     * @throws RepositoryException
     * @throws LoginException
     */
    private Session getSession() throws LoginException, RepositoryException {
        return MgnlContext.getJCRSession(VersionManager.VERSION_WORKSPACE);
    }

    /**
     * Get session of the specified workspace.
     * @param workspaceId
     * @throws RepositoryException
     * @throws LoginException
     */
    private Session getSession(String workspaceId) throws LoginException, RepositoryException {
        return MgnlContext.getJCRSession(workspaceId);
    }

    /**
     * Get temporary node.
     */
    private Node getTemporaryPath() throws RepositoryException {
        return getSession().getNode("/" + VersionManager.TMP_REFERENCED_NODES);
    }
}
