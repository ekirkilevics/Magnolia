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

/**
 * Get the current role or user manager.
 * @author philipp
 * @version $Revision$ ($Author$)
 *
 * @deprecated use SecurityManager.Factory.getInstance()
 */
public class Security {

    /**
     * Returns the configured RoleManager.
     */
    public static RoleManager getRoleManager() {
        return getSecurityManager().getRoleManager();
    }

    /**
     * Returns the configured UserManager.
     */
    public static UserManager getUserManager() {
        return getSecurityManager().getUserManager();
    }

    /**
     * Returns the configured GroupManager.
     */
    public static GroupManager getGroupManager() {
        return getSecurityManager().getGroupManager();
    }

    private static SecurityManager getSecurityManager() {
        return SecurityManager.Factory.getInstance();
    }
}
