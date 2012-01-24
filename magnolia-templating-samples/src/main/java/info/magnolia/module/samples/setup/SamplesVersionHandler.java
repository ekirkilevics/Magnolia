/**
 * This file Copyright (c) 2007-2011 Magnolia International
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
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CopyNodeTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;


/**
 * Used to update previous version of samples module to the new one, as the module has major changes
 * there are some tasks to perform.
 *
 * @version $Id$
 *
 */
public class SamplesVersionHandler extends DefaultModuleVersionHandler {
    private static final String I18N_BASENAME = "info.magnolia.module.samples.messages";

    /**
     * First thing is to back up the old module, configuration and templates. Then the new content is bootstrapped.
     */
    public SamplesVersionHandler() {
        register(DeltaBuilder.update("4.0", "New samples module, replaces the old one.")
                .addTask(new RemoveNodeTask("Remove menu items", "Removes the samples menu config item sample templates.",
                            RepositoryConstants.CONFIG, "/modules/adminInterface/config/menu/config/sample-templates"))
                .addTask(new RemoveNodeTask("Remove menu items", "Removes the samples menu config item sample components.",
                            RepositoryConstants.CONFIG, "/modules/adminInterface/config/menu/config/sample-components"))
                .addTask(new RemoveNodeTask("Remove menu items", "Removes the samples menu config item sample dialogs.",
                            RepositoryConstants.CONFIG, "/modules/adminInterface/config/menu/config/sample-dialogs"))

                .addTask(new RegisterModuleServletsTask())

        );

        register(DeltaBuilder.update("4.5", "")
                .addTask(new RemoveNodeTask("Remove sample filter", "Removes the samples menu config item sample templates.",
                            RepositoryConstants.CONFIG, "/server/filters/sample"))
                .addTask(new RemoveNodeTask("Remove sample filter menu item", "Removes the samples menu config item sample templates.",
                            RepositoryConstants.CONFIG, "/modules/adminInterface/config/menu/samples/filter"))
                .addTask(new RemoveNodeTask("Remove sample dialog", "Removes the samples config dialog.",
                            RepositoryConstants.CONFIG, "/modules/samples/dialog"))
                .addTask(new RemoveNodeTask("Remove sample template menu item", "Removes the samples config template.",
                            RepositoryConstants.CONFIG, "/modules/samples/templates"))
                .addTask(new RemoveNodeTask("Remove sample virtualUriMapping", "Removes the samples config virtualUriMapping.",
                            RepositoryConstants.CONFIG, "/modules/samples/virtualURIMapping"))
                .addTask(new RemoveNodeTask("Remove sample site", "Removes the samples site howTo-freemarker.",
                            RepositoryConstants.WEBSITE, "/howTo-freemarker"))
                .addTask(new RemoveNodeTask("Remove sample site", "Removes the samples site products-freemarker.",
                            RepositoryConstants.WEBSITE, "/products-freemarker"))
                .addTask(new RemoveNodeTask("Remove sample site", "Removes the samples site searchResult.",
                            RepositoryConstants.WEBSITE, "/searchResult"))
                //Remove all previous site/configuration..
                .addTask(new BootstrapSingleResource("Sample virtualUriMapping", "Adds a sample",
                        "/mgnl-bootstrap/samples/config.modules.samples.virtualURIMapping.xml"))
                .addTask(new BootstrapSingleResource("Sample dialog", "Adds a sample",
                        "/mgnl-bootstrap/samples/config.modules.samples.dialogs.xml"))
                .addTask(new BootstrapSingleResource("Sample templates", "Adds a sample",
                        "/mgnl-bootstrap/samples/config.modules.samples.templates.xml"))
                .addTask(new BootstrapSingleResource("Sample site", "Adds a sample site",
                        "/mgnl-bootstrap-samples/samples/website.ftl-sample-site.xml"))

                //Copy mgnl-files
                .addTask(new ModuleFilesExtraction())

                .addTasks(getCommonTasks())
        );
    }

    /**
     * There are some tasks common to the update process and the new installation process that are not automatically
     * bootstrapped. Add new menu item, and three sub menu items and then set the new menu item in the proper place And
     * then sets the default virtual URI on public instances
     *
     * @return
     */
    protected List<Task> getCommonTasks() {
        final List<Task> commonTasks = new ArrayList<Task>();
        // add the default uri task
        commonTasks.add(new AddMainMenuItemTask("samples", "samples.menu.label", I18N_BASENAME, "", "/.resources/icons/24/compass.gif", "security"));

        commonTasks.add(submenu("config", "/modules/samples"));
        commonTasks.add(submenu("servlet", "/server/filters/servlets/DisplaySamplesSourcesServlet"));

        commonTasks.add(new SetDefaultPublicURI("defaultPublicURI"));

        //Create the JSP Configuration
        //First copy FTL configuration
        commonTasks.add(new CopyNodeTask("sample", "Initialize the JPS template pages directory", RepositoryConstants.CONFIG, "/modules/samples/templates/pages/ftl", "/modules/samples/templates/pages/jsp", true));
        commonTasks.add(new CopyNodeTask("sample", "Initialize the JPS template components directory", RepositoryConstants.CONFIG, "/modules/samples/templates/components/ftl", "/modules/samples/templates/components/jsp", true));
        commonTasks.add(new CopyNodeTask("sample", "Initialize the JPS template components virtualUriMapping", RepositoryConstants.CONFIG, "/modules/samples/virtualURIMapping/ftl-products", "/modules/samples/virtualURIMapping/jsp-products", true));

        //Update copied FTL node conf to JSP
        commonTasks.add(new UpdateFromFtlToJspConfiguration("sample", "Set JSP properties template pages", RepositoryConstants.CONFIG, "/modules/samples/templates/pages/jsp"));
        commonTasks.add(new UpdateFromFtlToJspConfiguration("sample", "Set JSP properties template components", RepositoryConstants.CONFIG, "/modules/samples/templates/components/jsp"));
        commonTasks.add(new UpdateFromFtlToJspVirtualUriMapping("sample", "Set JSP properties virtualUriMapping", RepositoryConstants.CONFIG, "/modules/samples/virtualURIMapping/jsp-products"));

        // Create the JSP Site
        commonTasks.add(new CopyNodeTask("sample", "Initialize the JPS site", RepositoryConstants.WEBSITE, "/ftl-sample-site", "/jsp-sample-site", true));
        commonTasks.add(new UpdateFromFtlToJspSite("sample", "Set JSP properties for site", RepositoryConstants.WEBSITE, "/jsp-sample-site"));


        return commonTasks;
    }

    private AddSubMenuItemTask submenu(String name, String path) {
        return new AddSubMenuItemTask("samples", name, "samples." + name + ".menu.label", I18N_BASENAME, "MgnlAdminCentral.showTree('config', '" + path + "')", "/.resources/icons/16/gears.gif");
    }

    /**
     * Installation process will bootstrap everything in the bootstrap folder, then we have to add some extra tasks.
     */
    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        return getCommonTasks();
    }

}
