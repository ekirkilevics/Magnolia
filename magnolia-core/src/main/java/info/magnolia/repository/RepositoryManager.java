package info.magnolia.repository;

import java.util.Iterator;
import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import info.magnolia.cms.security.AccessDeniedException;

// WorkspaceAccessUtil
// LifetimeJCRSessionUtil
// ContentRepository
// JCRStats



public interface RepositoryManager {

    void init();

    void shutdown();

    // The implementation of this in ContentRepository seems fishy, it clears the Repository instances and reads the xml again
    // what is the effect on things added after init() ?
    void reload();

    Session getSession(String logicalWorkspaceName, Credentials credentials) throws RepositoryException;

    /**
     * The returned session must not be closed. It is intended to be alive for the lifetime of the application.
     */
    Session getSystemSession(String logicalWorkspaceName) throws RepositoryException;

    boolean checkIfInitialized() throws AccessDeniedException, RepositoryException;

    boolean checkIfInitialized(String repository) throws RepositoryException, AccessDeniedException;

    void loadRepository(RepositoryMapping map) throws RepositoryNotInitializedException, InstantiationException, IllegalAccessException, ClassNotFoundException;

    void loadWorkspace(String repositoryId, String workspaceId) throws RepositoryException;


    Iterator<String> getAllRepositoryNames();

    RepositoryMapping getRepositoryMapping(String repositoryId);

    Provider getRepositoryProvider(String repositoryId);

    /**
     * Returns the physical workspace name.
     * @param logicalWorkspaceName
     * @return mapped name as in repositories.xml RepositoryMapping element
     */
    String getMappedWorkspaceName(String logicalWorkspaceName);

    String getMappedRepositoryName(String logicalWorkspaceName);

    /**
     * Returns the logical name for a physical workspace.
     *
     * Note: if more than one repositories have a workspace with this name there's no guarantee which one it will return.
     *
     * @param workspaceName
     * @return
     */
    String getInternalWorkspaceName(String workspaceName);

    String getDefaultWorkspace(String repositoryId);

}
