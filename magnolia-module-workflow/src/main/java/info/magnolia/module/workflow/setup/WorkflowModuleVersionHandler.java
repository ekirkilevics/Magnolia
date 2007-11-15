/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.workflow.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.admininterface.setup.AddMainMenuItemTask;
import info.magnolia.module.admininterface.setup.AddSubMenuItemTask;
import info.magnolia.module.delta.BootstrapResourcesTask;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;
import info.magnolia.module.workflow.setup.for3_5.AddNewDefaultConfig;
import info.magnolia.module.workflow.setup.for3_5.AddUserToGroupTask;
import info.magnolia.module.workflow.setup.for3_5.InstallDefaultWorkflowDefinition;
import info.magnolia.module.workflow.setup.for3_5.RemoveMetadataFromExpressionsWorkspace;
import info.magnolia.module.workflow.setup.for3_5.SetDefaultWorkflowForActivationFlowCommands;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WorkflowModuleVersionHandler extends DefaultModuleVersionHandler {
    private final Task inboxMenu = new AddMainMenuItemTask("inbox", "menu.inbox", "info.magnolia.module.workflow.messages",
            "MgnlAdminCentral.showContent('/.magnolia/pages/inbox.html', false, false)", "/.resources/icons/24/mail.gif",
            "security");
    private final Task flowsPageMenu = new AddSubMenuItemTask("config", "workflows", "menu.config.workflows",
            "MgnlAdminCentral.showContent('/.magnolia/pages/flows.html');", "/.resources/icons/16/dot.gif");

    private final Task bootstrapSecurityConfig = new BootstrapResourcesTask("", "") {
        protected String[] getResourcesToBootstrap(final InstallContext installContext) {
            return new String[]{
                    "/mgnl-bootstrap/workflow/config.modules.adminInterface.config.securityConfiguration.repositories.Expressions.xml",
                    "/mgnl-bootstrap/workflow/config.modules.adminInterface.config.securityConfiguration.repositories.Store.xml"
            };
        }
    };

    public WorkflowModuleVersionHandler() {
        final Delta delta35 = DeltaBuilder.update("3.5", "")
                .addTask(inboxMenu)
                .addTask(flowsPageMenu)
                .addTask(new AddNewDefaultConfig())
                .addTask(new InstallDefaultWorkflowDefinition())
                .addTask(new RemoveMetadataFromExpressionsWorkspace())
                .addTask(new SetDefaultWorkflowForActivationFlowCommands())
                .addTask(bootstrapSecurityConfig);
        register(delta35);
    }

    protected List getExtraInstallTasks(InstallContext ctx) {
        final List tasks = new ArrayList();

        tasks.add(inboxMenu);
        tasks.add(flowsPageMenu);
        tasks.add(new InstallDefaultWorkflowDefinition());

        if (ctx.isModuleRegistered("samples")) {
            tasks.add(new AddUserToGroupTask("Sample user", "joe", "editors"));
            tasks.add(new AddUserToGroupTask("Sample user", "melinda", "publishers"));
        }

        return tasks;
    }

}
