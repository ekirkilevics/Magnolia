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
 * To be used as a replacement of /server/security or SecuritySupportImpl in mgnl-beans.properties
 * in case the configuration is messed up. For instance, edit
 * <code>WEB-INF/config/default/magnolia.properties</code> and add
 * <pre>info.magnolia.cms.security.SecuritySupport=info.magnolia.cms.security.RescueSecuritySupport</pre>
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RescueSecuritySupport implements SecuritySupport {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RescueSecuritySupport.class);

    public RescueSecuritySupport() {
        super();
        log.warn("Using RescueSecuritySupport !");
    }

    public UserManager getUserManager() {
        log.warn("Using RescueSecuritySupport, will instanciate SystemUserManager, please fix your configuration !");
        return new SystemUserManager();
    }

    public UserManager getUserManager(String realmName) {
        log.warn("Using RescueSecuritySupport, will instanciate SystemUserManager, please fix your configuration !");
        return new SystemUserManager();
    }

    public GroupManager getGroupManager() {
        log.warn("Using RescueSecuritySupport, will instanciate MgnlGroupManager, please fix your configuration !");
        return new MgnlGroupManager();
    }

    public RoleManager getRoleManager() {
        log.warn("Using RescueSecuritySupport, will instanciate MgnlRoleManager, please fix your configuration !");
        return new MgnlRoleManager();
    }
}
