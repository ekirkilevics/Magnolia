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

import info.magnolia.cms.module.Module;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.BasicDelta;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A ModuleVersionHandler used for modules which don't specify one.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class LegacyModuleVersionHandler extends DefaultModuleVersionHandler {

    private static final String EXCEPTION_DURING_INSTALLING_MODULE = "Exception during installing module using the old model";

    private Logger log = LoggerFactory.getLogger(LegacyModuleVersionHandler.class);

    private final Version currentModuleVersion;

    LegacyModuleVersionHandler(Version version) {
        this.currentModuleVersion = version;
    }

    public List getDeltas(InstallContext installContext, Version from) {
        if (from == null) {
            return Collections.singletonList(getInstall(installContext));
        }

        if (currentModuleVersion.isBeforeOrEquivalent(from)) {
            // we could be in the situation where the module version in the repo is > than the module definition's, but
            // we ignore it
            return Collections.emptyList();
        }

        return Collections.singletonList(new BasicDelta(
            "Nothing",
            "There's nothing we can do if the module developer did not provide a version handler for this module",
            new ModuleVersionToLatestTask()));
    }

    public Delta getInstall(InstallContext installContext) {
        Delta delta = super.getInstall(installContext);
        final ModuleDefinition moduleDefinition = installContext.getCurrentModuleDefinition();
        delta.getTasks().add(new AbstractTask("Call register() method", "") {

            public void execute(InstallContext installContext) throws TaskExecutionException {
                try {
                    Object moduleInstance = ClassUtil.newInstance(moduleDefinition.getClassName());
                    if (moduleInstance instanceof Module) {
                        // read the module node
                        ((Module) moduleInstance).register(
                            moduleDefinition,
                            installContext.getOrCreateCurrentModuleNode(),
                            Module.REGISTER_STATE_INSTALLATION);
                    }
                    ModuleRegistry registry = ModuleRegistry.Factory.getInstance();
                    registry.registerModuleInstance(moduleDefinition.getName(), moduleInstance);
                }
                catch (Exception e) {
                    throw new TaskExecutionException(EXCEPTION_DURING_INSTALLING_MODULE, e);
                }
            }
        });
        return delta;
    }
}
