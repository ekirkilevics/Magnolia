/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.SearchFactory;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import info.magnolia.repository.RepositoryNotInitializedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles $Id :$
 */
public final class ContentRepository {

    /**
     * default repository ID's.
     */
    public static final String WEBSITE = "website"; //$NON-NLS-1$

    public static final String USERS = "users"; //$NON-NLS-1$

    public static final String USER_ROLES = "userroles"; //$NON-NLS-1$

    public static final String GROUPS = "groups"; //$NON-NLS-1$

    public static final String CONFIG = "config"; //$NON-NLS-1$

    public static final String DEFAULT_WORKSPACE = "default"; //$NON-NLS-1$

    /**
     * magnolia namespace.
     */
    public static final String NAMESPACE_PREFIX = "mgnl"; //$NON-NLS-1$

    public static final String NAMESPACE_URI = "http://www.magnolia.info/jcr/mgnl"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentRepository.class);

    /**
     * repository element string.
     */
    private static final String ELEMENT_REPOSITORY = "Repository"; //$NON-NLS-1$

    private static final String ELEMENT_REPOSITORYMAPPING = "RepositoryMapping"; //$NON-NLS-1$

    private static final String ELEMENT_PARAM = "param"; //$NON-NLS-1$

    private static final String ELEMENT_WORKSPACE = "workspace"; //$NON-NLS-1$

    /**
     * Attribute names.
     */
    private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

    private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    private static final String ATTRIBUTE_LOAD_ON_STARTUP = "loadOnStartup"; //$NON-NLS-1$

    private static final String ATTRIBUTE_PROVIDER = "provider"; //$NON-NLS-1$

    private static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

    private static final String ATTRIBUTE_REPOSITORY_NAME = "repositoryName"; //$NON-NLS-1$

    /**
     * Magnolia system user.
     */
    public static final String SYSTEM_USER = "admin"; //$NON-NLS-1$

    /**
     * Magnolia system password
     */
    public static final char[] SYSTEM_PSWD = "admin".toCharArray(); //$NON-NLS-1$

    /**
     * All available repositories store.
     */
    private static Map repositories = new Hashtable();

    /**
     * JCR providers as mapped in repositories.xml.
     */
    private static Map repositoryProviders = new Hashtable();

    /**
     * All server hierarchy managers with full access to specific repositories.
     */
    private static Map hierarchyManagers = new Hashtable();

    /**
     * Repositories configuration as defined in repositories mapping file via attribute
     * <code>magnolia.repositories.config</code>.
     */
    private static Map repositoryMapping = new Hashtable();

    /**
     * holds all repository names as configured in repositories.xml
     */
    private static Map repositoryNameMap;

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
        log.info("System : loading JCR"); //$NON-NLS-1$
        repositories.clear();
        hierarchyManagers.clear();
        try {
            loadRepositories();
            log.info("System : JCR loaded"); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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
        Iterator repositoryNames = getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repository = (String) repositoryNames.next();
            if (log.isDebugEnabled()) {
                log.debug("Checking [" + repository + "] repository."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            HierarchyManager hm = getHierarchyManager(repository);

            if (hm == null) {
                throw new RuntimeException("Repository [" + repository + "] not loaded"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            Content startPage = hm.getRoot();
            if (startPage.getChildren(ItemType.CONTENT).size() > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Content found in [" + repository + "]."); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Re-load all configured repositories.
     * @see #init()
     */
    public static void reload() {
        log.info("System : reloading JCR"); //$NON-NLS-1$
        ContentRepository.init();
    }

    /**
     * Load repository mappings and params using repositories.xml
     * @throws Exception
     */
    private static void loadRepositories() throws Exception {
        Document document = buildDocument();
        Element root = document.getRootElement();
        loadRepositoryNameMap(root);
        Collection repositoryElements = root.getChildren(ContentRepository.ELEMENT_REPOSITORY);
        Iterator children = repositoryElements.iterator();
        int repositoryIndex = 0;
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
                parameters.put(param.getAttributeValue(ATTRIBUTE_NAME), param.getAttributeValue(ATTRIBUTE_VALUE));
            }
            map.setParameters(parameters);
            List workspaces = element.getChildren(ELEMENT_WORKSPACE);
            if (workspaces != null && !workspaces.isEmpty()) {
                Iterator wspIterator = workspaces.iterator();
                while (wspIterator.hasNext()) {
                    Element workspace = (Element) wspIterator.next();
                    String wspName = workspace.getAttributeValue(ATTRIBUTE_NAME);
                    map.addWorkspace(wspName);
                }
            }
            else {
                map.addWorkspace(DEFAULT_WORKSPACE);
            }
            ContentRepository.repositoryMapping.put(name, map);
            try {
                loadRepository(map);
            }
            catch (Exception e) {
                log.error("System : Failed to load JCR \"" + map.getName() + "\" " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            repositoryIndex++;
        }
    }

    /**
     * load repository name mapping
     * @param root element of repositories.xml
     */
    private static void loadRepositoryNameMap(Element root) {
        Element repositoryMapping = root.getChild(ContentRepository.ELEMENT_REPOSITORYMAPPING);
        Iterator children = repositoryMapping.getChildren().iterator();
        ContentRepository.repositoryNameMap = new Hashtable();
        while (children.hasNext()) {
            Element nameMap = (Element) children.next();
            ContentRepository.repositoryNameMap.put(nameMap.getAttributeValue(ATTRIBUTE_NAME), nameMap
                .getAttributeValue(ATTRIBUTE_REPOSITORY_NAME));
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
        log.info("System : loading JCR - " + map.getName()); //$NON-NLS-1$
        Provider handlerClass = (Provider) Class.forName(map.getProvider()).newInstance();
        handlerClass.init(map);
        Repository repository = handlerClass.getUnderlineRepository();
        ContentRepository.repositories.put(map.getName(), repository);
        ContentRepository.repositoryProviders.put(map.getName(), handlerClass);
        if (map.isLoadOnStartup()) {
            /* load hierarchy managers for each workspace */
            Iterator workspaces = map.getWorkspaces().iterator();
            while (workspaces.hasNext()) {
                String wspID = (String) workspaces.next();
                loadHierarchyManager(repository, wspID, map, handlerClass);
            }
        }
    }

    /**
     * Load hierarchy manager for the specified repository and workspace
     * @param repository
     * @param wspID
     * @param map
     * @param provider
     */
    private static void loadHierarchyManager(Repository repository, String wspID, RepositoryMapping map,
        Provider provider) {
        try {
            SimpleCredentials sc = new SimpleCredentials(ContentRepository.SYSTEM_USER, ContentRepository.SYSTEM_PSWD);
            Session session = repository.login(sc, wspID);
            provider.registerNamespace(NAMESPACE_PREFIX, NAMESPACE_URI, session.getWorkspace());
            provider.registerNodeTypes(session.getWorkspace());
            AccessManagerImpl accessManager = getAccessManager();
            HierarchyManager hierarchyManager = new HierarchyManager(ContentRepository.SYSTEM_USER);
            hierarchyManager.init(session.getRootNode());
            hierarchyManager.setAccessManager(accessManager);
            ContentRepository.hierarchyManagers.put(map.getName() + "_" + wspID, hierarchyManager); //$NON-NLS-1$

            try {
                QueryManager queryManager = SearchFactory.getAccessControllableQueryManager(hierarchyManager
                    .getWorkspace()
                    .getQueryManager(), accessManager);
                hierarchyManager.setQueryManager(queryManager);
            }
            catch (RepositoryException e) {
                // probably no search manager is configured for this workspace
                log.info("QueryManager not initialized for repository " + map.getName() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (RepositoryException re) {
            log.error("System : Failed to initialize hierarchy manager for JCR - " + map.getName()); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }
    }

    /**
     * Configures and returns a AccessManager with system permissions
     * @return The system AccessManager
     */
    public static AccessManagerImpl getAccessManager() {
        List acl = getSystemPermissions();
        AccessManagerImpl accessManager = new AccessManagerImpl();
        accessManager.setPermissionList(acl);
        return accessManager;
    }

    /**
     * Get maximum permission available
     * @return List of permissions
     */
    private static List getSystemPermissions() {
        List acl = new ArrayList();
        UrlPattern p = UrlPattern.MATCH_ALL;
        Permission permission = new PermissionImpl();
        permission.setPattern(p);
        permission.setPermissions(Permission.ALL);
        acl.add(permission);
        return acl;
    }

    /**
     * Builds JDOM document.
     * @return document
     * @throws IOException
     * @throws JDOMException
     */
    private static Document buildDocument() throws JDOMException, IOException {
        File source = Path.getRepositoriesConfigFile();
        if (!source.exists()) {
            throw new FileNotFoundException("Failed to locate magnolia repositories config file at " //$NON-NLS-1$
                + source.getAbsolutePath());
        }
        SAXBuilder builder = new SAXBuilder();
        return builder.build(source);
    }

    /**
     * Get mapped repository name
     * @param name
     * @return mapped name as in repositories.xml RepositoryMapping element
     */
    private static String getMappedRepositoryName(String name) {
        return (String) ContentRepository.repositoryNameMap.get(name);
    }

    /**
     * Get default workspace name
     * @return default name if there are no workspaces defined or there is no workspace present with name "default",
     * otherwise return same name as repository name.
     */
    public static String getDefaultWorkspace(String repositoryId) {
        RepositoryMapping mapping = getRepositoryMapping(repositoryId);
        if (mapping == null) {
            return DEFAULT_WORKSPACE;
        }
        Collection workspaces = mapping.getWorkspaces();
        if (workspaces.contains(repositoryId)) {
            return repositoryId;
        } 
        return DEFAULT_WORKSPACE;
    }

    /**
     * Hierarchy manager as created on startup. Note: this hierarchyManager is created with system rights and has full
     * access on the specified repository.
     */
    public static HierarchyManager getHierarchyManager(String repositoryID) {
        return getHierarchyManager(repositoryID, getDefaultWorkspace(repositoryID));
    }

    /**
     * Hierarchy manager as created on startup. Note: this hierarchyManager is created with system rights and has full
     * access on the specified repository.
     */
    public static HierarchyManager getHierarchyManager(String repositoryID, String workspaceID) {
        return (HierarchyManager) ContentRepository.hierarchyManagers.get(getMappedRepositoryName(repositoryID)
            + "_" + workspaceID); //$NON-NLS-1$
    }

    /**
     * Returns repository specified by the <code>repositoryID</code> as configured in repository config.
     */
    public static Repository getRepository(String repositoryID) {
        return (Repository) ContentRepository.repositories.get(getMappedRepositoryName(repositoryID));
    }

    /**
     * returns repository mapping as configured.
     */
    public static RepositoryMapping getRepositoryMapping(String repositoryID) {
        return (RepositoryMapping) ContentRepository.repositoryMapping.get(getMappedRepositoryName(repositoryID));
    }

    /**
     * Gets repository names array as configured in repositories.xml
     * @return repository names
     */
    public static Iterator getAllRepositoryNames() {
        return ContentRepository.repositoryNameMap.keySet().iterator();
    }

}
