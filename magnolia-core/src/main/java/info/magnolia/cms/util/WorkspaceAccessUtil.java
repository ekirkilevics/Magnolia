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

    private static final WorkspaceAccessUtil thisInstance = new WorkspaceAccessUtil();

    private WorkspaceAccessUtil() {}

    public static WorkspaceAccessUtil getInstance() {
        return thisInstance;
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
        AccessManager accessManager = new AccessManagerImpl();
        accessManager.setPermissionList(permissionList);

        return accessManager;
    }

    /**
     * Create access manager for the given permission list
     * @param permissions
     * */
    public AccessManager createAccessManager(List permissions) {
        AccessManager accessManager = new AccessManagerImpl();
        accessManager.setPermissionList(permissions);
        return accessManager;
    }

    /**
     * Create new access controlled magnolia query manager
     * @param jcrSession
     * @param accessManager
     * */
    public QueryManager createQueryManager(Session jcrSession, AccessManager accessManager)
            throws RepositoryException {
        javax.jcr.query.QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();
        return SearchFactory.getAccessControllableQueryManager(jcrQueryManager, accessManager);
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
                                                      AccessManager accessManager,
                                                      QueryManager queryManager) throws RepositoryException {
        return new DefaultHierarchyManager(userId ,jcrSession, accessManager, queryManager);
    }

}
