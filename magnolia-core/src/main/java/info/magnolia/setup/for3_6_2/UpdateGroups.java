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
public class UpdateGroups extends AllChildrenNodesOperation {

    private static Logger log = LoggerFactory.getLogger(UpdateGroups.class);

    public UpdateGroups() {
        super("Groups definition update", "Adds right to read their own node to all existing groups.", ContentRepository.USER_GROUPS,  "/", new NodeTypeFilter(ItemType.GROUP));
    }

    public void operateOnChildNode(Content group, InstallContext installContext)
        throws RepositoryException, TaskExecutionException {
        try {
            String handle = group.getHandle();
            boolean hasAccess = false;
            Content acls = group.getChildByName("acl_usergroups");
            if (acls == null) {
                acls = group.createContent("acl_usergroups", ItemType.CONTENTNODE);
                group.save();
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
                Content acl = acls.createContent(Path.getUniqueLabel(installContext.getHierarchyManager(ContentRepository.USER_GROUPS), acls.getHandle(), "0"), ItemType.CONTENTNODE);
                acl.createNodeData("path", handle);
                acl.createNodeData("permissions", new Long(Permission.READ));
                acls.save();
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new TaskExecutionException("Failed to update group permissions. See log file for more details.");
        }
    }
}
