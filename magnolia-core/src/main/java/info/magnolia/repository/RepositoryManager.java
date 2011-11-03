/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.repository.definition.RepositoryDefinition;
import info.magnolia.repository.definition.WorkspaceMappingDefinition;

import java.util.Collection;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;


/**
 * Manages JCR repositories. Initialization of a repository is handled by {@link Provider}s. Magnolia can be configured
 * to have its workspaces in more than one repository. This is abstracted through this class which maps a set of
 * "logical" workspace names to their actual "physical" workspace names in a repository.
 *
 * Configuration of providers and workspace mappings are done in repositories.xml.
 *
 * @see Provider
 * @see info.magnolia.repository.definition.RepositoryMappingDefinitionReader
 * @version $Id$
 */
public interface RepositoryManager {

    /**
     * Initializes by loading configuration from repositories.xml.
     */
    void init();

    /**
     * Shuts down all repositories (through Provider instances) and clears all mappings.
     */
    void shutdown();

    // TODO The implementation of this in ContentRepository seems fishy, it clears the Repository instances and reads the xml again
    // what is the effect on things added after init() ?
    /**
     * Re-load all configured repositories.
     * @see #init()
     */
    void reload();

    Session getSession(String logicalWorkspaceName, Credentials credentials) throws RepositoryException;

    Session getSystemSession(String logicalWorkspaceName) throws RepositoryException;

    /**
     * Verify the initialization state of all the workspaces. This methods returns <code>false</code> only if
     * <strong>all</strong> the workspaces are empty (no node else than the root one).
     *
     * @return <code>false</code> if all the workspaces are empty, <code>true</code> if at least one of them has content.
     * @throws AccessDeniedException repository authentication failed
     * @throws RepositoryException exception while accessing the repository
     */
    boolean checkIfInitialized() throws AccessDeniedException, RepositoryException;

    /**
     * Verifies the initialization state of a workspace.
     */
    boolean checkIfInitialized(String logicalWorkspace) throws RepositoryException, AccessDeniedException;

    /**
     * Adds a repository definition and instantiates its provider. If the loadOnStartup property is true it also
     * registers namespaces and node types. You must not call this method twice.
     */
    void loadRepository(RepositoryDefinition definition) throws RepositoryNotInitializedException, InstantiationException, IllegalAccessException, ClassNotFoundException;

    /**
     * Loads a workspace by registering namespaces and node types on a workspace that has not previously been loaded.
     * Also adds a workspace mapping that maps the physical workspace name as a logical name.
     */
    void loadWorkspace(String repositoryId, String physicalWorkspaceName) throws RepositoryException;

    boolean hasRepository(String repositoryId);

    /**
     * Returns repository mapping as configured, or null if not found.
     */
    RepositoryDefinition getRepositoryDefinition(String repositoryId);

    /**
     * Returns the provider instance for a repository.
     *
     * @throws IllegalArgumentException if there is no such repository
     */
    Provider getRepositoryProvider(String repositoryId);

    /**
     * Returns repository instance for a repository.
     *
     * @throws IllegalArgumentException if there is no such repository
     */
    Repository getRepository(String repositoryId);

    void addWorkspaceMapping(WorkspaceMappingDefinition mapping);

    boolean hasWorkspace(String logicalWorkspaceName);

    Collection<WorkspaceMappingDefinition> getWorkspaceMappings();

    WorkspaceMappingDefinition getWorkspaceMapping(String logicalWorkspaceName);

    /**
     * Returns workspace names.
     *
     * @return repository names
     */
    Collection<String> getWorkspaceNames();
}
