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

import info.magnolia.module.InstallContext;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class NodeCheckDelegateTask extends ConditionalDelegateTask {
    protected final String workspaceName;
    protected final String nodePath;
    protected final String propertyName;

    public NodeCheckDelegateTask(String taskName, String taskDescription, String workspaceName, String nodePath, String propertyName, Task ifTrue, Task ifFalse) {
        super(taskName, taskDescription, ifTrue, ifFalse);
        this.workspaceName = workspaceName;
        this.nodePath = nodePath;
        this.propertyName = propertyName;
    }

    protected boolean condition(InstallContext ctx) throws TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspaceName);
        if (!hm.isExist(nodePath)) {
            return false;
        }

        try {
            final Content node = hm.getContent(nodePath);
            return checkNode(node, ctx);
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Can't check for property " + propertyName + " in " + nodePath + ".", e);
        }
    }

    protected abstract boolean checkNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException;
}
