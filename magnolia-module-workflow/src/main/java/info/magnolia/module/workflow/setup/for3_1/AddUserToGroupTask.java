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
