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
package info.magnolia.context;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.callback.PlainTextCallbackHandler;
import info.magnolia.cms.util.ObservationUtil;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles $Id$
 */
public class AnonymousContext extends WebContextImpl {

    private static final Logger log = LoggerFactory.getLogger(AnonymousContext.class);

    /**
     * kept as static for performance reasons on live instance. reinitialized on any modification event on anonymous
     * role
     */
    private static Subject anonymousSubject;

    private static User anonymousUser;

    static {
        final String anonymousUserPath = "/" + Realm.REALM_SYSTEM + "/" + UserManager.ANONYMOUS_USER;
        ObservationUtil.registerChangeListener(
            ContentRepository.USERS,
            anonymousUserPath,
            true,
            "mgnl:user",
            new EventListener() {

                public void onEvent(EventIterator events) {
                    reset();
                }
            });

        ObservationUtil.registerChangeListener(
            ContentRepository.USER_GROUPS,
            "/",
            true,
            "mgnl:group",
            new EventListener() {

                public void onEvent(EventIterator events) {
                    reset();
                }
            });

        ObservationUtil.registerDefferedChangeListener(
            ContentRepository.USER_ROLES,
            "/",
            true,
            "mgnl:role",
            new EventListener() {

                public void onEvent(EventIterator events) {
                    reset();
                }
            },
            1000,
            5000);
    }

    /**
     * @deprecated Use {@link #init(HttpServletRequest,HttpServletResponse,ServletContext)} instead
     */

    public void init(HttpServletRequest request, HttpServletResponse response) {
        init(request, response, null);
    }

    public void init(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        super.init(request, response, servletContext);

    }

    public User getUser() {
        return getAnonymousUser();
    }

    protected Subject getSubject() {
        return getAnonymousSubject();
    }

    private static Subject getAnonymousSubject() {
        if (null == anonymousSubject) {
            setAnonymousSubject();
        }
        return anonymousSubject;
    }

    private static void setAnonymousSubject() {
        CredentialsCallbackHandler callbackHandler = new PlainTextCallbackHandler(
            getAnonymousUser().getName(),
            getAnonymousUser().getPassword().toCharArray(),
            Realm.REALM_SYSTEM);
        try {
            LoginContext loginContext = new LoginContext("magnolia", callbackHandler);
            loginContext.login();
            anonymousSubject = loginContext.getSubject();
        }
        catch (LoginException le) {
            log.error("Failed to login as anonymous user", le);
        }
    }

    private static User getAnonymousUser() {
        if (null == anonymousUser) {
            setAnonymousUser();
        }
        return anonymousUser;
    }

    private static void setAnonymousUser() {
        anonymousUser = Security.getUserManager().getAnonymousUser();
    }

    private synchronized static void reset() {
        setAnonymousSubject();
        setAnonymousUser();
        log.info("Anonymous context reloaded");
    }

    /**
     * We do not want to loose the hierarchy managers of the anonymous (default) user. Overriding super class' logout
     * method prevents from closing JCR sessions.
     */

    public void logout() {
        // do nothing
    }
}
