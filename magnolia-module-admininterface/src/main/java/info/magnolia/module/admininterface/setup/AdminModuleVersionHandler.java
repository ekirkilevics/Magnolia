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
package info.magnolia.module.admininterface.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.admininterface.setup.for4_4.RegisterMgnlDeletedType;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Version handler for the AdminInterface module.
 *
 * @version $Id$
 */
public class AdminModuleVersionHandler extends DefaultModuleVersionHandler {
    private final AddSubMenuItemTask sysUsersSubMenu = new AddSubMenuItemTask("security", "usersSystem", "menu.security.usersSystem", null, "MgnlAdminCentral.showTree('usersSystem')", "/.resources/icons/16/pawn_glass_red.gif", "groups");
    private final AddSubMenuItemTask adminUsersSubMenu = new AddSubMenuItemTask("security", "usersAdmin", "menu.security.usersAdmin", null, "MgnlAdminCentral.showTree('usersAdmin')", "/.resources/icons/16/pawn_glass_yellow.gif", "groups");
    private final AddSubMenuItemTask subscribersMenu = new AddSubMenuItemTask("config", "subscribers", "menu.config.subscribers", "info.magnolia.module.admininterface.messages", "MgnlAdminCentral.showTree('config','/server/activation/subscribers')", "/.resources/icons/16/dot.gif", "cache");

    public AdminModuleVersionHandler() {
        register(DeltaBuilder.update("4.5", "")
                .addTask(new BootstrapSingleResource("Security", "Bootstraps password hashing control.", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.controls.passwordHash.xml"))
                .addTask(new CheckAndModifyPropertyValueTask("Security", "Update user dialog to hash passwords.", "config", "/modules/adminInterface/dialogs/useredit/tabUser/pswd", "controlType", "password", "passwordHash"))
                .addTask(new CheckAndModifyPropertyValueTask("Security", "Update user properties dialog to hash passwords.", "config", "/modules/adminInterface/dialogs/userpreferences/tabUser/pswd", "controlType", "password", "passwordHash"))
                .addTask(new BootstrapConditionally("Add tool permission list", "Bootstraps permission list page", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.pages.permission.xml"))
                .addTask(new BootstrapConditionally("Add tool permission list", "Bootstraps permission list to menu", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.config.menu.tools.permission.xml")));
    }

    @Override
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

    @Override
    protected List<Task> getBasicInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>();
        tasks.add(new RegisterMgnlDeletedType());
        tasks.addAll(super.getBasicInstallTasks(installContext));
        return tasks;
    }
}
