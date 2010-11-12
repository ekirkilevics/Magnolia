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
import info.magnolia.cms.core.Path;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.context.MgnlContext;
import info.magnolia.cms.core.HierarchyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to copy nodes and hierarchies between workspaces. A {@link Content.ContentFilter} defines what such a copy process includes.
 * This is used to copy pages to the version workspace. While the paragraph nodes have to be copied the sub-pages should not.
 *
 * @author Sameer Charles
 * $Id$
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
    void copyToversion(Content source, Content.ContentFilter filter) throws RepositoryException {
        // first check if the node already exist
        Content root;
        try {
            root = this.getHierarchyManager().getContentByUUID(source.getUUID());
            if (root.getParent().getName().equalsIgnoreCase(VersionManager.TMP_REFERENCED_NODES)) {
                root.getJCRNode().getSession().move(root.getHandle(), "/" + root.getName());
            }
            this.removeProperties(root);
            // copy root properties
            this.updateProperties(source, root);

            this.updateNodeTypes(source, root);
            root.save();
        }
        catch (ItemNotFoundException e) {
            // create root for this versionable node
            try {
                this.importNode(this.getHierarchyManager().getRoot(), source);
            }
            catch (IOException ioe) {
                throw new RepositoryException("Failed to import node in magnolia version store : " + ioe.getMessage());
            }
            root = this.getHierarchyManager().getContentByUUID(source.getUUID());
            // copy root properties
            // this.updateProperties(source, root);
            // save parent node since this node is newly created
            getHierarchyManager().getRoot().save();
        }
        // copy all child nodes
        Iterator children = source.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            this.clone(child, root, filter, true);
        }
        this.removeNonExistingChildNodes(source, root, filter);
    }

    private void updateNodeTypes(Content source, Content root) throws RepositoryException {
        NodeType[] nodeTypes = source.getMixinNodeTypes();
        for (NodeType type : nodeTypes) {
            root.addMixin(type.getName());
        }
    }

    /**
     * Copy source to destination using the provided filter.
     * @param source node in version store
     * @param destination which needs to be restored
     * @param filter this must be the same filter as used while creating this version
     */
    void copyFromVersion(Content source, Content destination, Content.ContentFilter filter) throws RepositoryException {
        // merge top node properties
        this.removeProperties(destination);
        this.updateProperties(source, destination);
        // copy all nodes from version store
        this.copyAllChildNodes(source, destination, filter);
        // remove all non existing nodes
        this.removeNonExistingChildNodes(source, destination, filter);

        this.removeNonExistingMixins(source, destination);
    }

    private void removeNonExistingMixins(Content source, Content destination) throws RepositoryException {
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
    private void removeNonExistingChildNodes(Content source, Content destination, Content.ContentFilter filter)
    throws RepositoryException {
        // collect all uuids from the source node hierarchy using the given filter
        Iterator children = destination.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            // check if this child exist in source, if not remove it
            if (child.getJCRNode().getDefinition().isAutoCreated()) {
                continue;
            }
            try {
                source.getJCRNode().getSession().getNodeByUUID(child.getUUID());
                // if exist its ok, recursively remove all sub nodes
                this.removeNonExistingChildNodes(source, child, filter);
            }
            catch (ItemNotFoundException e) {
                PropertyIterator referencedProperties = child.getJCRNode().getReferences();
                if (referencedProperties.getSize() > 0) {
                    // remove all referenced properties, its safe since source workspace cannot have these
                    // properties if node with this UUID does not exist
                    while (referencedProperties.hasNext()) {
                        referencedProperties.nextProperty().remove();
                    }
                }
                child.delete();
            }
        }
    }

    /**
     * Copy all child nodes from node1 to node2.
     */
    private void copyAllChildNodes(Content node1, Content node2, Content.ContentFilter filter)
    throws RepositoryException {
        Iterator children = node1.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            this.clone(child, node2, filter, false);
        }
    }

    public void clone(Content node, Content parent, Content.ContentFilter filter, boolean removeExisting)
    throws RepositoryException {
        try {
            // it seems to be a bug in jackrabbit - cloning does not work if the node with the same uuid
            // exist, "removeExisting" has no effect
            // if node exist with the same UUID, simply update non propected properties
            String workspaceName = ContentRepository.getInternalWorkspaceName(parent.getWorkspace().getName());
            Content existingNode = getHierarchyManager(workspaceName)
            .getContentByUUID(node.getUUID());
            if (removeExisting) {
                existingNode.delete();
                parent.save();
                this.clone(node, parent);
                return;
            }
            this.removeProperties(existingNode);
            this.updateProperties(node, existingNode);
            Iterator children = node.getChildren(filter).iterator();
            while (children.hasNext()) {
                this.clone((Content) children.next(), existingNode, filter, removeExisting);
            }
        }
        catch (ItemNotFoundException e) {
            // its safe to clone if UUID does not exist in this workspace
            this.clone(node, parent);
        }
    }

    private void clone(Content node, Content parent) throws RepositoryException {
        if (node.getJCRNode().getDefinition().isAutoCreated()) {
            Content destination = parent.getContent(node.getName());
            this.removeProperties(destination);
            this.updateProperties(node, destination);
        }
        else {
            String parH = parent.getHandle();
            parent.getWorkspace().clone(
                    node.getWorkspace().getName(),
                    node.getHandle(),
                    parH + (parH != null && parH.endsWith("/") ? "" :"/") + node.getName(),
                    true);
        }
    }

    /**
     * Remove all properties under the given node.
     */
    private void removeProperties(Content node) throws RepositoryException {
        PropertyIterator properties = node.getJCRNode().getProperties();
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
    private void importNode(Content parent, Content node) throws RepositoryException, IOException {
        File file = File.createTempFile("mgnl", null, Path.getTempDirectory());
        FileOutputStream outStream = new FileOutputStream(file);
        node.getWorkspace().getSession().exportSystemView(node.getHandle(), outStream, false, true);
        outStream.flush();
        IOUtils.closeQuietly(outStream);
        FileInputStream inStream = new FileInputStream(file);
        parent.getWorkspace().getSession().importXML(
                parent.getHandle(),
                inStream,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
        IOUtils.closeQuietly(inStream);
        file.delete();
    }

    /**
     * Merge all non reserved properties.
     */
    private void updateProperties(Content source, Content destination) throws RepositoryException {
        Node sourceNode = source.getJCRNode();
        Node destinationNode = destination.getJCRNode();
        PropertyIterator properties = sourceNode.getProperties();
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
                        getHierarchyManager(destination.getWorkspace().getName())
                        .getContentByUUID(property.getString());
                    }
                    catch (ItemNotFoundException e) {
                        if (!StringUtils.equalsIgnoreCase(
                                destination.getWorkspace().getName(),
                                VersionManager.VERSION_WORKSPACE)) {
                            throw e;
                        }
                        // get referenced node under temporary store
                        // use jcr import, there is no other way to get a node without sub hierarchy
                        Content referencedNode = getHierarchyManager(source.getWorkspace().getName()).getContentByUUID(
                                property.getString());
                        try {
                            this.importNode(getTemporaryPath(), referencedNode);
                            this.removeProperties(getHierarchyManager().getContentByUUID(property.getString()));
                            getTemporaryPath().save();
                        }
                        catch (IOException ioe) {
                            log.error("Failed to import referenced node", ioe);
                        }
                    }
                }
                if (property.getDefinition().isMultiple()) {
                    destinationNode.setProperty(property.getName(), property.getValues());
                }
                else {
                    destinationNode.setProperty(property.getName(), property.getValue());
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
     * Get version store hierarchy manager.
     */
    private HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(VersionManager.VERSION_WORKSPACE);
    }

    /**
     * Get hierarchy manager of the specified workspace.
     * @param workspaceId
     */
    private HierarchyManager getHierarchyManager(String workspaceId) {
        return MgnlContext.getHierarchyManager(workspaceId);
    }

    /**
     * Get temporary node.
     */
    private Content getTemporaryPath() throws RepositoryException {
        return getHierarchyManager().getContent("/" + VersionManager.TMP_REFERENCED_NODES);
    }
}
