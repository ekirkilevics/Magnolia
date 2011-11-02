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
     * Name of the repository admin user.
     *
     * @deprecated since 4.5 - do not use
     */
    public static String REPOSITORY_USER;

    /**
     * Password for the repository admin user.
     *
     * @deprecated since 4.5 - do not use
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
     * Loads all configured repositories.
     *
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#init()} directly.
     */
    public static void init() {
        Components.getComponent(RepositoryManager.class).init();
    }

    /**
     * Shuts down all repositories (through Provider instances) and clears all mappings.
     *
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#shutdown()} directly.
     */
    public static void shutdown() {
        Components.getComponent(RepositoryManager.class).shutdown();
    }

    /**
     * Verify the initialization state of all the workspaces. This methods returns <code>false</code> only if
     * <strong>all</strong> the workspaces are empty (no node else than the root one).
     *
     * @return <code>false</code> if all the workspaces are empty, <code>true</code> if at least one of them has content.
     * @throws AccessDeniedException repository authentication failed
     * @throws RepositoryException exception while accessing the repository
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#checkIfInitialized()} directly.
     */
    public static boolean checkIfInitialized() throws AccessDeniedException, RepositoryException {
        return Components.getComponent(RepositoryManager.class).checkIfInitialized();
    }

    /**
     * Checks if a workspace has been initialized.
     *
     * @throws RepositoryException
     * @throws AccessDeniedException
     * @throws RuntimeException if the workspace doesn't exist or has not yet been loaded
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#checkIfInitialized(String)} directly.
     */
    public static boolean checkIfInitialized(String logicalWorkspaceName) throws RepositoryException, AccessDeniedException {
        return Components.getComponent(RepositoryManager.class).checkIfInitialized(logicalWorkspaceName);
    }

    /**
     * Reload all configured repositories.
     *
     * @see #init()
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#reload()} directly.
     */
    public static void reload() {
        Components.getComponent(RepositoryManager.class).reload();
    }

    /**
     * Adds a repository definition and loads it. You must not call this method twice.
     *
     * @param repositoryDefinition
     * @throws RepositoryNotInitializedException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#loadRepository(info.magnolia.repository.definition.RepositoryDefinition)} directly.
     */
    public static void loadRepository(RepositoryDefinition repositoryDefinition) throws RepositoryNotInitializedException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Components.getComponent(RepositoryManager.class).loadRepository(repositoryDefinition);
    }

    /**
     * Loads a previously not loaded workspace.
     *
     * If there's no logical workspace defined with the name it is added, if there's no physical workspace name by this name it is added.
     *
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#loadWorkspace(String, String)} directly.
     * */
    public static void loadWorkspace(String repositoryId, String workspaceName) throws RepositoryException {
        Components.getComponent(RepositoryManager.class).loadWorkspace(repositoryId, workspaceName);
    }

    /**
     * Returns magnolia specific Repository name where this workspace is registered within <Repository/>.
     *
     * Note: if more than one repository has a physical workspace by this name this method is ambigious as to which it returns
     *
     * @throws RepositoryException if no physical workspace exists by that name
     * @deprecated since 4.5 - do not use.
     */
    public static String getParentRepositoryName(String physicalWorkspaceName) throws RepositoryException {
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        for (WorkspaceMappingDefinition mapping : repositoryManager.getWorkspaceMappings()) {
            if (physicalWorkspaceName.equalsIgnoreCase(mapping.getPhysicalWorkspaceName())) {
                return mapping.getRepositoryName();
            }
        }
        throw new RepositoryException("No mapping found for "+physicalWorkspaceName+" repository in magnolia repositories.xml");
    }

    /**
     * Returns the name of the repository for a logical workspace name.
     *
     * @returns the repository name or if the logical name doesn't exist it is returned
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getWorkspaceMapping(String)}.
     */
    public static String getMappedRepositoryName(String logicalWorkspaceName) {
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        WorkspaceMappingDefinition mapping = repositoryManager.getWorkspaceMapping(logicalWorkspaceName);
        return mapping != null ? mapping.getRepositoryName() : logicalWorkspaceName;
    }

    /**
     * Returns the physical workspace name given a logical workspace name.
     *
     * @returns the physical name or if the logical name doesn't exist it is returned
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getWorkspaceMapping(String)}.
     */
    public static String getMappedWorkspaceName(String logicalWorkspaceName) {
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        WorkspaceMappingDefinition mapping = repositoryManager.getWorkspaceMapping(logicalWorkspaceName);
        return mapping != null ? mapping.getPhysicalWorkspaceName() : logicalWorkspaceName;
    }

    /**
     * Adds a workspace mapping.
     *
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#addWorkspaceMapping(info.magnolia.repository.definition.WorkspaceMappingDefinition)}  directly.
     */
    public static void addMappedRepositoryName(String logicalWorkspaceName, String repositoryName) {
        addMappedRepositoryName(logicalWorkspaceName, repositoryName, null);
    }

    /**
     * Adds a workspace mapping.
     *
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#addWorkspaceMapping(info.magnolia.repository.definition.WorkspaceMappingDefinition)}  directly.
     */
    public static void addMappedRepositoryName(String logicalWorkspaceName, String repositoryName, String workspaceName) {
        if (StringUtils.isEmpty(workspaceName)) {
            workspaceName = logicalWorkspaceName;
        }
        Components.getComponent(RepositoryManager.class).addWorkspaceMapping(new WorkspaceMappingDefinition(logicalWorkspaceName, repositoryName, workspaceName));
    }

    /**
     * Get default workspace name.
     *
     * @return default name if there are no workspaces defined or there is no workspace present with name "default", otherwise return same name as repository name.
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
     * Returns the repository for a repository or a workspace.
     *
     * @throws IllegalArgumentException if there is no repository or workspace by this name
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getRepository(String)}  directly.
     */
    public static Repository getRepository(String repositoryOrLogicalWorkspace) {
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        if (!repositoryManager.hasRepository(repositoryOrLogicalWorkspace)) {
            repositoryOrLogicalWorkspace = getMappedRepositoryName(repositoryOrLogicalWorkspace);
        }
        return repositoryManager.getRepository(repositoryOrLogicalWorkspace);
    }

    /**
     * Returns the repository provider for either a repository or the provider for the repository which contains the workspace.
     *
     * @throws IllegalArgumentException if there is no repository or workspace by this name
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getRepositoryProvider(String)}  directly.
     */
    public static Provider getRepositoryProvider(String repositoryOrLogicalWorkspace) {
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        if (!repositoryManager.hasRepository(repositoryOrLogicalWorkspace)) {
            repositoryOrLogicalWorkspace = getMappedRepositoryName(repositoryOrLogicalWorkspace);
        }
        return repositoryManager.getRepositoryProvider(repositoryOrLogicalWorkspace);
    }

    /**
     * Returns the repository definition for a repository or the repository for a given workspace.
     *
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getRepositoryDefinition(String)}  directly.
     */
    public static RepositoryDefinition getRepositoryMapping(String repositoryOrLogicalWorkspace) {
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        if (!repositoryManager.hasRepository(repositoryOrLogicalWorkspace)) {
            repositoryOrLogicalWorkspace = getMappedRepositoryName(repositoryOrLogicalWorkspace);
        }
        return repositoryManager.getRepositoryDefinition(repositoryOrLogicalWorkspace);
    }

    /**
     * Returns <code>true</code> if a mapping for the given repository name or workspace name exist.
     *
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#hasRepository(String)} directly.
     */
    public static boolean hasRepositoryMapping(String repositoryOrLogicalWorkspace) {
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        if (!repositoryManager.hasRepository(repositoryOrLogicalWorkspace)) {
            repositoryOrLogicalWorkspace = getMappedRepositoryName(repositoryOrLogicalWorkspace);
        }
        return repositoryManager.hasRepository(repositoryOrLogicalWorkspace);
    }

    /**
     * Gets the names of all logical workspaces.
     * 
     * @deprecated since 4.5 - use {@link info.magnolia.repository.RepositoryManager#getWorkspaceNames()} directly.
     */
    public static Iterator<String> getAllRepositoryNames() {
        return Components.getComponent(RepositoryManager.class).getWorkspaceNames().iterator();
    }

    /**
     * Returns the logical workspace name given a physical workspace name. Note that this method will return the FIRST
     * physical workspace it finds with this name. If there are more workspaces with this name it is not defined which
     * is returned.
     *
     * @return the logical workspace name or if no such logical workspace exists returns the physical workspace name given as an argument
     * @deprecated since 4.5 - do not use.
     * */
    public static String getInternalWorkspaceName(String physicalWorkspaceName) {
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        Collection<WorkspaceMappingDefinition> repositoryNameMap = repositoryManager.getWorkspaceMappings();
        for (WorkspaceMappingDefinition mappingDefinition : repositoryNameMap) {
            if (mappingDefinition.getPhysicalWorkspaceName().equalsIgnoreCase(physicalWorkspaceName)) {
                return mappingDefinition.getLogicalWorkspaceName();
            }
        }
        log.error("No Repository/Workspace name mapping defined for "+physicalWorkspaceName);
        return physicalWorkspaceName;
    }



}
