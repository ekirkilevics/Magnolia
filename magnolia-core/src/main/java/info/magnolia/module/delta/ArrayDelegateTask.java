/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.delta;

import info.magnolia.module.InstallContext;
import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;

/**
 * A task that simply delegates to an array of other tasks.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ArrayDelegateTask implements Task {
    private String name;
    private String description;
    private Task[] tasks;

    public ArrayDelegateTask(String name) {
        this(name, (String) null);
    }

    public ArrayDelegateTask(String name, String description) {
        this(name, description, new Task[0]);
    }

    public ArrayDelegateTask(String name, Task... tasks) {
        this(name, null, tasks);
    }

    public ArrayDelegateTask(String name, String description, Task... tasks) {
        this.name = name;
        this.description = description;
        this.tasks = tasks;
    }

    /**
     * Since 4.2: replaced by the varargs constructor. Informally deprecated; will be removed in 4.3.
     */
    public ArrayDelegateTask(String name, Task task1, Task task2) {
        this(name, (String) null, task1, task2);
    }

    /**
     * Since 4.2: replaced by the varargs constructor. Informally deprecated; will be removed in 4.3.
     */
    public ArrayDelegateTask(String name, String description, Task task1, Task task2) {
        this(name, description, new Task[]{task1, task2});
    }

    /**
     * Since 4.2: replaced by the varargs constructor. Informally deprecated; will be removed in 4.3.
     */
    public ArrayDelegateTask(String name, Task task1, Task task2, Task task3) {
        this(name, (String) null, task1, task2, task3);
    }

    /**
     * Since 4.2: replaced by the varargs constructor. Informally deprecated; will be removed in 4.3.
     */
    public ArrayDelegateTask(String name, String description, Task task1, Task task2, Task task3) {
        this(name, description, new Task[]{task1, task2, task3});
    }

    /**
     * Since 4.2: replaced by the varargs constructor. Informally deprecated; will be removed in 4.3.
     */
    public ArrayDelegateTask(String name, Task task1, Task task2, Task task3, Task task4) {
        this(name, null, task1, task2, task3, task4);
    }

    /**
     * Since 4.2: replaced by the varargs constructor. Informally deprecated; will be removed in 4.3.
     */
    public ArrayDelegateTask(String name, String description, Task task1, Task task2, Task task3, Task task4) {
        this(name, description, new Task[]{task1, task2, task3, task4});
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Tasks are meant to be immutable. Avoid this if possible.
     */
    public void addTask(Task task) {
        this.tasks = (Task[]) ArrayUtils.add(tasks, task);
    }

    @Override
    public String getDescription() {
        if (description != null) {
            return description;
        } else {
            final StringBuffer buf = new StringBuffer();
            for (int i = 0; i < tasks.length; i++) {
                if (i > 0) {
                    buf.append(" "); // TODO : line break ?
                }
                buf.append(tasks[i].getDescription());
            }
            return buf.toString();
        }
    }

    @Override
    public void execute(InstallContext ctx) throws TaskExecutionException {
        for (Task task : tasks) {
            task.execute(ctx);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(tasks);
    }
}
