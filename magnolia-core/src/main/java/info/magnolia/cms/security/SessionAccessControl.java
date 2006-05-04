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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public final class SessionAccessControl {

    private static Logger log = LoggerFactory.getLogger(SessionAccessControl.class);

    private static final String ATTRIBUTE_REPOSITORY_SESSION_PREFIX = "mgnlRepositorySession_";

    private static final String DEFAULT_REPOSITORY = ContentRepository.WEBSITE;

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
     * Gets the ticket creted while login, creates a new ticket if not existing.
     * @param request
     * @param repositoryID
     */
    protected static Session getSession(HttpServletRequest request, String repositoryID, String workspaceID)
        throws LoginException, RepositoryException {

        Session jcrSession = null;
        HttpSession httpSession = request.getSession(false);

        if (httpSession != null) {
            jcrSession = (Session) httpSession.getAttribute(ATTRIBUTE_REPOSITORY_SESSION_PREFIX
                + repositoryID
                + "_" + workspaceID); //$NON-NLS-1$
        }
        if (jcrSession == null) {

            SimpleCredentials sc = new SimpleCredentials(
                ContentRepository.REPOSITORY_USER,
                ContentRepository.REPOSITORY_PSWD.toCharArray());

            jcrSession = ContentRepository.getRepository(repositoryID).login(sc, workspaceID);

            if (httpSession != null) {
                httpSession.setAttribute(ATTRIBUTE_REPOSITORY_SESSION_PREFIX + repositoryID + "_" + workspaceID, //$NON-NLS-1$
                    jcrSession);
            }

        }
        return jcrSession;
    }

    /**
     * Gets hierarchy manager for the default repository using session ticket. Creates a new ticket and hierarchy
     * manager if not exist.
     * @see SessionAccessControl#DEFAULT_REPOSITORY
     * @param request
     * @deprecated use MgnlContext.getHierarchyManager
     * @see info.magnolia.cms.beans.runtime.MgnlContext
     */
    public static HierarchyManager getHierarchyManager(HttpServletRequest request) {
        return MgnlContext.getInstance().getHierarchyManager(DEFAULT_REPOSITORY);
    }

    /**
     * Gets hierarchy manager for the default repository using session ticket. Creates a new ticket and hierarchy
     * manager if not exist.
     * @param request
     * @param repositoryID
     * @deprecated use MgnlContext.getHierarchyManager
     * @see info.magnolia.cms.beans.runtime.MgnlContext
     */
    public static HierarchyManager getHierarchyManager(HttpServletRequest request, String repositoryID) {
        return MgnlContext.getInstance().getHierarchyManager(repositoryID);
    }

    /**
     * Gets hierarchy manager for the default repository using session ticket. Creates a new ticket and hierarchy
     * manager if not exist.
     * @param request
     * @param repositoryID
     * @deprecated use MgnlContext.getHierarchyManager
     * @see info.magnolia.cms.beans.runtime.MgnlContext
     */
    public static HierarchyManager getHierarchyManager(HttpServletRequest request, String repositoryID,
        String workspaceID) {
        return MgnlContext.getInstance().getHierarchyManager(repositoryID, workspaceID);
    }

    /**
     * gets AccessManager for the current user session for the default repository and workspace
     * @param request
     * @deprecated use MgnlContext.getAccessManager()
     * @see info.magnolia.cms.beans.runtime.MgnlContext
     */
    public static AccessManager getAccessManager(HttpServletRequest request) {
        return MgnlContext.getInstance().getAccessManager(DEFAULT_REPOSITORY);
    }

    /**
     * gets AccessManager for the current user session for the specified repository default workspace
     * @param request
     * @param repositoryID
     * @deprecated use MgnlContext.getAccessManager()
     * @see info.magnolia.cms.beans.runtime.MgnlContext
     */
    public static AccessManager getAccessManager(HttpServletRequest request, String repositoryID) {
        return MgnlContext.getInstance().getAccessManager(repositoryID);
    }

    /**
     * gets AccessManager for the current user session for the specified repository and workspace
     * @param request
     * @param repositoryID
     * @param workspaceID
     * @deprecated use MgnlContext.getAccessManager()
     * @see info.magnolia.cms.beans.runtime.MgnlContext
     */
    public static AccessManager getAccessManager(HttpServletRequest request, String repositoryID, String workspaceID) {
        return MgnlContext.getInstance().getAccessManager(repositoryID, workspaceID);
    }

    /**
     * Gets access controlled query manager
     * @param request
     * @deprecated MgnlContext.getQueryManager()
     * @see info.magnolia.cms.beans.runtime.MgnlContext
     */
    public static QueryManager getQueryManager(HttpServletRequest request) throws RepositoryException {
        return MgnlContext.getInstance().getQueryManager(DEFAULT_REPOSITORY);
    }

    /**
     * Gets access controlled query manager
     * @param request
     * @param repositoryID
     * @deprecated MgnlContext.getQueryManager()
     * @see info.magnolia.cms.beans.runtime.MgnlContext
     */
    public static QueryManager getQueryManager(HttpServletRequest request, String repositoryID)
        throws RepositoryException {
        return MgnlContext.getInstance().getQueryManager(repositoryID);
    }

    /**
     * Gets access controlled query manager
     * @param request
     * @param repositoryID
     * @param workspaceID
     * @deprecated MgnlContext.getQueryManager()
     * @see info.magnolia.cms.beans.runtime.MgnlContext
     */
    public static QueryManager getQueryManager(HttpServletRequest request, String repositoryID, String workspaceID)
        throws RepositoryException {
        return MgnlContext.getInstance().getQueryManager(repositoryID, workspaceID);
    }

    /**
     * @param request
     * @return true is user has a valid session
     * @deprecated use Authenticator.isAuthenticated(HttpServletRequest)
     */
    public static boolean isSecuredSession(HttpServletRequest request) {
        return Authenticator.isAuthenticated(request);
    }

    /**
     * invalidates user session
     * @param request
     * @deprecated
     */
    public static void invalidateUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

}
