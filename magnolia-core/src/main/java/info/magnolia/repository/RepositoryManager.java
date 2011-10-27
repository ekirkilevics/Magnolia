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

    SessionAcquisitionStrategy getLifetimeSessionStrategy();

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
     * @param repository
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    boolean checkIfInitialized(String repository) throws RepositoryException, AccessDeniedException;

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
    void loadWorkspace(String repositoryId, String workspaceId) throws RepositoryException;

    /**
     * Gets repository names array as configured in repositories.xml.
     * @return repository names
     */
    Iterator<String> getLogicalWorkspaceNames();

    /**
     * Returns repository mapping as configured, or null if not found.
     */
    RepositoryDefinition getRepositoryDefinition(String repositoryId);

    /**
     * Returns repository provider specified by the <code>repositoryID</code> as configured in repository config.
     *
     * @throws IllegalArgumentException if there is no such repository
     */
    Provider getRepositoryProvider(String repositoryId);

    /**
     * Returns the physical workspace name.
     * @param logicalWorkspaceName
     * @return mapped name as in repositories.xml RepositoryMapping element
     */
    String getPhysicalWorkspaceName(String logicalWorkspaceName);

    String getRepositoryName(String logicalWorkspaceName);

    /**
     * Get default workspace name.
     * @return default name if there are no workspaces defined or there is no workspace present with name "default",
     * otherwise return same name as repository name.
     */
    String getDefaultWorkspace(String repositoryId);

    Collection<WorkspaceMappingDefinition> getWorkspaceMappings();

    boolean hasRepository(String repositoryId);

    Repository getRepository(String repositoryId);

    void addWorkspaceMapping(WorkspaceMappingDefinition mapping);
}
