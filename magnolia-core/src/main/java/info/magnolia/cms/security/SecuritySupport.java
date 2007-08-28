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

import info.magnolia.cms.util.FactoryUtil;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface SecuritySupport {

    /**
     * Returns a generic UserManager, either for a default realm, or an implementation which delegates to other UserManager instances.
     */
    UserManager getUserManager();

    /**
     * Returns a UserManager for the given realm.
     */
    UserManager getUserManager(String realmName);

    GroupManager getGroupManager();

    RoleManager getRoleManager();

    public final static class Factory {
        public static SecuritySupport getInstance() {
            return (SecuritySupport) FactoryUtil.getSingleton(SecuritySupport.class);
        }
    }
}
