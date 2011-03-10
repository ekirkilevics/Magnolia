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
package info.magnolia.module.admincentral.setup;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.admincentral.commands.ConvertDialogsFromFourOhToFiveOhConfigurationStyleCommand;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Module's version handler.
 * @author fgrilli
 *
 */
public class AdminCentralModuleVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>();
        tasks.addAll(super.getExtraInstallTasks(installContext));
        tasks.add (new AbstractTask("Update dialogs", "Will update dialog structure from the old format into new one") {
            public void execute(InstallContext installContext) throws TaskExecutionException {
                try {
                    new ConvertDialogsFromFourOhToFiveOhConfigurationStyleCommand().execute(MgnlContext.getSystemContext());
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                    installContext.warn("Failed to update dialogs, please restart the instance.");
                }
            }
        });
//        tasks.add (new AbstractTask("Update menu", "Will update menu structure from the old format into new one") {
//            public void execute(InstallContext installContext) throws TaskExecutionException {
//                try {
//                    new ConvertMenuFromFourOhToFiveOhConfigurationStyleCommand().execute(MgnlContext.getSystemContext());
//                } catch (Exception e) {
//                    log.warn(e.getMessage(), e);
//                    installContext.warn("Failed to update menu, please restart the instance.");
//                }
//            }
//        });
        return tasks;
    }
}
