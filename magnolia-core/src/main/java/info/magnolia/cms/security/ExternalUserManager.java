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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.api.HierarchyManager;
import info.magnolia.context.MgnlContext;

import java.util.Collection;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the JAAS users.
 * @author philipp
 * @version $Revision:9391 $ ($Author:scharles $)
 */
public class ExternalUserManager implements UserManager {

    /**
     * Logger
     */
    public static Logger log = LoggerFactory.getLogger(MgnlUserManager.class);

    public User getUser(String name) throws UnsupportedOperationException {
        // we only support accessing current User object
        // - implement source specific UserManager if needed
        if (name.equalsIgnoreCase(MgnlContext.getUser().getName())) {
            return MgnlContext.getUser();
        }
        throw new UnsupportedOperationException("not implemented yet");
    }

    public Collection getAllUsers() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public User createUser(String name, String pw) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Initialize new user using JAAS authenticated/authorized subject
     * @param subject
     * @throws UnsupportedOperationException
     */
    public User getUser(Subject subject) throws UnsupportedOperationException {
        return new ExternalUser(subject);
    }

    /**
     * Get system user, this user must always exist in magnolia repository.
     * @return system user
     */
    public User getSystemUser() {
        try {
            return new MgnlUser(getHierarchyManager().getContent(UserManager.SYSTEM_USER));
        }
        catch (Exception e) {
            log.error("can't find System user", e);
            log.info("Try to create new system user with default password");
            return this.createUser(UserManager.SYSTEM_USER, UserManager.SYSTEM_PSWD);
        }
    }

    /**
     * Get Anonymous user, this user must always exist in magnolia repository.
     * @return anonymous user
     */
    public User getAnonymousUser() {
        try {
            return new MgnlUser(getHierarchyManager().getContent(UserManager.ANONYMOUS_USER));
        }
        catch (Exception e) {
            log.error("can't find Anonymous user", e);
            log.info("Try to create new system user with default password");
            return this.createUser(UserManager.ANONYMOUS_USER, "");
        }
    }

    /**
     * return the user HierarchyManager
     */
    protected HierarchyManager getHierarchyManager() {
        return ContentRepository.getHierarchyManager(ContentRepository.USERS);
    }

}
