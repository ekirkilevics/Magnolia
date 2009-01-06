/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
