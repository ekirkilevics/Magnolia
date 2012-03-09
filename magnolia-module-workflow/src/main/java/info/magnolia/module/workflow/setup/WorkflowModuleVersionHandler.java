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
package info.magnolia.module.workflow.setup;

import info.magnolia.cms.security.Permission;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.admininterface.setup.AddMainMenuItemTask;
import info.magnolia.module.admininterface.setup.AddSubMenuItemTask;
import info.magnolia.module.admininterface.trees.WebsiteTreeConfiguration;
import info.magnolia.module.delta.AddPermissionTask;
import info.magnolia.module.delta.AddRoleToGroupTask;
import info.magnolia.module.delta.AddUserToGroupTask;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.ModuleDependencyBootstrapTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.workflow.trees.WorkflowWebsiteTreeConfiguration;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link info.magnolia.module.ModuleVersionHandler} for the workflow module.
 *
 * @version $Revision: $ ($Author: $)
 */
public class WorkflowModuleVersionHandler extends DefaultModuleVersionHandler {

    private final Task inboxMenu = new AddMainMenuItemTask("inbox", "menu.inbox", "info.magnolia.module.workflow.messages",
            "MgnlAdminCentral.showContent('/.magnolia/pages/inbox.html', false, false)", "/.resources/icons/24/mail.gif",
    "security");
    private final Task flowsPageMenu = new AddSubMenuItemTask("config", "workflows", "menu.config.workflows",
            "MgnlAdminCentral.showContent('/.magnolia/pages/flows.html');", "/.resources/icons/16/dot.gif");

    private final Task changeWebsiteTreeConfigurationTask = new CheckAndModifyPropertyValueTask("Website tree configuration", "Modifies the website tree configuration so that a message window pops up for activation.",
            RepositoryConstants.CONFIG,
            "/modules/adminInterface/trees/website",
            "configurationClass",
            WebsiteTreeConfiguration.class.getName(),
            WorkflowWebsiteTreeConfiguration.class.getName());

    private final Task changeDMSTreeConfigurationTask = new IsModuleInstalledOrRegistered(
            "DMS tree configuration", "Modifies the dms tree configuration so that a comment window pops up for activation.",
            "dms",
            new CheckAndModifyPropertyValueTask("DMS tree configuration", "Modifies the dms tree configuration so that a comment window pops up for activation.",
                    RepositoryConstants.CONFIG,
                    "/modules/dms/trees/dms",
                    "configurationClass",
                    // can't add a dependency to the dms so must use the class names
                    "info.magnolia.module.dms.DMSAdminTreeConfig",
                    "info.magnolia.module.dms.WorkflowDMSAdminTreeConfig"
            ));

    public WorkflowModuleVersionHandler() {
        register(DeltaBuilder.checkPrecondition("4.4.6", "4.5"));
    }

    @Override
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
            tasks.add(new AddUserToGroupTask("Add sample user eve to the editors group", "eve", "editors"));
            tasks.add(new AddUserToGroupTask("Add sample user patrick to the publishers group", "patrick", "publishers"));
            tasks.add(new AddRoleToGroupTask("Update editors group with samples role", "editors","editors"));
        }

        tasks.add(new AddUserToGroupTask("Add superuser to the publishers group", "superuser", "publishers"));

        // MAGNOLIA-2603 and MAGNOLIA-2971
        // the worflow base role grants only read permission. Now that the superuser is added to the publisher group he gets this restrictive permission assigned
        // to allow the superuser editing the workflow definition we have to add that permission explicitly to the superuser role
        tasks.add(new AddPermissionTask("Update Superuser Role", "Add all those permissions explicitly which could be overwritten by assigning the workflow base role.", "superuser", "config", "/modules/workflow/config/flows", Permission.ALL, true));

        return tasks;
    }

}
