/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
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
import java.util.Iterator;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

// WorkspaceAccessUtil
// LifetimeJCRSessionUtil
// ContentRepository
// JCRStats


/**
 * Main repository abstraction.
 *
 * @version $Id$
 */
public interface RepositoryManager {

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
     */
    void init();

    /**
     * Shuts down all repositories (through Provider instances) and clears all mappings.
     */
    void shutdown();

    // The implementation of this in ContentRepository seems fishy, it clears the Repository instances and reads the xml again
    // what is the effect on things added after init() ?
    /**
     * Re-load all configured repositories.
     * @see #init()
     */
    void reload();

    Session getSession(String logicalWorkspaceName, Credentials credentials) throws RepositoryException;

    Session getSystemSession(String logicalWorkspaceName) throws RepositoryException;

    /**
     * Verify the initialization state of all the repositories. This methods returns <code>false</code> only if
     * <strong>all</strong> the repositories are empty (no node else than the root one).
     * @return <code>false</code> if all the repositories are empty, <code>true</code> if at least one of them has
     * content.
     * @throws AccessDeniedException repository authentication failed
     * @throws RepositoryException exception while accessing the repository
     */
    boolean checkIfInitialized() throws AccessDeniedException, RepositoryException;

    /**
     * @param workspace
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    boolean checkIfInitialized(String workspace) throws RepositoryException, AccessDeniedException;

    /**
     * Adds a repository definition and instantiates its provider. If the loadOnStartup property is true it also
     * registers namespaces and node types. You must not call this method twice.
     */
    void loadRepository(RepositoryDefinition definition) throws RepositoryNotInitializedException, InstantiationException, IllegalAccessException, ClassNotFoundException;

    /**
     * Loads a workspace by registering namespaces and node types on a workspace that has not previously been loaded.
     *
     * TODO not sure if the workspaceId is supposed to be logical or physical, likely physical since we also pass a repo id which would be useless otherwise
     */
    void loadWorkspace(String repositoryId, String workspaceName) throws RepositoryException;

    boolean hasRepository(String repositoryId);

    /**
     * Returns repository mapping as configured, or null if not found.
     */
    RepositoryDefinition getRepositoryDefinition(String repositoryId);

    /**
     * Returns repository provider specified by the <code>repositoryId</code> as configured in repository config.
     *
     * @throws IllegalArgumentException if there is no such repository
     */
    Provider getRepositoryProvider(String repositoryId);

    Repository getRepository(String repositoryId);

    /**
     * Gets repository names array as configured in repositories.xml.
     * @return repository names
     */
    Iterator<String> getLogicalWorkspaceNames();

    WorkspaceMappingDefinition getWorkspaceMapping(String logicalWorkspaceName);

    Collection<WorkspaceMappingDefinition> getWorkspaceMappings();

    boolean hasWorkspace(String logicalWorkspaceName);

    void addWorkspaceMapping(WorkspaceMappingDefinition mapping);
}
