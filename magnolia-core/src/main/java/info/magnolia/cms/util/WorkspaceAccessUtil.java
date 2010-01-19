/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.auth.PrincipalCollection;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.SearchFactory;
import info.magnolia.cms.core.DefaultHierarchyManager;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.objectfactory.Components;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.RepositoryException;
import javax.jcr.Repository;
import javax.security.auth.Subject;

import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;


/**
 * This class replaces SessionStore and provide generic methods to create magnolia specific JCR-workspace access objects:
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

    public static WorkspaceAccessUtil getInstance() {
        return Components.getSingleton(WorkspaceAccessUtil.class);
    }

    /**
     * @return Default SimpleCredentials as configured in magnolia.properties
     * */
    public SimpleCredentials getDefaultCredentials() {
        return new SimpleCredentials(ContentRepository.REPOSITORY_USER,ContentRepository.REPOSITORY_PSWD.toCharArray());
    }

    /**
     * Login to the specified repository/default workspace using given credentials
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
     * Login to the specified repository/workspace using given credentials
     * @param credentials
     * @param repositoryName
     * @param workspaceName
     * @return newly created JCR session
     * @throws RepositoryException if login fails or workspace does not exist
     * */
    public Session createRepositorySession(SimpleCredentials credentials,
                                              String repositoryName,
                                              String workspaceName) throws RepositoryException {
        return createRepositorySession(credentials, ContentRepository.getRepository(repositoryName), workspaceName);
    }

    /**
     * Login to the specified repository/workspace using given credentials
     * @param credentials
     * @param repository
     * @param workspaceName
     * @return newly created JCR session
     * @throws RepositoryException if login fails or workspace does not exist
     * */
    public Session createRepositorySession(SimpleCredentials credentials,
                                           Repository repository,
                                           String workspaceName) throws RepositoryException {
        return repository.login(credentials, ContentRepository.getMappedWorkspaceName(workspaceName));
    }

    /**
     * Create access manager of jaas authorized subject
     * @param subject
     * @param repositoryName
     * @return newly created accessmanager
     * */
    public AccessManager createAccessManager(Subject subject, String repositoryName) {
        return this.createAccessManager(subject, repositoryName, ContentRepository.getDefaultWorkspace(repositoryName));
    }

    /**
     * Create access manager of jaas authorized subject
     * @param subject
     * @param repositoryName
     * @param workspaceName
     * @return newly created accessmanager
     * */
    public AccessManager createAccessManager(Subject subject, String repositoryName, String workspaceName) {
        List permissionList = new ArrayList();
        if (subject != null) {
            Set principalSet = subject.getPrincipals(PrincipalCollection.class);
            Iterator it = principalSet.iterator();
            PrincipalCollection principals = (PrincipalCollection) it.next();
            ACL acl = (ACL) principals.get(repositoryName + "_" + workspaceName);
            if (acl != null) {
                permissionList = acl.getList();
            }
        }
        return createAccessManager(permissionList, repositoryName, workspaceName);
    }

    /**
     * Create access manager for the given permission list
     * @param permissions
     * */
    public AccessManager createAccessManager(List permissions, String repositoryName, String workspaceName) {
        AccessManager accessManager = new AccessManagerImpl();
        accessManager.setPermissionList(permissions);
        return accessManager;
    }

    /**
     * Create new access controlled magnolia query manager
     * @param jcrSession
     * @param accessManager
     * */
    public QueryManager createQueryManager(Session jcrSession, HierarchyManager hm)
            throws RepositoryException {
        javax.jcr.query.QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();
        return SearchFactory.getAccessControllableQueryManager(jcrQueryManager, hm);
    }

    /**
     * Create new instance of DefaultHierarchyManager for the given session
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

}
