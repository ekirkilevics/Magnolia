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
package info.magnolia.module.admininterface.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.admininterface.setup.for4_0.UpdatedDefaultPublicURIWarning;
import info.magnolia.module.admininterface.setup.for4_3.ReplaceWrongDialogNodeTypeTask;
import info.magnolia.module.admininterface.trees.WebsiteTreeHandler;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PropertyValueDelegateTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author philipp
 * @version $Id$
 */
public class AdminModuleVersionHandler extends DefaultModuleVersionHandler {
    private final AddSubMenuItemTask sysUsersSubMenu = new AddSubMenuItemTask("security", "usersSystem", "menu.security.usersSystem", null, "MgnlAdminCentral.showTree('usersSystem')", "/.resources/icons/16/pawn_glass_red.gif", "groups");
    private final AddSubMenuItemTask adminUsersSubMenu = new AddSubMenuItemTask("security", "usersAdmin", "menu.security.usersAdmin", null, "MgnlAdminCentral.showTree('usersAdmin')", "/.resources/icons/16/pawn_glass_yellow.gif", "groups");
    private final AddSubMenuItemTask subscribersMenu = new AddSubMenuItemTask("config", "subscribers", "menu.config.subscribers", "info.magnolia.module.admininterface.messages", "MgnlAdminCentral.showTree('config','/server/activation/subscribers')", "/.resources/icons/16/dot.gif", "cache");

    public AdminModuleVersionHandler() {
        final String pathToRestartPage = "/modules/adminInterface/pages/restart";
        register(DeltaBuilder.update("3.5", "")
                // sets a default virtual uri mapping - only if node doesn't exist. This is also bootstrapped on installs, but overridden by SetDefaultPublicURI on public instances.
                .addTask(new BootstrapConditionally("Install VirtualURI mappings", "Installs new configuration of virtualURI mappings.", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.virtualURIMapping.default.xml"))

                .addTask(new BootstrapSingleResource("New ACL configuration", "Bootstraps the new configuration for the ACL dialogs.", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.config.securityConfiguration.xml"))
                .addTask(new RemoveNodeTask("New ACL Dialog", "Deletes the old ACL page.", ContentRepository.CONFIG, "/modules/adminInterface/pages/rolesACL"))
                .addTask(new RemovePropertyTask("New ACL Dialog", "Removes the include property.", ContentRepository.CONFIG, "/modules/adminInterface/dialogs/roleedit", "file"))
                .addTask(new CheckAndModifyPropertyValueTask("New ACL Dialog", "Changes the control type for the ACL.", ContentRepository.CONFIG, "/modules/adminInterface/dialogs/roleedit/tabACL/aCL", "controlType", "include", "info.magnolia.module.admininterface.dialogs.ACLSDialogControl"))
                .addTask(new ArrayDelegateTask("Users menu", "System and admin users are now differentiated, creating two sub menus.", new Task[]{
                        new RemoveNodeTask(null, null, ContentRepository.CONFIG, "/modules/adminInterface/config/menu/security/users"),
                        adminUsersSubMenu,
                        sysUsersSubMenu}))
                .addTask(new ArrayDelegateTask("Menu", "Updates subscriber menu item in config menu", new Task[]{
                        new RemoveNodeTask(null, null, ContentRepository.CONFIG, "/modules/adminInterface/config/menu/config/subscriber"),
                        subscribersMenu}))
                .addTask(new NodeExistsDelegateTask("Remove Kupu richEdit control", "Checks for previous Kupu editor installation and removes richEdit control if existent.", ContentRepository.CONFIG, "/modules/adminInterface/controls/richEdit",
                        new RemoveNodeTask("Remove Kupu richEdit control", "Removes the richEdit control from Admin Interface since the Kupu Module is not delivered anymore.", ContentRepository.CONFIG, "/modules/adminInterface/controls/richEdit")))
                .addTask(new PropertyValueDelegateTask("Unused page", "Removes the now unused \"restart\" page.", ContentRepository.CONFIG, pathToRestartPage, "class", "info.magnolia.module.admininterface.pages.RestartPage", false,
                        new RemoveNodeTask(null, null, ContentRepository.CONFIG, pathToRestartPage)))
                .addTask(new RegisterModuleServletsTask())
        );

        register(DeltaBuilder.update("3.5.9", "")
                .addTask(new RemoveNodeTask("Remove Secured URIs menu item", "Removes deprecated menu iten, since secured URIs are now handled through the anonymous user's permissions.", ContentRepository.CONFIG, "/modules/adminInterface/config/menu/config/secureURIs"))
        );

        register(DeltaBuilder.update("3.6", "")
                .addTask(new SetPropertyTask(ContentRepository.CONFIG, "/modules/adminInterface/trees/website", "class", WebsiteTreeHandler.class.getName()))
                .addTask(new BootstrapSingleResource("Javascript messages", "Adds dynamic virtual url.", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.virtualURIMapping.messages.xml"))
                .addTask(new BootstrapSingleResource("Javascript messages", "Adds page to provide i18n messages in javascript.", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.pages.messages.xml"))
        );

        register(DeltaBuilder.update("3.6.4", "")
                .addTask(new ArrayDelegateTask("Update groups tree",new MoveNodeTask("Update groups tree", "Updates incorrectly named usergroup tree",ContentRepository.CONFIG, "/modules/adminInterface/trees/groups", "/modules/adminInterface/trees/usergroups", false),
                new CheckAndModifyPropertyValueTask("Update Group tree reference", "Updates reference to new usergroups tree name.", ContentRepository.CONFIG, "/modules/adminInterface/config/menu/security/groups", "onclick", "MgnlAdminCentral.showTree('groups')", "MgnlAdminCentral.showTree('usergroups')"),
                new CheckAndModifyPropertyValueTask("Update User dialog", "Updates reference to new usergroups tree name.", ContentRepository.CONFIG, "/modules/adminInterface/dialogs/useredit/tabUser/groups", "chooseOnclick", "mgnlOpenTreeBrowserWithControl($('${prefix}'), 'groups');", "mgnlOpenTreeBrowserWithControl($('${prefix}'), 'usergroups');"),
                new CheckAndModifyPropertyValueTask("Update Group dialog", "Updates reference to new usergroups tree name.", ContentRepository.CONFIG, "/modules/adminInterface/dialogs/groupedit/tabGroup/groups", "chooseOnclick", "mgnlOpenTreeBrowserWithControl($('${prefix}'), 'groups');", "mgnlOpenTreeBrowserWithControl($('${prefix}'), 'usergroups');")))
        );
        
        final String pathToDeploymentUtilsPage = "/modules/adminInterface/pages/deploymentUtils";
        register(DeltaBuilder.update("4.0", "")
                .addTask(new PropertyValueDelegateTask("Unused page", "Removes the now unused \"deployment\" page.", ContentRepository.CONFIG,
                // if still using the original class, we can go and ahead and delete:
                pathToDeploymentUtilsPage, "class", "info.magnolia.module.admininterface.pages.DeploymentUtilsPage", false,
                new ArrayDelegateTask(null,
                        new RemoveNodeTask(null, null, ContentRepository.CONFIG, pathToDeploymentUtilsPage),
                        new RemoveNodeTask(null, null, ContentRepository.CONFIG, "/modules/adminInterface/config/menu/tools/deployment"))))

                .addTask(new BootstrapSingleResource("Paragraph edit dialog", "The paragraph edition dialog is now a regular dialog.", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.dialogs.editParagraph.xml"))
                .addTask(new BootstrapSingleResource("Quickstart page", "Registers the page to be displayed as default in case there are no templates defined yet.", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.pages.quickstart.xml"))
                .addTask(new BootstrapSingleResource("User preferences dialog", "The user preferences dialog is not the user edit dialog anymore.", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.dialogs.userpreferences.xml"))
        );

        register(DeltaBuilder.update("4.1.1", "")
                // defaultPublicURI was updated in 4.0, and it shoudn't have.
                // This warns that defaultPublicURI was changed on 4.0, needs to be fixed for 4.0.3 and 4.1.1
                // This task is registered on the 4.0.3 version in the 4.0 branch.
                .addTask(new UpdatedDefaultPublicURIWarning())
        );
        
        register(DeltaBuilder.update("4.3", "")
            .addTask(new BootstrapSingleModuleResource("New i18n authoring support", "Adds i18n authoring configuration to /server/i18n.", "config.server.i18n.authoring.xml"))
            .addTask(new ReplaceWrongDialogNodeTypeTask())
            .addTask(new BootstrapSingleModuleResource("Folders in users tree", "Adds a new tree configuration with the correct root path.", "config.modules.adminInterface.trees.usersAdmin.xml"))
            .addTask(new BootstrapSingleModuleResource("Folders in users tree", "Adds a new tree configuration with the correct root path.", "config.modules.adminInterface.trees.usersSystem.xml"))
            // use the new trees
            .addTask(new SetPropertyTask("config", "/modules/adminInterface/config/menu/security/usersSystem", "onclick", "MgnlAdminCentral.showTree('usersSystem')"))
            .addTask(new SetPropertyTask("config", "/modules/adminInterface/config/menu/security/usersAdmin", "onclick", "MgnlAdminCentral.showTree('usersAdmin')"))
        );
        
    }

    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(new AddMainMenuItemTask("security", "menu.security", "info.magnolia.modules.admininterface.messages", "", "/.resources/icons/24/key1.gif", "config"));
        tasks.add(new AddSubMenuItemTask("security", "groups", "menu.groups", "MgnlAdminCentral.showTree('usergroups')", "/.resources/icons/16/group.gif"));
        tasks.add(new AddSubMenuItemTask("security", "roles", "menu.roles", "MgnlAdminCentral.showTree('userroles')", "/.resources/icons/16/hat_white.gif"));
        tasks.add(adminUsersSubMenu);
        tasks.add(sysUsersSubMenu);
        tasks.add(subscribersMenu);

        //set public uri only on installation
        tasks.add(new SetDefaultPublicURI("defaultPublicURI"));

        return tasks;
    }
}
