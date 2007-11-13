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

import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Id$
 * @deprecated please
 */
public final class Authenticator {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Authenticator.class);

    /**
     * Utility class, don't instantiate.
     */
    private Authenticator() {
        // unused
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
        return !getUserId().equals(UserManager.ANONYMOUS_USER);
    }

    public static User getAnonymousUser() {
        return getSecuritySupport().getUserManager().getAnonymousUser();
    }

    private static SecuritySupport getSecuritySupport() {
        return SecuritySupport.Factory.getInstance();
    }

}