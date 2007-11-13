/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.workflow.setup.for3_1;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddUserToGroupTask extends AbstractTask {
    private static final String REALM = "admin";
    private final String username;
    private final String groupname;

    public AddUserToGroupTask(String taskName, String username, String groupname) {
        super(taskName, "Adding user \"" + username + "\" to group \"" + groupname + "\"");
        this.username = username;
        this.groupname = groupname;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        final UserManager userManager = SecuritySupport.Factory.getInstance().getUserManager(REALM);
        final User user = userManager.getUser(username);
        if (user == null) {
            ctx.warn("User \"" + username + "\" not found, can't add him/her to the \"" + groupname + "\" group.");
        } else {
            // TODO this saves at node level, thus breaking the "save once per module install/update" rule :( 
            user.addGroup(groupname);
        }
    }
}
