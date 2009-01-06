/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module;

import info.magnolia.module.delta.ModuleBootstrapTask;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.SetupModuleRepositoriesTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.SamplesBootstrapTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class DefaultModuleVersionHandler extends AbstractModuleVersionHandler {

    /**
     * Returns the most common installation tasks:
     * register repositories, nodetypes and workspaces as stated in the
     * module definition, bootstrap the module's mgnl-bootstrap files,
     * extract the module's mgnl-files files, register the module's servlets.
     *
     * This method should generally not be overridden.
     */
    protected List getBasicInstallTasks(InstallContext installContext) {
        final List basicInstallTasks = new ArrayList();
        basicInstallTasks.add(new SetupModuleRepositoriesTask());
        basicInstallTasks.add(new ModuleBootstrapTask());
        basicInstallTasks.add(new SamplesBootstrapTask());
        basicInstallTasks.add(new ModuleFilesExtraction());
        basicInstallTasks.add(new RegisterModuleServletsTask());
        return basicInstallTasks;
    }

}
