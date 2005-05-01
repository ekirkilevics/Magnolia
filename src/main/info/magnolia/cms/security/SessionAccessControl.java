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
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.SearchFactory;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public final class SessionAccessControl {

    private static Logger log = Logger.getLogger(SessionAccessControl.class);

    private static final String ATTRIBUTE_REPOSITORY_SESSION_PREFIX = "mgnlRepositorySession_";

    private static final String ATTRIBUTE_HM_PREFIX = "mgnlHMgr_";

    private static final String ATTRIBUTE_AM_PREFIX = "mgnlAccessMgr_";

    private static final String ATTRIBUTE_QM_PREFIX = "mgnlQueryMgr_";

    private static final String DEFAULT_REPOSITORY = ContentRepository.WEBSITE;

    private static final String DEFAULT_WORKSPACE = ContentRepository.DEFAULT_WORKSPACE;

    /**
     * Utility class, don't instantiate.
     */
    private SessionAccessControl() {
        // unused
    }

    /**
     * Gets the ticket creted while login, creates a new ticket if not existing.
     * @param request
     */
    protected static Session getSession(HttpServletRequest request) throws LoginException, RepositoryException {
        return getSession(request, DEFAULT_REPOSITORY);
    }

    /**
     * Gets the ticket creted while login, creates a new ticket if not existing.
     * @param request
     * @param repositoryID
     */
    protected static Session getSession(HttpServletRequest request, String repositoryID) throws LoginException,
        RepositoryException {
        return getSession(request, repositoryID, DEFAULT_WORKSPACE);
    }

    /**
     * Gets the ticket creted while login, creates a new ticket if not existing .
     * @param request
     * @param repositoryID
     */
    protected static Session getSession(HttpServletRequest request, String repositoryID, String workspaceID)
        throws LoginException, RepositoryException {
        return getRepositorySession(request, repositoryID, workspaceID);
    }

    /**
     * Gets hierarchy manager for the default repository using session ticket. Creates a new ticket and hierarchy
     * manager if not exist.
     * @see SessionAccessControl#DEFAULT_REPOSITORY
     * @param request
     */
    public static HierarchyManager getHierarchyManager(HttpServletRequest request) {
        return getHierarchyManager(request, DEFAULT_REPOSITORY);
    }

    /**
     * Gets hierarchy manager for the default repository using session ticket. Creates a new ticket and hierarchy
     * manager if not exist.
     * @param request
     * @param repositoryID
     */
    public static HierarchyManager getHierarchyManager(HttpServletRequest request, String repositoryID) {
        return getHierarchyManager(request, repositoryID, DEFAULT_WORKSPACE);
    }

    /**
     * Gets hierarchy manager for the default repository using session ticket. Creates a new ticket and hierarchy
     * manager if not exist.
     * @param request
     * @param repositoryID
     */
    public static HierarchyManager getHierarchyManager(HttpServletRequest request, String repositoryID,
        String workspaceID) {
        HierarchyManager hm = (HierarchyManager) request.getSession().getAttribute(
            ATTRIBUTE_HM_PREFIX + repositoryID + "_" + workspaceID);
        if (hm == null) {
            createHierarchyManager(request, repositoryID, workspaceID);
            return (HierarchyManager) request.getSession().getAttribute(
                ATTRIBUTE_HM_PREFIX + repositoryID + "_" + workspaceID);
        }
        return hm;
    }

    /**
     * gets AccessManager for the current user session for the default repository and workspace
     * @param request
     */
    public static AccessManager getAccessManager(HttpServletRequest request) {
        return getAccessManager(request, DEFAULT_REPOSITORY);
    }

    /**
     * gets AccessManager for the current user session for the specified repository default workspace
     * @param request
     * @param repositoryID
     */
    public static AccessManager getAccessManager(HttpServletRequest request, String repositoryID) {
        return getAccessManager(request, repositoryID, DEFAULT_WORKSPACE);
    }

    /**
     * gets AccessManager for the current user session for the specified repository and workspace
     * @param request
     * @param repositoryID
     * @param workspaceID
     */
    public static AccessManager getAccessManager(HttpServletRequest request, String repositoryID, String workspaceID) {

        AccessManager accessManager = (AccessManager) request.getSession().getAttribute(
            ATTRIBUTE_AM_PREFIX + repositoryID + "_" + workspaceID);

        if (accessManager == null) {
            // initialize appropriate repository/workspace session, which will create access manager for it
            getHierarchyManager(request, repositoryID, workspaceID);
            // now session value for access manager must be set
            accessManager = (AccessManager) request.getSession().getAttribute(
                ATTRIBUTE_AM_PREFIX + repositoryID + "_" + workspaceID);
        }

        return accessManager;
    }

    /**
     * Gets access controlled query manager
     * @param request
     */
    public static QueryManager getQueryManager(HttpServletRequest request) throws RepositoryException {
        return getQueryManager(request, DEFAULT_REPOSITORY);
    }

    /**
     * Gets access controlled query manager
     * @param request
     * @param repositoryID
     */
    public static QueryManager getQueryManager(HttpServletRequest request, String repositoryID)
        throws RepositoryException {
        return getQueryManager(request, repositoryID, DEFAULT_WORKSPACE);
    }

    /**
     * Gets access controlled query manager
     * @param request
     * @param repositoryID
     * @param workspaceID
     */
    public static QueryManager getQueryManager(HttpServletRequest request, String repositoryID, String workspaceID)
        throws RepositoryException {
        QueryManager queryManager = (QueryManager) request.getSession().getAttribute(
            ATTRIBUTE_QM_PREFIX + repositoryID + "_" + workspaceID);
        if (queryManager == null) {
            javax.jcr.query.QueryManager qm = getSession(request, repositoryID, workspaceID)
                .getWorkspace()
                .getQueryManager();
            queryManager = SearchFactory.getAccessControllableQueryManager(qm, getAccessManager(
                request,
                repositoryID,
                workspaceID));
            request.getSession().setAttribute(ATTRIBUTE_QM_PREFIX + repositoryID + "_" + workspaceID, queryManager);
        }
        return queryManager;
    }

    private static Session getRepositorySession(HttpServletRequest request, String repositoryID, String workspaceID)
        throws LoginException, RepositoryException {
        Object ticket = request.getSession().getAttribute(
            ATTRIBUTE_REPOSITORY_SESSION_PREFIX + repositoryID + "_" + workspaceID);
        if (ticket == null) {
            createRepositorySession(request, repositoryID, workspaceID);
            return (Session) request.getSession().getAttribute(
                ATTRIBUTE_REPOSITORY_SESSION_PREFIX + repositoryID + "_" + workspaceID);
        }
        return (Session) ticket;
    }

    /**
     * create user ticket and set ACL (user + group) in the session
     * @param request
     */
    public static void createSession(HttpServletRequest request) throws LoginException, RepositoryException {
        createRepositorySession(request, DEFAULT_REPOSITORY);
    }

    /**
     * create user ticket and set ACL (user + group) in the session
     * @param request
     */
    private static void createRepositorySession(HttpServletRequest request, String repositoryID) throws LoginException,
        RepositoryException {
        createRepositorySession(request, repositoryID, DEFAULT_WORKSPACE);
    }

    /**
     * create user ticket and set ACL (user + group) in the session
     * @param request
     */
    private static void createRepositorySession(HttpServletRequest request, String repositoryID, String workspaceID)
        throws LoginException, RepositoryException {
        SimpleCredentials sc = new SimpleCredentials(Authenticator.getUserId(request), Authenticator
            .getPassword(request));
        Session session = ContentRepository.getRepository(repositoryID).login(sc, workspaceID);
        request.getSession().setAttribute(
            ATTRIBUTE_REPOSITORY_SESSION_PREFIX + repositoryID + "_" + workspaceID,
            session);
        Content userNode = getUserNode(request);
        List acl = new ArrayList();
        updateACL(userNode, acl, repositoryID);
        updateRolesACL(userNode, acl, repositoryID);
        updateGroupsACL(userNode, acl, repositoryID);
        AccessManagerImpl accessManager = new AccessManagerImpl();
        accessManager.setPermissionList(acl);
        request.getSession().setAttribute(ATTRIBUTE_AM_PREFIX + repositoryID + "_" + workspaceID, accessManager);
    }

    private static void createHierarchyManager(HttpServletRequest request, String repositoryID, String workspaceID) {
        HierarchyManager hm = new HierarchyManager(Authenticator.getUserId(request));
        try {
            hm.init(getSession(request, repositoryID, workspaceID).getRootNode());
            hm.setAccessManager((AccessManager) request.getSession().getAttribute(
                ATTRIBUTE_AM_PREFIX + repositoryID + "_" + workspaceID));
            request.getSession().setAttribute(ATTRIBUTE_HM_PREFIX + repositoryID + "_" + workspaceID, hm);
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
                userPage = ContentRepository.getHierarchyManager(ContentRepository.USERS).getContent(
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
     * Adds user acl of the specified user to the given userACL
     * @param userNode
     * @param userACL
     */
    private static void updateACL(Content userNode, List userACL, String repositoryID) {
        try {
            /* get access rights of this node (user) */
            Content acl = null;
            try {
                acl = userNode.getContent("acl_" + repositoryID);
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
                Content map = (Content) children.next();
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
     * Adds group acl of the specified user to the given groupACL
     * @param userNode
     * @param groupACL
     */
    private static void updateRolesACL(Content userNode, List groupACL, String repositoryID) {
        try {
            HierarchyManager rolesHierarchy = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
            // get access rights of this user

            Content acl = null;
            try {
                acl = userNode.getContent("roles");
            }
            catch (PathNotFoundException e) {
                log.warn("No roles defined for user " + userNode.getHandle());
                return;
            }

            Collection aclCollection = acl.getChildren();
            if (aclCollection == null) {
                return;
            }
            Iterator children = aclCollection.iterator();
            /* find the exact match for the current url and acl for it */
            while (children.hasNext()) {
                Content map = (Content) children.next();
                String groupPath = map.getNodeData("path").getString();
                if (StringUtils.isNotEmpty(groupPath)) {
                    Content groupNode = rolesHierarchy.getContent(groupPath);
                    updateACL(groupNode, groupACL, repositoryID);
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to update roles ACL");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Adds group acl of the specified user to the given groupACL
     * @param userNode
     * @param groupACL
     * @todo groups are not handled yet?
     */
    private static void updateGroupsACL(Content userNode, List groupACL, String repositoryID) {
        try {
            HierarchyManager groupsHierarchy = ContentRepository.getHierarchyManager(ContentRepository.GROUPS);
            // get access rights of this user
            Content acl = null;
            try {
                acl = userNode.getContent("groups");
            }
            catch (PathNotFoundException e) {
                log.debug("No groups defined for user " + userNode.getHandle());
                return;
            }
            Collection aclCollection = acl.getChildren();
            if (aclCollection == null) {
                return;
            }
            Iterator children = aclCollection.iterator();
            while (children.hasNext()) {
                Content map = (Content) children.next();
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
     * invalidates user session
     * @param request
     */
    public static void invalidateUser(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    /**
     * logout user (as in request) from the specified repository session
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
