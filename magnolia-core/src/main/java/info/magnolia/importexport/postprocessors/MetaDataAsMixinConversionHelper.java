/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.importexport.postprocessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;

/**
 * Converts the MetaData sub node into properties on the mixins <code>mgnl:created</code>,
 * <code>mgnl:lastModified</code>, <code>mgnl:renderable</code>, <code>mgnl:activatable</code> and
 * <code>mgnl:activatable</code>. It also renames the property <code>mgnl:deletedOn</code> property on the mixin
 * <code>mgnl:deleted</code> to <code>mgnl:deleted</code>. The MetaData node itself is optionally removed if there are
 * no additional properties on it.
 */
public class MetaDataAsMixinConversionHelper {

    private static final int PERIODIC_SAVE_FREQUENCY = 20;
    private static final String DEPRECATED_DELETION_DATE_PROPERTY_NAME = "mgnl:deletedOn";

    private final Logger logger = LoggerFactory.getLogger(MetaDataAsMixinConversionHelper.class);

    private final HashMap<String, String> propertyNameMapping = new HashMap<String, String>();

    private boolean deleteMetaDataIfEmptied = false;
    private boolean periodicSaves = false;

    public MetaDataAsMixinConversionHelper() {
        propertyNameMapping.put(NodeTypes.MGNL_PREFIX + MetaData.CREATION_DATE, NodeTypes.Created.CREATED);
        propertyNameMapping.put(NodeTypes.MGNL_PREFIX + MetaData.LAST_ACTION, NodeTypes.Activatable.LAST_ACTIVATED);
        propertyNameMapping.put(NodeTypes.MGNL_PREFIX + MetaData.ACTIVATOR_ID, NodeTypes.Activatable.LAST_ACTIVATED_BY);
        propertyNameMapping.put(NodeTypes.MGNL_PREFIX + MetaData.ACTIVATED, NodeTypes.Activatable.ACTIVATION_STATUS);
        propertyNameMapping.put(NodeTypes.MGNL_PREFIX + MetaData.TEMPLATE, NodeTypes.Renderable.TEMPLATE);
        propertyNameMapping.put(NodeTypes.MGNL_PREFIX + MetaData.LAST_MODIFIED, NodeTypes.LastModified.LAST_MODIFIED);
        propertyNameMapping.put(NodeTypes.MGNL_PREFIX + MetaData.AUTHOR_ID, NodeTypes.LastModified.LAST_MODIFIED_BY);
        propertyNameMapping.put("mgnl:comment", NodeTypes.Versionable.COMMENT);
    }

    public boolean isDeleteMetaDataIfEmptied() {
        return deleteMetaDataIfEmptied;
    }

    public void setDeleteMetaDataIfEmptied(boolean deleteMetaDataIfEmptied) {
        this.deleteMetaDataIfEmptied = deleteMetaDataIfEmptied;
    }

    public boolean isPeriodicSaves() {
        return periodicSaves;
    }

    /**
     * Sets whether to save periodically as the sub tree is converted. This reduces the amount of memory required.
     */
    public void setPeriodicSaves(boolean periodicSaves) {
        this.periodicSaves = periodicSaves;
    }

    public void convertNodeAndChildren(Node startNode) throws RepositoryException {

        int nodesProcessed = 0;
        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.add(startNode);

        while (!nodes.isEmpty()) {
            // Take the most recently added node creating a depth-first scan because it has smaller memory footprint
            // than a breadth-first scan.
            Node node = nodes.remove(nodes.size() - 1);

            processNode(node);

            // Save the session every x nodes if period saves is enabled
            nodesProcessed++;
            if (periodicSaves && nodesProcessed % PERIODIC_SAVE_FREQUENCY == 0) {
                node.getSession().save();
            }

            // Queue child nodes
            NodeIterator children = node.getNodes();
            while (children.hasNext()) {
                Node child = children.nextNode();
                if (!(child.getName().equals(MetaData.DEFAULT_META_NODE) && NodeUtil.isNodeType(child, MgnlNodeType.NT_METADATA))) {
                    nodes.add(child);
                }
            }
        }
    }

    private void processNode(Node node) throws RepositoryException {

        // Rename mgnl:deletedOn to mgnl:deleted for mixin mgnl:deleted
        if (node.hasProperty(DEPRECATED_DELETION_DATE_PROPERTY_NAME)) {
            moveProperty(node, DEPRECATED_DELETION_DATE_PROPERTY_NAME, node, NodeTypes.Deleted.DELETED);
        }

        // Transfer properties from the MetaData node
        if (node.hasNode(MetaData.DEFAULT_META_NODE)) {
            Node metaDataNode = node.getNode(MetaData.DEFAULT_META_NODE);
            if (NodeUtil.isNodeType(metaDataNode, MgnlNodeType.NT_METADATA)) {
                moveProperties(node, metaDataNode, propertyNameMapping);
            }

            if (deleteMetaDataIfEmptied && isEmptyMetaDataNode(metaDataNode)) {
                metaDataNode.remove();
            }
        }
    }

    /**
     * Returns true if the MetaData node is considered empty, i.e. has no sub nodes and no unexpected properties.
     */
    private boolean isEmptyMetaDataNode(Node node) throws RepositoryException {
        if (node.getNodes().getSize() != 0) {
            logger.warn("MetaData node not removed because it has sub nodes " + node.getPath());
            return false;
        }
        PropertyIterator iterator = node.getProperties();
        while (iterator.hasNext()) {
            Property property = iterator.nextProperty();
            if (!isExpectedMetaDataProperty(property)) {
                logger.warn("MetaData node not removed because of unrecognized property: " + property.getPath());
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the property is expected to be found on the MetaData node after conversion.
     */
    private boolean isExpectedMetaDataProperty(Property property) throws RepositoryException {
        String propertyName = property.getName();

        // We expect there to be standard JCR properties like jcr:createdBy etc
        if (propertyName.startsWith("jcr:")) {
            return true;
        }

        // We're not transferring mgnl:title and mgnl:templatetype so we'll expect those to still be present
        if (propertyName.equals(NodeTypes.MGNL_PREFIX + MetaData.TITLE) || propertyName.equals(NodeTypes.MGNL_PREFIX + MetaData.TEMPLATE_TYPE)) {
            return true;
        }

        // Legacy property deprecated in Magnolia 3.0
        if (propertyName.equals(NodeTypes.MGNL_PREFIX + "Data") && property.getString().equals("MetaData")) {
            return true;
        }

        return false;
    }

    /**
     * Moves a set of properties from one node to another changing their names in the process.
     *
     * @param dstNode      node to move properties to
     * @param srcNode      node to move properties from
     * @param nameMappings maps current property names to their new names
     * @throws RepositoryException
     */
    private void moveProperties(Node dstNode, Node srcNode, Map<String, String> nameMappings) throws RepositoryException {
        for (Map.Entry<String, String> entry : nameMappings.entrySet()) {
            String srcPropertyName = entry.getKey();
            String dstPropertyName = entry.getValue();
            if (!dstNode.hasProperty(dstPropertyName) && srcNode.hasProperty(srcPropertyName)) {
                moveProperty(srcNode, srcPropertyName, dstNode, dstPropertyName);
            }
        }
    }

    /**
     * Moves a property from a node to another node and changes its name in the process. If a property already exists on
     * the destination node it will be overwritten.
     *
     * @param srcNode         node containing the property
     * @param srcPropertyName name of the property
     * @param dstNode         node to which the property should be moved
     * @param dstPropertyName new name after the move
     * @throws RepositoryException
     * @throws javax.jcr.PathNotFoundException
     *                             if the source property does not exist
     */
    private void moveProperty(Node srcNode, String srcPropertyName, Node dstNode, String dstPropertyName) throws RepositoryException {
        Property srcProperty = srcNode.getProperty(srcPropertyName);
        if (srcProperty.isMultiple()) {
            dstNode.setProperty(dstPropertyName, srcProperty.getValues());
        } else {
            dstNode.setProperty(dstPropertyName, srcProperty.getValue());
        }
        srcProperty.remove();
    }
}
