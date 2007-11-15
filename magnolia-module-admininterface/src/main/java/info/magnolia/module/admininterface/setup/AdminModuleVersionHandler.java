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
package info.magnolia.module.admininterface.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.Task;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author philipp
 * @version $Id$
 */
public class AdminModuleVersionHandler extends DefaultModuleVersionHandler {
    private final AddSubMenuItemTask sysUsersSubMenu = new AddSubMenuItemTask("security", "usersSystem", "menu.security.usersSystem", "MgnlAdminCentral.showTree('users', '/system', true)", "/.resources/icons/16/pawn_glass_red.gif");
    private final AddSubMenuItemTask adminUsersSubMenu = new AddSubMenuItemTask("security", "usersAdmin", "menu.security.usersAdmin", "MgnlAdminCentral.showTree('users', '/admin', true)", "/.resources/icons/16/pawn_glass_yellow.gif");

    public AdminModuleVersionHandler() {
        final Delta for35 = DeltaBuilder.update("3.5", "")
                .addTask(new BootstrapConditionally("Install VirtualURI mappings", "Install new configuration of virtualURI mappings", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.virtualURIMapping.default.xml"))
                .addTask(new BootstrapSingleResource("New ACL configuration", "Bootstraps the new configuration for the ACL dialogs", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.config.securityConfiguration.xml"))
                .addTask(new RemoveNodeTask("New ACL Dialog", "Deletes the old ACL page", ContentRepository.CONFIG, "/modules/adminInterface/pages/rolesACL"))
                .addTask(new RemovePropertyTask("New ACL Dialog", "Removes the include property", ContentRepository.CONFIG, "/modules/adminInterface/dialogs/roleedit", "file"))
                .addTask(new CheckAndModifyPropertyValueTask("New ACL Dialog", "Change the control type for the ACL ", ContentRepository.CONFIG, "/modules/adminInterface/dialogs/roleedit", "controlType", "include", "info.magnolia.module.admininterface.dialogs.ACLSDialogControl"))
                .addTask(new ArrayDelegateTask("Users menu", "System and admin users are now differentiated, creating two sub menus", new Task[]{
                        new RemoveNodeTask(null, null, ContentRepository.CONFIG, "/modules/adminInterface/config/menu/security/users"),
                        sysUsersSubMenu,
                        adminUsersSubMenu}))
                .addTask(new NodeExistsDelegateTask("Remove Kupu richEdit control", "Checks for previous Kupu editor installation and removes richEdit control if existent.", ContentRepository.CONFIG, "/modules/adminInterface/controls/richEdit", 
                        new RemoveNodeTask("Remove Kupu richEdit control", "Removes the richEdit control from Admin Interface since the Kupu Module is not delivered anymore.", ContentRepository.CONFIG, "/modules/adminInterface/controls/richEdit")))
                .addTask(new RegisterModuleServletsTask());

        register(for35);
    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        final List tasks = new ArrayList();
        tasks.add(new AddMainMenuItemTask("security", "menu.security", "info.magnolia.modules.admininterface.messages", "", "/.resources/icons/24/key1.gif", "config"));
        tasks.add(sysUsersSubMenu);
        tasks.add(adminUsersSubMenu);
        tasks.add(new AddSubMenuItemTask("security", "groups", "menu.groups", "MgnlAdminCentral.showTree('groups')", "/.resources/icons/16/group.gif"));
        tasks.add(new AddSubMenuItemTask("security", "roles", "menu.roles", "MgnlAdminCentral.showTree('userroles')", "/.resources/icons/16/hat_white.gif"));

        return tasks;
    }
}
