/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
import info.magnolia.module.workflow.setup.for3_1.AddNewDefaultConfig;
import info.magnolia.module.workflow.setup.for3_1.AddUserToGroupTask;
import info.magnolia.module.workflow.setup.for3_1.InstallDefaultWorkflowDefinition;
import info.magnolia.module.workflow.setup.for3_1.RemoveMetadataFromExpressionsWorkspace;
import info.magnolia.module.workflow.setup.for3_1.SetDefaultWorkflowForActivationFlowCommands;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WorkflowModuleVersionHandler extends DefaultModuleVersionHandler {
    private final Task mainMenu = new AddMainMenuItemTask("inbox", "menu.inbox", "info.magnolia.module.workflow.messages",
            "MgnlAdminCentral.showContent('/.magnolia/pages/inbox.html', false, false)", "/.resources/icons/24/mail.gif",
            "security");
    private final Task subMenu = new AddSubMenuItemTask("config", "workflows", "menu.config.workflows",
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
        final Delta delta31 = DeltaBuilder.update("3.1", "")
                .addTask(mainMenu)
                .addTask(subMenu)
                .addTask(new AddNewDefaultConfig())
                .addTask(new InstallDefaultWorkflowDefinition())
                .addTask(new RemoveMetadataFromExpressionsWorkspace())
                .addTask(new SetDefaultWorkflowForActivationFlowCommands())
                .addTask(bootstrapSecurityConfig);
        register(delta31);
    }

    protected List getExtraInstallTasks(InstallContext ctx) {
        final List tasks = new ArrayList();

        tasks.add(mainMenu);
        tasks.add(subMenu);
        tasks.add(new InstallDefaultWorkflowDefinition());

        if (ctx.isModuleRegistered("samples")) {
            tasks.add(new AddUserToGroupTask("Sample user", "joe", "editors"));
            tasks.add(new AddUserToGroupTask("Sample user", "melinda", "publishers"));
        }

        return tasks;
    }

}
