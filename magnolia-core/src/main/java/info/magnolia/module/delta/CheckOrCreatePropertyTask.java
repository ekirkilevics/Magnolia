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
public class CheckOrCreatePropertyTask extends PropertyValuesTask {
    private final String workspaceName;
    private final String nodePath;
    private final String propertyName;
    private final String expectedValue;

    public CheckOrCreatePropertyTask(String name, String description, String workspaceName, String nodePath, String propertyName, String expectedValue) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.nodePath = nodePath;
        this.propertyName = propertyName;
        this.expectedValue = expectedValue;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspaceName);
        try {
            final Content node = hm.getContent(nodePath);
            checkOrCreateProperty(ctx, node, propertyName, expectedValue);
        } catch (RepositoryException e) {
            ctx.error(format("Could not check property {0} of node at {1} which was supposed to have value {2}, please create it.", propertyName, nodePath, expectedValue), e);
        }
    }

}
