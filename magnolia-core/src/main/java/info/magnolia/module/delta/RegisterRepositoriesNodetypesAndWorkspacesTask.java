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

import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.module.RepositoryDefinition;
import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;

import java.util.Iterator;

/**
 * Register repositories, nodetypes and workspaces describer in the module definition.
 * Also grants ALL permissions to the superuser role on newly created workspaces.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RegisterRepositoriesNodetypesAndWorkspacesTask extends AbstractTask {
    public RegisterRepositoriesNodetypesAndWorkspacesTask() {
        super("Repositories, nodetypes and workspaces", "Registers repositories, nodetypes and workspaces for the current module.");
    }

    // TODO finer exception handling ?
    public void execute(InstallContext ctx) throws TaskExecutionException {
        try {
            final ModuleDefinition def = ctx.getCurrentModuleDefinition();
            // register repositories
            for (Iterator iter = def.getRepositories().iterator(); iter.hasNext();) {
                final RepositoryDefinition repDef = (RepositoryDefinition) iter.next();
                final String repositoryName = repDef.getName();

                final String nodetypeFile = repDef.getNodeTypeFile();
                boolean repositoryAdded = ModuleUtil.registerRepository(repositoryName, nodetypeFile);
                if (repositoryAdded) {
                    ctx.restartNeeded("New repository: " + repositoryName);
                }

                for (Iterator iterator = repDef.getWorkspaces().iterator(); iterator.hasNext();) {
                    final String workspaceName = (String) iterator.next();

                    if (ModuleUtil.registerWorkspace(repositoryName, workspaceName)) {
                        ModuleUtil.grantRepositoryToSuperuser(workspaceName);
                        ctx.restartNeeded("New workspace: " + workspaceName);
                    }
                }
            }
        } catch (RegisterException e) {
            throw new TaskExecutionException("Could not register repository, node types or workspace: " + e.getMessage());
        }

    }
}
