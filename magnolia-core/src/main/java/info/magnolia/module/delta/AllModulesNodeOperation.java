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

/**
 * An abstract that will perform an operation on all modules node found in the configuration repository.
 * @see #operateOnChildNode(info.magnolia.cms.core.Content,info.magnolia.module.InstallContext)
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AllModulesNodeOperation extends AllChildrenNodesOperation {

    public AllModulesNodeOperation(String name, String description) {
        super(name, description, null, null);
    }

    protected HierarchyManager getHierarchyManager(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        return ctx.getConfigHierarchyManager();
    }

    protected Content getParentNode(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        if (!ctx.hasModulesNode()) {
            throw new TaskExecutionException("Modules node does not exist, can not proceed.");
        }
        return ctx.getModulesNode();
    }

    protected void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getConfigHierarchyManager();
        operateOnModuleNode(node, hm, ctx);
    }

    protected abstract void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx) throws RepositoryException, TaskExecutionException;
}
