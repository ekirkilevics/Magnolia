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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.callback.PlainTextCallbackHandler;
import info.magnolia.cms.util.ObservationUtil;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible to handle system users like anonymous and superuser.
 * @author philipp
 * @version $Id$
 */
public class SystemUserManager extends MgnlUserManager {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SystemUserManager.class);

    /**
     * kept as static for performance reasons on live instance. reinitialized on any modification event on anonymous
     * role
     */
    private User anonymousUser;

    public SystemUserManager() {

        EventListener anonymousListener = new EventListener() {

            public void onEvent(EventIterator events) {
                anonymousUser = null;
                log.info("Anonymous user reloaded");
            }

        };

        final String anonymousUserPath = "/" + Realm.REALM_SYSTEM + "/" + UserManager.ANONYMOUS_USER;
        ObservationUtil.registerChangeListener(
            ContentRepository.USERS,
            anonymousUserPath,
            true,
            "mgnl:user",
            anonymousListener);

        ObservationUtil.registerChangeListener(
            ContentRepository.USER_GROUPS,
            "/",
            true,
            "mgnl:group",
            anonymousListener);

        ObservationUtil.registerDefferedChangeListener(
            ContentRepository.USER_ROLES,
            "/",
            true,
            "mgnl:role",
            anonymousListener,
            1000,
            5000);
    }

    public User getSystemUser() {
        return getOrCreateUser(UserManager.SYSTEM_USER, UserManager.SYSTEM_PSWD);
    }

    public User getAnonymousUser() {
        if(anonymousUser == null){
            anonymousUser = getOrCreateUser(UserManager.ANONYMOUS_USER, "");
            anonymousUser.setSubject(getAnonymousSubject());
        }
        return anonymousUser;
    }

    protected User getOrCreateUser(String userName, String password) {
        User user = getUser(userName);
        if (user == null) {
            log.error("Failed to get system or anonymous user [{}], will try to create new system user with default password", userName);
            user = this.createUser(userName, password);
        }
        return user;
    }
    
    protected Subject getAnonymousSubject() {
        CredentialsCallbackHandler callbackHandler = new PlainTextCallbackHandler(
            getAnonymousUser().getName(),
            getAnonymousUser().getPassword().toCharArray(),
            Realm.REALM_SYSTEM);
        try {
            LoginContext loginContext = new LoginContext("magnolia", callbackHandler);
            loginContext.login();
            return loginContext.getSubject();
        }
        catch (LoginException le) {
            log.error("Failed to login as anonymous user", le);
        }
        return null;
    }

}
