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

import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.login.LoginHandler;
import info.magnolia.cms.security.auth.login.LoginResult;
import info.magnolia.context.MgnlContext;

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
 * @version $Id$
 */
public final class Authenticator {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Authenticator.class);

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
    public static final String ATTRIBUTE_LOGINERROR = "mgnlLoginError";

 
    /**
     * Utility class, don't instantiate.
     */
    private Authenticator() {
        // unused
    }
    
    /**
     * Any subclass of LoginException will be stored as a request attribute,
     * but a plain LoginException will just be re-thrown.
     */
    protected static void handleLoginException(LoginException e, HttpServletRequest request) throws LoginException {
        if (LoginException.class.equals(e.getClass())) {
            throw e;
        } else {
            request.setAttribute(ATTRIBUTE_LOGINERROR, e);
            log.debug("Wrong credentials or locked account... or else.", e);
        }
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
     * @deprecated
     */
    public static String getUserId(HttpServletRequest request) {
        return getUserId();
    }

    public static String getUserId() {
        return MgnlContext.getUser().getName();
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
     * @deprecated
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        return isAuthenticated();
    }    

    public static boolean isAuthenticated() {
        return !MgnlContext.getUser().getName().equals(UserManager.ANONYMOUS_USER);
    }

    public static User getAnonymousUser() {
        return Security.getUserManager().getAnonymousUser();
    }

    public static LoginResult authenticate(CredentialsCallbackHandler callbackHandler, String customLoginModule) {
        Subject subject;
        try {
            LoginContext loginContext = new LoginContext(
                StringUtils.defaultString(customLoginModule, "magnolia"),
                callbackHandler);
            loginContext.login();
            subject = loginContext.getSubject();
            User user = callbackHandler.getUser();
            // not all jaas modules will support magnolia users
            if(user == null){
                user = SecuritySupport.Factory.getInstance().getUserManager().getUser(subject);
            }
            user.setSubject(subject);
            return new LoginResult(LoginHandler.STATUS_SUCCEDED, user);
        }
        catch (LoginException e) {
            return new LoginResult(LoginHandler.STATUS_FAILED, e);
        }
   }


}