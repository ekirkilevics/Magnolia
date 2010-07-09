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
package info.magnolia.module.rest.tree.commands;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ExclusiveWrite;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

public class SetNodeDataCommand extends AbstractTreeCommand {

    private String nodeDataName;
    private String value;

    public void setNodeDataName(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object execute() throws RepositoryException {

        // This is Tree.saveNodeData()

        synchronized (ExclusiveWrite.getInstance()) {
            Content content = getHierarchyManager().getContent(this.getPath().path());
            NodeData node;
            int type = PropertyType.STRING;
            if (!content.getNodeData(nodeDataName).isExist()) {
                node = content.createNodeData(nodeDataName);
            } else {
                node = content.getNodeData(nodeDataName);
                type = node.getType();
            }
            // todo: share with Contorol.Save
            if (node.isMultiValue() != NodeData.MULTIVALUE_TRUE) {
                switch (type) {
                    case PropertyType.STRING:
                        node.setValue(value);
                        break;
                    case PropertyType.BOOLEAN:
                        if (value.equals("true")) { //$NON-NLS-1$
                            node.setValue(true);
                        } else {
                            node.setValue(false);
                        }
                        break;
                    case PropertyType.DOUBLE:
                        try {
                            node.setValue(Double.valueOf(value).doubleValue());
                        }
                        catch (Exception e) {
                            node.setValue(0);
                        }
                        break;
                    case PropertyType.LONG:
                        try {
                            node.setValue(Long.valueOf(value).longValue());
                        }
                        catch (Exception e) {
                            node.setValue(0);
                        }
                        break;
                    case PropertyType.DATE:
                        // todo
                        break;
                }
            }
            content.updateMetaData();
            content.save();
            return content;
        }
    }
}
