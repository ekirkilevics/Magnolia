/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllModulesNodeOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Moves property nodes to new nodetype inside like templates/x will be templates/newnode/x
 * @author tmiyar
 *
 */
public class NestPropertiesAllModulesNodeTask extends AllModulesNodeOperation {

    private static Logger log = LoggerFactory.getLogger(NestPropertiesAllModulesNodeTask.class);

    private final String baseNodeName;
    private final String type;
    private final String newNodeName;
    private final List exclude;

    public NestPropertiesAllModulesNodeTask(String name, String description, String baseNodeName, List exclude, String newNodeName, String type) {
        super(name, description);
        this.baseNodeName = baseNodeName;
        this.newNodeName = newNodeName;
        this.exclude = exclude;
        this.type = type;
    }


    protected void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx)
            throws RepositoryException, TaskExecutionException {
        try {
            if(node.hasContent(baseNodeName)){
                Content srcNode = node.getContent(baseNodeName);
                if(srcNode.hasChildren(ItemType.CONTENTNODE.getSystemName())) {
                    final Collection children = srcNode.getChildren(ItemType.CONTENTNODE.getSystemName());
                    final Iterator it = children.iterator();
                    Content nodeTemplate;
                    while(it.hasNext()) {
                        nodeTemplate = (Content) it.next();
                        Content newNode;
                        if(nodeTemplate.hasContent(newNodeName) ) {
                            newNode = nodeTemplate.getContent(newNodeName);
                        } else {
                            newNode = nodeTemplate.createContent(newNodeName, type);
                        }
                        if(nodeTemplate.hasChildren()) {
                            final Collection childrenDatas = nodeTemplate.getNodeDataCollection();
                            final Iterator it2 = childrenDatas.iterator();
                            NodeData nodeData;
                            while(it2.hasNext()) {
                                nodeData = (NodeData) it2.next();
                                if(!exclude.contains(nodeData.getName())) {
                                    newNode.createNodeData(nodeData.getName(), nodeData.getValue());
                                    nodeTemplate.deleteNodeData(nodeData.getName());
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


