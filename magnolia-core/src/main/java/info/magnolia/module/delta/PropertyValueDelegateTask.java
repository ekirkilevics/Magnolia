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
