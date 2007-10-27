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

import org.apache.commons.lang.ArrayUtils;

/**
 * A tasks that delegates to an array of more tasks.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ArrayDelegateTask implements Task {
    private String name;
    private Task[] tasks;

    public ArrayDelegateTask(String name) {
        this(name, new Task[0]);
    }

    public ArrayDelegateTask(String name, Task[] tasks) {
        this.name = name;
        this.tasks = tasks;
    }

    public ArrayDelegateTask(String name, Task task1, Task task2) {
        this(name, new Task[]{task1, task2});
    }

    public ArrayDelegateTask(String name, Task task1, Task task2, Task task3) {
        this(name, new Task[]{task1, task2, task3});
    }

    public ArrayDelegateTask(String name, Task task1, Task task2, Task task3, Task task4) {
        this(name, new Task[]{task1, task2, task3, task4});
    }

    public String getName() {
        return name;
    }

    public void addTask(Task task){
        this.tasks = (Task[]) ArrayUtils.add(tasks, task);
    }

    public String getDescription() {
        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < tasks.length; i++) {
            if (i > 0) {
                buf.append(" "); // TODO : line break ?
            }
            buf.append(tasks[i].getDescription());
        }
        return buf.toString();
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        for (int i = 0; i < tasks.length; i++) {
            tasks[i].execute(ctx);
        }
    }


    public String toString() {
        final StringBuffer buf = new StringBuffer();

        buf.append("{");
        for (int i = 0; i < tasks.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(tasks[i].toString());
        }
        buf.append("}");
        return buf.toString();
    }
}
