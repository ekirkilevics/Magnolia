/**
 * This file Copyright (c) 2010 Magnolia International
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.admincentral.commands.ConvertDialogsCommand;
import info.magnolia.module.admincentral.dialog.DialogRegistry;
import info.magnolia.module.admincentral.dialog.MockDialogProvider;
import info.magnolia.module.admincentral.dialog.configured.ConfiguredDialogManager;

/**
 * Magnolia's AdminCentral Module.
 *
 * @author fgrilli
 */
public class AdminCentralVaadinModule implements ModuleLifecycle{

    private static final Logger log = LoggerFactory.getLogger(AdminCentralVaadinModule.class);

    public void start(ModuleLifecycleContext ctx) {
        ctx.registerModuleObservingComponent("mgnl50dialogs", ConfiguredDialogManager.getInstance());

        // TODO: convert dialogs during upgrade process and not everytime on startup
        try {
            new ConvertDialogsCommand().execute(MgnlContext.getInstance());
        } catch (Exception e) {
            log.error("Failed to convert dialog structure.", e);
        }

        DialogRegistry.getInstance().registerDialog("mock", new MockDialogProvider());
    }

    public void stop(ModuleLifecycleContext ctx) {
    }
}
