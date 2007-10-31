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
package info.magnolia.setup.for3_1;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllChildrenNodesOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddURIPermissionsToAllRoles extends AllChildrenNodesOperation {
    private static final int ALLOW_ALL = 63;
    private static final int DENY = 0;

    private final boolean isAuthorInstance;

    public AddURIPermissionsToAllRoles(boolean isAuthorInstance) {
        super("URI permissions", "Introduction of URI-based security. All existing roles will have GET/POST permissions on /*.", ContentRepository.USER_ROLES, "/");
        this.isAuthorInstance = isAuthorInstance;
    }

    protected void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Content uriPermissionsNode = node.createContent("acl_uri", ItemType.CONTENTNODE);
        if ("anonymous".equals(node.getName())) {
            if (isAuthorInstance) {
                addPermission(uriPermissionsNode, "0", "/*", DENY);
            } else {
                addPermission(uriPermissionsNode, "0", "/*", ALLOW_ALL);
                addPermission(uriPermissionsNode, "00", "/.magnolia", DENY);
                addPermission(uriPermissionsNode, "01", "/.magnolia/*", DENY);
            }
        } else {
            addPermission(uriPermissionsNode, "0", "/*", ALLOW_ALL);
        }
    }

    private void addPermission(Content uriRepoNode, String permNodeName, String path, long value) throws RepositoryException {
        final Content permNode = uriRepoNode.createContent(permNodeName, ItemType.CONTENTNODE);
        permNode.createNodeData("path", path);
        permNode.createNodeData("permissions", new Long(value));
    }
}
