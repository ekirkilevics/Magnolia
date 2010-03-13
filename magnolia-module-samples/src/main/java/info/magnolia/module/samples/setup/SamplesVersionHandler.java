/**
 * This file Copyright (c) 2007-2010 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.admininterface.setup.AddMainMenuItemTask;
import info.magnolia.module.admininterface.setup.AddSubMenuItemTask;
import info.magnolia.module.admininterface.setup.SetDefaultPublicURI;
import info.magnolia.module.delta.BackupTask;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapResourcesTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.RemoveNodeTask;

import java.util.ArrayList;
import java.util.List;


/**
 * Used to update previous version of samples module to the new one, as the module has major changes
 * there are some tasks to perform.
 *
 * @author philipp
 * @version $Id$
 *
 */
public class SamplesVersionHandler extends DefaultModuleVersionHandler {
    private static final String I18N_BASENAME = "info.magnolia.module.samples.messages";

    /**
     * First thing is to back up the old module, configuration and templates.
     * Then the new content is bootstraped.
     */
    public SamplesVersionHandler() {
        register(DeltaBuilder.update("4.0", "New samples module, replaces the old one.")
                .addTask(new NodeExistsDelegateTask("Backup samples config", "Creates a backup", "config",
                        "/modules/samples", new BackupTask("config", "/modules/samples", true)))
                .addTask(new NodeExistsDelegateTask("Backup samples website samples", "Creates a backup", "website",
                        "/help", new BackupTask("website", "/help", true)))
                .addTask(new NodeExistsDelegateTask("Backup samples website samples", "Creates a backup", "website",
                        "/mailform", new BackupTask("website", "/mailform", true)))
                .addTask(new NodeExistsDelegateTask("Backup samples website samples", "Creates a backup", "website",
                        "/mails", new BackupTask("website", "/mails", true)))
                .addTask(new BootstrapResourcesTask("New configuration", "Bootstraps new default samples configuration.") {
                    protected String[] getResourcesToBootstrap(final InstallContext installContext) {
                        return new String[]{
                                "/mgnl-bootstrap/samples/config.modules.samples.dialogs.controlsShowRoom.xml",
                                "/mgnl-bootstrap/samples/config.modules.samples.dialogs.howTo.xml",
                                "/mgnl-bootstrap/samples/config.modules.samples.dialogs.mainProperties.xml",
                                "/mgnl-bootstrap/samples/config.modules.samples.paragraphs.samplesControlsShowRoom.xml",
                                "/mgnl-bootstrap/samples/config.modules.samples.paragraphs.samplesHowToFTL.xml",
                                "/mgnl-bootstrap/samples/config.modules.samples.paragraphs.samplesHowToJSP.xml",
                                "/mgnl-bootstrap/samples/config.modules.samples.paragraphs.samplesSearchResult.xml",
                                "/mgnl-bootstrap/samples/config.modules.samples.templates.samplesMainFTL.xml",
                                "/mgnl-bootstrap/samples/config.modules.samples.templates.samplesMainJSP.xml",
                                "/mgnl-bootstrap/samples/config.modules.samples.templates.samplesVirtualURI.xml",
                                "/mgnl-bootstrap/samples/config.server.filters.sample.xml",
                        };
                    }
                })
                .addTask(new BootstrapSingleResource("Sample VirtualUri", "Adds a sample",
                        "/mgnl-bootstrap-samples/samples/config.modules.samples.virtualURIMapping.xml"))
                .addTask(new BootstrapSingleResource("Sample how to freemarker", "Adds a sample",
                        "/mgnl-bootstrap-samples/samples/website.howTo-freemarker.xml"))
                .addTask(new BootstrapSingleResource("Sample how to jsp", "Adds a sample",
                        "/mgnl-bootstrap-samples/samples/website.howTo-jsp.xml"))
                .addTask(new BootstrapSingleResource("Sample of search result", "Adds a sample",
                        "/mgnl-bootstrap-samples/samples/website.searchResult-jsp.xml"))
                .addTask(new BootstrapSingleResource("Sample using wirtual uri", "Adds a sample",
                        "/mgnl-bootstrap-samples/samples/website.products-freemarker.xml"))
                .addTask(new BootstrapConditionally("Samples developers", "Adds developers group if does not exist",
                        "/mgnl-bootstrap-samples/samples/usergroups.developers.xml"))
                .addTask(new BootstrapConditionally("Samples employees", "Adds employees group if does not exist",
                        "/mgnl-bootstrap-samples/samples/usergroups.employees.xml"))
                .addTask(new BootstrapConditionally("Samples editor", "Adds editor role if does not exist",
                        "/mgnl-bootstrap-samples/samples/userroles.editor.xml"))
                .addTask(new BootstrapConditionally("Samples user", "Adds user if does not exist",
                        "/mgnl-bootstrap-samples/samples/users.admin.david.xml"))
                .addTask(new BootstrapConditionally("Samples user", "Adds user if does not exist",
                        "/mgnl-bootstrap-samples/samples/users.admin.eve.xml"))
                .addTask(new BootstrapConditionally("Samples user", "Adds user if does not exist",
                        "/mgnl-bootstrap-samples/samples/users.admin.patrick.xml"))
                .addTask(new RemoveNodeTask("Remove menu items", "Removes the samples menu config item sample templates.",
                            ContentRepository.CONFIG, "/modules/adminInterface/config/menu/config/sample-templates"))
                .addTask(new RemoveNodeTask("Remove menu items", "Removes the samples menu config item sample paragraphs.",
                            ContentRepository.CONFIG, "/modules/adminInterface/config/menu/config/sample-paragraphs"))
                .addTask(new RemoveNodeTask("Remove menu items", "Removes the samples menu config item sample dialogs.",
                            ContentRepository.CONFIG, "/modules/adminInterface/config/menu/config/sample-dialogs"))

                .addTask(new RegisterModuleServletsTask())
                .addTasks(getCommonTasks())
        );
    }

    /**
     * There are some tasks common to the update process and the new installation process
     * that are not automatically bootstraped.
     * Add new menu item, and three sub menu items and then set the new menu item in the proper place
     * And then sets the default virtual URI on public instances
     *
     * @return
     */
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

    /**
     * Installation process will boostrap everything in the bootstrap folder, then
     * we have to add some extra tasks.
     */
    protected List getExtraInstallTasks(InstallContext installContext) {
        return getCommonTasks();
    }

}
