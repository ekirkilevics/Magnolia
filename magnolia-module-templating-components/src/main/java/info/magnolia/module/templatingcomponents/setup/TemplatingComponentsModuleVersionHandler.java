/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.templatingcomponents.setup;

import static info.magnolia.nodebuilder.Ops.addNode;
import static info.magnolia.nodebuilder.Ops.addProperty;
import static info.magnolia.nodebuilder.Ops.getNode;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.nodebuilder.task.ErrorHandling;
import info.magnolia.nodebuilder.task.NodeBuilderTask;

import java.util.ArrayList;
import java.util.List;

/**
 * The module's version handler.
 * 
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class TemplatingComponentsModuleVersionHandler extends DefaultModuleVersionHandler {

    // TODO ui should be removed as an update task

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final ArrayList<Task> tasks = new ArrayList<Task>();
        // templating components were added with 4.3
        tasks.add(new NodeBuilderTask(
                "New templating UI components",
                "Registers new UI components for templating.",
                ErrorHandling.strict,
                ContentRepository.CONFIG,
                "/server/rendering/freemarker",
                getNode("sharedVariables")
                        .then(addNode("cms", ItemType.CONTENTNODE)
                                .then(addProperty("class",
                                        info.magnolia.module.templatingcomponents.freemarker.Directives.class.getName())),
                                addNode("cmsfn", ItemType.CONTENTNODE)
                                        .then(addProperty(
                                                "class",
                                                info.magnolia.module.templatingcomponents.functions.TemplatingFunctions.class
                                                        .getName())))));
        return tasks;
    }
}
