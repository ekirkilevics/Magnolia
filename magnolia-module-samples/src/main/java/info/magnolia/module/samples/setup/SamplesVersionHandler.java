/**
 * This file Copyright (c) 2007-2008 Magnolia International
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

import info.magnolia.module.InstallContext;
import info.magnolia.module.admininterface.setup.AddMainMenuItemTask;
import info.magnolia.module.admininterface.setup.AddSubMenuItemTask;
import info.magnolia.module.admininterface.setup.SimpleContentVersionHandler;
import info.magnolia.module.delta.IsInstallSamplesTask;

import java.util.ArrayList;
import java.util.List;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class SamplesVersionHandler extends SimpleContentVersionHandler {
    private final List installOrUpdateTasks = new ArrayList();

    private static final String I18N_BASENAME = "info.magnolia.module.samples.messages";

    public SamplesVersionHandler() {

    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        final List installTasks = new ArrayList(installOrUpdateTasks);
        // add the default uri task
        installTasks.add(new IsInstallSamplesTask("Default URI", "Sets a new default URI if samples are to be installed.", getSetDefaultPublicURITask(installContext)));
        installTasks.add(new AddMainMenuItemTask("samples", "samples.menu.label", I18N_BASENAME, "", "/.resources/icons/24/compass.gif", "security"));

        installTasks.add(submenu("config", "/modules/samples"));
        return installTasks;
    }

    private AddSubMenuItemTask submenu(String name, String path) {
        return new AddSubMenuItemTask("samples", name, "samples." + name + ".menu.label", I18N_BASENAME, "MgnlAdminCentral.showTree('" + name + "', '" + path + "')", "/.resources/icons/16/gears.gif");
    }

}
