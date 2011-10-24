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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ConfigUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.registry.DefaultSessionProvider;
import info.magnolia.jcr.registry.SessionProviderRegistry;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import info.magnolia.repository.RepositoryNameMap;
import info.magnolia.repository.RepositoryNotInitializedException;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO needs serious refactoring and cleanup.
 *
 * @author Sameer Charles
 * $Id$
 *
 * @deprecated since 4.5 - has no direct replacement. Some of its functionality will be dropped, other will be located in {@link info.magnolia.jcr.registry.SessionProviderManager} instead.
 */
public final class ContentRepository {
    private static final Logger log = LoggerFactory.getLogger(ContentRepository.class);

    /**
     * default repository ID's.
     */
    public static final String WEBSITE = "website";

    public static final String USERS = "users";

    public static final String USER_ROLES = "userroles";

    public static final String USER_GROUPS = "usergroups";

    public static final String CONFIG = "config";

    public static final String DEFAULT_WORKSPACE = "default";

    public static final String VERSION_STORE = "mgnlVersion";

    /**
     * magnolia namespace.
     */
    public static final String NAMESPACE_PREFIX = "mgnl";

    public static final String NAMESPACE_URI = "http://www.magnolia.info/jcr/mgnl";

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

    /**
     * repository user.
     */
    public static String REPOSITORY_USER;

    /**
     * repository default password.
     */
    public static String REPOSITORY_PSWD;

    static {
        REPOSITORY_USER = SystemProperty.getProperty("magnolia.connection.jcr.userId");
        REPOSITORY_PSWD = SystemProperty.getProperty("magnolia.connection.jcr.password");
    }

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
    private static Map<String, RepositoryMapping> repositoryMapping = new Hashtable<String, RepositoryMapping>();

    /**
     * holds all repository names as configured in repositories.xml.
     */
    private static Map<String, RepositoryNameMap> repositoryNameMap = new Hashtable<String, RepositoryNameMap>();

    /**
     * Utility class, don't instantiate.
     */
    private ContentRepository() {
        // unused constructor
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
    public static void init() {
        log.info("Loading JCR");
        repositories.clear();
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
    public static void shutdown() {
        log.info("Shutting down JCR");
        final Iterator<Provider> providers = repositoryProviders.values().iterator();
        while (providers.hasNext()) {
            final Provider provider = providers.next();
            provider.shutdownRepository();
        }
        repositoryProviders.clear();
        repositoryMapping.clear();
        repositoryNameMap.clear();
        repositories.clear();
    }

    /**
     * Verify the initialization state of all the repositories. This methods returns <code>false</code> only if
     * <strong>all</strong> the repositories are empty (no node else than the root one).
     * @return <code>false</code> if all the repositories are empty, <code>true</code> if at least one of them has
     * content.
     * @throws AccessDeniedException repository authentication failed
     * @throws RepositoryException exception while accessing the repository
     */
    public static boolean checkIfInitialized() throws AccessDeniedException, RepositoryException {
        Iterator<String> repositoryNames = getAllRepositoryNames();
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
    public static boolean checkIfInitialized(String repository) throws RepositoryException, AccessDeniedException {
        log.debug("Checking [{}] repository.", repository);
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(repository);

        if (hm == null) {
            throw new RuntimeException("Repository [" + repository + "] not loaded");
        }

        Content startPage = hm.getRoot();

        // return any kind of children
        Collection<Content> children = startPage.getChildren(new Content.ContentFilter() {
            @Override
            public boolean accept(Content content) {
                return (!content.getName().startsWith(MgnlNodeType.JCR_PREFIX) && !content.getName().startsWith("rep:"));
            }
        });

        if (!children.isEmpty()) {
            log.debug("Content found in [{}].", repository);
            return true;
        }
        return false;
    }

    /**
     * Re-load all configured repositories.
     * @see #init()
     */
    public static void reload() {
        log.info("Reloading JCR");
        init();
    }

    /**
     * Load repository mappings and params using repositories.xml.
     * @throws Exception
     */
    private static void loadRepositories() throws Exception {
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
                map.addWorkspace(DEFAULT_WORKSPACE);
            }
            repositoryMapping.put(name, map);
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
    private static void loadRepositoryNameMap(Element root) {
        Element repositoryMapping = root.getChild(ELEMENT_REPOSITORYMAPPING);
        Iterator children = repositoryMapping.getChildren().iterator();
        while (children.hasNext()) {
            Element nameMap = (Element) children.next();
            addMappedRepositoryName(
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
    public static void loadRepository(RepositoryMapping map) throws RepositoryNotInitializedException,
    InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("Loading JCR {}", map.getName());
        Provider handlerClass = Classes.newInstance(map.getProvider());
        handlerClass.init(map);
        Repository repository = handlerClass.getUnderlyingRepository();
        repositories.put(map.getName(), repository);
        repositoryProviders.put(map.getName(), handlerClass);
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
    public static void loadWorkspace(String repositoryId, String workspaceId) throws RepositoryException {
        log.info("Loading workspace {}", workspaceId);
        if(!repositoryNameMap.containsKey(workspaceId)){
            addMappedRepositoryName(workspaceId, repositoryId, workspaceId);
        }
        RepositoryMapping map = getRepositoryMapping(repositoryId);
        if(!map.getWorkspaces().contains(workspaceId)){
            map.addWorkspace(workspaceId);
        }
        Provider provider = getRepositoryProvider(repositoryId);
        provider.registerWorkspace(workspaceId);
        registerNameSpacesAndNodeTypes(getRepository(repositoryId),  workspaceId, map, provider);
    }

    /**
     * Load hierarchy manager for the specified repository and workspace.
     */
    private static void registerNameSpacesAndNodeTypes(Repository repository, String wspID, RepositoryMapping map,
            Provider provider) {
        try {
            SimpleCredentials sc = new SimpleCredentials(REPOSITORY_USER, REPOSITORY_PSWD.toCharArray());
            // TODO dlipp - hack for now. Logical and physical workspaceName are identical here!
            Components.getComponent(SessionProviderRegistry.class).register(new DefaultSessionProvider(wspID, repository, wspID));
            try {
                Session session = Components.getComponent(SessionProviderRegistry.class).get(wspID).createSession(sc);
                try {
                    provider.registerNamespace(NAMESPACE_PREFIX, NAMESPACE_URI, session.getWorkspace());
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
    private static Document buildDocument() throws JDOMException, IOException {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG);
        final String tokenizedConfig = ConfigUtil.getTokenizedConfigFile(path);
        return ConfigUtil.string2JDOM(tokenizedConfig);
    }

    /**
     * Get mapped repository name.
     * @param name
     * @return mapped name as in repositories.xml RepositoryMapping element
     */
    private static String getMappedRepositoryName(String name) {
        RepositoryNameMap nameMap = repositoryNameMap.get(name);
        if(nameMap==null){
            return name;
        }
        return nameMap.getRepositoryName();
    }

    /**
     * Get mapped workspace name.
     * @param name
     * @return mapped name as in repositories.xml RepositoryMapping element
     */
    public static String getMappedWorkspaceName(String name) {
        RepositoryNameMap nameMap = repositoryNameMap.get(name);
        if (nameMap == null) {
            return name;
        }
        return nameMap.getWorkspaceName();
    }

    /**
     * Add a mapped repository name.
     * @param name
     * @param repositoryName
     * @param workspaceName
     */
    private static void addMappedRepositoryName(String name, String repositoryName, String workspaceName) {
        if (StringUtils.isEmpty(workspaceName)) {
            workspaceName = name;
        }
        RepositoryNameMap nameMap = new RepositoryNameMap(repositoryName, workspaceName);
        repositoryNameMap.put(name, nameMap);
    }

    /**
     * Get default workspace name.
     * @return default name if there are no workspaces defined or there is no workspace present with name "default",
     * otherwise return same name as repository name.
     */
    public static String getDefaultWorkspace(String repositoryId) {
        RepositoryMapping mapping = getRepositoryMapping(repositoryId);
        if (mapping == null) {
            return DEFAULT_WORKSPACE;
        }
        Collection<String> workspaces = mapping.getWorkspaces();
        if (workspaces.contains(getMappedWorkspaceName(repositoryId))) {
            return repositoryId;
        }
        return DEFAULT_WORKSPACE;
    }

    /**
     * Returns repository specified by the <code>repositoryID</code> as configured in repository config.
     */
    public static Repository getRepository(String repositoryID) {
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
    public static Provider getRepositoryProvider(String repositoryID) {
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
    public static RepositoryMapping getRepositoryMapping(String repositoryID) {
        String name = getMappedRepositoryName(repositoryID);
        if (name != null && repositoryMapping.containsKey(name)) {
            return repositoryMapping.get(getMappedRepositoryName(repositoryID));
        }
        log.debug("No mapping for the repository {}", repositoryID);
        return null;
    }

    /**
     * Gets repository names array as configured in repositories.xml.
     * @return repository names
     */
    public static Iterator<String> getAllRepositoryNames() {
        return repositoryNameMap.keySet().iterator();
    }

    /**
     * get internal workspace name.
     * @param workspaceName
     * @return workspace name as configured in magnolia repositories.xml
     * */
    public static String getInternalWorkspaceName(String workspaceName) {
        Iterator<String> keys = repositoryNameMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            RepositoryNameMap nameMap = repositoryNameMap.get(key);
            if (nameMap.getWorkspaceName().equalsIgnoreCase(workspaceName)) {
                return key;
            }
        }
        log.error("No Repository/Workspace name mapping defined for "+workspaceName);
        return workspaceName;
    }

}
