/**
 * This file Copyright (c) 2007 Magnolia International
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
package info.magnolia.module.samples.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.InstallContext;
import info.magnolia.module.admininterface.setup.AddSubMenuItemTask;
import info.magnolia.module.admininterface.setup.SimpleContentVersionHandler;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;

import java.util.ArrayList;
import java.util.List;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class SamplesVersionHandler extends SimpleContentVersionHandler {
    private final List installOrUpdateTasks = new ArrayList();

    public SamplesVersionHandler() {
        // - replace Templates menu item
        final AddSubMenuItemTask addTemplateMenuItem = new AddSubMenuItemTask("config", "sample-templates", "config.menu.config.templates.samples", "info.magnolia.module.samples.messages", "MgnlAdminCentral.showTree('config','/modules/samples/templates')", "/.resources/icons/16/dot.gif", "subscribers");
        final String templatesMenuItemPath = "/modules/adminInterface/config/menu/config/templates";
        final NodeExistsDelegateTask removeOldTemplatesMenuItem = new NodeExistsDelegateTask("Menu", "Remove the Templates menu item if existent", ContentRepository.CONFIG, templatesMenuItemPath,
            new RemoveNodeTask("Menu", "Removes the Template menu item as it will be replaced with a new configuration", ContentRepository.CONFIG, templatesMenuItemPath));
        installOrUpdateTasks.add(removeOldTemplatesMenuItem);
        installOrUpdateTasks.add(addTemplateMenuItem);

        // - replace Paragraphs menu item
        final AddSubMenuItemTask addParagraphsMenuItem = new AddSubMenuItemTask("config", "sample-paragraphs", "config.menu.config.paragraphs.samples", "info.magnolia.module.samples.messages", "MgnlAdminCentral.showTree('config','/modules/samples/paragraphs')", "/.resources/icons/16/dot.gif", "subscribers");
        final String paragraphsMenuItemPath = "/modules/adminInterface/config/menu/config/paragraphs";
        final NodeExistsDelegateTask removeOldParagraphsMenuItem = new NodeExistsDelegateTask("Menu", "Remove the Paragraphs menu item if existent", ContentRepository.CONFIG, paragraphsMenuItemPath,
            new RemoveNodeTask("Menu", "Removes the Paragraph menu item as it will be replaced with a new configuration", ContentRepository.CONFIG, paragraphsMenuItemPath));
        installOrUpdateTasks.add(removeOldParagraphsMenuItem);
        installOrUpdateTasks.add(addParagraphsMenuItem);

        // - replace Dialogs menu item
        final AddSubMenuItemTask addDialogsMenuItem = new AddSubMenuItemTask("config", "sample-dialogs", "config.menu.config.dialogs.samples", "info.magnolia.module.samples.messages", "MgnlAdminCentral.showTree('config','/modules/samples/dialogs')", "/.resources/icons/16/dot.gif", "subscribers");
        final String dialogsMenuItemPath = "/modules/adminInterface/config/menu/config/dialogs";
        final NodeExistsDelegateTask removeOldDialogsMenuItem = new NodeExistsDelegateTask("Menu", "Remove the Dialogs menu item if existent", ContentRepository.CONFIG, dialogsMenuItemPath,
            new RemoveNodeTask("Menu", "Removes the Paragraph menu item as it will be replaced with a new configuration", ContentRepository.CONFIG, dialogsMenuItemPath));
        installOrUpdateTasks.add(removeOldDialogsMenuItem);
        installOrUpdateTasks.add(addDialogsMenuItem);

        final Delta for35 = DeltaBuilder.update("3.5", "").addTasks(installOrUpdateTasks);

        register(for35);
    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        final List installTasks = new ArrayList(installOrUpdateTasks);
        // add the default uri task
        installTasks.add(getSetDefaultPublicURITask(installContext));
        return installTasks;
    }

}
