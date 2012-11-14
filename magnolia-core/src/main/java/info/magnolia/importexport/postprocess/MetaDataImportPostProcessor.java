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
package info.magnolia.importexport.postprocess;

import java.util.HashMap;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import info.magnolia.jcr.MgnlNodeTypeNames;
import info.magnolia.jcr.MgnlPropertyNames;
import info.magnolia.jcr.util.NodeUtil;

/**
 * Transforms the MetaData sub node into properties on the mixins <code>mgnl:renderable</code> and
 * <code>mgnl:activatable</code>. The MetaData node itself is removed.
 */
public class MetaDataImportPostProcessor implements ImportPostProcessor {

    private static final String METADATA_NODE_NAME = "MetaData";
    private static final String METADATA_NODE_TYPE = MgnlNodeTypeNames.METADATA;

    private final HashMap<String, String> propertyNameMapping = new HashMap<String, String>();

    public MetaDataImportPostProcessor() {
        propertyNameMapping.put("mgnl:creationdate", MgnlPropertyNames.CREATED);
        propertyNameMapping.put("mgnl:lastaction", MgnlPropertyNames.LAST_ACTIVATED);
        propertyNameMapping.put("mgnl:activatorid", MgnlPropertyNames.LAST_ACTIVATED_BY);
        propertyNameMapping.put("mgnl:activated", MgnlPropertyNames.ACTIVATION_STATUS);
        propertyNameMapping.put("mgnl:template", MgnlPropertyNames.TEMPLATE);
        propertyNameMapping.put("mgnl:authorid", MgnlPropertyNames.LAST_MODIFIED_BY);
        propertyNameMapping.put("mgnl:lastmodified", MgnlPropertyNames.LAST_MODIFIED);
    }

    @Override
    public void postProcessNode(Node node) throws RepositoryException {

        // Transfer properties from the MetaData node
        if (node.hasNode(METADATA_NODE_NAME)) {
            Node metaDataNode = node.getNode(METADATA_NODE_NAME);
            if (NodeUtil.isNodeType(metaDataNode, METADATA_NODE_TYPE)) {
                moveProperties(node, metaDataNode, propertyNameMapping);
            }

//            metaDataNode.remove();
        }

        // Iterate recursively through the sub tree
        NodeIterator children = node.getNodes();
        while (children.hasNext()) {
            Node child = children.nextNode();
            if (!(child.getName().equals(METADATA_NODE_NAME) && NodeUtil.isNodeType(child, METADATA_NODE_TYPE))) {
                postProcessNode(child);
            }
        }
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
     * @throws javax.jcr.PathNotFoundException if the source property does not exist
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
