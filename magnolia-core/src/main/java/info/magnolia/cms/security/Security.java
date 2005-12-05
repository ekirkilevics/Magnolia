/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import info.magnolia.cms.util.FactoryUtil;


/**
 * Get the current role or user manager.
 * @author philipp
 * @version $Revision$ ($Author$)
 *
 */
public class Security {

    /**
     * Returns the current RoleManager. Depends on the configuration.
     * @return
     */
    public static RoleManager getRoleManager() {
        return (RoleManager) FactoryUtil.getSingleton(RoleManager.class);
    }

    /**
     * Returns the current UserManager. Depends on the configuration.
     * @return
     */
    public static UserManager getUserManager() {
        return (UserManager) FactoryUtil.getSingleton(UserManager.class);
    }

}
