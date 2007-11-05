/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module;

import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.ui.ModuleManagerUI;

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
    List loadDefinitions() throws ModuleManagementException;

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

    void performInstallOrUpdate() throws ModuleManagementException;

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
        private final List list;

        public ModuleManagementState() {
            this.list = new ArrayList();
        }

        public boolean needsUpdateOrInstall() {
            return !(list.isEmpty());
        }

        void addModule(ModuleDefinition module, Version currentVersion, List deltas) {
            list.add(new ModuleAndDeltas(module, currentVersion, deltas));
        }

        public List getList() {
            return list;
        }
    }

    /**
     * Represents what's to be done for each module.
     */
    public final static class ModuleAndDeltas {
        private final ModuleDefinition module;
        private final Version currentVersion;
        private final List deltas;

        public ModuleAndDeltas(ModuleDefinition module, Version currentVersion, List deltas) {
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

        public List getDeltas() {
            return deltas;
        }
    }


}
