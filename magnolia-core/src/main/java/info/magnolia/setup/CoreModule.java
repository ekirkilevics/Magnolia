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
package info.magnolia.setup;

import info.magnolia.cms.beans.config.ParagraphManager;
import info.magnolia.cms.beans.config.ParagraphRendererManager;
import info.magnolia.cms.beans.config.ShutdownManager;
import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.beans.config.TemplateRendererManager;
import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.commands.CommandsManager;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CoreModule implements ModuleLifecycle {

    public void start(ModuleLifecycleContext ctx) {
        ctx.registerModuleObservingComponent("virtualURIMapping", VirtualURIManager.getInstance());
        ctx.registerModuleObservingComponent("templates", TemplateManager.getInstance());
        ctx.registerModuleObservingComponent("template-renderers", TemplateRendererManager.getInstance());
        ctx.registerModuleObservingComponent("paragraphs", ParagraphManager.getInstance());
        ctx.registerModuleObservingComponent("paragraph-renderers", ParagraphRendererManager.getInstance());
        ctx.registerModuleObservingComponent("commands", CommandsManager.getInstance());
        ctx.registerModuleObservingComponent("shutdown", ShutdownManager.getInstance());
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        throw new IllegalStateException("not implemented yet !");
    }

    public void restart(ModuleLifecycleContext moduleLifecycleContext) {
        throw new IllegalStateException("not implemented yet !");
    }
}
