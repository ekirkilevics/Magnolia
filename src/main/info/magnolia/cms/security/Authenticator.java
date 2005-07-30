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
package info.magnolia.cms.security;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.i18n.MessagesManager;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public final class Authenticator {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Authenticator.class);

    /**
     * Session attribute holding the magnolia user id.
     */
    private static final String ATTRIBUTE_USER_ID = "mgnlUserId"; //$NON-NLS-1$

    /**
     * Session attribute holding the magnolia user password.
     */
    private static final String ATTRIBUTE_PSWD = "mgnlUserPSWD"; //$NON-NLS-1$

    /**
     * Session attribute holding the magnolia user node from the jcr repository.
     */
    private static final String ATTRIBUTE_USER_NODE = "mgnlUserNode"; //$NON-NLS-1$

    /**
     * Utility class, don't instantiate.
     */
    private Authenticator() {
        // unused
    }

    /**
     * Authenticate authorization request with the usersRepository.
     * @param req as received by the servlet engine
     * @return boolean
     */
    public static boolean authenticate(HttpServletRequest req) {
        String credentials = req.getHeader("Authorization"); //$NON-NLS-1$
        if (StringUtils.isEmpty(credentials) || credentials.length() <= 6) {
            return false;
        }
        credentials = getDecodedCredentials(credentials.substring(6).trim());
        Authenticator.setUserId(credentials, req);
        Authenticator.setPassword(credentials, req);
        boolean isValid = isValidUser(req);
        if (!isValid) {
            req.getSession().invalidate();
        }
        return isValid;
    }

    /**
     * checks is the credentials exist in the repository
     * @param request current HttpServletRequest
     * @return boolean
     */
    private static boolean isValidUser(HttpServletRequest request) {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        try {
            String userid = Authenticator.getUserId(request);
            if (StringUtils.isEmpty(userid)) {
                return false;
            }
            Content userPage = hm.getContent(userid);
            String encodedPassword = new String(Base64.encodeBase64(Authenticator
                .getPasswordAsString(request)
                .getBytes()));
            String fromRepository = userPage.getNodeData("pswd").getString().trim(); //$NON-NLS-1$
            String fromBrowser = encodedPassword.trim();
            if (fromRepository.equalsIgnoreCase(fromBrowser)) {
                request.getSession().setAttribute(ATTRIBUTE_USER_NODE, userPage);

                // we must set the language because the JSTL will not use our classes
                String lang = userPage.getNodeData("language").getString(); //$NON-NLS-1$
                if (StringUtils.isEmpty(lang)) {
                    lang = MessagesManager.getDefaultLocale().getLanguage();
                }
                MessagesManager.setUserLanguage(lang, request.getSession());
                return true;
            }
        }
        catch (PathNotFoundException e) {
            log.info("Unable to locate user [" + Authenticator.getUserId(request) + "], authentication failed"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (RepositoryException e) {
            log.error("Unable to locate user [" //$NON-NLS-1$
                + Authenticator.getUserId(request) + "], authentication failed due to a " //$NON-NLS-1$
                + e.getClass().getName(), e);
        }
        return false;
    }

    /**
     * @param credentials to be decoded
     * @return String decoded credentials <b>name:password </b>
     */
    private static String getDecodedCredentials(String credentials) {
        return (new String(Base64.decodeBase64(credentials.getBytes())));
    }

    /**
     * @param decodedCredentials , BASE64Decoded credentials from the request
     * @param request current HttpServletRequest
     */
    private static void setUserId(String decodedCredentials, HttpServletRequest request) {
        request.getSession().setAttribute(ATTRIBUTE_USER_ID, StringUtils.substringBefore(decodedCredentials, ":")); //$NON-NLS-1$
    }

    /**
     * @param request current HttpServletRequest
     * @param decodedCredentials , BASE64Decoded credentials from the request
     */
    private static void setPassword(String decodedCredentials, HttpServletRequest request) {
        request.getSession().setAttribute(ATTRIBUTE_PSWD, StringUtils.substringAfter(decodedCredentials, ":")); //$NON-NLS-1$
    }

    /**
     * @param request current HttpServletRequest
     * @return String , current logged in user
     */
    public static String getUserId(HttpServletRequest request) {
        Object userId = request.getSession().getAttribute(ATTRIBUTE_USER_ID);
        if (userId == null) {
            String credentials = request.getHeader("Authorization"); //$NON-NLS-1$
            if (credentials == null) {
                return "superuser"; //$NON-NLS-1$
            }

            credentials = getDecodedCredentials(credentials.substring(6).trim());
            Authenticator.setUserId(credentials, request);
            userId = request.getSession().getAttribute(ATTRIBUTE_USER_ID);
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
     * @return String password
     */
    private static String getPasswordAsString(HttpServletRequest request) {
        return ((String) request.getSession().getAttribute(ATTRIBUTE_PSWD));
    }

    /**
     * @param request current HttpServletRequest
     * @return credentials , as received from the servlet request
     */
    public static String getCredentials(HttpServletRequest request) {
        return request.getHeader("Authorization"); //$NON-NLS-1$
    }

    /**
     * @param request current HttpServletRequest
     * @return current logged in user page
     */
    public static Content getUserPage(HttpServletRequest request) {
        return (Content) request.getSession().getAttribute(ATTRIBUTE_USER_NODE);
    }

    /**
     * @param request current HttpServletRequest
     * @return the current user object
     */
    public static User getUser(HttpServletRequest request) {
        return new User(getUserPage(request));
    }

    /**
     * checks user session for attribute "user node"
     * @param request current HttpServletRequest
     * @return <code>true</code> if the user is authenticated, <code>false</code> otherwise
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        // don't force a creation of a new session
        HttpSession session = request.getSession(false);
        if (session != null) {
            try {
                return session.getAttribute(ATTRIBUTE_USER_NODE) != null;
            }
            catch (IllegalStateException e) {
                // can happen if the session has just been invalidated
                log.debug("IllegalStateException caught"); //$NON-NLS-1$
                return false;
            }
        }

        return false;
    }
}