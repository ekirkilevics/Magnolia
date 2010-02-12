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
package info.magnolia.module.delta;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.MgnlRole;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContext;

/**
 * @author zdenekskodik
 * @version $Id$
 *
 */
public class RemovePermissionTask extends AbstractTask {

    private String roleName;
    private String workspaceName;
    private String pathToRemove;
    private long permission;
    
    public RemovePermissionTask(String taskName, String taskDescription, String roleName, String workspaceName, String pathToRemove, long permission) {
        super(taskName, taskDescription);
        this.roleName = roleName;
        this.workspaceName = workspaceName;
        this.pathToRemove = pathToRemove;
        this.permission = permission;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        SecuritySupport securitySupport = SecuritySupport.Factory.getInstance();
        RoleManager roleManager = securitySupport.getRoleManager();
        Role role = roleManager.getRole(roleName);

        if (role != null) {   
            try {  
                HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.USER_ROLES);
                Content roleNode = hm.getContent(roleName);
                Content aclNode = roleNode.getContent("acl_" + workspaceName);
                role.removePermission(workspaceName, pathToRemove, permission);
                if (this.existsPermission(aclNode, pathToRemove + "/*", permission)) {
                    role.removePermission(workspaceName, pathToRemove + "/*", permission); 
                }
            } catch (UnsupportedOperationException e1) {
                ctx.warn("Can't update role \"" + roleName + "\" due to an unsupported operation exception. This is most likely the case if the roles are managed externaly.");
            } catch (PathNotFoundException e2) {
                ctx.warn("Role \"" + roleName + "\" doesn't contain permission to specified path " + pathToRemove + ".");
            } catch (RepositoryException e3) {
                ctx.warn("Workspace " + workspaceName + " not found. Check its syntax.");
            }
        }
        else {
            ctx.warn("Role \"" + roleName + "\" not found, can't remove its ACL permission.");
        }              
    }
    
    private boolean existsPermission(Content aclNode, String path, long permission) {
        Collection children = aclNode.getChildren();
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            if (child.getNodeData("path").getString().equals(path)) {
                if (permission == MgnlRole.PERMISSION_ANY
                    || child.getNodeData("permissions").getLong() == permission) {
                    return true;
                }
            }
        }
        return false;
    }
}