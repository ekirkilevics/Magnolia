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
package info.magnolia.module.workflow.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.Permission;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.admininterface.setup.AddMainMenuItemTask;
import info.magnolia.module.admininterface.setup.AddSubMenuItemTask;
import info.magnolia.module.admininterface.trees.WebsiteTreeConfiguration;
import info.magnolia.module.delta.AddPermissionTask;
import info.magnolia.module.delta.AddRoleToGroupTask;
import info.magnolia.module.delta.AddUserToGroupTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BackupTask;
import info.magnolia.module.delta.BootstrapResourcesTask;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.ConditionalDelegateTask;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.ModuleDependencyBootstrapTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PropertyValueDelegateTask;
import info.magnolia.module.delta.RemovePermissionTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.module.workflow.setup.for3_5.AddNewDefaultConfig;
import info.magnolia.module.workflow.setup.for3_5.CheckAndUpdateDefaultWorkflowDefinition;
import info.magnolia.module.workflow.setup.for3_5.RemoveMetadataFromExpressionsWorkspace;
import info.magnolia.module.workflow.setup.for3_5.SetDefaultWorkflowForActivationFlowCommands;
import info.magnolia.module.workflow.trees.WorkflowWebsiteTreeConfiguration;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;

/**
 * The {@link info.magnolia.module.ModuleVersionHandler} for the workflow module.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WorkflowModuleVersionHandler extends DefaultModuleVersionHandler {

    private static final String BACKUP_PATH = "/server/install/backup";
    private final Task inboxMenu = new AddMainMenuItemTask("inbox", "menu.inbox", "info.magnolia.module.workflow.messages",
            "MgnlAdminCentral.showContent('/.magnolia/pages/inbox.html', false, false)", "/.resources/icons/24/mail.gif",
    "security");
    private final Task flowsPageMenu = new AddSubMenuItemTask("config", "workflows", "menu.config.workflows",
            "MgnlAdminCentral.showContent('/.magnolia/pages/flows.html');", "/.resources/icons/16/dot.gif");

    private final Task bootstrapSecurityConfig = new BootstrapResourcesTask("New ACL configuration", "Bootstraps the new configuration for the ACL dialogs") {
        @Override
        protected String[] getResourcesToBootstrap(final InstallContext installContext) {
            return new String[]{
                    "/mgnl-bootstrap/workflow/config.modules.adminInterface.config.securityConfiguration.repositories.Expressions.xml",
                    "/mgnl-bootstrap/workflow/config.modules.adminInterface.config.securityConfiguration.repositories.Store.xml"
            };
        }
    };

    private final Task changeWebsiteTreeConfigurationTask = new CheckAndModifyPropertyValueTask("Website tree configuration", "Modifies the website tree configuration so that a message window pops up for activation.",
            ContentRepository.CONFIG,
            "/modules/adminInterface/trees/website",
            "configurationClass",
            WebsiteTreeConfiguration.class.getName(),
            WorkflowWebsiteTreeConfiguration.class.getName());

    private final Task changeDMSTreeConfigurationTask = new IsModuleInstalledOrRegistered(
            "DMS tree configuration", "Modifies the dms tree configuration so that a comment window pops up for activation.",
            "dms",
            new CheckAndModifyPropertyValueTask("DMS tree configuration", "Modifies the dms tree configuration so that a comment window pops up for activation.",
                    ContentRepository.CONFIG,
                    "/modules/dms/trees/dms",
                    "configurationClass",
                    // can't add a dependency to the dms so must use the class names
                    "info.magnolia.module.dms.DMSAdminTreeConfig",
                    "info.magnolia.module.dms.WorkflowDMSAdminTreeConfig"
            ));


    public WorkflowModuleVersionHandler() {
        final Delta delta35 = DeltaBuilder.update("3.5", "")
        .addTask(inboxMenu)
        .addTask(flowsPageMenu)
        .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the Workflow Utils page.", "/mgnl-bootstrap/workflow/config.modules.workflow.pages.flows.xml"))
        .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the Inbox Subpages.", "/mgnl-bootstrap/workflow/config.modules.workflow.pages.inboxSubPages.xml"))
        // TODO refactor the following ArrayDelegateTask into a single one: BackupAndBootstrap("resource.xml", "/backup/location", "Message", MessageType.WARNING), will be fixed with MAGNOLIA-1945
        .addTask(new ArrayDelegateTask("Backup and Bootstrap", "Makes a backup of the current 'EditWorkItem' dialog and re-installs it.", new Task[] {
                new BackupTask(ContentRepository.CONFIG, "/modules/workflow/dialogs/editWorkItem", true),
                new BootstrapSingleResource("Bootstrap", "Bootstraps the Inbox Subpages.", "/mgnl-bootstrap/workflow/config.modules.workflow.dialogs.editWorkItem.xml", ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW),
                new WarnTask("Note: ", "Note that the 'EditWorkItem' dialog was re-installed. Magnolia put a backup of original dialog into '" + BACKUP_PATH + "/editWorkItem'. Please review the changes manually.")
        }))
        .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the 'EditActivationWorkItem' dialog", "/mgnl-bootstrap/workflow/config.modules.workflow.dialogs.editActivationWorkItem.xml"))
        .addTask(new AddNewDefaultConfig())
        .addTask(new CheckAndUpdateDefaultWorkflowDefinition())
        .addTask(new RemoveMetadataFromExpressionsWorkspace())
        .addTask(new SetDefaultWorkflowForActivationFlowCommands())
        .addTask(bootstrapSecurityConfig)
        // TODO we might want to backup the following configurations before overwriting them, as well.
        .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the 'editors' group.", "/mgnl-bootstrap/workflow/usergroups.editors.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
        .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the 'publishers' group.", "/mgnl-bootstrap/workflow/usergroups.publishers.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
        .addTask(new BootstrapSingleResource("Bootstrap", "Bootstraps the 'workflow-base' role.", "/mgnl-bootstrap/workflow/userroles.workflow-base.xml"));
        register(delta35);

        register(DeltaBuilder.update("3.6", "")
                .addTask(changeWebsiteTreeConfigurationTask)
                .addTask(changeDMSTreeConfigurationTask)
                .addTask(new BootstrapSingleResource("Comment on activation dialog", "Registers the dialog for activation comments.", "/mgnl-bootstrap/workflow/config.modules.workflow.dialogs.startActivationWorkflow.xml"))
                .addTask(new IsModuleInstalledOrRegistered("Sample users and groups", "Adds sample users to sample editors and publishers group, if the sample module is installed or registered.",
                        "samples", new ArrayDelegateTask("",
                                new AddUserToGroupTask("Sample user", "eve", "editors"),
                                new AddUserToGroupTask("Sample user", "patrick", "publishers"))
                )));

        register(DeltaBuilder.update("3.6.4", "")
                .addTask(new ArrayDelegateTask("Update tree configuration",
                        new BootstrapSingleResource("Update tree configuration", "Adds Store workspace tree configuration.", "/mgnl-bootstrap/workflow/config.modules.adminInterface.trees.Store.xml"),
                        new BootstrapSingleResource("Update tree configuration", "Adds Expressions workspace tree configuration.", "/mgnl-bootstrap/workflow/config.modules.adminInterface.trees.Expressions.xml")))
        );
        register(DeltaBuilder.update("4.0", "")
                .addTask(new AddUserToGroupTask("Superuser", "superuser", "publishers"))
        );
        register(DeltaBuilder.update("4.2", "")
                // while this is checked and changed by DMS in 1.4 (bundled w/ Mgnl 4.1), there was a brief period of time (between 4.1 to 4.2) when workflow might have had overridden it.
                // if the property exists with a wrong value, fix it otherwise let it be as someone might have just removed the versioning or it uses custom or correct command
                .addTask(new IsModuleInstalledOrRegistered("Versioning", "Update of DMS specific versioning command.", "dms",
                        new PropertyValueDelegateTask("", "", ContentRepository.CONFIG, "/modules/dms/commands/dms/activate/version", "class", "info.magnolia.module.admininterface.commands.VersionCommand", false,
                                new CheckAndModifyPropertyValueTask("", "", ContentRepository.CONFIG, "/modules/dms/commands/dms/activate/version", "class", "info.magnolia.module.admininterface.commands.VersionCommand", "info.magnolia.module.dms.commands.DocumentVersionCommand"))))
                                .addTask(new IsModuleInstalledOrRegistered("Activation update","Enable workflow use for activation of data entries.", "data",
                                        new ConditionalDelegateTask("", "", new ArrayDelegateTask("","", new BackupTask(ContentRepository.CONFIG, "/modules/data/commands/data/activate"), new BootstrapSingleResource("","", "/info/magnolia/module/workflow/setup/data/config.modules.data.commands.data.activate.xml"))) {
                                    @Override
                                    protected boolean condition(InstallContext installContext) throws TaskExecutionException {
                                        // the installation of the activation command is backported also to 1.2 branch, so it is possible that this particular activation command have been already bootstrapped. Do not backup & re-bootstrap it in this case again
                                        try {
                                            Content activation = installContext.getHierarchyManager(ContentRepository.CONFIG).getContentByUUID("a71a96a9-0c2b-4358-90f0-0be55e79361c");
                                            return activation == null;
                                        } catch (RepositoryException e) {
                                            // doesn't exist, install
                                            return true;
                                        }
                                    }
                                }))
        );

        register(DeltaBuilder.update("4.2.3", "")
                // MAGNOLIA-2971
                .addTask(new AddPermissionTask("Update base role", "Adds permission to read the workflow definitions.", "workflow-base", "config", "/modules/workflow/config/flows", Permission.READ, true))
        );

        register(DeltaBuilder.update("4.3", "")
                // TODO use node builder instead of overwriting the dialogs completely
                .addTask(new BootstrapSingleModuleResource("Publication date","Add new controls to the activation dialog.", "config.modules.workflow.dialogs.startActivationWorkflow.xml"))
                .addTask(new BootstrapSingleModuleResource("Publication date","Add new controls to the workitem dialog", "config.modules.workflow.dialogs.editActivationWorkItem.xml"))
                .addTask(new RemovePermissionTask("Update workflow-base role", "Updates the workflow-base role, removing unnecessary permission to self.", "workflow-base", "userroles", "/workflow-base", Permission.READ)));

        register(DeltaBuilder.update("4.3.3", "")
                .addTask(new NodeExistsDelegateTask("StartActivationWorkflowDialog", "Checks if startActivationWorkflow node exists", ContentRepository.CONFIG, "/modules/workflow/dialogs/startActivationWorkflow", new CheckAndModifyPropertyValueTask("StartActivationWorkflowDialog", "Increases the height to 450px to prevent visual artefacts on FF on Linux", ContentRepository.CONFIG, "/modules/workflow/dialogs/startActivationWorkflow", "height", "400", "450")))
        );
        register(DeltaBuilder.update("4.4", "")
                .addTask(new InstallWorkflowDefinitionTask("Setup default deactivation workflow definition", "Adds the default deactivation workflow definition under the /modules/workflow/config/flows/deactivation config node.", "deactivation", "info/magnolia/module/workflow/deactivation-workflow.xml"))
                .addTask(new BootstrapSingleModuleResource("Deactivation","Adds new deactivation dialog.", "config.modules.workflow.dialogs.startDeactivationWorkflow.xml"))
                .addTask(new BootstrapSingleModuleResource("Deactivation","Adds deactivation tab to the workitem dialog", "config.modules.workflow.dialogs.editDeactivationWorkItem.xml"))
                .addTask(new IsModuleInstalledOrRegistered("Deactivation (DMS)","Adds deactivation command.","dms", new BootstrapSingleResource("Deactivate Command", "Installs deactivation command.", "/info/magnolia/module/workflow/setup/dms/config.modules.dms.commands.dms.deactivate.xml")))
                .addTask(new IsModuleInstalledOrRegistered("Deactivation (Data)","Adds deactivation command.","data", new BootstrapSingleResource("Deactivate Command", "Installs deactivation command.", "/info/magnolia/module/workflow/setup/data/config.modules.data.commands.data.deactivate.xml")))
        );

        register(DeltaBuilder.update("4.4.1", "")
                .addTask(new BootstrapSingleModuleResource("Deactivation (Website)","Adds deactivation command.", "config.modules.adminInterface.commands.website.deactivate.xml"))
        );
    }

    protected List<Task> getExtraInstallTasks(InstallContext ctx) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(new ModuleDependencyBootstrapTask("dms"));
        tasks.add(new ModuleDependencyBootstrapTask("data"));
        tasks.add(inboxMenu);
        tasks.add(flowsPageMenu);
        tasks.add(new InstallWorkflowDefinitionTask("Setup default activation workflow definition", "Adds the default activation workflow definition under the /modules/workflow/config/flows/activation config node.",
                "activation", "info/magnolia/module/workflow/default-activation-workflow.xml"));
        tasks.add(new InstallWorkflowDefinitionTask("Setup default deactivation workflow definition", "Adds the default deactivation workflow definition under the /modules/workflow/config/flows/deactivation config node.",
                "deactivation", "info/magnolia/module/workflow/deactivation-workflow.xml"));
        tasks.add(changeWebsiteTreeConfigurationTask);
        tasks.add(changeDMSTreeConfigurationTask);

        // TODO: MAGNOLIA-2979, move that to the samples
        if (ctx.isModuleRegistered("samples")) {
            tasks.add(new AddUserToGroupTask("Sample user", "eve", "editors"));
            tasks.add(new AddUserToGroupTask("Sample user", "patrick", "publishers"));
            tasks.add(new AddRoleToGroupTask("Update editors group with samples role", "editors","editors"));
        }

        tasks.add(new AddUserToGroupTask("Superuser", "superuser", "publishers"));

        // MAGNOLIA-2603 and MAGNOLIA-2971
        // the worflow base role grants only read permission. Now that the superuser is added to the publisher group he gets this restrictive permission assigned
        // to allow the superuser editing the workflow definition we have to add that permission explicitly to the superuser role
        tasks.add(new AddPermissionTask("Update Superuser Role", "Add all those permissions explicitly which could be overwritten by assigning the workflow base role.", "superuser", "config", "/modules/workflow/config/flows", Permission.ALL, true));

        return tasks;
    }

}
