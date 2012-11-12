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

import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.util.NodeUtil;

/**
 * Transforms the MetaData sub node into properties on the mixins <code>mgnl:renderable</code> and
 * <code>mgnl:activatable</code>. The MetaData node itself is removed.
 */
public class MetaDataImportPostProcessor implements ImportPostProcessor {

    private final HashMap<String, String> propertyNameMapping = new HashMap<String, String>();

    public MetaDataImportPostProcessor() {
        propertyNameMapping.put("mgnl:creationdate", "mgnl:created");
        propertyNameMapping.put("mgnl:lastaction", "mgnl:lastActivated");
        propertyNameMapping.put("mgnl:activatorid", "mgnl:lastActivatedBy");
        propertyNameMapping.put("mgnl:activated", "mgnl:activationStatus");
        propertyNameMapping.put("mgnl:template", "mgnl:template");
        propertyNameMapping.put("mgnl:authorid", "jcr:lastModifiedBy");
        propertyNameMapping.put("mgnl:lastmodified", "jcr:lastModified");
    }

    @Override
    public void postProcessNode(Node node) throws RepositoryException {

        // Transfer properties from the MetaData node
        if (node.hasNode(MetaData.DEFAULT_META_NODE)) {
            Node metaDataNode = node.getNode(MetaData.DEFAULT_META_NODE);
            if (NodeUtil.isNodeType(metaDataNode, MgnlNodeType.NT_METADATA)) {
                moveProperties(node, metaDataNode, propertyNameMapping);
            }

//            metaDataNode.remove();
        }

        // Iterate recursively through the sub tree
        NodeIterator children = node.getNodes();
        while (children.hasNext()) {
            Node child = children.nextNode();
            if (!(child.getName().equals(MetaData.DEFAULT_META_NODE) && NodeUtil.isNodeType(child, MgnlNodeType.NT_METADATA))) {
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
            moveProperty(srcNode, entry.getKey(), dstNode, entry.getValue());
        }
    }

    /**
     * Moves a property from a node to another node and changes its name in the process. If a property already exists on
     * the destination node it will not be overwritten.
     *
     * @param srcNode         node containing the property
     * @param srcPropertyName name of the property
     * @param dstNode         node to which the property should be moved
     * @param dstPropertyName new name after the move
     * @throws RepositoryException
     */
    private void moveProperty(Node srcNode, String srcPropertyName, Node dstNode, String dstPropertyName) throws RepositoryException {
        if (!dstNode.hasProperty(dstPropertyName) && srcNode.hasProperty(srcPropertyName)) {
            Property srcProperty = srcNode.getProperty(srcPropertyName);
            if (srcProperty.isMultiple()) {
                dstNode.setProperty(dstPropertyName, srcProperty.getValues());
            } else {
                dstNode.setProperty(dstPropertyName, srcProperty.getValue());
            }
            srcProperty.remove();
        }
    }
}
