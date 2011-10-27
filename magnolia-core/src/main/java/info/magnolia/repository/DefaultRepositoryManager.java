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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ConfigUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.registry.DefaultSessionProvider;
import info.magnolia.jcr.registry.SessionProviderRegistry;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.repository.definition.RepositoryDefinition;
import info.magnolia.repository.definition.RepositoryMappingDefinition;
import info.magnolia.repository.definition.RepositoryMappingDefinitionReader;
import info.magnolia.repository.definition.WorkspaceMappingDefinition;

import java.io.InputStream;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all used Repositories.
 *
 * @version $Id$
 */
@Singleton
public final class DefaultRepositoryManager implements RepositoryManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultRepositoryManager.class);

    private final WorkspaceMapping workspaceMapping;

    private String repositoryUser;

    private String repositoryPassword;

    /**
     * Utility class, don't instantiate.
     */
    @Inject
    DefaultRepositoryManager(WorkspaceMapping workspaceMapping) {
        this.workspaceMapping = workspaceMapping;
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
     * </pre>
     */
    @Override
    public void init() {
        log.info("Loading JCR");
        workspaceMapping.clearRepositories();
        try {
            loadRepositories();
            log.debug("JCR loaded");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Shuts down all repositories (through Provider instances) and clears all mappings.
     */
    @Override
    public void shutdown() {
        log.info("Shutting down JCR");
        final Iterator<Provider> providers = workspaceMapping.getRepositoryProviders();
        while (providers.hasNext()) {
            final Provider provider = providers.next();
            provider.shutdownRepository();
        }
        workspaceMapping.clearAll();
    }

    /**
     * Verify the initialization state of all the repositories. This methods returns <code>false</code> only if
     * <strong>all</strong> the repositories are empty (no node else than the root one).
     *
     * @return <code>false</code> if all the repositories are empty, <code>true</code> if at least one of them has
     *         content.
     * @throws AccessDeniedException
     *             repository authentication failed
     * @throws RepositoryException
     *             exception while accessing the repository
     */
    @Override
    public boolean checkIfInitialized() throws AccessDeniedException, RepositoryException {
        Iterator<String> repositoryNames = workspaceMapping.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repository = repositoryNames.next();
            if (checkIfInitialized(repository)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param repository
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    @Override
    public boolean checkIfInitialized(String repository) throws RepositoryException, AccessDeniedException {
        log.debug("Checking [{}] repository.", repository);
        Session session = MgnlContext.getSystemContext().getJCRSession(repository);

        if (session == null) {
            throw new RuntimeException("Repository [" + repository + "] not loaded");
        }

        Node startPage = session.getRootNode();

        // return any kind of children
        Iterable<Node> children = NodeUtil.getNodes(startPage, new AbstractPredicate<Node>() {
            @Override
            public boolean evaluateTyped(Node content) {
                String name;
                try {
                    name = content.getName();
                } catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
                return (!name.startsWith(MgnlNodeType.JCR_PREFIX) && !name.startsWith("rep:"));
            }
        });

        if (children.iterator().hasNext()) {
            log.debug("Content found in [{}].", repository);
            return true;
        }
        return false;
    }

    /**
     * Re-load all configured repositories.
     *
     * @see #init()
     */
    @Override
    public void reload() {
        log.info("Reloading JCR");
        init();
    }

    /**
     * Load repository mappings and params using repositories.xml.
     *
     * @throws Exception
     */
    private void loadRepositories() throws Exception {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG);
        final String tokenizedConfig = ConfigUtil.getTokenizedConfigFile(path);
        InputStream stream = IOUtils.toInputStream(tokenizedConfig);

        RepositoryMappingDefinitionReader reader = new RepositoryMappingDefinitionReader();
        RepositoryMappingDefinition mapping = reader.read(stream);

        for(RepositoryDefinition repo: mapping.getRepositories()) {
            workspaceMapping.addRepositoryDefinition(repo);
            loadRepository(repo);
        }

        for(WorkspaceMappingDefinition wsDef: mapping.getWorkspaceMappings()) {
            workspaceMapping.addWorkspaceMappingDefinition(wsDef);
        }
    }

    /**
     * This method initializes the repository. You must not call this method twice.
     *
     * @param map
     * @throws RepositoryNotInitializedException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Override
    public void loadRepository(RepositoryDefinition map) throws RepositoryNotInitializedException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("Loading JCR {}", map.getName());
        Provider handlerClass = Classes.newInstance(map.getProvider());
        handlerClass.init(map);
        Repository repository = handlerClass.getUnderlyingRepository();
        workspaceMapping.addWorkspaceNameToRepository(map.getName(), repository);
        workspaceMapping.addWorkspaceNameToProvider(map.getName(), handlerClass);
        if (map.isLoadOnStartup()) {
            // load hierarchy managers for each workspace
            Iterator<String> workspaces = map.getWorkspaces().iterator();
            while (workspaces.hasNext()) {
                String wspID = workspaces.next();
                registerNameSpacesAndNodeTypes(repository, wspID, map, handlerClass);
            }
        }
    }

    /**
     * @param repositoryId
     * @param workspaceId
     * @throws RepositoryException
     * */
    @Override
    public void loadWorkspace(String repositoryId, String workspaceId) throws RepositoryException {
        log.info("Loading workspace {}", workspaceId);

        // TODO dlipp - we should create an WorkspaceMappingDefinition here
        workspaceMapping.addWorkspaceNameToRepositoryNameIfNotYetAvailable(new WorkspaceMappingDefinition(workspaceId,
                repositoryId, workspaceId));
        Provider provider = workspaceMapping.getRepositoryProvider(repositoryId);
        provider.registerWorkspace(workspaceId);
        Repository repo = workspaceMapping.getRepository(repositoryId);
        RepositoryDefinition map = workspaceMapping.getRepositoryMapping(repositoryId);

        registerNameSpacesAndNodeTypes(repo, workspaceId, map, provider);
    }

    /**
     * Load hierarchy manager for the specified repository and workspace.
     *
     * TODO dlipp - better naming!
     */
    private void registerNameSpacesAndNodeTypes(Repository repository, String wspID, RepositoryDefinition map, Provider provider) {
        try {
            SimpleCredentials sc = new SimpleCredentials(ContentRepository.REPOSITORY_USER, ContentRepository.REPOSITORY_PSWD.toCharArray());
            SessionProviderRegistry sessionProviderRegistry = Components.getComponent(SessionProviderRegistry.class);
            // FIXME dlipp - hack for now. Logical and physical workspaceName are identical here!
            sessionProviderRegistry.register(new DefaultSessionProvider(wspID, repository, wspID));
            try {
                Session session = sessionProviderRegistry.get(wspID).createSession(sc);
                try {
                    provider.registerNamespace(RepositoryConstants.NAMESPACE_PREFIX, RepositoryConstants.NAMESPACE_URI,
                            session.getWorkspace());
                    provider.registerNodeTypes();
                } finally {
                    session.logout();
                }
            } catch (RegistrationException e) {
                throw new RepositoryException(e);
            }
        } catch (RepositoryException e) {
            log.error("Failed to initialize hierarchy manager for JCR " + map.getName(), e);
        }
    }

    @Override
    public Session getSession(String logicalWorkspaceName, Credentials credentials) throws RepositoryException {
        throw new NotImplementedException("Work in progress");
    }

    @Override
    public Session getSystemSession(String logicalWorkspaceName) throws RepositoryException {
        throw new NotImplementedException("Work in progress");
    }

    @Override
    public Iterator<String> getAllRepositoryNames() {
        return workspaceMapping.getAllRepositoryNames();
    }

    @Override
    public RepositoryDefinition getRepositoryMapping(String repositoryId) {
        return workspaceMapping.getRepositoryMapping(repositoryId);
    }

    @Override
    public Provider getRepositoryProvider(String repositoryId) {
        return workspaceMapping.getRepositoryProvider(repositoryId);
    }

    @Override
    public String getMappedWorkspaceName(String logicalWorkspaceName) {
        return workspaceMapping.getMappedWorkspaceName(logicalWorkspaceName);
    }

    @Override
    public String getMappedRepositoryName(String logicalWorkspaceName) {
        return workspaceMapping.getMappedRepositoryName(logicalWorkspaceName);
    }

    @Override
    public String getInternalWorkspaceName(String workspaceName) {
        return workspaceMapping.getInternalWorkspaceName(workspaceName);
    }

    @Override
    public String getDefaultWorkspace(String repositoryId) {
        return workspaceMapping.getDefaultWorkspace(repositoryId);
    }
}
