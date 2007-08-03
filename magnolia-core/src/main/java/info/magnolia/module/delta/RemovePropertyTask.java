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
 * Removes a property and optionally logs its absence.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RemovePropertyTask extends AbstractRepositoryTask {
    private final String workspaceName;
    private final String parentPath;
    private final String propertyToRemove;

    public RemovePropertyTask(String name, String description, String workspaceName, String parentPath, String propertyToRemove) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.parentPath = parentPath;
        this.propertyToRemove = propertyToRemove;
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspaceName);
        if (!hm.isExist(parentPath)) {
            ctx.warn("Was supposed to remove property " + propertyToRemove + " at " + parentPath + " but the node was not found in workspace " + workspaceName);
            return;
        }
        final Content node = hm.getContent(parentPath);
        if (node.hasNodeData(propertyToRemove)) {
            node.deleteNodeData(propertyToRemove);
        } else {
            ctx.info(parentPath + "/" + propertyToRemove + " was supposed to be removed, but wasn't found.");
        }
    }
}
