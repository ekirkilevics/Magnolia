/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.setup.for4_3;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.NodeTypeFilter;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllChildrenNodesOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Updates all users to add an extra permission to read their own configuration node..
 * @author had
 * @version $Id: $
 *
 */
public class UpdateUserPermissions extends AllChildrenNodesOperation {

    private static Logger log = LoggerFactory.getLogger(UpdateUserPermissions.class);

    public UpdateUserPermissions() {
        super("User definition update", "Changes user rights to allow properties updates while preventing user from modifying ACLs unintentionally.", ContentRepository.USERS,  "/", new NodeTypeFilter(ItemType.FOLDER));
    }

    public void operateOnChildNode(Content node, InstallContext installContext)
        throws RepositoryException, TaskExecutionException {
        try {
            for (Content user : node.getChildren(ItemType.USER)) {
                String handle = user.getHandle();
                boolean hadAccess = false;
                Content acls = user.getContent("acl_users");
                if (acls == null) {
                    // not a proper user node just skip over.
                    installContext.warn("User " + user.getName() + " doesn't seem to be properly configured. Account path is " + handle + ".");
                    continue;
                }
                for (Content permission : acls.getChildren()) {
                    // remove write access to own node (if found)
                    if ((handle + "/*").equals(permission.getNodeData("path").getString()) && (permission.getNodeData("permissions").getLong() >= Permission.WRITE)) {
                        hadAccess = true;
                        permission.delete();
                        break;
                    }
                }
                if (hadAccess) {
                    // those who had access to their nodes should get access to their own props
                    addWrite(handle, MgnlUserManager.PROPERTY_EMAIL, acls);
                    addWrite(handle, MgnlUserManager.PROPERTY_LANGUAGE, acls);
                    addWrite(handle, MgnlUserManager.PROPERTY_LASTACCESS, acls);
                    addWrite(handle, MgnlUserManager.PROPERTY_PASSWORD, acls);
                    addWrite(handle, MgnlUserManager.PROPERTY_TITLE, acls);
                    // and of course the meta data
                    addWrite(handle, MetaData.DEFAULT_META_NODE, acls);
                    acls.save();
                }
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new TaskExecutionException("Failed to update user permissions. See log file for more details.");
        }
    }

    private Content addWrite(String parentPath, String property, Content acls) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content acl = acls.createContent(Path.getUniqueLabel(acls.getHierarchyManager(), acls.getHandle(), "0"), ItemType.CONTENTNODE);
        acl.setNodeData("path", parentPath + "/" + property);
        acl.setNodeData("permissions", new Long(Permission.ALL));
        return acl;
    }
}
