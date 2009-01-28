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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllModulesNodeOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Renames property node in all subnodes of all modules, from a base node, i.e. in every paragraph definition
 * rename property 'a' to 'b'
 * @author tmiyar
 *
 */
public class RenamePropertyAllModulesNodeTask extends AllModulesNodeOperation {

    private static Logger log = LoggerFactory.getLogger(RenamePropertyAllModulesNodeTask.class);

    private final String srcPropertyName;
    private final String destPropertyName;
    private final String baseNodeName;

    public RenamePropertyAllModulesNodeTask(String name, String description, String baseNodeName, String srcPropertyName, String destPropertyName) {
        super(name, description);
        this.srcPropertyName = srcPropertyName;
        this.destPropertyName = destPropertyName;
        this.baseNodeName = baseNodeName;
    }


    protected void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx)
            throws RepositoryException, TaskExecutionException {
        try {
            if(node.hasContent(baseNodeName)){
                ContentUtil.visit(node.getContent(baseNodeName), new ContentUtil.Visitor(){
                   public void visit(Content subNode) throws Exception {
                       if(subNode.hasNodeData(srcPropertyName)){
                           Value value = subNode.getNodeData(srcPropertyName).getValue();
                           subNode.deleteNodeData(srcPropertyName);
                           NodeData newNodeData = NodeDataUtil.getOrCreate(subNode, destPropertyName);
                           newNodeData.setValue(value);
                       }
                   }
                });
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

