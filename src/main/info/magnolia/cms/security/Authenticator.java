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
import java.io.IOException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/**
 * User: sameercharles Date: Sept 22, 2004 Time: 10:09:42 AM
 * @author Sameer Charles
 * @version 2.0
 */
public class Authenticator {

    private static Logger log = Logger.getLogger(Authenticator.class);

    private static final String ATTRIBUTE_USER_ID = "mgnlUserId";

    private static final String ATTRIBUTE_PSWD = "mgnlUserPSWD";

    private static final String ATTRIBUTE_USER_NODE = "mgnlUserNode";

    /**
     * <p>
     * Authenticate authorization request with the usersRepository
     * </p>
     * @param req as received by the servlet engine
     * @return boolean
     * @throws IOException
     */
    public static boolean authenticate(HttpServletRequest req) throws IOException {
        String credentials = req.getHeader("Authorization");
        if (credentials == null)
            return false;
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
     * <p>
     * checks is the credentials exist in the repository
     * </p>
     * @return boolean
     */
    private static boolean isValidUser(HttpServletRequest request) {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        try {
            Content userPage = hm.getPage(Authenticator.getUserId(request));
            BASE64Encoder encoder = new BASE64Encoder();
            String encodedPassword = new String(encoder.encodeBuffer(Authenticator
                .getPasswordAsString(request)
                .getBytes()));
            String fromRepositiry = userPage.getNodeData("pswd").getString().trim();
            String fromBrowser = encodedPassword.trim();
            if (fromRepositiry.equalsIgnoreCase(fromBrowser)) {
                request.getSession().setAttribute(ATTRIBUTE_USER_NODE, userPage);
                return true;
            }
            return false;
        }
        catch (RepositoryException re) {
            log.error("Unable to locate user - " + Authenticator.getUserId(request));
            return false;
        }
    }

    /**
     * <p>
     * uses sun.misc.BASE64Decoder
     * </p>
     * @param credentials to be decoded
     * @return String decoded credentials <b>name:password </b>
     */
    private static String getDecodedCredentials(String credentials) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        return (new String(decoder.decodeBuffer(credentials)));
    }

    /**
     * @param decodedCredentials , BASE64Decoded credentials from the request
     */
    private static void setUserId(String decodedCredentials, HttpServletRequest request) {
        int indexOfSeperator = decodedCredentials.indexOf(":");
        request.getSession().setAttribute(ATTRIBUTE_USER_ID, decodedCredentials.substring(0, indexOfSeperator));
    }

    /**
     * @param decodedCredentials , BASE64Decoded credentials from the request
     */
    private static void setPassword(String decodedCredentials, HttpServletRequest request) {
        int indexOfSeperator = decodedCredentials.indexOf(":");
        request.getSession().setAttribute(ATTRIBUTE_PSWD, decodedCredentials.substring(indexOfSeperator + 1).trim());
    }

    /**
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
     * @return char[] , decoded current user password
     */
    public static char[] getPassword(HttpServletRequest request) {
        Object pswd = request.getSession().getAttribute(ATTRIBUTE_PSWD);
        if (pswd == null)
            return "".toCharArray();
        return ((String) pswd).toCharArray();
    }

    /**
     * @return String password
     */
    private static String getPasswordAsString(HttpServletRequest request) {
        return ((String) request.getSession().getAttribute(ATTRIBUTE_PSWD));
    }

    /**
     * @return credentials , as received from the servlet request
     */
    public static String getCredentials(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    /**
     * @return current logged in user page
     */
    public static Content getUserPage(HttpServletRequest request) {
        return (Content) request.getSession().getAttribute(ATTRIBUTE_USER_NODE);
    }

    /**
     * checks user session for attribute "user node"
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        Object user = request.getSession().getAttribute(ATTRIBUTE_USER_NODE);
        return !(user == null);
    }
}
