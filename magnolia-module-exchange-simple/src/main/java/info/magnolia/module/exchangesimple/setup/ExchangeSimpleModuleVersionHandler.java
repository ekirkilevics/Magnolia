/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.exchangesimple.setup;

import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.IsAuthorInstanceDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * VersionHandler of the ExchangeSimple module.
 *
 * @version $Id$
 */
public class ExchangeSimpleModuleVersionHandler extends DefaultModuleVersionHandler {
    public ExchangeSimpleModuleVersionHandler() {
        super();

        register(DeltaBuilder.update("4.5", "URL of activation filter is no longer password protected but uses encryption instead.")
                .addTask(new BootstrapSingleModuleResource("", "", "config.modules.exchange-simple.pages.activationPage.xml"))
                .addTask(new BootstrapSingleModuleResource("", "", "config.modules.adminInterface.config.menu.tools.activationPage.xml"))
                .addTask(new IsAuthorInstanceDelegateTask("", "", new AbstractTask("", "") {

                    @Override
                    public void execute(InstallContext installContext) throws TaskExecutionException {
                        try {
                            SecurityUtil.updateKeys(SecurityUtil.generateKeyPair(1024));
                        } catch (NoSuchAlgorithmException e) {
                            throw new TaskExecutionException(e.getMessage(), e);
                        }
                    }
                }))
                .addTask(new FilterOrderingTask("activation", new String[] { "context", "login", "multipartRequest" })));

    }

    @Override
    protected List<Task> getBasicInstallTasks(InstallContext installContext) {
        final List<Task> installTasks = new ArrayList<Task>(super.getBasicInstallTasks(installContext));
        installTasks.add(new FilterOrderingTask("activation", new String[] { "context", "login", "multipartRequest" }));
        return installTasks;
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>();
        tasks.addAll(super.getExtraInstallTasks(installContext));
        tasks.add(new IsAuthorInstanceDelegateTask("Activation Keys", "Generate new Activation Key Pair.", new AbstractTask("", "") {

            @Override
            public void execute(InstallContext installContext) throws TaskExecutionException {
                try {
                    SecurityUtil.updateKeys(SecurityUtil.generateKeyPair(1024));
                } catch (NoSuchAlgorithmException e) {
                    throw new TaskExecutionException(e.getMessage(), e);
                }
            }
        }));

        return tasks;
    }


}
