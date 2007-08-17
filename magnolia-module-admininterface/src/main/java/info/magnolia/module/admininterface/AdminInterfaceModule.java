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
package info.magnolia.module.admininterface;

import info.magnolia.cms.gui.controlx.RenderKitFactory;
import info.magnolia.cms.gui.dialog.ControlsManager;
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

    private static AdminInterfaceModule instance;

    public AdminInterfaceModule() {
        instance = this;
    }

    public void start(ModuleLifecycleContext ctx) {
        ctx.registerModuleObservingComponent("controls", ControlsManager.getInstance());
        ctx.registerModuleObservingComponent("dialogs", DialogHandlerManager.getInstance());
        ctx.registerModuleObservingComponent("pages", PageHandlerManager.getInstance());
        ctx.registerModuleObservingComponent("trees", TreeHandlerManager.getInstance());

        // register the admin renderer kit
        RenderKitFactory.registerRenderKit(RenderKitFactory.ADMIN_INTERFACE_RENDER_KIT, new AdminInterfaceRenderKit());
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }

    public SecurityConfiguration getSecurityConfiguration() {
        return this.securityConfiguration;
    }

    public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }


    public static AdminInterfaceModule getInstance() {
        return instance;
    }

}