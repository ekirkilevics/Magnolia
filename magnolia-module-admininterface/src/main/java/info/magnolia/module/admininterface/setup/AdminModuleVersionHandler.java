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

import java.util.ArrayList;
import java.util.List;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BasicDelta;
import info.magnolia.module.delta.BootstrapSingleResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class AdminModuleVersionHandler extends DefaultModuleVersionHandler {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(AdminModuleVersionHandler.class);

    public AdminModuleVersionHandler() {
        register("3.1", BasicDelta.createBasicDelta("Update to 3.1", "", new BootstrapSingleResource("New ACL configuration", "Bootstraps the new configuration for the ACL dialogs", "/mgnl-bootstrap/adminInterface/config.module.adminInterface.config.securityConfiguration.xml")));
    }
    
    protected List getExtraInstallTasks(InstallContext installContext) {
		final List tasks = new ArrayList();
		tasks.add(new AddMainMenuItemTask("security", "menu.security", "info.magnolia.modules.admininterface.messages", "", "/.resources/icons/24/key1.gif", "config"));
		tasks.add(new AddSubMenuItemTask("security", "usersSystem",
				"menu.security.usersSystem",
				"MgnlAdminCentral.showTree('users', '/system', true)",
				"/.resources/icons/16/pawn_glass_red.gif"));
		tasks.add(new AddSubMenuItemTask("security", "usersAdmin",
				"menu.security.usersAdmin",
				"MgnlAdminCentral.showTree('users', '/admin', true)",
				"/.resources/icons/16/pawn_glass_yellow.gif"));
		tasks.add(new AddSubMenuItemTask("security", "groups",
				"menu.groups",
				"MgnlAdminCentral.showTree('groups')",
				"/.resources/icons/16/group.gif"));
		tasks.add(new AddSubMenuItemTask("security", "roles",
				"menu.roles",
				"MgnlAdminCentral.showTree('userroles')",
				"/.resources/icons/16/hat_white.gif"));

		return tasks;
    }
}
