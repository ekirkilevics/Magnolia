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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.ConditionalDelegateTask;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.IsAuthorInstanceDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.exchangesimple.setup.for3_5.UpdateActivationConfigTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 3.5 being the first version of exchange-simple as a module, it is always "installed",
 * but we need it to behave differently if magnolia was installed previously
 * (ie. updating from 3.0), which is why there are so many "conditional
 * tasks". Once 3.5 is out the door, this will need to be revised
 * completely.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ExchangeSimpleModuleVersionHandler extends DefaultModuleVersionHandler {
    private final Task createEmptyActivationConfig = new ArrayDelegateTask("Activation configuration", "Creates an empty activation configuration", new Task[] {
            new CreateNodeTask("Activation configuration", "Creates empty activation configuration", ContentRepository.CONFIG, "/server", "activation", ItemType.CONTENT.getSystemName()),
            new SetPropertyTask(ContentRepository.CONFIG, "/server/activation", "class", info.magnolia.module.exchangesimple.DefaultActivationManager.class.getName()),
            new CreateNodeTask("Activation configuration", "Creates empty subscribers node", ContentRepository.CONFIG, "/server/activation", "subscribers", ItemType.CONTENT.getSystemName())
        });
    
    private final Task updateConfigFrom30OrBootstrap = new ConditionalDelegateTask("Activation configuration", "The activation configuration changed. This either updates your existing configuration or bootstraps a new one",
            new UpdateActivationConfigTask(),
            new IsAuthorInstanceDelegateTask("", "", new BootstrapSingleResource("Bootstrap new activation configuration", "Bootstrap new activation configuration",
                    "/mgnl-bootstrap/exchange-simple/config.server.activation.xml"), createEmptyActivationConfig)) {

        protected boolean condition(InstallContext ctx) {
            final HierarchyManager hm = ctx.getConfigHierarchyManager();
            return hm.isExist(UpdateActivationConfigTask.CE30_ROOT_PATH) || hm.isExist(UpdateActivationConfigTask.EE30_ROOT_PATH);
        }
    };

    public ExchangeSimpleModuleVersionHandler() {
        super();
        // 3.5.0 is the first version of this module. install tasks take care of updating existing config
    }

    protected List getBasicInstallTasks(InstallContext installContext) {
        final List installTasks = new ArrayList();
        installTasks.add(updateConfigFrom30OrBootstrap);
        installTasks.add(new BootstrapSingleResource("Bootstrap new filter", "Bootstrap new filter",
                "/mgnl-bootstrap/exchange-simple/config.server.filters.activation.xml"));
        installTasks.add(new FilterOrderingTask("activation", new String[]{"context", "login", "uriSecurity", "multipartRequest"}));
        return installTasks;
    }
}
