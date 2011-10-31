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

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import javax.inject.Singleton;
import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.version.MgnlVersioningSession;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ConfigUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.definition.RepositoryDefinition;
import info.magnolia.repository.definition.RepositoryMappingDefinition;
import info.magnolia.repository.definition.RepositoryMappingDefinitionReader;
import info.magnolia.repository.definition.WorkspaceMappingDefinition;
import info.magnolia.repository.mbean.TrackingSessionWrapper;
import info.magnolia.stats.JCRStats;

/**
 * Manages all used Repositories.
 *
 * @version $Id$
 */
@Singleton
public final class DefaultRepositoryManager implements RepositoryManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultRepositoryManager.class);

    private final WorkspaceMapping workspaceMapping = new WorkspaceMapping();

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
     * @throws AccessDeniedException repository authentication failed
     * @throws RepositoryException   exception while accessing the repository
     */
    @Override
    public boolean checkIfInitialized() throws AccessDeniedException, RepositoryException {
        Iterator<String> repositoryNames = workspaceMapping.getLogicalWorkspaceNames();
        while (repositoryNames.hasNext()) {
            String repository = repositoryNames.next();
            if (checkIfInitialized(repository)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param workspace
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    @Override
    public boolean checkIfInitialized(String workspace) throws RepositoryException, AccessDeniedException {
        log.debug("Checking [{}] repository.", workspace);
        // TODO cant we login without using the system context?
        Session session = MgnlContext.getSystemContext().getJCRSession(workspace);

        if (session == null) {
            throw new RuntimeException("Repository [" + workspace + "] not loaded");
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
            log.debug("Content found in [{}].", workspace);
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

        // TODO what exactly is this method supposed to do?!

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

        for (RepositoryDefinition repositoryDefinition : mapping.getRepositories()) {
            if (repositoryDefinition.getWorkspaces().isEmpty()) {
                repositoryDefinition.addWorkspace("default");
            }
            workspaceMapping.addRepositoryDefinition(repositoryDefinition);
            loadRepository(repositoryDefinition);
        }

        for (WorkspaceMappingDefinition workspaceMapping : mapping.getWorkspaceMappings()) {
            this.workspaceMapping.addWorkspaceMappingDefinition(workspaceMapping);
        }
    }

    /**
     * This method initializes the repository. You must not call this method twice.
     *
     * @param definition
     * @throws RepositoryNotInitializedException
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Override
    public void loadRepository(RepositoryDefinition definition) throws RepositoryNotInitializedException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("Loading JCR {}", definition.getName());

        Class<? extends Provider> providerClass = Classes.getClassFactory().forName(definition.getProvider());
        Provider provider = Components.getComponentProvider().newInstance(providerClass);
        provider.init(definition);
        Repository repository = provider.getUnderlyingRepository();
        workspaceMapping.setRepository(definition.getName(), repository);
        workspaceMapping.setRepositoryProvider(definition.getName(), provider);

        if (definition.isLoadOnStartup()) {
            // load hierarchy managers for each workspace
            for (String workspaceId : definition.getWorkspaces()) {
                registerNameSpacesAndNodeTypes(workspaceId, definition, provider);
            }
        }
    }

    /**
     * @param repositoryId
     * @param workspaceName
     * @throws RepositoryException
     */
    @Override
    public void loadWorkspace(String repositoryId, String workspaceName) throws RepositoryException {
        log.info("Loading workspace {}", workspaceName);

        workspaceMapping.addWorkspaceMapping(new WorkspaceMappingDefinition(workspaceName, repositoryId, workspaceName));

        Provider provider = getRepositoryProvider(repositoryId);
        provider.registerWorkspace(workspaceName);
        RepositoryDefinition repositoryDefinition = workspaceMapping.getRepositoryDefinition(repositoryId);

        registerNameSpacesAndNodeTypes(workspaceName, repositoryDefinition, provider);
    }

    private void registerNameSpacesAndNodeTypes(String physicalWorkspaceName, RepositoryDefinition repositoryDefinition, Provider provider) {
        try {
            Session session = provider.getSystemSession(physicalWorkspaceName);
            try {
                provider.registerNamespace(RepositoryConstants.NAMESPACE_PREFIX, RepositoryConstants.NAMESPACE_URI, session.getWorkspace());
                provider.registerNodeTypes();
            } finally {
                session.logout();
            }
        } catch (RepositoryException e) {
            log.error("Failed to initialize hierarchy manager for JCR " + repositoryDefinition.getName(), e);
        }
    }

    @Override
    public Session getSession(String logicalWorkspaceName, Credentials credentials) throws RepositoryException {
        WorkspaceMappingDefinition mapping = this.workspaceMapping.getWorkspaceMapping(logicalWorkspaceName);
        Repository repository = getRepository(mapping.getRepositoryName());
        String physicalWorkspaceName = mapping.getWorkspaceName();

        Session session = repository.login(credentials, physicalWorkspaceName);
        return wrapSession(session, logicalWorkspaceName);
    }

    @Override
    public Session getSystemSession(String logicalWorkspaceName) throws RepositoryException {
        WorkspaceMappingDefinition mapping = this.workspaceMapping.getWorkspaceMapping(logicalWorkspaceName);
        Provider provider = getRepositoryProvider(mapping.getRepositoryName());
        return wrapSession(provider.getSystemSession(mapping.getWorkspaceName()), logicalWorkspaceName);
    }

    private Session wrapSession(Session session, String logicalWorkspaceName) {
        session = new TrackingSessionWrapper(session, JCRStats.getInstance());
        if (RepositoryConstants.VERSION_STORE.equals(logicalWorkspaceName)) {
            //do not wrapp version store in versioning session or we get infinite redirect loop and stack overflow
            return session;
        }
        return new MgnlVersioningSession(session);
    }

    @Override
    public WorkspaceMappingDefinition getWorkspaceMapping(String logicalWorkspaceName) {
        return workspaceMapping.getWorkspaceMapping(logicalWorkspaceName);
    }

    @Override
    public Iterator<String> getLogicalWorkspaceNames() {
        return workspaceMapping.getLogicalWorkspaceNames();
    }

    @Override
    public RepositoryDefinition getRepositoryDefinition(String repositoryId) {
        return workspaceMapping.getRepositoryDefinition(repositoryId);
    }

    @Override
    public Provider getRepositoryProvider(String repositoryId) {
        return workspaceMapping.getRepositoryProvider(repositoryId);
    }

    @Override
    public Collection<WorkspaceMappingDefinition> getWorkspaceMappings() {
        return workspaceMapping.getWorkspaceMappings();
    }

    @Override
    public boolean hasRepository(String repositoryId) {
        return workspaceMapping.getRepositoryDefinition(repositoryId) != null;
    }

    @Override
    public Repository getRepository(String repositoryId) {
        return workspaceMapping.getRepository(repositoryId);
    }

    @Override
    public void addWorkspaceMapping(WorkspaceMappingDefinition mapping) {
        workspaceMapping.addWorkspaceMapping(mapping);
    }

    @Override
    public boolean hasWorkspace(String logicalWorkspaceName) {
        return workspaceMapping.getWorkspaceMapping(logicalWorkspaceName) != null;
    }
}
