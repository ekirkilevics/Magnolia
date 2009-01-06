/**
 * This file Copyright (c) 2007-2009 Magnolia International
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
package info.magnolia.setup.for3_6_2;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.NodeTypeFilter;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllChildrenNodesOperation;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Updates all users to add an extra permission to read their own configuration node..
 * @author had
 * @version $Id: $
 *
 */
public class UpdateRoles extends AllChildrenNodesOperation {

    private static Logger log = LoggerFactory.getLogger(UpdateRoles.class);

    public UpdateRoles() {
        super("Roles definition update", "Adds right to read their own node to all existing roles.", ContentRepository.USER_ROLES,  "/", new NodeTypeFilter(ItemType.ROLE));
    }

    public void operateOnChildNode(Content role, InstallContext installContext)
        throws RepositoryException, TaskExecutionException {
        try {
            String handle = role.getHandle();
            boolean hasAccess = false;
            Content acls = role.getChildByName("acl_roles");
            if (acls == null) {
                acls = role.createContent("acl_roles", ItemType.CONTENTNODE);
                role.save();
            }
            Iterator iter2 = acls.getChildren().iterator();
            while (iter2.hasNext()) {
                Content permission = (Content)iter2.next();
                if (handle.equals(permission.getNodeData("path").getString()) && (permission.getNodeData("permissions").getLong() >= Permission.READ)) {
                    hasAccess = true;
                    break;
                }
            }
            if (!hasAccess) {
                Content acl = acls.createContent(Path.getUniqueLabel(installContext.getHierarchyManager(ContentRepository.USER_ROLES), acls.getHandle(), "0"), ItemType.CONTENTNODE);
                acl.createNodeData("path", handle);
                acl.createNodeData("permissions", new Long(Permission.READ));
                acls.save();
            }
            // base role needs special handling
            if ("base".equals(role.getName())) {
                // add read permission to root of all workspaces and deny to all the children of the root
                Iterator iter = role.getChildren(ItemType.CONTENTNODE).iterator();
                while (iter.hasNext()) {
                    Content acl = (Content) iter.next();
                    Iterator iter3 = acl.getChildren().iterator();
                    boolean found = false;
                    while (iter3.hasNext()) {
                        Content permission = (Content) iter3.next();
                        if ("/*".equals(permission.getNodeData("path").getString())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Content permission = acl.createContent(Path.getUniqueLabel(installContext.getHierarchyManager(ContentRepository.USER_ROLES), acl.getHandle(), "0"), ItemType.CONTENTNODE);
                        permission.createNodeData("path", "/");
                        permission.createNodeData("permissions", new Long(Permission.READ));
                        acl.save();
                        permission = acl.createContent(Path.getUniqueLabel(installContext.getHierarchyManager(ContentRepository.USER_ROLES), acl.getHandle(), "0"), ItemType.CONTENTNODE);
                        permission.createNodeData("path", "/*");
                        permission.createNodeData("permissions", new Long(Permission.NONE));
                        acl.save();
                    }
                }
            }

        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new TaskExecutionException("Failed to update user permissions. See log file for more details.");
        }
    }
}
