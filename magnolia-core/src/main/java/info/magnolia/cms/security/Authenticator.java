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

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import info.magnolia.cms.security.auth.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.MD5CallbackHandler;
import info.magnolia.cms.security.auth.Base64CallbackHandler;


/**
 * @author Sameer Charles
 * @version 2.0
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
     * Utility class, don't instantiate.
     */
    private Authenticator() {
        // unused
    }

    /**
     * Authenticate authorization request using JAAS login module as configured
     * @param request as received by the servlet engine
     * @return boolean
     */
    public static boolean authenticate(HttpServletRequest request) {
        String credentials = request.getHeader("Authorization");
        String userid;
        String pswd;
        CredentialsCallbackHandler callbackHandler;
        String loginModuleToInitialize = "magnolia"; // default login module

        if (StringUtils.isEmpty(credentials) || credentials.length() <= 6) {
            // check for form based login request
            if (StringUtils.isNotEmpty(request.getParameter(PARAMETER_USER_ID))) {
                userid = request.getParameter(PARAMETER_USER_ID);
                pswd = StringUtils.defaultString(request.getParameter(PARAMETER_PSWD));
                callbackHandler = new MD5CallbackHandler(userid, pswd.toCharArray());
            }
            else {
                // invalid auth request
                return false;
            }
        }
        else {
            // its a basic authentication request
            callbackHandler = new Base64CallbackHandler(credentials);
        }
        // select login module to use
        if (request.getUserPrincipal() != null) {
            loginModuleToInitialize = "magnolia_authorization";
        }

        Subject subject;
        try {
            LoginContext loginContext = new LoginContext(loginModuleToInitialize, callbackHandler);
            loginContext.login();
            subject = loginContext.getSubject();
            // ok, we NEED a session here since the user has been authenticated
            HttpSession httpsession = request.getSession(true);
            httpsession.setAttribute(ATTRIBUTE_JAAS_SUBJECT, subject);
        }
        catch (LoginException le) {
            if (log.isDebugEnabled())
                log.debug("Exception caught", le);

            HttpSession httpsession = request.getSession(false);
            if (httpsession != null) {
                httpsession.invalidate();
            }
            return false;
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