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
package info.magnolia.cms.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.predicate.Predicate;
import info.magnolia.jcr.registry.DefaultSessionProvider;
import info.magnolia.jcr.registry.SessionProviderRegistry;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import info.magnolia.repository.RepositoryNotInitializedException;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.BooleanUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Loads the repositories as defined in repository.xml and in Module descriptors.
 *
 * @version $Id$
 */
@Singleton
public final class RepositoryLoader {
    private static final Logger log = LoggerFactory.getLogger(RepositoryLoader.class);

    /**
     * repository element string.
     */
    private static final String ELEMENT_REPOSITORY = "Repository";

    private static final String ELEMENT_REPOSITORYMAPPING = "RepositoryMapping";

    private static final String ELEMENT_PARAM = "param";

    private static final String ELEMENT_WORKSPACE = "workspace";

    /**
     * Attribute names.
     */
    private static final String ATTRIBUTE_NAME = "name";

    private static final String ATTRIBUTE_LOAD_ON_STARTUP = "loadOnStartup";

    private static final String ATTRIBUTE_PROVIDER = "provider";

    private static final String ATTRIBUTE_VALUE = "value";

    private static final String ATTRIBUTE_REPOSITORY_NAME = "repositoryName";

    private static final String ATTRIBUTE_WORKSPACE_NAME = "workspaceName";

    private final WorkspaceMapping workspaceMapping;
    /**
     * Utility class, don't instantiate.
     */
    @Inject
    RepositoryLoader(WorkspaceMapping workspaceMapping) {
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
     *</pre>
     */
    public void init() {
        log.info("Loading JCR");
        workspaceMapping.clearRepositories();
        try {
            loadRepositories();
            log.debug("JCR loaded");
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Shuts down all repositories (through Provider instances) and clears all mappings.
     */
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
     * @return <code>false</code> if all the repositories are empty, <code>true</code> if at least one of them has
     * content.
     * @throws AccessDeniedException repository authentication failed
     * @throws RepositoryException exception while accessing the repository
     */
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
    public boolean checkIfInitialized(String repository) throws RepositoryException, AccessDeniedException {
        log.debug("Checking [{}] repository.", repository);
        Session session = MgnlContext.getSystemContext().getJCRSession(repository);

        if (session == null) {
            throw new RuntimeException("Repository [" + repository + "] not loaded");
        }

        Node startPage = session.getRootNode();

        // return any kind of children
        Iterable<Node> children = NodeUtil.getNodes(startPage, new Predicate<Node>() {
            @Override
            public boolean evaluate(Node content) {
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
     * @see #init()
     */
    public void reload() {
        log.info("Reloading JCR");
        init();
    }

    /**
     * Load repository mappings and params using repositories.xml.
     * @throws Exception
     */
    private void loadRepositories() throws Exception {
        Document document = buildDocument();
        Element root = document.getRootElement();
        loadRepositoryNameMap(root);
        Collection repositoryElements = root.getChildren(ELEMENT_REPOSITORY);
        Iterator children = repositoryElements.iterator();
        while (children.hasNext()) {
            Element element = (Element) children.next();
            String name = element.getAttributeValue(ATTRIBUTE_NAME);
            String load = element.getAttributeValue(ATTRIBUTE_LOAD_ON_STARTUP);
            String provider = element.getAttributeValue(ATTRIBUTE_PROVIDER);
            RepositoryMapping map = new RepositoryMapping();
            map.setName(name);
            map.setProvider(provider);
            boolean loadOnStartup = BooleanUtils.toBoolean(load);
            map.setLoadOnStartup(loadOnStartup);
            /* load repository parameters */
            Iterator params = element.getChildren(ELEMENT_PARAM).iterator();
            Map parameters = new Hashtable();
            while (params.hasNext()) {
                Element param = (Element) params.next();
                String value = param.getAttributeValue(ATTRIBUTE_VALUE);
                parameters.put(param.getAttributeValue(ATTRIBUTE_NAME), value);
            }
            // TODO : it looks like params here are not interpolated
            map.setParameters(parameters);
            List workspaces = element.getChildren(ELEMENT_WORKSPACE);
            if (workspaces != null && !workspaces.isEmpty()) {
                Iterator wspIterator = workspaces.iterator();
                while (wspIterator.hasNext()) {
                    Element workspace = (Element) wspIterator.next();
                    String wspName = workspace.getAttributeValue(ATTRIBUTE_NAME);
                    log.info("Loading workspace {}", wspName);
                    map.addWorkspace(wspName);
                }
            }
            else {
                map.addWorkspace(WorkspaceMapping.DEFAULT_WORKSPACE_NAME);
            }
            workspaceMapping.addWorkspaceNameToRepositoryMapping(name, map);
            try {
                loadRepository(map);
            }
            catch (Exception e) {
                log.error("Failed to load JCR \"" + map.getName() + "\" " + e.getMessage(), e);
            }
        }
    }

    /**
     * load repository name mapping.
     * @param root element of repositories.xml
     */
    private void loadRepositoryNameMap(Element root) {
        Element repositoryMapping = root.getChild(ELEMENT_REPOSITORYMAPPING);
        Iterator children = repositoryMapping.getChildren().iterator();
        while (children.hasNext()) {
            Element nameMap = (Element) children.next();
            workspaceMapping.addMappedRepositoryName(
                    nameMap.getAttributeValue(ATTRIBUTE_NAME),
                    nameMap.getAttributeValue(ATTRIBUTE_REPOSITORY_NAME),
                    nameMap.getAttributeValue(ATTRIBUTE_WORKSPACE_NAME));
        }
    }

    /**
     * This method initializes the repository. You must not call this method twice.
     * @param map
     * @throws RepositoryNotInitializedException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public void loadRepository(RepositoryMapping map) throws RepositoryNotInitializedException,
    InstantiationException, IllegalAccessException, ClassNotFoundException {
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
    public void loadWorkspace(String repositoryId, String workspaceId) throws RepositoryException {
        log.info("Loading workspace {}", workspaceId);
        workspaceMapping.addWorkspaceNameToRepositoryNameIfNotYetAvailable(workspaceId, repositoryId, workspaceId);
        Provider provider = workspaceMapping.getRepositoryProvider(repositoryId);
        provider.registerWorkspace(workspaceId);
        Repository repo = workspaceMapping.getRepository(repositoryId);
        RepositoryMapping map = workspaceMapping.getRepositoryMapping(repositoryId);

        registerNameSpacesAndNodeTypes(repo,  workspaceId, map, provider);
    }

    /**
     * Load hierarchy manager for the specified repository and workspace.
     *
     * TODO dlipp - better naming!
     */
    private void registerNameSpacesAndNodeTypes(Repository repository, String wspID, RepositoryMapping map,
            Provider provider) {
        try {
            SimpleCredentials sc = new SimpleCredentials(ContentRepository.REPOSITORY_USER, ContentRepository.REPOSITORY_PSWD.toCharArray());
            // TODO dlipp - hack for now. Logical and physical workspaceName are identical here!
            Components.getComponent(SessionProviderRegistry.class).register(new DefaultSessionProvider(wspID, repository, wspID));
            try {
                Session session = Components.getComponent(SessionProviderRegistry.class).get(wspID).createSession(sc);
                try {
                    provider.registerNamespace(RepositoryConstants.NAMESPACE_PREFIX, RepositoryConstants.NAMESPACE_URI, session.getWorkspace());
                    provider.registerNodeTypes();
                }
                finally {
                    session.logout();
                }
            } catch (RegistrationException e) {
                throw new RepositoryException(e);
            }
        }
        catch (RepositoryException e) {
            log.error("Failed to initialize hierarchy manager for JCR " + map.getName(), e);
        }
    }

    /**
     * Builds JDOM document.
     * @return document
     * @throws IOException
     * @throws JDOMException
     */
    private Document buildDocument() throws JDOMException, IOException {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG);
        final String tokenizedConfig = ConfigUtil.getTokenizedConfigFile(path);
        return ConfigUtil.string2JDOM(tokenizedConfig);
    }
}
