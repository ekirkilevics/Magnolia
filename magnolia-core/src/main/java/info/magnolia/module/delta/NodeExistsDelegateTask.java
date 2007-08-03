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

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class NodeExistsDelegateTask extends ConditionalDelegateTask {
    private final String workspaceName;
    private final String pathToCheck;

    public NodeExistsDelegateTask(String name, String description, String workspaceName, String pathToCheck, Task ifTrue) {
        this(name, description, workspaceName, pathToCheck, ifTrue, null);
    }

    public NodeExistsDelegateTask(String name, String description, String workspaceName, String pathToCheck, Task ifTrue, Task ifFalse) {
        super(name, description, ifTrue, ifFalse);
        this.pathToCheck = pathToCheck;
        this.workspaceName = workspaceName;
    }

    protected boolean condition(InstallContext ctx) {
        return ctx.getHierarchyManager(workspaceName).isExist(pathToCheck);
    }
}
