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
public abstract class AllChildrenNodesOperation extends AbstractRepositoryTask {
    private final String repositoryName;
    private final String parentNodePath;

    public AllChildrenNodesOperation(String name, String description, String repositoryName, String parentNodePath) {
        super(name, description);
        this.repositoryName = repositoryName;
        this.parentNodePath = parentNodePath;
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Content modulesRoot = getParentNode(ctx);
        final Iterator it = modulesRoot.getChildren().iterator();
        while (it.hasNext()) {
            final Content node = (Content) it.next();
            operateOnChildNode(node, ctx);
        }
    }

    protected Content getParentNode(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(repositoryName);
        return hm.getContent(parentNodePath);
    }

    protected abstract void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException;
}
