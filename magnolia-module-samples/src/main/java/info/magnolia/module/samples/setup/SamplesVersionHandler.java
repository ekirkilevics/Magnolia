/**
 * This file Copyright (c) 2007-2009 Magnolia International
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
package info.magnolia.module.samples.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.admininterface.setup.AddMainMenuItemTask;
import info.magnolia.module.admininterface.setup.AddSubMenuItemTask;
import info.magnolia.module.admininterface.setup.SetDefaultPublicURI;
import info.magnolia.module.delta.BackupTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.ReplaceIfExistsTask;

import java.util.ArrayList;
import java.util.List;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class SamplesVersionHandler extends DefaultModuleVersionHandler {
    private static final String I18N_BASENAME = "info.magnolia.module.samples.messages";

    public SamplesVersionHandler() {
        register(DeltaBuilder.update("4.0", "New samples module, replaces the old one.")
                .addTask(new BackupTask("config", "/modules/samples", true))
                .addTask(new ReplaceIfExistsTask("Configuration","Replace configuration.",
                        "Samples configuration doesn't exist", "config",
                        "/modules/samples/config", "config.modules.samples.config.xml"))
                .addTask(new ReplaceIfExistsTask("Dialogs","Replace dialogs.",
                        "Samples dialogs don't exist", "config",
                        "/modules/samples/dialogs", "config.modules.samples.dialogs.xml"))
                .addTask(new ReplaceIfExistsTask("Templates","Replace templates.",
                        "Samples templates don't exist", "config",
                        "/modules/samples/templates", "config.modules.samples.templates.xml"))
                .addTask(new ReplaceIfExistsTask("Paragraphs","Replace paragraphs.",
                        "Samples paragraphs don't exist", "config",
                        "/modules/samples/paragraphs", "config.modules.samples.paragraphs.xml"))
                .addTask(new BootstrapSingleResource("Sample Filter", "Adds a sample filter",
                        "/mgnl-bootstrap/samples/config.server.filters.sample.xml"))
                .addTask(new RegisterModuleServletsTask())
                .addTasks(getCommonTasks())
        );
    }

    protected List getCommonTasks() {
        final List commonTasks = new ArrayList();
        // add the default uri task
        commonTasks.add(new AddMainMenuItemTask("samples", "samples.menu.label", I18N_BASENAME, "", "/.resources/icons/24/compass.gif", "security"));

        commonTasks.add(submenu("config", "/modules/samples"));
        commonTasks.add(submenu("filter", "/server/filters/sample"));
        commonTasks.add(submenu("servlet", "/server/filters/servlets/DisplaySamplesSourcesServlet"));

        commonTasks.add(new FilterOrderingTask("sample", new String[]{"servlets"}));

        commonTasks.add(new SetDefaultPublicURI("defaultPublicURI"));

        return commonTasks;
    }

    private AddSubMenuItemTask submenu(String name, String path) {
        return new AddSubMenuItemTask("samples", name, "samples." + name + ".menu.label", I18N_BASENAME, "MgnlAdminCentral.showTree('config', '" + path + "')", "/.resources/icons/16/gears.gif");
    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        return getCommonTasks();
    }

}
