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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;

/**
 * A task which delegates to another if a property has a given value.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PropertyValueDelegateTask extends NodeCheckDelegateTask {
    private final String expectedValue;
    private final boolean propertyMustExist;

    public PropertyValueDelegateTask(String taskName, String taskDescription, String workspaceName, String nodePath, String propertyName, String expectedValue, boolean propertyMustExist, Task ifTrue) {
        this(taskName, taskDescription, workspaceName, nodePath, propertyName, expectedValue, propertyMustExist, ifTrue, null);
    }

    /**
     * @param propertyMustExist if false, the condition() method will evaluate to false even if the property does not exist. If true and the property does not exist, will throw a TaskExecutionException.
     * TODO : warning or exception ? consistency between this and other tasks for these kind of cases !
     */
    public PropertyValueDelegateTask(String taskName, String taskDescription, String workspaceName, String nodePath, String propertyName, String expectedValue, boolean propertyMustExist, Task ifTrue, Task ifFalse) {
        super(taskName, taskDescription, workspaceName, nodePath, propertyName, ifTrue, ifFalse);
        this.expectedValue = expectedValue;
        this.propertyMustExist = propertyMustExist;
    }

    protected boolean checkNode(Content node, InstallContext ctx) throws TaskExecutionException, RepositoryException {
        if (node.hasNodeData(propertyName)) {
            final NodeData prop = node.getNodeData(propertyName);
            return expectedValue.equals(prop.getString());
        } else if (propertyMustExist) {
            throw new TaskExecutionException("Property \"" + propertyName + "\" was expected to exist at " + nodePath);
        } else {
            ctx.warn("Property \"" + propertyName + "\" was expected to be found at " + nodePath + " but does not exist.");
            return false;
        }
    }
}
