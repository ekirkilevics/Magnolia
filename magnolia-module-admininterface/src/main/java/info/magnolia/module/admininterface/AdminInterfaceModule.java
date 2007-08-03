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
import info.magnolia.cms.module.RegisterException;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version 2.0
 */
public class AdminInterfaceModule extends AbstractAdminModule implements ModuleLifecycle {

    public void start(ModuleLifecycleContext ctx) {
        // TODO maybe this one needs to be in gui ?
        ctx.registerModuleObservingComponent("controls", ControlsManager.getInstance());
        ctx.registerModuleObservingComponent("dialogs", DialogHandlerManager.getInstance());
        ctx.registerModuleObservingComponent("pages", PageHandlerManager.getInstance());
        ctx.registerModuleObservingComponent("trees", TreeHandlerManager.getInstance());
    }

    /**
     * @see info.magnolia.module.admininterface.AbstractAdminModule#onInit()
     */
    public void onInit() {
        // register the admin renderer kit
        RenderKitFactory.registerRenderKit(RenderKitFactory.ADMIN_INTERFACE_RENDER_KIT, new AdminInterfaceRenderKit());
    }

    /**
     * @see info.magnolia.module.admininterface.AbstractAdminModule#onRegister(int)
     */
    protected void onRegister(int registerState) throws RegisterException {
        // everything done
    }

}