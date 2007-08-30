/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Responsible to handle system users like anonymous and supperuser
 * @author philipp
 * @version $Id$
 */
public class SystemUserManger extends MgnlUserManager {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SystemUserManger.class);

    public User getSystemUser() {
        return getOrCreateUser(UserManager.SYSTEM_USER, UserManager.SYSTEM_PSWD);
    }

    public User getAnonymousUser() {
        return getOrCreateUser(UserManager.ANONYMOUS_USER, "");
    }

    protected User getOrCreateUser(String userName, String password) {
        User user = getUser(userName);
        if(user == null){
            log.error("failed to get system or anonymous user [{}]", userName);
            log.info("Try to create new system user with default password");
            user = this.createUser(userName, password);
        }
        return user;
    }
}
