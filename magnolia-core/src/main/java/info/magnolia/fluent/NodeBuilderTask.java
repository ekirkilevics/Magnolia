/**
 * This file Copyright (c) 2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.fluent;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class NodeBuilderTask extends AbstractRepositoryTask {
    private final String workspaceName;
    private final String rootPath;
    private final NodeOperation[] operations;

    public NodeBuilderTask(String taskName, String description, String workspaceName, NodeOperation... operations) {
        this(taskName, description, workspaceName, "/", operations);
    }

    public NodeBuilderTask(String taskName, String description, String workspaceName, String rootPath, NodeOperation... operations) {
        super(taskName, description);
        this.workspaceName = workspaceName;
        this.rootPath = rootPath;
        this.operations = operations;
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspaceName);
        final Content root = hm.getContent(rootPath);
        final Root rootOp = new Root(root, operations);
        rootOp.exec();
    }

}