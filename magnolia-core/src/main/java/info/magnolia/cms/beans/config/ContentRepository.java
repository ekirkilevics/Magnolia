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
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.repository.RepositoryNotInitializedException;
import info.magnolia.repository.definition.RepositoryDefinition;
import info.magnolia.repository.definition.WorkspaceMappingDefinition;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Legacy type - will be dropped with one of the next release.
 *
 * @version $Id$
 *
 * @deprecated since 4.5 - Use {@link info.magnolia.repository.RepositoryConstants}, {@link info.magnolia.repository.WorkspaceMapping}, {@link info.magnolia.jcr.registry.SessionProviderRegistry} instead.
 */
public final class ContentRepository {

    private static final Logger log = LoggerFactory.getLogger(ContentRepository.class);

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

    public static final String DEFAULT_WORKSPACE = "default";

    /**
     * repository user.
     *
     * @deprecated since 4.5 - not yet decided how it will be replaced
     */
    public static String REPOSITORY_USER;

    /**
     * repository default password.
     *
     * @deprecated since 4.5 - not yet decided how it will be replaced
     */
    public static String REPOSITORY_PSWD;

    static {
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
     * loads all configured repository using ID as Key, as configured in repositories.xml.
     *
     * <pre>
     * &lt;Repository name="website"
     *                id="website"
     *                provider="info.magnolia.jackrabbit.ProviderImpl"
     *                loadOnStartup="true" >
     *   &lt;param name="configFile"
     *             value="WEB-INF/config/repositories/website.xml"/>
     *   &lt;param name="repositoryHome"
     *             value="repositories/website"/>
     *   &lt;param name="contextFactoryClass"
     *             value="org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory"/>
     *   &lt;param name="providerURL"
     *             value="localhost"/>
     *   &lt;param name="id" value="website"/>
     * &lt;/Repository>
     *</pre>
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#init()} directly.
     */
    public static void init() {
        Components.getComponent(RepositoryManager.class).init();
    }

    /**
     * Shuts down all repositories (through Provider instances) and clears all mappings.
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#shutdown()} directly.
     */
    public static void shutdown() {
        Components.getComponent(RepositoryManager.class).shutdown();
    }

    /**
     * Verify the initialization state of all the repositories. This methods returns <code>false</code> only if
     * <strong>all</strong> the repositories are empty (no node else than the root one).
     * @return <code>false</code> if all the repositories are empty, <code>true</code> if at least one of them has
     * content.
     * @throws AccessDeniedException repository authentication failed
     * @throws RepositoryException exception while accessing the repository
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#checkIfInitialized()} directly.
     */
    public static boolean checkIfInitialized() throws AccessDeniedException, RepositoryException {
        return Components.getComponent(RepositoryManager.class).checkIfInitialized();
    }

    /**
     * @param repository
     * @throws RepositoryException
     * @throws AccessDeniedException
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#checkIfInitialized(String)} directly.
     */
    public static boolean checkIfInitialized(String repository) throws RepositoryException, AccessDeniedException {
        return Components.getComponent(RepositoryManager.class).checkIfInitialized(repository);
    }

    /**
     * Re-load all configured repositories.
     * @see #init()
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#reload()} directly.
     */
    public static void reload() {
        Components.getComponent(RepositoryManager.class).reload();
    }

    /**
     * This method initializes the repository. You must not call this method twice.
     * @param map
     * @throws RepositoryNotInitializedException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#loadRepository(info.magnolia.repository.definition.RepositoryDefinition)} directly.
     */
    public static void loadRepository(RepositoryDefinition map) throws RepositoryNotInitializedException,
    InstantiationException, IllegalAccessException, ClassNotFoundException {
        Components.getComponent(RepositoryManager.class).loadRepository(map);
    }

    /**
     * @param repositoryId
     * @param workspaceId
     * @throws RepositoryException
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#loadWorkspace(String, String)} directly.
     * */
    public static void loadWorkspace(String repositoryId, String workspaceId) throws RepositoryException {
        Components.getComponent(RepositoryManager.class).loadWorkspace(repositoryId, workspaceId);
    }

    /**
     * Returns magnolia specific Repository name where this workspace is registered
     * within <Repository/>.
     * @deprecated since 4.5 - do not use.
     * */
    public static String getParentRepositoryName(String workspaceName) throws RepositoryException {
        throw new UnsupportedOperationException("Deprecated since 4.5");
    }

    /**
     * Get mapped repository name.
     * @param name
     * @return mapped name as in repositories.xml RepositoryMapping element
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getRepositoryName(String)}  directly.
     */
    public static String getMappedRepositoryName(String name) {
        return Components.getComponent(RepositoryManager.class).getRepositoryName(name);
    }

    /**
     * Get mapped workspace name.
     * @param name
     * @return mapped name as in repositories.xml RepositoryMapping element
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getPhysicalWorkspaceName(String)}  directly.
     */
    public static String getMappedWorkspaceName(String name) {
        String physicalWorkspaceName = Components.getComponent(RepositoryManager.class).getPhysicalWorkspaceName(name);
        if (physicalWorkspaceName == null)
            return name;
        return physicalWorkspaceName;
    }

    /**
     * Add a mapped repository name.
     * @param name
     * @param repositoryName
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#addWorkspaceMapping(info.magnolia.repository.definition.WorkspaceMappingDefinition)}  directly.
     */
    public static void addMappedRepositoryName(String name, String repositoryName) {
        addMappedRepositoryName(name, repositoryName, null);
    }

    /**
     * Add a mapped repository name.
     * @param name
     * @param repositoryName
     * @param workspaceName
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#addWorkspaceMapping(info.magnolia.repository.definition.WorkspaceMappingDefinition)}  directly.
     */
    public static void addMappedRepositoryName(String name, String repositoryName, String workspaceName) {
        if (StringUtils.isEmpty(workspaceName)) {
            workspaceName = name;
        }
        Components.getComponent(RepositoryManager.class).addWorkspaceMapping(new WorkspaceMappingDefinition(name, repositoryName, workspaceName));
    }

    /**
     * Get default workspace name.
     * @return default name if there are no workspaces defined or there is no workspace present with name "default",
     * otherwise return same name as repository name.
     */
    public static String getDefaultWorkspace(String repositoryId) {
        RepositoryDefinition mapping = getRepositoryMapping(repositoryId);
        if (mapping == null) {
            return DEFAULT_WORKSPACE;
        }
        Collection<String> workspaces = mapping.getWorkspaces();
        if (workspaces.contains(getMappedWorkspaceName(repositoryId))) {
            return repositoryId;
        }
        return DEFAULT_WORKSPACE;
    }

    /**
     * Returns repository specified by the <code>repositoryID</code> as configured in repository config.
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getRepository(String)}  directly.
     */
    public static Repository getRepository(String repositoryID) {
        return Components.getComponent(RepositoryManager.class).getRepository(repositoryID);
    }

    /**
     * Returns repository provider specified by the <code>repositoryID</code> as configured in repository config.
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getRepositoryProvider(String)}  directly.
     */
    public static Provider getRepositoryProvider(String repositoryID) {
        repositoryID = getMappedRepositoryName(repositoryID);
        return Components.getComponent(RepositoryManager.class).getRepositoryProvider(repositoryID);
    }

    /**
     * returns repository mapping as configured.
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getRepositoryDefinition(String)}  directly.
     */
    public static RepositoryDefinition getRepositoryMapping(String repositoryID) {
        return Components.getComponent(RepositoryManager.class).getRepositoryDefinition(repositoryID);
    }

    /**
     * Returns <code>true</code> if a mapping for the given repository name does exist.
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#hasRepository(String)} directly.
     */
    public static boolean hasRepositoryMapping(String repositoryID) {
        return Components.getComponent(RepositoryManager.class).hasRepository(repositoryID);
    }

    /**
     * Gets repository names array as configured in repositories.xml.
     * @return repository names
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getLogicalWorkspaceNames()} directly.
     */
    public static Iterator<String> getAllRepositoryNames() {
        return Components.getComponent(RepositoryManager.class).getLogicalWorkspaceNames();
    }

    /**
     * get internal workspace name.
     * @param workspaceName
     * @return workspace name as configured in magnolia repositories.xml
     * @deprecated since 4.5 - do not use.
     * */
    public static String getInternalWorkspaceName(String workspaceName) {
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        Collection<WorkspaceMappingDefinition> repositoryNameMap = repositoryManager.getWorkspaceMappings();
        for (WorkspaceMappingDefinition mappingDefinition : repositoryNameMap) {
            if (mappingDefinition.getWorkspaceName().equalsIgnoreCase(workspaceName)) {
                return mappingDefinition.getLogicalWorkspaceName();
            }
        }
        log.error("No Repository/Workspace name mapping defined for "+workspaceName);
        return workspaceName;
    }



}
