/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module.admininterface.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
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
        final Delta for31 = DeltaBuilder.update("3.1", "")
                .addTask(new BootstrapSingleResource("New ACL configuration", "Bootstraps the new configuration for the ACL dialogs", "/mgnl-bootstrap/adminInterface/config.modules.adminInterface.config.securityConfiguration.xml"))
                .addTask(new RemoveNodeTask("New ACL Dialog", "Deletes the old ACL page", ContentRepository.CONFIG, "/modules/adminInterface/pages/rolesACL"))
                .addTask(new RemovePropertyTask("New ACL Dialog", "Removes the include property", ContentRepository.CONFIG, "/modules/adminInterface/dialogs/roleedit", "file"))
                .addTask(new CheckAndModifyPropertyValueTask("New ACL Dialog", "Change the control type for the ACL ", ContentRepository.CONFIG, "/modules/adminInterface/dialogs/roleedit", "controlType", "include", "info.magnolia.module.admininterface.dialogs.ACLSDialogControl"))
                .addTask(new ArrayDelegateTask("Users menu", "System and admin users are now differentiated, creating two sub menus", new Task[]{
                        new RemoveNodeTask(null, null, ContentRepository.CONFIG, "/modules/adminInterface/config/menu/security/users"),
                        sysUsersSubMenu,
                        adminUsersSubMenu}))
                .addTask(new RegisterModuleServletsTask());

        register(for31);
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
