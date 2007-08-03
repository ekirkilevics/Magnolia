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
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PropertyExistsDelegateTask extends ConditionalDelegateTask {
    private final String workspaceName;
    private final String parentPath;
    private final String propertyName;

    public PropertyExistsDelegateTask(String taskName, String taskDescription, String workspaceName, String parentPath, String propertyName, Task ifTrue) {
        this(taskName, taskDescription, workspaceName, parentPath, propertyName, ifTrue, null);
    }

    public PropertyExistsDelegateTask(String taskName, String taskDescription, String workspaceName, String parentPath, String propertyName, Task ifTrue, Task ifFalse) {
        super(taskName, taskDescription, ifTrue, ifFalse);
        this.workspaceName = workspaceName;
        this.parentPath = parentPath;
        this.propertyName = propertyName;
    }

    protected boolean condition(InstallContext ctx) throws TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspaceName);
        if (!hm.isExist(parentPath)) {
            ctx.debug("Checking for property " + propertyName + " in " + parentPath + " but node does not exist.");
            return false;
        }
        try {
            final Content node = hm.getContent(parentPath);
            return node.hasNodeData(propertyName);
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Can't check for property " + propertyName + " in " + parentPath + ".", e);
        }
    }
}
