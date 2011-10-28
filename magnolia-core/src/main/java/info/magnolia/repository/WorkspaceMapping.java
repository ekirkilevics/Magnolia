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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.jcr.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.repository.definition.RepositoryDefinition;
import info.magnolia.repository.definition.WorkspaceMappingDefinition;

/**
 * Maintains a registry of repositories and a list of mappings from logical workspaces to the real workspaces in the
 * repositories. It also keeps lookup maps of providers and {@link Repository} instances for each repository.
 *
 * @version $Id$
 */
public class WorkspaceMapping {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceMapping.class);

    /**
     * Map of Repository instances for each repository.
     */
    private Map<String, Repository> repositories = new Hashtable<String, Repository>();

    /**
     * Map of Provider instances for each repository.
     */
    private Map<String, Provider> providers = new Hashtable<String, Provider>();

    /**
     * Map of configured repositories.
     */
    private Map<String, RepositoryDefinition> repositoryDefinitions = new Hashtable<String, RepositoryDefinition>();

    /**
     * Map of configured workspace mappings.
     */
    private Map<String, WorkspaceMappingDefinition> workspaceMappingDefinitions = new Hashtable<String, WorkspaceMappingDefinition>();


    public void clearRepositories() {
        repositories.clear();
    }

    public void clearAll() {
        providers.clear();
        repositoryDefinitions.clear();
        workspaceMappingDefinitions.clear();
        clearRepositories();
    }

    public void addRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
        repositoryDefinitions.put(repositoryDefinition.getName(), repositoryDefinition);
    }

    /**
     * Adds workspace mapping, if the physical workspace doesn't exist it is added.
     */
    public void addWorkspaceMapping(WorkspaceMappingDefinition mapping) {
        if (workspaceMappingDefinitions.containsKey(mapping.getLogicalWorkspaceName())) {
            return;
        }

        addWorkspaceMappingDefinition(mapping);

        RepositoryDefinition repositoryDefinition = getRepositoryDefinition(mapping.getRepositoryName());
        if (!repositoryDefinition.getWorkspaces().contains(mapping.getWorkspaceName())) {
            repositoryDefinition.addWorkspace(mapping.getWorkspaceName());
        }
    }

    public void addWorkspaceMappingDefinition(WorkspaceMappingDefinition definition) {
        workspaceMappingDefinitions.put(definition.getLogicalWorkspaceName(), definition);
    }

    /**
     * Returns a configured repository definition.
     */
    public RepositoryDefinition getRepositoryDefinition(String repositoryId) {
        return repositoryDefinitions.get(repositoryId);
    }

    public Iterator<String> getLogicalWorkspaceNames() {
        return workspaceMappingDefinitions.keySet().iterator();
    }

    public WorkspaceMappingDefinition getWorkspaceMapping(String logicalWorkspaceName) {
        return workspaceMappingDefinitions.get(logicalWorkspaceName);
    }

    /**
     * Returns the name of the repository that a logical workspace name is mapped to.
     */
    public String getRepositoryName(String logicalWorkspaceName) {
        WorkspaceMappingDefinition mapping = workspaceMappingDefinitions.get(logicalWorkspaceName);
        return mapping != null ? mapping.getRepositoryName() : null;
    }

    public boolean hasRepository(String repositoryId) {
        return repositoryDefinitions.containsKey(repositoryId);
    }

    public Collection<WorkspaceMappingDefinition> getWorkspaceMappings() {
        return workspaceMappingDefinitions.values();
    }

    public void setRepository(String repositoryId, Repository repository) {
        repositories.put(repositoryId, repository);
    }

    public void setRepositoryProvider(String repositoryId, Provider provider) {
        providers.put(repositoryId, provider);
    }

    /**
     * Returns repository specified by the <code>repositoryID</code> as configured in repository config.
     */
    public Repository getRepository(String repositoryId) {
        Repository repository = repositories.get(repositoryId);
        if (repository == null) {
            final String s = "Failed to retrieve repository '" + repositoryId + "'. Your Magnolia instance might not have been initialized properly.";
            log.warn(s);
            throw new IllegalArgumentException(s);
        }
        return repository;
    }

    /**
     * Returns repository provider specified by the <code>repositoryID</code> as configured in repository config.
     */
    public Provider getRepositoryProvider(String repositoryId) {
        Provider provider = providers.get(repositoryId);
        if (provider == null) {
            final String s = "Failed to retrieve repository provider '" + repositoryId + "'. Your Magnolia instance might not have been initialized properly.";
            log.warn(s);
            throw new IllegalArgumentException(s);
        }
        return provider;
    }

    public Iterator<Provider> getRepositoryProviders() {
        return providers.values().iterator();
    }
}
