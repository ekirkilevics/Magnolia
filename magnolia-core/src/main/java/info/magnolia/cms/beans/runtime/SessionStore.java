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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.SearchFactory;
import info.magnolia.cms.security.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is meant to be used by WebContextImpl
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
final class SessionStore {

    private static Logger log = LoggerFactory.getLogger(SessionStore.class);

    private static final String ATTRIBUTE_REPOSITORY_SESSION_PREFIX = "mgnlRepositorySession_";

    private static final String ATTRIBUTE_HM_PREFIX = "mgnlHMgr_";

    private static final String ATTRIBUTE_AM_PREFIX = "mgnlAccessMgr_";

    private static final String ATTRIBUTE_QM_PREFIX = "mgnlQueryMgr_";

    private static final String DEFAULT_REPOSITORY = ContentRepository.WEBSITE;

    /**
     * Utility class, don't instantiate.
     */
    private SessionStore() {
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
        return getSession(request, repositoryID, ContentRepository.getDefaultWorkspace(repositoryID));
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
     * @see SessionStore#DEFAULT_REPOSITORY
     * @param request
     */
    protected static HierarchyManager getHierarchyManager(HttpServletRequest request) {
        return getHierarchyManager(request, DEFAULT_REPOSITORY);
    }

    /**
     * Gets hierarchy manager for the default repository using session ticket. Creates a new ticket and hierarchy
     * manager if not exist.
     * @param request
     * @param repositoryID
     */
    protected static HierarchyManager getHierarchyManager(HttpServletRequest request, String repositoryID) {
        return getHierarchyManager(request, repositoryID, ContentRepository.getDefaultWorkspace(repositoryID));
    }

    /**
     * Gets hierarchy manager for the default repository using session ticket. Creates a new ticket and hierarchy
     * manager if not exist.
     * @param request
     * @param repositoryID
     */
    protected static HierarchyManager getHierarchyManager(HttpServletRequest request, String repositoryID,
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
    protected static AccessManager getAccessManager(HttpServletRequest request) {
        return getAccessManager(request, DEFAULT_REPOSITORY);
    }

    /**
     * gets AccessManager for the current user session for the specified repository default workspace
     * @param request
     * @param repositoryID
     */
    protected static AccessManager getAccessManager(HttpServletRequest request, String repositoryID) {
        return getAccessManager(request, repositoryID, ContentRepository.getDefaultWorkspace(repositoryID));
    }

    /**
     * gets AccessManager for the current user session for the specified repository and workspace
     * @param request
     * @param repositoryID
     * @param workspaceID
     */
    protected static AccessManager getAccessManager(HttpServletRequest request, String repositoryID, String workspaceID) {

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
    protected static QueryManager getQueryManager(HttpServletRequest request) throws RepositoryException {
        return getQueryManager(request, DEFAULT_REPOSITORY);
    }

    /**
     * Gets access controlled query manager
     * @param request
     * @param repositoryID
     */
    protected static QueryManager getQueryManager(HttpServletRequest request, String repositoryID)
        throws RepositoryException {
        return getQueryManager(request, repositoryID, ContentRepository.getDefaultWorkspace(repositoryID));
    }

    /**
     * Gets access controlled query manager
     * @param request
     * @param repositoryID
     * @param workspaceID
     */
    protected static QueryManager getQueryManager(HttpServletRequest request, String repositoryID, String workspaceID)
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

    protected static Session getRepositorySession(HttpServletRequest request, String repositoryID, String workspaceID)
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
    protected static void createSession(HttpServletRequest request) throws LoginException, RepositoryException {
        createRepositorySession(request, DEFAULT_REPOSITORY);
    }

    /**
     * create user ticket and set ACL (user + group) in the session
     * @param request
     */
    protected static void createRepositorySession(HttpServletRequest request, String repositoryID) throws LoginException,
        RepositoryException {
        createRepositorySession(request, repositoryID, ContentRepository.getDefaultWorkspace(repositoryID));
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

        // JAAS specific
        Subject subject = Authenticator.getSubject(request);

        List permissionList = null;
        if (subject != null) {
            Set principalSet = subject.getPrincipals(PrincipalCollection.class);
            Iterator it = principalSet.iterator();
            PrincipalCollection principals = (PrincipalCollection) it.next();
            ACL acl = (ACL) principals.get(repositoryID + "_" + workspaceID);
            if (acl != null) {
                permissionList = acl.getList();
            }
            else {
                permissionList = new ArrayList(); // no permissions assigned to this workspace
            }
        }

        AccessManagerImpl accessManager = new AccessManagerImpl();
        accessManager.setPermissionList(permissionList);
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
     * @return true is user has a valid session
     */
    protected static boolean isSecuredSession(HttpServletRequest request) {
        return Authenticator.isAuthenticated(request);
    }

    /**
     * invalidates user session
     * @param request
     */
    protected static void invalidateUser(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    /**
     * @return the current user object
     */
    protected static User getUser() {
        return MgnlContext.getUser();
    }

    /**
     * logout user (as in request) from the specified repository session
     * @param request
     * @param repositoryID
     */
    protected static void logout(HttpServletRequest request, String repositoryID) {
        try {
            getSession(request, repositoryID).logout();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }
}
