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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.RepositoryConstants;
import info.magnolia.cms.util.RepositoryLoader;
import info.magnolia.cms.util.WorkspaceMapping;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import info.magnolia.repository.RepositoryNotInitializedException;

import java.util.Iterator;

import javax.jcr.RepositoryException;


/**
 * Legacy type - will be dropped with one of the next release.
 *
 * @version $Id$
 *
 * @deprecated since 4.5 - Use {@link info.magnolia.cms.util.RepositoryConstants}, {@link info.magnolia.cms.util.WorkspaceMapping}, {@link info.magnolia.jcr.registry.SessionProviderRegistry} instead.
 */
public final class ContentRepository {

    /**
     * @deprecated Use {@link RepositoryConstants#WEBSITE} instead
     */
    public static final String WEBSITE = RepositoryConstants.WEBSITE;

    /**
     * @deprecated Use {@link RepositoryConstants#USERS} instead
     */
    public static final String USERS = RepositoryConstants.USERS;

    /**
     * @deprecated Use {@link RepositoryConstants#USER_ROLES} instead
     */
    public static final String USER_ROLES = RepositoryConstants.USER_ROLES;

    /**
     * @deprecated Use {@link RepositoryConstants#USER_GROUPS} instead
     */
    public static final String USER_GROUPS = RepositoryConstants.USER_GROUPS;

    /**
     * @deprecated Use {@link RepositoryConstants#CONFIG} instead
     */
    public static final String CONFIG = RepositoryConstants.CONFIG;

    /**
     * @deprecated Use {@link RepositoryConstants#VERSION_STORE} instead
     */
    public static final String VERSION_STORE = RepositoryConstants.VERSION_STORE;

    /**
     * magnolia namespace.
     * @deprecated Use {@link RepositoryConstants#NAMESPACE_PREFIX} instead
     */
    public static final String NAMESPACE_PREFIX = RepositoryConstants.NAMESPACE_PREFIX;

    /**
     * @deprecated Use {@link RepositoryConstants#NAMESPACE_URI} instead
     */
    public static final String NAMESPACE_URI = RepositoryConstants.NAMESPACE_URI;

    /**
     * repository user.
     */
    public static String REPOSITORY_USER;

    /**
     * Needs to be references here as long as the deprecated methods delegating to it are there.
     */
    private static final WorkspaceMapping workspaceMapping = Components.getSingleton(WorkspaceMapping.class);

    private static final RepositoryLoader repositoryLoader = Components.getSingleton(RepositoryLoader.class);

    /**
     * repository default password.
     */
    public static String REPOSITORY_PSWD;

    static {
        // TODO dlipp - where to put these two variables?
        REPOSITORY_USER = SystemProperty.getProperty("magnolia.connection.jcr.userId");
        REPOSITORY_PSWD = SystemProperty.getProperty("magnolia.connection.jcr.password");
    }

    /**
     * Utility class, don't instantiate.
     */
    private ContentRepository() {
        // unused constructor
    }

    /**
     * @deprecated since 4.5 - use {@link RepositoryLoader#init()} instead
     */
    public static void init() {
        repositoryLoader.init();
    }

    /**
     * @deprecated since 4.5 - use {@link RepositoryLoader#shutdown()} instead
     */
    public static void shutdown() {
        repositoryLoader.shutdown();
    }

    /**
     * @deprecated since 4.5 - use {@link RepositoryLoader#checkIfInitialized()} instead
     */
    public static boolean checkIfInitialized() throws AccessDeniedException, RepositoryException {
        return repositoryLoader.checkIfInitialized();
    }

    /**
     * @deprecated since 4.5 - use {@link RepositoryLoader#checkIfInitialized(String)} instead
     */
    public static boolean checkIfInitialized(String repository) throws RepositoryException, AccessDeniedException {
        return repositoryLoader.checkIfInitialized(repository);
    }

    /**
     * @deprecated since 4.5 - use {@link RepositoryLoader#reload()} instead
     */
    public static void reload() {
        repositoryLoader.reload();
    }

    /**
     * @deprecated since 4.5 - use {@link RepositoryLoader#loadRepository(RepositoryMapping)} instead
     */
    public static void loadRepository(RepositoryMapping map) throws RepositoryNotInitializedException,
    InstantiationException, IllegalAccessException, ClassNotFoundException {
        repositoryLoader.loadRepository(map);
    }

    /**
     * @deprecated since 4.5 - use {@link RepositoryLoader#loadWorkspace(String, String)} instead
     */
    public static void loadWorkspace(String repositoryId, String workspaceId) throws RepositoryException {
        repositoryLoader.loadWorkspace(repositoryId, workspaceId);
    }

    /**
     * @deprecated since 4.5 - use {@link WorkspaceMapping#getAllRepositoryNames()} directly.
     */
    public static Iterator<String> getAllRepositoryNames() {
        return workspaceMapping.getAllRepositoryNames();
    }

    /**
     * @deprecated since 4.5 - use {@link WorkspaceMapping#getRepositoryMapping(String)} directly.
     */
    public static RepositoryMapping getRepositoryMapping(String repositoryID) {
        return workspaceMapping.getRepositoryMapping(repositoryID);
    }

    /**
     * @deprecated since 4.5 - use {@link WorkspaceMapping#getRepositoryProvider(String)} directly.
     */
    public static Provider getRepositoryProvider(String repositoryID) {
        return workspaceMapping.getRepositoryProvider(repositoryID);
    }

    /**
     * @deprecated since 4.5 - use {@link WorkspaceMapping#getMappedWorkspaceName(String)} directly.
     */
    /**
     * Get mapped workspace name.
     * @param name
     * @return mapped name as in repositories.xml RepositoryMapping element
     */
    public static String getMappedWorkspaceName(String name) {
        return workspaceMapping.getMappedWorkspaceName(name);
    }

    /**
     * @deprecated since 4.5 - user {@link WorkspaceMapping#getInternalWorkspaceName(String)} directly.
     */
    public static String getInternalWorkspaceName(String workspaceName) {
        return workspaceMapping.getInternalWorkspaceName(workspaceName);
    }

    /**
     * @deprecated since 4.5 - user {@link WorkspaceMapping#getDefaultWorkspace(String)} directly.
     */
    public static String getDefaultWorkspace(String repositoryId) {
        return workspaceMapping.getDefaultWorkspace(repositoryId);
    }

}
