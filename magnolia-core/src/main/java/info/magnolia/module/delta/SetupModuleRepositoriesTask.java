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
package info.magnolia.module.delta;

import info.magnolia.cms.beans.config.Bootstrapper;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.RepositoryDefinition;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.Security;
import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;

import java.util.Iterator;


/**
 * Bootstrap empty repositories for the current module (loading is already performed before install tasks)
 * @author gjoseph
 * @version $Revision$ ($Author$)
 */
public class SetupModuleRepositoriesTask extends AbstractTask {

    public SetupModuleRepositoriesTask() {
        super("Setup module repositories", "Bootstrap empty repositories, grant them to superuser and subscribe them so that activation can be used.");
    }

    // TODO finer exception handling ?
    public void execute(InstallContext ctx) throws TaskExecutionException {
        try {
            final ModuleDefinition def = ctx.getCurrentModuleDefinition();
            // register repositories
            for (Iterator iter = def.getRepositories().iterator(); iter.hasNext();) {
                final RepositoryDefinition repDef = (RepositoryDefinition) iter.next();

                for (Iterator iterator = repDef.getWorkspaces().iterator(); iterator.hasNext();) {
                    final String workspace = (String) iterator.next();

                    // bootstrap the workspace if empty
                    if (!ContentRepository.checkIfInitialized(workspace)) {
                        Bootstrapper.bootstrapRepository(workspace, new Bootstrapper.BootstrapFilter() {

                            public boolean accept(String filename) {
                                return filename.startsWith(workspace + ".");
                            }
                        });
                    }

                    // TODO move the code to a better place
                    ModuleUtil.grantRepositoryToSuperuser(workspace);
                    ModuleUtil.subscribeRepository(workspace);
                }
            }
        }
        catch (Throwable e) {
            throw new TaskExecutionException("Could not bootstrap workspace: " + e.getMessage(), e);
        }

    }
}
