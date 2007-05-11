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

import info.magnolia.cms.security.auth.callback.Base64CallbackHandler;
import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.callback.PlainTextCallbackHandler;

import javax.security.auth.Subject;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Id$
 */
public final class Authenticator {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Authenticator.class);

    /**
     * Session attribute holding the magnolia user id.
     */
    static final String ATTRIBUTE_USER_ID = "mgnlUserId";

    /**
     * session attribute holding authenticated JAAS subject
     */
    static final String ATTRIBUTE_JAAS_SUBJECT = "mgnlJAASSubject";

    /**
     * request parameter user id
     */
    public static final String PARAMETER_USER_ID = "mgnlUserId";

    /**
     * request parameter password
     */
    public static final String PARAMETER_PSWD = "mgnlUserPSWD";

    /**
     * request attribute holding the login exception
     */
    protected static final String ATTRIBUTE_LOGINERROR = "mgnlLoginError";

    /**
     * Utility class, don't instantiate.
     */
    private Authenticator() {
        // unused
    }

    /**
     * Authenticate authorization request using JAAS login module as configured
     * @param request as received by the servlet engine
     * @return boolean
     * @deprecated Since 3.1 use LoginFilter->LoginHandlers
     */
    public static boolean authenticate(HttpServletRequest request) throws LoginException {
        log.warn("Deprecated: Since 3.1 use LoginFilter->LoginHandlers");
        String credentials = request.getHeader("Authorization");
        CredentialsCallbackHandler callbackHandler;

        if (StringUtils.isEmpty(credentials) || credentials.length() <= 6) {
            // check for form based login request
            if (StringUtils.isNotEmpty(request.getParameter(PARAMETER_USER_ID))) {
                String userid = request.getParameter(PARAMETER_USER_ID);
                String pswd = StringUtils.defaultString(request.getParameter(PARAMETER_PSWD));
                callbackHandler = new PlainTextCallbackHandler(userid, pswd.toCharArray());
            }
            else {
                // select login module to use if user is authenticated against the container
                if (request.getUserPrincipal() != null) {
                    callbackHandler = new PlainTextCallbackHandler(request.getUserPrincipal().getName(), ""
                        .toCharArray());
                    return authenticate(request, callbackHandler, "magnolia_authorization");
                }

                // invalid auth request
                return false;
            }
        }
        else {
            // its a basic authentication request
            callbackHandler = new Base64CallbackHandler(credentials);
        }

        return authenticate(request, callbackHandler);
    }

    /**
     * Authenticate using the given CredentialsCallbackHandler and the default login module.
     * @param request HttpServletRequest
     * @param callbackHandler CredentialsCallbackHandler instance
     * @return <code>true</code> if the authentication request succeeds
     * @throws LoginException if the authentication request is not valid
     */
    public static boolean authenticate(HttpServletRequest request, CredentialsCallbackHandler callbackHandler)
        throws LoginException {
        return authenticate(request, callbackHandler, null);
    }

    /**
     * Authenticate using the given CredentialsCallbackHandler and a custom login module.
     * @param request HttpServletRequest
     * @param callbackHandler CredentialsCallbackHandler instance
     * @param customLoginModule login module to use, null for the default (magnolia) module
     * @return <code>true</code> if the authentication request succeeds
     * @throws LoginException if the authentication request is not valid
     */
    public static boolean authenticate(HttpServletRequest request, CredentialsCallbackHandler callbackHandler,
        String customLoginModule) throws LoginException {
        Subject subject;
        try {
            LoginContext loginContext = new LoginContext(
                StringUtils.defaultString(customLoginModule, "magnolia"),
                callbackHandler);
            loginContext.login();
            subject = loginContext.getSubject();
            // ok, we NEED a session here since the user has been authenticated
            HttpSession httpsession = request.getSession(true);
            httpsession.setAttribute(ATTRIBUTE_JAAS_SUBJECT, subject);

            request.removeAttribute(ATTRIBUTE_LOGINERROR);
        }
        catch (FailedLoginException fle) {

            request.setAttribute(ATTRIBUTE_LOGINERROR, fle);

            if (log.isDebugEnabled()) {
                log.debug("Wrong credentials", fle);
            }
            HttpSession httpsession = request.getSession(false);
            if (httpsession != null) {
                httpsession.invalidate();
            }
            return false;
        }
        catch (LoginException le) {
            throw le;
        }

        return true;
    }

    /**
     * @param credentials to be decoded
     * @return String decoded credentials <b>name:password </b>
     */
    private static String getDecodedCredentials(String credentials) {
        return (new String(Base64.decodeBase64(credentials.getBytes())));
    }

    /**
     * @param request current HttpServletRequest
     * @return String , current logged in user
     */
    public static String getUserId(HttpServletRequest request) {
        String userId = null;

        HttpSession httpsession = request.getSession(false);
        if (httpsession != null) {
            userId = (String) httpsession.getAttribute(ATTRIBUTE_USER_ID);
        }

        if (userId == null) {
            String credentials = request.getHeader("Authorization");
            if (credentials != null) {
                try {
                    userId = getDecodedCredentials(credentials.substring(6).trim());
                    if (httpsession != null) {
                        httpsession.setAttribute(ATTRIBUTE_USER_ID, userId);
                    }
                }
                catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
            else {
                return UserManager.ANONYMOUS_USER;
            }
        }

        return userId;
    }

    /**
     * @param request current HttpServletRequest
     * @return credentials , as received from the servlet request
     */
    public static String getCredentials(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    /**
     * checks user session for attribute "user node"
     * @param request current HttpServletRequest
     * @return <code>true</code> if the user is authenticated, <code>false</code> otherwise
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        HttpSession httpsession = request.getSession(false);
        if (httpsession != null) {
            Object user = httpsession.getAttribute(ATTRIBUTE_JAAS_SUBJECT);
            return user != null;
        }
        return false;
    }

    /**
     * Get JAAS authenticated subject
     * @param request
     * @return Authenticated JAAS subject
     */
    public static Subject getSubject(HttpServletRequest request) {
        HttpSession httpsession = request.getSession(false);
        if (httpsession != null) {
            return (Subject) httpsession.getAttribute(ATTRIBUTE_JAAS_SUBJECT);
        }
        return null;
    }
}