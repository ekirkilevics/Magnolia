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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.beans.runtime.MgnlContext;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author Sameer Charles
 * $Id$
 * Utility class to copy nodes using specified Roles to the magnolia specific version store
 */
public class CopyUtil {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CopyUtil.class);

    /**
     * singleton instance
     * */
    private static CopyUtil thisInstance = new CopyUtil();

    /**
     * private class
     * */
    private CopyUtil() {
    }

    /**
     * get instance
     * */
    static CopyUtil getInstance() {
        return thisInstance;
    }

    /**
     * copy given node to the version store using specified filter
     * @param source
     * @param filter
     * */
    void copyToversion(Content source, Content.ContentFilter filter)
            throws RepositoryException {
        // first check if the node already exist
        Content root;
        try {
            root = this.getHierarchyManager().getContent(source.getUUID());
            this.removeProperties(root);
            // copy root properties
            this.updateProperties(source, root);
        } catch (PathNotFoundException e) {
            // create root for this versionable node
            root = this.getHierarchyManager().createContent("", source.getUUID(), source.getNodeType().getName());
            // copy root properties
            this.updateProperties(source, root);
            root.save();
        }
        // copy all child nodes
        Iterator children = source.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            this.clone(child, root, filter, true);
        }
        this.removeNonExistingChildNodes(source, root, filter);
    }

    /**
     * copy source to destination using the provided filter
     * @param source node in version store
     * @param destination which needs to be restored
     * @param filter this must be the same filter as used while creating this version
     * */
    void copyFromVersion(Content source, Content destination, Content.ContentFilter filter)
            throws RepositoryException {
        // merge top node properties
        this.removeProperties(destination);
        this.updateProperties(source, destination);
        // copy all nodes from version store
        this.copyAllChildNodes(source, destination, filter);
        /*
        // remove properties created on version
        if (destination.hasMetaData()) {
            MetaData metaData = destination.getMetaData();
            if (metaData.hasProperty(MetaData.VERSION_USER))
                metaData.removeProperty(MetaData.VERSION_USER);
            if (metaData.hasProperty(MetaData.NAME))
                metaData.removeProperty(MetaData.NAME);
        }
        */
        // remove all non existing nodes
        this.removeNonExistingChildNodes(source, destination, filter);
    }

    /**
     * recursively removes all child nodes from node using specified filter
     * @param source
     * @param destination
     * @param filter
     * */
    private void removeNonExistingChildNodes(Content source, Content destination ,Content.ContentFilter filter)
            throws RepositoryException {
        // collect all uuids from the source node hierarchy using the given filter
        Iterator children = destination.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            // check if this child exist in source, if not remove it
            if (child.getJCRNode().getDefinition().isAutoCreated()) continue;
            try {
                source.getJCRNode().getSession().getNodeByUUID(child.getUUID());
                // if exist its ok, recursively remove all sub nodes
                this.removeNonExistingChildNodes(source, child, filter);
            } catch (ItemNotFoundException e) {
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
     * copy all child nodes from node1 to node2
     * @param node1
     * @param node2
     * @param filter
     * */
    private void copyAllChildNodes(Content node1, Content node2, Content.ContentFilter filter)
            throws RepositoryException {
        Iterator children = node1.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            this.clone(child, node2, filter, false);
        }
    }

    /**
     * clone
     * @param node
     * @param parent
     * @param filter
     * @param removeExisting
     * */
    private void clone(Content node, Content parent, Content.ContentFilter filter, boolean removeExisting)
            throws RepositoryException {
        try {
            // it seems to be a bug in jackrabbit - cloning does not work if the node with the same uuid
            // exist, "removeExisting" has no effect
            // if node exist with the same UUID, simply update non propected properties
            Content existingNode =
                    getHierarchyManager(parent.getWorkspace().getName()).getContentByUUID(node.getUUID());
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
                this.clone((Content)children.next(), existingNode, filter, removeExisting);
            }
        } catch (ItemNotFoundException e) {
            // its safe to clone if UUID does not exist in this workspace
            this.clone(node, parent);
        }
    }

    /**
     * clone
     * @param node
     * @param parent
     * */
    private void clone(Content node, Content parent) throws RepositoryException {
        if (node.getJCRNode().getDefinition().isAutoCreated()) {
            Content destination = parent.getContent(node.getName());
            this.removeProperties(destination);
            this.updateProperties(node, destination);
        } else {
            parent.getWorkspace().
                clone(node.getWorkspace().getName(), node.getHandle(), parent.getHandle()+"/"+node.getName(), true);
        }
    }


    /**
     * remove all properties under the given node
     * @param node
     * */
    private void removeProperties(Content node) throws RepositoryException {
        PropertyIterator properties = node.getJCRNode().getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            try {
                property.remove();
            } catch (ConstraintViolationException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Property "+property.getName()+" is a reserved property");
                }
            }
        }
    }

    /**
     * import while preserving UUID, parameters supplied must be from separate workspaces
     * @param parent under which the specified node will be imported
     * @param node
     * @throws RepositoryException
     * @throws IOException if failed to import or export
     * */
    private void importNode(Content parent, Content node) throws RepositoryException, IOException {
        File file = File.createTempFile("mgnl",null,Path.getTempDirectory());
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
     * merge all non reserved properties
     * @param source
     * @param destination
     * */
    private void updateProperties(Content source, Content destination)
            throws RepositoryException {
        Node sourceNode = source.getJCRNode();
        Node destinationNode = destination.getJCRNode();
        PropertyIterator properties = sourceNode.getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            // exclude system property Rule and Version specific properties which were created on version
            if (property.getName().equalsIgnoreCase(VersionManager.PROPERTY_RULE)) continue;
            try {
                if (property.getDefinition().isProtected()) continue;
                if (property.getType() == PropertyType.REFERENCE) {
                    // first check for the referenced node existence
                    try {
                        getHierarchyManager(destination.getWorkspace().getName())
                                .getContentByUUID(property.getString());
                    } catch (ItemNotFoundException e) {
                        if (!StringUtils.equalsIgnoreCase(
                                destination.getWorkspace().getName(),
                                VersionManager.VERSION_WORKSPACE)) throw e;
                        // get referenced node under temporary store
                        // use jcr import, there is no other way to get a node without sub hierarchy
                        Content referencedNode = getHierarchyManager(source.getWorkspace().getName())
                                .getContentByUUID(property.getString());
                        try {
                            this.importNode(getTemporaryPath(), referencedNode);
                        } catch (IOException ioe) {
                            log.error("Failed to import referenced node", ioe);
                        }
                    }
                }
                if (property.getDefinition().isMultiple()) {
                    destinationNode.setProperty(property.getName(), property.getValues());
                } else {
                    destinationNode.setProperty(property.getName(), property.getValue());
                }
            } catch (ConstraintViolationException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Property "+property.getName()+" is a reserved property");
                }
            }
        }
    }

    /**
     * get version store hierarchy manager
     * */
    private HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(VersionManager.VERSION_WORKSPACE);
    }

    /**
     * get hierarchy manager of the specified workspace
     * @param workspaceId
     * */
    private HierarchyManager getHierarchyManager(String workspaceId) {
        return MgnlContext.getHierarchyManager(workspaceId);
    }

    /**
     * get temporary node
     * */
    private Content getTemporaryPath() throws RepositoryException {
        return getHierarchyManager().getContent("/"+VersionManager.TMP_REFERENCED_NODES);
    }
}



