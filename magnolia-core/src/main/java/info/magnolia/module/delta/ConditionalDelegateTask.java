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
 * A task that delegates to another if a condition is true, or to an optional other if it is false.
 *
 * TODO : get descriptions from delegate + a description of the condition ?
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class ConditionalDelegateTask extends AbstractTask {
    private final Task ifTrue;
    private final Task ifFalse;

    protected ConditionalDelegateTask(String taskName, String taskDescription, Task ifTrue) {
        this(taskName, taskDescription, ifTrue, null);
    }

    public ConditionalDelegateTask(String taskName, String taskDescription, Task ifTrue, Task ifFalse) {
        super(taskName, taskDescription);
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        final Task task = condition(ctx) ? ifTrue : ifFalse;
        if (task != null) {
            task.execute(ctx);
        }
    }

    protected abstract boolean condition(InstallContext installContext) throws TaskExecutionException;

}
