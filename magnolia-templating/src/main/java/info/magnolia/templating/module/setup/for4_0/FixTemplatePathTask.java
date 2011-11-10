/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.templating.module.setup.for4_0;

import java.util.Collection;
import java.util.Iterator;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllModulesNodeOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;


/**
 * since 4.0 the templatePath property was moved to parameters content node, this class fixes it by
 * moving it back.
 *
 * @version $Id$
 */
public class FixTemplatePathTask extends AllModulesNodeOperation {

    public FixTemplatePathTask(String name, String description) {
        super(name, description);
    }

    @Override
    protected void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx)
            throws RepositoryException, TaskExecutionException {
        final String moveFromNodeName = "parameters";
        final String baseNodeName = "templates";
        final String propertyToMoveName = "templatePath";

        try {
            if(node.hasContent(baseNodeName)){
                Content baseNode = node.getContent(baseNodeName);
                if(baseNode.hasChildren(ItemType.CONTENTNODE.getSystemName())) {
                    final Collection children = baseNode.getChildren(ItemType.CONTENTNODE.getSystemName());
                    final Iterator it = children.iterator();
                    Content baseNodeChild;
                    while(it.hasNext()) {
                        baseNodeChild = (Content) it.next();
                        Content parametersNode;
                        if (!baseNodeChild.hasNodeData(propertyToMoveName) && baseNodeChild.hasContent(moveFromNodeName)) {
                            parametersNode = baseNodeChild.getContent(moveFromNodeName);

                            if(parametersNode.hasNodeData(propertyToMoveName)) {
                                NodeData templatePath = parametersNode.getNodeData(propertyToMoveName);
                                baseNodeChild.createNodeData(templatePath.getName(), templatePath.getValue());
                                parametersNode.deleteNodeData(templatePath.getName());
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


