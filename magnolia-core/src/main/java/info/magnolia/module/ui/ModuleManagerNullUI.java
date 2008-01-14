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
package info.magnolia.module.ui;

import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.InstallStatus;

import java.io.Writer;

/**
 * An implementation of ModuleManagerUI which does everything by itself,
 * with no human intervention and simply logs its results.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleManagerNullUI implements ModuleManagerUI {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleManagerNullUI.class);

    private final ModuleManager moduleManager;

    public ModuleManagerNullUI(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void onStartup() throws ModuleManagementException {
        final ModuleManager.ModuleManagementState moduleMgtState = moduleManager.getStatus();
        if (moduleMgtState.needsUpdateOrInstall()) {
            log.info("magnolia.update.auto is set to true, will start bootstrapping/update automatically");
            moduleManager.performInstallOrUpdate();
            final InstallStatus status = moduleManager.getInstallContext().getStatus();
            if (!InstallStatus.installDone.equals(status)) {
                log.info("Install could not be performed. Please check your logs and fix the appropriate issues before trying again.");
                return;
            }
            moduleManager.startModules();
        } else {
            moduleManager.startModules();
        }
    }

    public boolean execute(Writer out, String command) throws ModuleManagementException {
        throw new IllegalStateException("This implementation of ModuleManagerUI is only meant to be used at startup.");
    }

    public void renderTempPage(Writer out) {
        throw new IllegalStateException("This implementation of ModuleManagerUI is only meant to be used at startup.");
    }
}
