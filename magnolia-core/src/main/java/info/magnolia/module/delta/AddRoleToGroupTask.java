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

import javax.jcr.RepositoryException;

import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.module.InstallContext;

/**
 * A task to add a role to a group, using {@link info.magnolia.cms.security.GroupManager}.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddRoleToGroupTask extends AbstractRepositoryTask {
    private final String roleName;
    private final String groupName;

    public AddRoleToGroupTask(String taskName, String roleName, String groupName) {
        super(taskName, "Adding role \"" + roleName + "\" to group \"" + groupName + "\"");
        this.roleName = roleName;
        this.groupName = groupName;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        GroupManager groupManager = SecuritySupport.Factory.getInstance().getGroupManager();
        Group group = groupManager.getGroup(groupName);
        if (group == null) {
            ctx.warn("Group \"" + groupName + "\" not found, can't add the \"" + roleName + "\" role.");
        } else {
            // TODO this saves at node level, thus breaking the "save once per module install/update" rule :(
            try{
                group.addRole(roleName);
            }
            catch (UnsupportedOperationException e) {
                ctx.warn("Can't add the role \"" + roleName + "\" to the \"" + groupName + "\" group due to an unsupported operation exception. This is most likely the case if the groups are managed externally.");
            }
        }
    }
}
