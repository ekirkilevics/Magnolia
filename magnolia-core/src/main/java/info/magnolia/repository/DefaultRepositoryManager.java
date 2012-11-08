/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
import javax.inject.Singleton;
import javax.jcr.Credentials;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import info.magnolia.jcr.RuntimeRepositoryException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.version.MgnlVersioningSession;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ConfigUtil;
import info.magnolia.context.MgnlContext;
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

    @Override
    public void shutdown() {
        log.info("Shutting down JCR");
        for (RepositoryDefinition repositoryDefinition : workspaceMapping.getRepositoryDefinitions()) {
            Provider provider = workspaceMapping.getRepositoryProvider(repositoryDefinition.getName());
            provider.shutdownRepository();
        }
        workspaceMapping.clearAll();
    }

    @Override
    public boolean checkIfInitialized() throws AccessDeniedException, RepositoryException {
        Collection<String> workspaceNames = workspaceMapping.getLogicalWorkspaceNames();
        for (String workspace : workspaceNames) {
            if (checkIfInitialized(workspace)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkIfInitialized(String logicalWorkspace) throws RepositoryException, AccessDeniedException {
        log.debug("Checking [{}] repository.", logicalWorkspace);
        // TODO cant we login without using the system context?
        Session session = MgnlContext.getSystemContext().getJCRSession(logicalWorkspace);

        if (session == null) {
            throw new RuntimeException("Repository [" + logicalWorkspace + "] not loaded");
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
            log.debug("Content found in [{}].", logicalWorkspace);
            return true;
        }
        return false;
    }

    @Override
    public void reload() {

        // TODO what exactly is this method supposed to do?!

        log.info("Reloading JCR");
        init();
    }

    private void loadRepositories() throws Exception {
        final String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG);
        if (path == null) {
            throw new RepositoryNotInitializedException("No value found for property " + SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG + ": can not start repository.");
        }
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
            for (String workspaceId : definition.getWorkspaces()) {
                registerNameSpacesAndNodeTypes(workspaceId, definition, provider);
            }
        }
    }

    @Override
    public void loadWorkspace(String repositoryId, String physicalWorkspaceName) throws RepositoryException {
        log.info("Loading workspace {}", physicalWorkspaceName);

        workspaceMapping.addWorkspaceMapping(new WorkspaceMappingDefinition(physicalWorkspaceName, repositoryId, physicalWorkspaceName));

        Provider provider = getRepositoryProvider(repositoryId);
        provider.registerWorkspace(physicalWorkspaceName);
        RepositoryDefinition repositoryDefinition = workspaceMapping.getRepositoryDefinition(repositoryId);

        registerNameSpacesAndNodeTypes(physicalWorkspaceName, repositoryDefinition, provider);
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
            log.error("Failed to initialize workspace " + physicalWorkspaceName + " in repository " + repositoryDefinition.getName(), e);
        }
    }

    @Override
    public Session getSession(String logicalWorkspaceName, Credentials credentials) throws RepositoryException {
        WorkspaceMappingDefinition mapping = this.workspaceMapping.getWorkspaceMapping(logicalWorkspaceName);
        if (mapping == null) throw new NoSuchWorkspaceException(logicalWorkspaceName);
        Repository repository = getRepository(mapping.getRepositoryName());
        String physicalWorkspaceName = mapping.getPhysicalWorkspaceName();

        Session session = repository.login(credentials, physicalWorkspaceName);
        return wrapSession(session, logicalWorkspaceName);
    }

    @Override
    public Session getSystemSession(String logicalWorkspaceName) throws RepositoryException {
        WorkspaceMappingDefinition mapping = this.workspaceMapping.getWorkspaceMapping(logicalWorkspaceName);
        if (mapping == null) throw new NoSuchWorkspaceException(logicalWorkspaceName);
        Provider provider = getRepositoryProvider(mapping.getRepositoryName());
        return wrapSession(provider.getSystemSession(mapping.getPhysicalWorkspaceName()), logicalWorkspaceName);
    }

    private Session wrapSession(Session session, String logicalWorkspaceName) {
        session = new TrackingSessionWrapper(session, JCRStats.getInstance());
        if (RepositoryConstants.VERSION_STORE.equals(logicalWorkspaceName)) {
            //do not wrap version store in versioning session or we get infinite redirect loop and stack overflow
            return session;
        }
        return new MgnlVersioningSession(session);
    }

    @Override
    public boolean hasRepository(String repositoryId) {
        return workspaceMapping.getRepositoryDefinition(repositoryId) != null;
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

    @Override
    public Collection<WorkspaceMappingDefinition> getWorkspaceMappings() {
        return workspaceMapping.getWorkspaceMappings();
    }

    @Override
    public WorkspaceMappingDefinition getWorkspaceMapping(String logicalWorkspaceName) {
        return workspaceMapping.getWorkspaceMapping(logicalWorkspaceName);
    }

    @Override
    public Collection<String> getWorkspaceNames() {
        return workspaceMapping.getLogicalWorkspaceNames();
    }
}
