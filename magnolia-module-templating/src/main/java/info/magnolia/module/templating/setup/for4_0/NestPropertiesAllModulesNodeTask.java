/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.templating.setup.for4_0;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllModulesNodeOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Moves property nodes to new nodetype inside like <code>templates/x</code> will be <code>templates/newnode/x</code>.
 * @author tmiyar
 *
 */
public class NestPropertiesAllModulesNodeTask extends AllModulesNodeOperation {

    private static Logger log = LoggerFactory.getLogger(NestPropertiesAllModulesNodeTask.class);

    private final String baseNodeName;
    private final String newNodeType;
    private final String newNodeName;
    private final List excludePropertiesList;

    public NestPropertiesAllModulesNodeTask(String name, String description, String baseNodeName, List excludePropertiesList, String newNodeName, String newNodeType) {
        super(name, description);
        this.baseNodeName = baseNodeName;
        this.newNodeName = newNodeName;
        this.excludePropertiesList = excludePropertiesList;
        this.newNodeType = newNodeType;
    }


    protected void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx)
            throws RepositoryException, TaskExecutionException {
        try {
            if(node.hasContent(baseNodeName)){
                Content baseNode = node.getContent(baseNodeName);
                if(baseNode.hasChildren(ItemType.CONTENTNODE.getSystemName())) {
                    final Collection children = baseNode.getChildren(ItemType.CONTENTNODE.getSystemName());
                    final Iterator it = children.iterator();
                    Content baseNodeChild;
                    while(it.hasNext()) {
                        baseNodeChild = (Content) it.next();
                        Content newNode;
                        if(baseNodeChild.hasContent(newNodeName) ) {
                            newNode = baseNodeChild.getContent(newNodeName);
                        } else {
                            newNode = baseNodeChild.createContent(newNodeName, newNodeType);
                        }
                        if(baseNodeChild.hasChildren()) {
                            final Collection childrenNodeData = baseNodeChild.getNodeDataCollection();
                            final Iterator it2 = childrenNodeData.iterator();
                            NodeData nodeData;
                            while(it2.hasNext()) {
                                nodeData = (NodeData) it2.next();
                                if(!excludePropertiesList.contains(nodeData.getName())) {
                                    newNode.createNodeData(nodeData.getName(), nodeData.getValue());
                                    baseNodeChild.deleteNodeData(nodeData.getName());
                                }
                            }
                        }
                    }

                }

            }
        }
        catch(RepositoryException e){
            throw e;
        }
        catch (Exception e) {
            throw new TaskExecutionException("can't reconfigure tasks", e);
        }

    }

}


