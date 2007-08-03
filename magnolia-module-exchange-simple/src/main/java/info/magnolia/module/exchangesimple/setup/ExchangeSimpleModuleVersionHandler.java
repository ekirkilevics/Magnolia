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
package info.magnolia.module.exchangesimple.setup;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.ConditionalDelegateTask;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.exchangesimple.setup.for3_1.UpdateActivationConfigTask;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ExchangeSimpleModuleVersionHandler extends DefaultModuleVersionHandler {
    private final Task updateConfigFrom30OrBootstrap = new ConditionalDelegateTask("Activation configuration", "The activation configuration changed. This either updates your existing configuration or bootstraps a new one",
            new UpdateActivationConfigTask(),
            new BootstrapSingleResource("Bootstrap new activation configuration", "Bootstrap new activation configuration",
                    "/mgnl-bootstrap/exchange-simple/config.server.activation.xml")) {

        protected boolean condition(InstallContext ctx) {
            final HierarchyManager hm = ctx.getConfigHierarchyManager();
            return hm.isExist(UpdateActivationConfigTask.CE30_ROOT_PATH) || hm.isExist(UpdateActivationConfigTask.EE30_ROOT_PATH);
        }
    };

    public ExchangeSimpleModuleVersionHandler() {
        super();
        // 3.1.0 is the first version of this module. install tasks take care of updating existing config
    }

    // TODO : review / validate - since 3.1 is the first version, we can't just bootstrap all on install
    protected List getBasicInstallTasks(InstallContext installContext) {
        final List installTasks = new ArrayList();
        installTasks.add(updateConfigFrom30OrBootstrap);
        installTasks.add(new BootstrapSingleResource("Bootstrap new filter", "Bootstrap new filter",
                "/mgnl-bootstrap/exchange-simple/config.server.filters.activation.xml"));
        installTasks.add(new FilterOrderingTask("activation", new String[]{"context", "login", "uriSecurity", "multipartRequest"}));
        return installTasks;
    }
}
