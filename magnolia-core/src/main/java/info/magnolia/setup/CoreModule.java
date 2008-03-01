/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
    }
}

