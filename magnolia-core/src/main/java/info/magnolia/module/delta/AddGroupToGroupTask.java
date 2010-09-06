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
package info.magnolia.module.delta;

import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;

/**
 * A task to nest a group inside another, using {@link info.magnolia.cms.security.GroupManager}.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddGroupToGroupTask extends AbstractRepositoryTask {
    private final String parentGroupName;
    private final String nestedGroupName;

    public AddGroupToGroupTask(String taskName, String nestedGroupName, String parentGroupName) {
        super(taskName, "Adding group \"" + nestedGroupName + "\" to group \"" + parentGroupName + "\"");
        this.parentGroupName = parentGroupName;
        this.nestedGroupName = nestedGroupName;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final GroupManager groupManager = SecuritySupport.Factory.getInstance().getGroupManager();
        final Group group = groupManager.getGroup(parentGroupName);
        if (group == null) {
            ctx.warn("Group \"" + parentGroupName + "\" not found, can't add \"" + nestedGroupName + "\" to its sub-groups.");
        } else {
            // TODO this saves at node level, thus breaking the "save once per module install/update" rule :(
            try{
                group.addGroup(nestedGroupName);
            }
            catch (UnsupportedOperationException e) {
                ctx.warn("Can't add the group \"" + nestedGroupName + "\" to the \"" + parentGroupName+ "\" group due to an unsupported operation exception. This is most likely the case if the groups are managed externally.");
            }
        }
    }
}