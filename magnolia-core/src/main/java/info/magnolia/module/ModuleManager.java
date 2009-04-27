/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module;

import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.ui.ModuleManagerUI;
import info.magnolia.module.delta.Delta;

import java.util.ArrayList;
import java.util.List;

/**
 * ModuleManager is responsible for the lifecycle of modules.
 * (loads definitions, install/update/uninstall, start/stop)
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ModuleManager {

    /**
     * Loads modules definitions, validates dependencies and sorts modules
     * by dependencies.
     */
    List<ModuleDefinition> loadDefinitions() throws ModuleManagementException;

    /**
     * Checks if we need to do any module installation, update or uninstall.
     */
    void checkForInstallOrUpdates() throws ModuleManagementException;

    /**
     * Returns the status as discovered by checkForInstallOrUpdates().
     * @throws IllegalStateException if checkForInstallOrUpdates was never called.
     */
    ModuleManagementState getStatus();

    ModuleManagerUI getUI();

    void performInstallOrUpdate();

    InstallContext getInstallContext();

    void startModules();

    void stopModules();

    /**
     * Use this to retrieve the configured singleton impl of ModuleManager.
     */
    public class Factory {
        public static ModuleManager getInstance() {
            return (ModuleManager) FactoryUtil.getSingleton(ModuleManager.class);
        }
    }

    /**
     * Represent what's to be done for all modules.
     */
    public final static class ModuleManagementState {
        private final List<ModuleAndDeltas> list;

        public ModuleManagementState() {
            this.list = new ArrayList<ModuleAndDeltas>();
        }

        public boolean needsUpdateOrInstall() {
            return !(list.isEmpty());
        }

        void addModule(ModuleDefinition module, Version currentVersion, List<Delta> deltas) {
            list.add(new ModuleAndDeltas(module, currentVersion, deltas));
        }

        public List<ModuleAndDeltas> getList() {
            return list;
        }
    }

    /**
     * Represents what's to be done for each module.
     */
    public final static class ModuleAndDeltas {
        private final ModuleDefinition module;
        private final Version currentVersion;
        private final List<Delta> deltas;

        public ModuleAndDeltas(ModuleDefinition module, Version currentVersion, List<Delta> deltas) {
            this.module = module;
            this.currentVersion = currentVersion;
            this.deltas = deltas;
        }

        public ModuleDefinition getModule() {
            return module;
        }

        public Version getCurrentVersion() {
            return currentVersion;
        }

        public List<Delta> getDeltas() {
            return deltas;
        }

        public String toString() {
            final StringBuffer sb = new StringBuffer("ModuleAndDeltas for ");
            sb.append(module.getName());
            if (currentVersion != null) {
                sb.append(": current version is ");
                sb.append(currentVersion);
                sb.append(", updating to ");
            } else {
                sb.append(": installing version ");
            }
            sb.append(module.getVersion());
            sb.append(" with ");
            sb.append(deltas.size());
            sb.append(" deltas.");
            return sb.toString();
        }
    }

}
