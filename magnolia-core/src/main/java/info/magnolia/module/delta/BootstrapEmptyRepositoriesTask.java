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
package info.magnolia.module.delta;

import info.magnolia.cms.beans.config.Bootstrapper;
import info.magnolia.cms.beans.config.ContentRepository;
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
 * @version $Revision: $ ($Author: $)
 */
public class BootstrapEmptyRepositoriesTask extends AbstractTask {

    public BootstrapEmptyRepositoriesTask() {
        super("Bootstrap module repositories", "Bootstrap empty repositories for the current module.");
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

                    // grant workspace to superuser
                    Role superuser = Security.getRoleManager().getRole("superuser");
                    superuser.addPermission(workspace, "/*", Permission.ALL);
                }
            }
        }
        catch (Throwable e) {
            throw new TaskExecutionException("Could not bootstrap workspace: " + e.getMessage(), e);
        }

    }
}
