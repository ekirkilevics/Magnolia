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
package info.magnolia.repository;


import info.magnolia.repository.definition.RepositoryDefinition;
import info.magnolia.repository.definition.WorkspaceMappingDefinition;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Singleton;
import javax.jcr.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps mapping between sessions, physical- and logical workspace names.
 *
 * TODO dlipp - naming? Does it keep workspace or repository mappings?
 * TODO dlipp - check naming of all maps and methods!
 *
 * TODO dlipp - move newly created replacements for ContentRepo to info.magnolia.repository
 *
 * @version $Id$
 */
@Singleton
public class WorkspaceMapping {
    private static final Logger log = LoggerFactory.getLogger(WorkspaceMapping.class);

    public static final String DEFAULT_WORKSPACE_NAME = "default";

    /**
     * All available repositories store.
     */
    private static Map<String, Repository> repositories = new Hashtable<String, Repository>();

    /**
     * JCR providers as mapped in repositories.xml.
     */
    private static Map<String, Provider> repositoryProviders = new Hashtable<String, Provider>();

    /**
     * Repositories configuration as defined in repositories mapping file via attribute
     * <code>magnolia.repositories.config</code>.
     */
    private static Map<String, RepositoryDefinition> repositoryDefinitions = new Hashtable<String, RepositoryDefinition>();

    /**
     * holds all repository names as configured in repositories.xml.
     */
    private static Map<String, WorkspaceMappingDefinition> workspaceMappingDefinitions = new Hashtable<String, WorkspaceMappingDefinition>();


    public void clearRepositories() {
        repositories.clear();
    }

    public void clearAll() {
        repositoryProviders.clear();
        repositoryDefinitions.clear();
        workspaceMappingDefinitions.clear();
        clearRepositories();
    }

    public Iterator<Provider> getRepositoryProviders() {
        return repositoryProviders.values().iterator();
    }

    public void addRepositoryDefinition(RepositoryDefinition mapping) {
        repositoryDefinitions.put(mapping.getName(), mapping);
    }

    // TODO dlipp - find better method- and param-name
    public void addWorkspaceNameToRepositoryNameIfNotYetAvailable(WorkspaceMappingDefinition definition) {
        if(!workspaceMappingDefinitions.containsKey(definition.getLogicalWorkspaceName())){
            addWorkspaceMappingDefinition(definition);
        }
        // TODO dlipp - comment why the next lines are needed
        RepositoryDefinition map = getRepositoryMapping(definition.getRepositoryName());
        if(!map.getWorkspaces().contains(definition.getWorkspaceName())){
            map.addWorkspace(definition.getWorkspaceName());
        }
    }

    public void addWorkspaceNameToRepository(String workspaceName, Repository repo) {
        repositories.put(workspaceName, repo);
    }

    public void addWorkspaceNameToProvider(String workspaceName, Provider provider) {
        repositoryProviders.put(workspaceName, provider);
    }

    /**
     * Get mapped repository name.
     * @param name
     * @return mapped name as in repositories.xml RepositoryMapping element
     */
    public String getMappedRepositoryName(String name) {
        WorkspaceMappingDefinition nameMap = workspaceMappingDefinitions.get(name);
        if(nameMap==null){
            return name;
        }
        return nameMap.getRepositoryName();
    }


    public void addWorkspaceMappingDefinition(WorkspaceMappingDefinition definition) {
        workspaceMappingDefinitions.put(definition.getLogicalWorkspaceName(), definition);
    }

    /**
     * Get default workspace name.
     * @return default name if there are no workspaces defined or there is no workspace present with name "default",
     * otherwise return same name as repository name.
     */
    public String getDefaultWorkspace(String repositoryId) {
        RepositoryDefinition mapping = getRepositoryMapping(repositoryId);
        if (mapping == null) {
            return DEFAULT_WORKSPACE_NAME;
        }
        Collection<String> workspaces = mapping.getWorkspaces();
        if (workspaces.contains(getMappedRepositoryName(repositoryId))) {
            return repositoryId;
        }
        return DEFAULT_WORKSPACE_NAME;
    }

    /**
     * Returns repository specified by the <code>repositoryID</code> as configured in repository config.
     */
    public Repository getRepository(String repositoryID) {
        Repository repository = repositories.get(repositoryID);
        if (repository == null) {
            final String mappedRepositoryName = getMappedRepositoryName(repositoryID);
            if (mappedRepositoryName != null) {
                repository = repositories.get(mappedRepositoryName);
            }
            if (repository == null) {
                final String s = "Failed to retrieve repository '" + repositoryID + "' (mapped as '" + mappedRepositoryName + "'). Your Magnolia instance might not have been initialized properly.";
                log.warn(s);
                throw new IllegalArgumentException(s);
            }
        }
        return repository;
    }

    /**
     * Returns repository provider specified by the <code>repositoryID</code> as configured in repository config.
     */
    public Provider getRepositoryProvider(String repositoryID) {
        Provider provider = repositoryProviders.get(repositoryID);
        if (provider == null) {
            final String mappedRepositoryName = getMappedRepositoryName(repositoryID);
            if (mappedRepositoryName != null) {
                provider = repositoryProviders.get(mappedRepositoryName);
            }
            if (provider == null) {
                final String s = "Failed to retrieve repository provider '" + repositoryID + "' (mapped as '" + mappedRepositoryName + "'). Your Magnolia instance might not have been initialized properly.";
                log.warn(s);
                throw new IllegalArgumentException(s);
            }
        }
        return provider;
    }

    /**
     * returns repository mapping as configured.
     */
    public RepositoryDefinition getRepositoryMapping(String repositoryID) {
        String name = getMappedRepositoryName(repositoryID);
        if (name != null && repositoryDefinitions.containsKey(name)) {
            return repositoryDefinitions.get(getMappedRepositoryName(repositoryID));
        }
        log.debug("No mapping for the repository {}", repositoryID);
        return null;
    }

    /**
     * Gets repository names array as configured in repositories.xml.
     * @return repository names
     */
    public Iterator<String> getAllRepositoryNames() {
        return workspaceMappingDefinitions.keySet().iterator();
    }

    /**
     * get internal workspace name.
     * @param workspaceName
     * @return workspace name as configured in magnolia repositories.xml
     * */
    public String getInternalWorkspaceName(String workspaceName) {
        Iterator<String> keys = workspaceMappingDefinitions.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            WorkspaceMappingDefinition nameMap = workspaceMappingDefinitions.get(key);
            if (nameMap.getWorkspaceName().equalsIgnoreCase(workspaceName)) {
                return key;
            }
        }
        log.error("No Repository/Workspace name mapping defined for "+workspaceName);
        return workspaceName;
    }

    /**
     * Get mapped workspace name.
     * @param name
     * @return mapped name as in repositories.xml RepositoryMapping element
     */
    public String getMappedWorkspaceName(String name) {
        WorkspaceMappingDefinition nameMap = workspaceMappingDefinitions.get(name);
        if (nameMap == null) {
            return name;
        }
        return nameMap.getWorkspaceName();
    }

}
