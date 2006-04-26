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
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;

/**
 * @author Sameer Charles
 * $Id$
 * Utility class to copy nodes using specified Roles to the magnolia specific version store
 * todo - use inter workspace cloning instead of xml import/export
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
            root = this.getHierarchyManager().getContentByUUID(source.getUUID());
            // clean up first, leaving the top node "root" intact
            NodeIterator subNodes = root.getJCRNode().getNodes();
            while (subNodes.hasNext()) {
                this.depthFirstRemoval(subNodes.nextNode());
            }
            // copy root properties
            this.removeProperties(root);
            this.updateProperties(source, root);
            // copy all child nodes
            Iterator children = source.getChildren(filter).iterator();
            while (children.hasNext()) {
                Content child = (Content) children.next();
                this.copyToVersion(child, root, filter);
            }
        } catch (ItemNotFoundException e) {
            root = this.getHierarchyManager().getRoot();
            this.copyToVersion(source, root, filter);
        }
    }

    /**
     * recursively copy all nodes under the source according to the given content filter
     * @param source
     * @param parent
     * @param filter
     * */
    private void copyToVersion(Content source, Content parent, Content.ContentFilter filter)
            throws RepositoryException {
        Content newNode = this.safeCopy(source, parent);
        Iterator children = source.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            copyToVersion(child, newNode, filter);
        }
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
        List uuidList = new ArrayList();
        this.copyAllChildNodes(source, destination, filter, uuidList);
        // remove all non existing nodes
        this.removeNonExistingChildNodes(destination, filter, uuidList);
    }

    /**
     * recursively removes all child nodes from node using specified filter
     * @param node
     * @param filter
     * */
    private void removeNonExistingChildNodes(Content node, Content.ContentFilter filter, List uuidList)
            throws RepositoryException {
        Iterator children = node.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            if (!uuidList.contains(child.getUUID())) {
                child.delete();
            }
        }
    }

    /**
     * copy all child nodes from node1 to node2
     * @param node1
     * @param node2
     * @param filter
     * @param uuidList of UUID of copied nodes
     * */
    private void copyAllChildNodes(Content node1, Content node2, Content.ContentFilter filter, List uuidList)
            throws RepositoryException {
        Iterator children = node1.getChildren(filter).iterator();
        while (children.hasNext()) {
            Content child = (Content) children.next();
            Content copiedNode = this.safeCopy(child, node2);
            uuidList.add(child.getUUID());
            this.copyAllChildNodes(child, copiedNode, filter, uuidList);
        }
    }

    /**
     * clone all properties of this node and add referenced property
     * @param node
     * @param parent
     * @return newly copied content
     * */
    private Content safeCopy(Content node, Content parent) throws RepositoryException {
        try {
            this.importNode(parent, node);
            return parent.getContent(node.getName());
        } catch (IOException e) {
            throw new RepositoryException(e);
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
     * remove nodes from bottom -> top
     * this methods makes sure that nodes which are referenced from anywhere in a workspace are preserved
     * before nodes are imported, otherwise we could end up with nodes which could never be versioned
     * @param node to be removed or preserved in tmp workspace
     * */
    private void depthFirstRemoval(Node node) throws RepositoryException {
        if (node.hasNodes()) {
            NodeIterator nodeIterator = node.getNodes();
            while (nodeIterator.hasNext()) {
                this.depthFirstRemoval(nodeIterator.nextNode());
            }
        }
        long references = 0;
        try {
            references = node.getReferences().getSize();
        } catch (RepositoryException re) {
            // this could happen if the node is non referenceable
            log.error(re.getMessage());
            log.debug(re.getMessage(), re);
        }
        if (references > 0) {
            node.getSession().move(node.getPath(), VersionManager.TMP_REFERENCED_NODES+"/"+node.getName());
        } else {
            try {
                node.remove();
            } catch (RepositoryException re) {
                // if cant be removed for some reason it will be removed on node import with the same uuid
                re.printStackTrace();
                log.debug(re.getMessage(), re);
                node.getSession().move(node.getPath(), VersionManager.TMP_REFERENCED_NODES+"/"+node.getName());
            }
        }
    }


    /**
     * merge all non reserved properties
     * @param source
     * @param destination
     * */
    private void updateProperties(Content source, Content destination) throws RepositoryException {
        Node sourceNode = source.getJCRNode();
        Node destinationNode = destination.getJCRNode();

        PropertyIterator properties = sourceNode.getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            try {
                if (property.getDefinition().isMultiple()) {
                    destinationNode.setProperty(property.getName(), property.getValues());
                } else {
                    destinationNode.setProperty(property.getName(), property.getValue());
                }
                destinationNode.setProperty(property.getName(), property.getValue());
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
}



