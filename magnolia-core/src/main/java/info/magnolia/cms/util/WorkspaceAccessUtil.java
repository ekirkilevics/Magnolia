/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.User;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.SearchFactory;
import info.magnolia.cms.core.version.MgnlVersioningSession;
import info.magnolia.cms.core.DefaultHierarchyManager;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.RepositoryException;
import javax.jcr.Repository;
import javax.security.auth.Subject;

import java.util.List;

/**
 * This class replaces SessionStore and provide generic methods to create Magnolia specific JCR-workspace access objects.
 * @see HierarchyManager
 * @see javax.jcr.Session
 * @see AccessManager
 * @see QueryManager
 * @author Sameer Charles
 * $Id$
 */
public class WorkspaceAccessUtil {

    public WorkspaceAccessUtil() {
    }

    /**
     * @deprecated since 5.0, use IoC.
     */
    @Deprecated
    public static WorkspaceAccessUtil getInstance() {
        return Components.getSingleton(WorkspaceAccessUtil.class);
    }

    /**
     * @return Default SimpleCredentials as configured in magnolia.properties
     * */
    public SimpleCredentials getDefaultCredentials() {
        User user = MgnlContext.getUser();
        if (user == null) {
            // there is no user logged in, so this is just a system call. Returned credentials are used only to access repository, but do not allow any access over Magnolia.
            return getAnonymousUserCredentials();
        }
        return new SimpleCredentials(user.getName(),user.getPassword().toCharArray());
    }

    public SimpleCredentials getCredentials(User user) {
        return new SimpleCredentials(user.getName(),user.getPassword().toCharArray());
    }
    /**
     * Login to the specified repository/default workspace using given credentials.
     * @param credentials
     * @param repositoryName
     * @return newly created JCR session
     * @throws RepositoryException if login fails or workspace does not exist
     * */
    public Session createRepositorySession(SimpleCredentials credentials,
            String repositoryName) throws RepositoryException {
        return this.createRepositorySession
        (credentials, repositoryName, ContentRepository.getDefaultWorkspace(repositoryName));
    }

    /**
     * Login to the specified repository/workspace using given credentials.
     * @param credentials
     * @param repositoryName
     * @param workspaceName
     * @return newly created JCR session
     * @throws RepositoryException
     * @throws NoSuchWorkspaceException
     * @throws LoginException
     * @throws RepositoryException if login fails or workspace does not exist
     * */
    public Session createRepositorySession(Credentials credentials,
            String repositoryName,
            String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return createRepositorySession(credentials, ContentRepository.getRepository(repositoryName), workspaceName);
    }

    /**
     * Login to the specified repository/workspace using given credentials.
     * @param credentials
     * @param repository
     * @param workspaceName
     * @return newly created JCR session
     * @throws RepositoryException
     * @throws NoSuchWorkspaceException
     * @throws LoginException
     * @throws RepositoryException if login fails or workspace does not exist
     * */
    public Session createRepositorySession(Credentials credentials,
            Repository repository,
            String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        Session session = repository.login(credentials, ContentRepository.getMappedWorkspaceName(workspaceName));
        if (ContentRepository.VERSION_STORE.equals(workspaceName)) {
            //do not wrapp version store in versioning session or we get infinite redirect loop and stack overflow
            return session;
        }
        return new MgnlVersioningSession(session);
    }

    /**
     * Create access manager of jaas authorized subject.
     * @param subject
     * @param repositoryName
     * @return newly created accessmanager
     * */
    public AccessManager createAccessManager(Subject subject, String repositoryName) {
        return this.createAccessManager(subject, repositoryName, ContentRepository.getDefaultWorkspace(repositoryName));
    }

    /**
     * Create access manager of jaas authorized subject.
     * @param subject
     * @param repositoryName
     * @param workspaceName
     * @return newly created accessmanager
     * */
    public AccessManager createAccessManager(Subject subject, String repositoryName, String workspaceName) {
        return null;
    }

    /**
     * Create access manager for the given permission list.
     * @param permissions
     * */
    public AccessManager createAccessManager(List<Permission> permissions, String repositoryName, String workspaceName) {
        return null;
    }

    /**
     * Create new access controlled magnolia query manager.
     * @param jcrSession
     * @param accessManager
     * */
    public QueryManager createQueryManager(Session jcrSession, HierarchyManager hm)
    throws RepositoryException {
        javax.jcr.query.QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();
        return SearchFactory.getInstance().getQueryManager(jcrQueryManager, hm);
    }

    /**
     * Create new instance of DefaultHierarchyManager for the given session.
     * @param userId this is used in MetaData of objects created via this HierarchyManager instance
     * @param jcrSession
     * @param accessManager
     * @param queryManager
     * */
    public HierarchyManager createHierarchyManager(String userId,
            Session jcrSession,
            AccessManager accessManager) throws RepositoryException {
        return new DefaultHierarchyManager(userId ,jcrSession, accessManager);
    }

    public Session createAdminRepositorySession(String workspace) throws RepositoryException {
        // TODO: how safe is it to expose method like this? (not worse then old system context, but not better either
        return createRepositorySession(getAdminUserCredentials(), workspace);
    }

    protected SimpleCredentials getAdminUserCredentials() {
        // FIXME: stop using SystemProperty, but pico is not ready yet when this is called (config loader calls repo.init() which results in authentication calls being made and this method being invoked
        String user = SystemProperty.getProperty("magnolia.connection.jcr.admin.userId", SystemProperty.getProperty("magnolia.connection.jcr.userId", "admin"));
        String pwd = SystemProperty.getProperty("magnolia.connection.jcr.admin.password", SystemProperty.getProperty("magnolia.connection.jcr.password", "admin"));
        return new SimpleCredentials(user, pwd.toCharArray());
    }

    protected SimpleCredentials getAnonymousUserCredentials() {
        // FIXME: stop using SystemProperty, but pico is not ready yet when this is called (config loader calls repo.init() which results in authentication calls being made and this method being invoked
        // TODO: can also read it from the Login Module properties ... but WAU has no access to that
        String user = SystemProperty.getProperty("magnolia.connection.jcr.anonymous.userId", "anonymous");
        String pwd = SystemProperty.getProperty("magnolia.connection.jcr.anonymous.password", "anonymous");
        return new SimpleCredentials(user, pwd.toCharArray());
    }

    public Session createRepositorySession(String workspace) throws RepositoryException {
        String user = MgnlContext.getUser().getName();
        String pwd = MgnlContext.getUser().getPassword();
        return createRepositorySession(new SimpleCredentials(user, pwd.toCharArray()), workspace);
    }
}
