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
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.admininterface.setup.AddMainMenuItemTask;
import info.magnolia.module.admininterface.setup.AddSubMenuItemTask;
import info.magnolia.module.delta.BasicDelta;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.module.workflow.setup.for3_1.AddNewDefaultConfig;
import info.magnolia.module.workflow.setup.for3_1.AddUserToGroupTask;
import info.magnolia.module.workflow.setup.for3_1.I18nMenuPoint;
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
    private final Delta delta31 = BasicDelta.createBasicDelta("Updating to 3.1", "",
            new Task[]{
                    new I18nMenuPoint(),
                    new AddNewDefaultConfig(),
                    new InstallDefaultWorkflowDefinition(),
                    new RemoveMetadataFromExpressionsWorkspace(),
                    new SetDefaultWorkflowForActivationFlowCommands()
            });

    public WorkflowModuleVersionHandler() {
        super();
        final Version version3_1 = Version.parseVersion(3, 1, 0);
        register(version3_1, delta31);
    }

    // TODO : check install

    // TODO : add config submenu item ?

    protected List getExtraInstallTasks(InstallContext installContext) {
        final List tasks = new ArrayList();

        tasks.add(new InstallDefaultWorkflowDefinition());

        // TODO : this does not overwrite an existing menu item, so if the module is being reinstalled, we get the menu item twice ...
        tasks.add(new AddMainMenuItemTask("inbox", "menu.inbox", "info.magnolia.module.workflow.messages",
                "MgnlAdminCentral.showContent('/.magnolia/pages/inbox.html', false, false)", "/.resources/icons/24/mail.gif",
                "security"));
        tasks.add(new AddSubMenuItemTask("config", "workflows", "menu.config.workflows",
                "MgnlAdminCentral.showContent('/.magnolia/pages/flows.html');", "/.resources/icons/16/dot.gif"));

        // TODO : a simpler to check if a module is registered? in InstallContext ?
        if (ModuleRegistry.Factory.getInstance().getDefinition("samples") != null) {
            tasks.add(new AddUserToGroupTask("Sample user", "joe", "editors"));
            tasks.add(new AddUserToGroupTask("Sample user", "melinda", "publishers"));
        }

        return tasks;
    }

}
