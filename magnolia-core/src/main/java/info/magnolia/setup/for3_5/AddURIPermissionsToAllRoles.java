/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.setup.for3_5;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllChildrenNodesOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddURIPermissionsToAllRoles extends AllChildrenNodesOperation {
    private static final int ALLOW_ALL = 63;
    private static final int DENY = 0;

    private final boolean isAuthorInstance;

    private static Logger log = LoggerFactory.getLogger(AllChildrenNodesOperation.class);

    public AddURIPermissionsToAllRoles(boolean isAuthorInstance) {
        super("URI permissions", "Introduction of URI-based security. All existing roles will have GET/POST permissions on /*.", ContentRepository.USER_ROLES, "/", new Content.ContentFilter() {
            public boolean accept(Content content) {
                try {
                    final String itemType = content.getItemType().getSystemName();
                    // TODO reconsider after 3.5 final: is ItemType.ROLE enough here?
                    return itemType.startsWith("mgnl:") && !itemType.equals(ItemType.NT_METADATA);
                }
                catch (RepositoryException e) {
                    log.error("Unable to read itemtype for node {}", content.getHandle());
                    return false;
                }
            }
            
        });
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
