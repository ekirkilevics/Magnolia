/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.admincentral;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.admincentral.commands.ConvertDialogsFromFourOhToFiveOhConfigurationStyleCommand;
import info.magnolia.module.admincentral.dialog.registry.ConfiguredDialogManager;
import info.magnolia.module.admincentral.navigation.MenuItemConfiguration;
import info.magnolia.module.admincentral.tree.ConfiguredTreeManager;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Magnolia's AdminCentral Module.
 *
 * @author fgrilli
 */
public class AdminCentralModule implements ModuleLifecycle {

    private static final Logger log = LoggerFactory.getLogger(AdminCentralModule.class);

    private Map<String, MenuItemConfiguration> menu = new LinkedHashMap<String, MenuItemConfiguration>();

    private boolean stopCalled;

    public void start(ModuleLifecycleContext ctx) {
        ctx.registerModuleObservingComponent("mgnl50dialogs", ConfiguredDialogManager.getInstance());
        ctx.registerModuleObservingComponent("mgnl50trees", ConfiguredTreeManager.getInstance());

        // do not run import on every module restart
        if (!this.stopCalled) {
            try {
                // TODO: convert dialogs during upgrade process and not everytime on startup, but not on restart
                new ConvertDialogsFromFourOhToFiveOhConfigurationStyleCommand().execute(MgnlContext.getInstance());
                //FIXME command to convert old menu must be revised after architectural changes. For teh time being reads menug config from bootstrap file.
                //new ConvertMenuFromFourOhToFiveOhConfigurationStyleCommand().execute(MgnlContext.getInstance());
            } catch (Exception e) {
                log.error("Failed to convert dialog structure.", e);
            }
            this.stopCalled = false;
        }

        // DialogRegistry.getInstance().registerDialog("mock", new MockDialogProvider());
    }

    public void stop(ModuleLifecycleContext ctx) {
        this.stopCalled = true;
    }

    public Map<String, MenuItemConfiguration> getMenuItems() {
        return menu;
    }

    public void setMenuItems(Map<String, MenuItemConfiguration> menu) {
        this.menu = menu;
    }

    public void addMenuItem(String name, MenuItemConfiguration menuItem) {
        menuItem.setLocation("/modules/admin-central/config/menu/" + name);
        this.menu.put(name, menuItem);
    }
}
