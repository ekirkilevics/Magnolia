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
package info.magnolia.templating.module.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.OrderNodeBeforeTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RenamePropertyAllModulesNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;


/**
 * Module's version handler.
 *
 * @version Id$
 */
public class TemplatingModuleVersionHandler extends DefaultModuleVersionHandler {

    public TemplatingModuleVersionHandler() {

        register(DeltaBuilder.update("4.5", "")
                .addTask(new RemoveNodeTask("Remove backwards compatibility filter", "", RepositoryConstants.CONFIG, "/server/filters/cms/backwardCompatibility"))
                .addTask(new RenamePropertyAllModulesNodeTask("Templates configuration", "templatePath is now templateScript.", "templates", "templatePath", "templateScript"))
                .addTask(new RenamePropertyAllModulesNodeTask("Paragraphs configuration", "templatePath is now templateScript.", "paragraphs", "templatePath", "templateScript"))
                .addTask(new RenamePropertyAllModulesNodeTask("Templates configuration", "type is now renderType.", "templates", "type", "renderType"))
                .addTask(new RenamePropertyAllModulesNodeTask("Paragraphs configuration", "type is now renderType.", "paragraphs", "type", "renderType"))
                .addTask(new BootstrapSingleResource(
                    "Register Context ",
                    "Add plaintext renderer Attribute",
                    "/mgnl-bootstrap/templating/config.modules.templating.renderers.plaintext.xml"))
                .addTask(new BootstrapSingleResource(
                    "Register Context Attribute",
                    "Add cms and cmsfn context Attribute",
                    "/mgnl-bootstrap/templating/config.modules.templating.renderers.plaintext.contextAttributes.xml"))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final ArrayList<Task> tasks = new ArrayList<Task>();
        tasks.add(new OrderNodeBeforeTask("Order model execution filter", "", RepositoryConstants.CONFIG, "/server/filters/cms/modelExecution", "rendering"));
        return tasks;
    }
}
