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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;
import java.util.Iterator;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AllModulesNodeOperation extends AbstractRepositoryTask {

    public AllModulesNodeOperation(String name, String description) {
        super(name, description);
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        if (!ctx.hasModulesNode()) {
            throw new TaskExecutionException("Modules node does not exist, can not proceed.");
        }
        final HierarchyManager hm = ctx.getConfigHierarchyManager();
        final Content modulesRoot = ctx.getModulesNode();
        final Iterator it = modulesRoot.getChildren().iterator();
        while (it.hasNext()) {
            final Content moduleNode = (Content) it.next();
            operateOnModuleNode(moduleNode, hm, ctx);
        }
    }

    protected abstract void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx) throws RepositoryException, TaskExecutionException;
}
