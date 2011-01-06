/**
 * This file Copyright (c) 2009-2011 Magnolia International
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

import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.module.InstallContext;

/**
 * A task to add a permission to a role, using {@link info.magnolia.cms.security.RoleManager}.
 *
 * @author pbaerfuss
 * @version $Id$
 */
public class AddPermissionTask extends AbstractTask {

    private String roleName;
    private String workspaceName;
    private String path;
    private long permission;
    private boolean includingSubNodes;

    public AddPermissionTask(String taskName, String taskDescription, String roleName, String workspaceName, String path, long permission, boolean includingSubNodes) {
        super(taskName, taskDescription);
        this.roleName = roleName;
        this.workspaceName = workspaceName;
        this.path = path;
        this.permission = permission;
        this.includingSubNodes = includingSubNodes;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        SecuritySupport securitySupport = SecuritySupport.Factory.getInstance();
        RoleManager roleManager = securitySupport.getRoleManager();
        Role role = roleManager.getRole(roleName);
        if(role == null){
            ctx.warn("Role \"" + roleName + "\" not found, can't add permissions to " + path + ".");
        }
        else{
            try {
                role.addPermission(workspaceName, path, permission);
                if(includingSubNodes){
                    role.addPermission(workspaceName, path + "/*", permission);
                }
            }
            catch (UnsupportedOperationException e) {
                ctx.warn("Can't update role \"" + roleName + "\" due to an unsupported operation exception. This is most likely the case if the roles are managed externally.");
            }
        }
    }
}
