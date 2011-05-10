/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.workflow.setup.for3_5;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 * Adds new configuration properties introduced in 3.5.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddNewDefaultConfig extends AbstractTask {
    //TODO why not extend PropertyValuesTask ??

    public AddNewDefaultConfig() {
        super("New config items", "Adding some new configuration items with their default values.");
    }

    @Override
    public void execute(InstallContext ctx) throws TaskExecutionException {
        try {
            final Content configNode = ctx.getOrCreateCurrentModuleConfigNode();

            final Content flowDefinitionManagerCfg = configNode.createContent("flowDefinitionManager", ItemType.CONTENTNODE);
            flowDefinitionManagerCfg.createNodeData("class").setValue("info.magnolia.module.workflow.flows.DefaultFlowDefinitionManager");

            // these two should not be necessary, associated code should be removed
            flowDefinitionManagerCfg.createNodeData("flowDefinitionURLPattern").setValue("auto");
            flowDefinitionManagerCfg.createNodeData("saveWorkflowDefinitionInWorkItem").setValue("true");

            // TODO the following two properties should not be made public - untested code
            configNode.createNodeData("backupWorkItems").setValue(false);
            configNode.createNodeData("deferredExpressionStorage").setValue(false);
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Could not add new default workflow configuration.", e);
        }
    }
}
