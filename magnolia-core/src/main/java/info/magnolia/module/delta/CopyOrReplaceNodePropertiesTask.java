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
import info.magnolia.cms.core.NodeData;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Copies a node's properties to another node. Existing properties are overwritten and
 * extra properties on the target node are left untouched.
 * Only works with properties of type String!
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CopyOrReplaceNodePropertiesTask extends AbstractRepositoryTask {
    private final String workspaceName;
    private final String sourceNodePath;
    private final String targetNodePath;

    public CopyOrReplaceNodePropertiesTask(String name, String description, String workspaceName, String sourceNodePath, String targetNodePath) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.sourceNodePath = sourceNodePath;
        this.targetNodePath = targetNodePath;
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspaceName);
        final Content source = hm.getContent(sourceNodePath);
        final Content target = hm.getContent(targetNodePath);
        final Collection props = source.getNodeDataCollection();
        final Iterator it = props.iterator();
        while (it.hasNext()) {
            final NodeData prop = (NodeData) it.next();
            final String name = prop.getName();
            final String value = prop.getString();
            if (target.hasNodeData(name)) {
                target.getNodeData(name).setValue(value);
            } else {
                target.createNodeData(name, value);
            }
        }

    }
}
