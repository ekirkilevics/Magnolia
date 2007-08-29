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
package info.magnolia.cms.security;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SecuritySupportImpl implements SecuritySupport {
    private final Map userManagers = new LinkedHashMap();
    private GroupManager groupManager;
    private RoleManager roleManager;

    /**
     * Returns a UserManager which is delegating to the configured UserManagers.
     * @see info.magnolia.cms.security.DelegatingUserManager
     */
    public UserManager getUserManager() {
        return new DelegatingUserManager(userManagers);
    }

    public UserManager getUserManager(String realmName) {
        if(Realm.REALM_ALL.equals(realmName)){
            return getUserManager();
        }
        return (UserManager) userManagers.get(realmName);
    }

    public Map getUserManagers() {
        return userManagers;
    }

    public void addUserManager(String realmName, UserManager delegate) {
        userManagers.put(realmName, delegate);
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }
}
