/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.module.exchangesimple.setup;

import info.magnolia.cms.core.HierarchyManager;
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
