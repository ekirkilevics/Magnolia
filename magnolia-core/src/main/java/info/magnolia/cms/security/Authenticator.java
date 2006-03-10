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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
     * Session attribute holding the magnolia user password.
     */
    static final String ATTRIBUTE_PSWD = "mgnlUserPSWD";

    /**
     * Session attribute holding the magnolia user node from the jcr repository.
     */
    static final String ATTRIBUTE_USER = "mgnlUser";

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
     * @param req as received by the servlet engine
     * @return boolean
     */
    public static boolean authenticate(HttpServletRequest req) {
        String credentials = req.getHeader("Authorization");
        if (StringUtils.isEmpty(credentials) || credentials.length() <= 6) {
            // check for form based login request
            if (StringUtils.isNotEmpty(req.getParameter(PARAMETER_USER_ID))) {
                setFormAuthProperties(req);
            }
            else {
                return false;
            }
        }
        else {
            setBasicAuthProperties(credentials, req);
        }
        Subject subject;
        // first check if user has been authenticated by some other service or container itself
        if (req.getUserPrincipal() == null) {
            // JAAS authentication
            CredentialsCallbackHandler callbackHandler = new CredentialsCallbackHandler(
                getUserId(req),
                getPassword(req));
            try {
                LoginContext loginContext = new LoginContext("magnolia", callbackHandler);
                loginContext.login();
                subject = loginContext.getSubject();
                req.getSession().setAttribute(ATTRIBUTE_JAAS_SUBJECT, subject);
            }
            catch (LoginException le) {
                log.debug("Exception caught", le);
                req.getSession().invalidate();
                return false;
            }
        }
        else {
            // user already authenticated via JAAS, try to load roles for it via configured authorization module
            String userName = req.getUserPrincipal().getName();
            CredentialsCallbackHandler callbackHandler = new CredentialsCallbackHandler(userName, getPassword(req));
            try {
                LoginContext loginContext = new LoginContext("magnolia_authorization", callbackHandler);
                loginContext.login();
                subject = loginContext.getSubject();
                req.getSession().setAttribute(ATTRIBUTE_JAAS_SUBJECT, subject);
            }
            catch (LoginException le) {
                log.debug("Exception caught", le);
                req.getSession().invalidate();
                return false;
            }
        }
        return true;
    }

    /**
     * set basic authentication properties
     * @param credentials
     * @param request
     */
    private static void setBasicAuthProperties(String credentials, HttpServletRequest request) {
        credentials = getDecodedCredentials(credentials.substring(6).trim());
        Authenticator.setUserId(StringUtils.substringBefore(credentials, ":"), request);
        Authenticator.setPassword(StringUtils.substringAfter(credentials, ":"), request);
    }

    /**
     * set form authentication properties
     * @param request
     */
    private static void setFormAuthProperties(HttpServletRequest request) {
        Authenticator.setUserId(request.getParameter(PARAMETER_USER_ID), request);
        String pswd = request.getParameter(PARAMETER_PSWD);
        if (pswd == null) {
            pswd = "";
        }
        Authenticator.setPassword(pswd, request);
    }

    /**
     * @param credentials to be decoded
     * @return String decoded credentials <b>name:password </b>
     */
    private static String getDecodedCredentials(String credentials) {
        return (new String(Base64.decodeBase64(credentials.getBytes())));
    }

    /**
     * @param userName
     * @param request current HttpServletRequest
     */
    private static void setUserId(String userName, HttpServletRequest request) {
        request.getSession().setAttribute(ATTRIBUTE_USER_ID, userName);
    }

    /**
     * @param request current HttpServletRequest
     * @param pswd
     */
    private static void setPassword(String pswd, HttpServletRequest request) {
        request.getSession().setAttribute(ATTRIBUTE_PSWD, pswd);
    }

    /**
     * @param request current HttpServletRequest
     * @return String , current logged in user
     */
    public static String getUserId(HttpServletRequest request) {
        Object userId = request.getSession().getAttribute(ATTRIBUTE_USER_ID);
        if (userId == null) {
            String credentials = request.getHeader("Authorization");
            if (credentials == null) {
                return "superuser";
            }
            try {
                credentials = getDecodedCredentials(credentials.substring(6).trim());
                Authenticator.setUserId(credentials, request);
                userId = request.getSession().getAttribute(ATTRIBUTE_USER_ID);
            }
            catch (Exception e) {
                return "superuser";
            }
        }
        return (String) userId;
    }

    /**
     * @param request current HttpServletRequest
     * @return char[] , decoded current user password
     */
    public static char[] getPassword(HttpServletRequest request) {
        Object pswd = request.getSession().getAttribute(ATTRIBUTE_PSWD);
        if (pswd == null) {
            return StringUtils.EMPTY.toCharArray();
        }
        return ((String) pswd).toCharArray();
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
        Object user = request.getSession().getAttribute(ATTRIBUTE_JAAS_SUBJECT);
        return !(user == null);
    }

    /**
     * Get JAAS authenticated subject
     * @param request
     * @return Authenticated JAAS subject
     */
    public static Subject getSubject(HttpServletRequest request) {
        return (Subject) request.getSession().getAttribute(ATTRIBUTE_JAAS_SUBJECT);
    }
}