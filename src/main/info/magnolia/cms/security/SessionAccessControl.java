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
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.regex.RegexWildcardPattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * User: sameercharles Date: June 8, 2004 Time: 05:40:21 PM
 * @author Sameer Charles
 * @version 2.0
 */
public final class SessionAccessControl {

    private static Logger log = Logger.getLogger(SessionAccessControl.class);

    private static final String ATTRIBUTE_REPOSITORY_SESSION_PREFIX = "mgnlRepositorySession_";

    private static final String ATTRIBUTE_HM_PREFIX = "mgnlHMgr_";

    private static final String ATTRIBUTE_AM_PREFIX = "mgnlAccessMgr_";

    private static final String DEFAULT_REPOSITORY = ContentRepository.WEBSITE;

    /**
     * Utility class, don't instantiate.
     */
    private SessionAccessControl() {
        // unused
    }

    /**
     * <p>
     * gets the ticket creted while login, creates a new ticket if not existing <br>
     * </p>
     * @param request
     */
    public static Session getSession(HttpServletRequest request) throws LoginException, RepositoryException {
        return getSession(request, DEFAULT_REPOSITORY);
    }

    /**
     * <p>
     * gets the ticket creted while login, creates a new ticket if not existing <br>
     * </p>
     * @param request
     * @param repositoryID
     */
    public static Session getSession(HttpServletRequest request, String repositoryID) throws LoginException,
        RepositoryException {
        return getRepositorySession(request, repositoryID);
    }

    /**
     * <p>
     * gets hierarchy manager for the default repository using session ticket <br>
     * creates a new ticket and hierarchy manager if not exist
     * </p>
     * @see SessionAccessControl#DEFAULT_REPOSITORY
     * @param request
     */
    public static HierarchyManager getHierarchyManager(HttpServletRequest request) {
        return getHierarchyManager(request, DEFAULT_REPOSITORY);
    }

    /**
     * <p>
     * gets hierarchy manager for the specified repository using session ticket <br>
     * creates a new ticket and hierarchy manager if not exist
     * </p>
     * @param request
     * @param repositoryID
     */
    public static HierarchyManager getHierarchyManager(HttpServletRequest request, String repositoryID) {
        HierarchyManager hm = (HierarchyManager) request.getSession().getAttribute(ATTRIBUTE_HM_PREFIX + repositoryID);
        if (hm == null) {
            createHierarchyManager(request, repositoryID);
            return (HierarchyManager) request.getSession().getAttribute(ATTRIBUTE_HM_PREFIX + repositoryID);
        }
        return hm;
    }

    private static Session getRepositorySession(HttpServletRequest request, String repositoryID) throws LoginException,
        RepositoryException {
        Object ticket = request.getSession().getAttribute(ATTRIBUTE_REPOSITORY_SESSION_PREFIX + repositoryID);
        if (ticket == null) {
            createRepositorySession(request, repositoryID);
            return (Session) request.getSession().getAttribute(ATTRIBUTE_REPOSITORY_SESSION_PREFIX + repositoryID);
        }
        return (Session) ticket;
    }

    /**
     * <p>
     * create user ticket and set ACL (user + group) in the session
     * </p>
     * @param request
     */
    public static void createSession(HttpServletRequest request) throws LoginException, RepositoryException {
        createRepositorySession(request, DEFAULT_REPOSITORY);
    }

    /**
     * <p>
     * create user ticket and set ACL (user + group) in the session
     * </p>
     * @param request
     */
    public static void createRepositorySession(HttpServletRequest request, String repositoryID) throws LoginException,
        RepositoryException {
        SimpleCredentials sc = new SimpleCredentials(Authenticator.getUserId(request), Authenticator
            .getPassword(request));
        Session session = ContentRepository.getRepository(repositoryID).login(sc, null);
        request.getSession().setAttribute(ATTRIBUTE_REPOSITORY_SESSION_PREFIX + repositoryID, session);
        Content userNode = getUserNode(request);
        List acl = new ArrayList();
        updateACL(userNode, acl, repositoryID);
        updateRolesACL(userNode, acl, repositoryID);
        updateGroupsACL(userNode, acl, repositoryID);
        AccessManagerImpl accessManager = new AccessManagerImpl();
        accessManager.setPermissionList(acl);
        request.getSession().setAttribute(ATTRIBUTE_AM_PREFIX + repositoryID, accessManager);
    }

    private static void createHierarchyManager(HttpServletRequest request, String repositoryID) {
        HierarchyManager hm = new HierarchyManager(Authenticator.getUserId(request));
        try {
            hm.init(getSession(request, repositoryID).getRootNode());
            hm.setAccessManager((AccessManager) request.getSession().getAttribute(ATTRIBUTE_AM_PREFIX + repositoryID));
            request.getSession().setAttribute(ATTRIBUTE_HM_PREFIX + repositoryID, hm);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }

    /**
     * @param request
     * @return Node representing currently logged in user
     */
    public static Content getUserNode(HttpServletRequest request) {
        Content userPage = Authenticator.getUserPage(request);
        try {
            if (userPage == null) {
                userPage = ContentRepository.getHierarchyManager(ContentRepository.USERS).getPage(
                    Authenticator.getUserId(request));
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return userPage;
    }

    /**
     * @param request
     * @return true is user has a valid session
     */
    public static boolean isSecuredSession(HttpServletRequest request) {
        return Authenticator.isAuthenticated(request);
    }

    /**
     * <p>
     * Adds user acl of the specified user to the given userACL
     * </p>
     * @param userNode
     * @param userACL
     */
    private static void updateACL(Content userNode, List userACL, String repositoryID) {
        try {
            /* get access rights of this node (user) */
            ContentNode acl = null;
            try {
                acl = userNode.getContentNode("acl_" + repositoryID);
            }
            catch (PathNotFoundException e) {
                log.warn("No acl defined for user " + userNode.getHandle() + " on repository \"" + repositoryID + "\"");
                return;
            }

            Collection aclCollection = acl.getChildren();
            if (aclCollection == null) {
                return;
            }
            Iterator children = aclCollection.iterator();
            while (children.hasNext()) {
                ContentNode map = (ContentNode) children.next();
                StringBuffer uriStringBuffer = new StringBuffer();
                char[] chars = map.getNodeData("path").getString().toCharArray();
                int i = 0, last = 0;
                while (i < chars.length) {
                    char c = chars[i];
                    if (c == '*') {
                        uriStringBuffer.append(chars, last, i - last);
                        uriStringBuffer.append(RegexWildcardPattern.getMultipleCharPattern());
                        last = i + 1;
                    }
                    i++;
                }
                uriStringBuffer.append(chars, last, i - last);
                Pattern p = Pattern.compile(uriStringBuffer.toString());
                Permission permission = new PermissionImpl();
                permission.setPattern(p);
                permission.setPermissions(map.getNodeData("permissions").getLong());
                userACL.add(permission);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }

    /**
     * <p>
     * Adds group acl of the specified user to the given groupACL
     * </p>
     * @param userNode
     * @param groupACL
     */
    private static void updateRolesACL(Content userNode, List groupACL, String repositoryID) {
        try {
            HierarchyManager rolesHierarchy = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
            /* get access rights of this user */
            ContentNode acl = userNode.getContentNode("roles");
            Collection aclCollection = acl.getChildren();
            if (aclCollection == null) {
                return;
            }
            Iterator children = aclCollection.iterator();
            /* find the exact match for the current url and acl for it */
            while (children.hasNext()) {
                ContentNode map = (ContentNode) children.next();
                String groupPath = map.getNodeData("path").getString();
                ContentNode groupNode = rolesHierarchy.getContentNode(groupPath);
                updateACL(groupNode, groupACL, repositoryID);
            }
        }
        catch (Exception e) {
            log.error("Failed to update roles ACL");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Adds group acl of the specified user to the given groupACL
     * </p>
     * @param userNode
     * @param groupACL
     */
    private static void updateGroupsACL(Content userNode, List groupACL, String repositoryID) {
        try {
            HierarchyManager groupsHierarchy = ContentRepository.getHierarchyManager(ContentRepository.GROUPS);
            /* get access rights of this user */
            ContentNode acl = null;
            try {
                acl = userNode.getContentNode("groups");
            }
            catch (PathNotFoundException e) {
                log.warn("No groups defined for user " + userNode.getHandle());
                return;
            }
            Collection aclCollection = acl.getChildren();
            if (aclCollection == null) {
                return;
            }
            Iterator children = aclCollection.iterator();
            while (children.hasNext()) {
                ContentNode map = (ContentNode) children.next();
                String groupPath = map.getNodeData("path").getString();
                Content groupNode = groupsHierarchy.getContent(groupPath);
                updateRolesACL(groupNode, groupACL, repositoryID);
            }
        }
        catch (Exception e) {
            log.error("Failed to load user group ACL");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * <p>
     * invalidates user session
     * </p>
     * @param request
     */
    public static void invalidateUser(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    /**
     * <p>
     * logout user (as in request) from the specified repository session
     * </p>
     * @param request
     * @param repositoryID
     */
    public static void logout(HttpServletRequest request, String repositoryID) {
        try {
            getSession(request, repositoryID).logout();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }
}
