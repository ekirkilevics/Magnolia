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
package info.magnolia.module.admininterface;

import info.magnolia.cms.gui.controlx.RenderKitFactory;
import info.magnolia.cms.gui.dialog.ControlsManager;
import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.admininterface.config.SecurityConfiguration;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version 2.0
 */
public class AdminInterfaceModule implements ModuleLifecycle {

    private SecurityConfiguration securityConfiguration;

    /**
     * @deprecated since 5.0, use IoC !
     */
    private static AdminInterfaceModule instance;

    public AdminInterfaceModule() {
        instance = this;
    }

    @Override
    public void start(ModuleLifecycleContext ctx) {
        ctx.registerModuleObservingComponent("controls", ControlsManager.getInstance());
        ctx.registerModuleObservingComponent("dialogs", DialogHandlerManager.getInstance());
        ctx.registerModuleObservingComponent("pages", PageHandlerManager.getInstance());
        ctx.registerModuleObservingComponent("trees", TreeHandlerManager.getInstance());

        // register the admin renderer kit
        RenderKitFactory.registerRenderKit(RenderKitFactory.ADMIN_INTERFACE_RENDER_KIT, new AdminInterfaceRenderKit());
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }

    public SecurityConfiguration getSecurityConfiguration() {
        return this.securityConfiguration;
    }

    public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    /**
     * @deprecated since 5.0, use IoC !
     */
    public static AdminInterfaceModule getInstance() {
        DeprecationUtil.isDeprecated("Use IoC!");
        return instance;
    }

}
