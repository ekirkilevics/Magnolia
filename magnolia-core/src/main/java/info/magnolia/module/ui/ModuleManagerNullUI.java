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
package info.magnolia.module.ui;

import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleManagementException;

import java.io.Writer;
import java.util.Map;

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
            if (!moduleManager.getInstallContext().isInstallDone()) {
                log.info("Install could not be performed. Please check your logs and fix the appropriate issues before trying again.");
                return;
            }
            moduleManager.startModules();
            moduleMgtState.done();
        } else {
            moduleManager.startModules();
        }
    }

    public boolean execute(Writer out, Map params) throws ModuleManagementException {
        throw new IllegalStateException("This implementation of ModuleManagerUI is only meant to be used at startup.");
    }

    public void renderTempPage(Writer out) {
        throw new IllegalStateException("This implementation of ModuleManagerUI is only meant to be used at startup.");
    }
}
