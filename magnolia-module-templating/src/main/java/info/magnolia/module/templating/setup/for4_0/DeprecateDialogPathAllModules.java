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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Renames dialogPath to dialog and sets the property to the dialog name, in all modules in all paragraphs nodes.
 * @author tmiyar
 *
 */
public class DeprecateDialogPathAllModules extends AllModulesNodeOperation {

    private static Logger log = LoggerFactory.getLogger(DeprecateDialogPathAllModules.class);

    public DeprecateDialogPathAllModules(String name, String description) {
        super(name, description);
    }


    protected void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx)
            throws RepositoryException, TaskExecutionException {
        try {
            //find in paragraphs definitions property name dialogPath and rename it to dialog
            if(node.hasContent("paragraphs")){
                Content paragraphsNode = node.getContent("paragraphs");
                ContentUtil.visit(paragraphsNode, new ContentUtil.Visitor(){
                   public void visit(Content paragraphDefNode) throws Exception {
                       if(paragraphDefNode.hasNodeData("dialogPath")){
                           String dialogPath = paragraphDefNode.getNodeData("dialogPath").getString();
                           paragraphDefNode.deleteNodeData("dialogPath");
                           NodeData dialogNameNodeData = NodeDataUtil.getOrCreate(paragraphDefNode, "dialog");
                           dialogNameNodeData.setValue(StringUtils.substring(dialogPath, StringUtils.lastIndexOf(dialogPath, "/") + 1));
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


