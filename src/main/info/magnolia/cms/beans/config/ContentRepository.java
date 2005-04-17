/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.regex.RegexWildcardPattern;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import info.magnolia.repository.RepositoryNotInitializedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


/**
 * @author Sameer Charles
 * @version 2.01
 */
public final class ContentRepository {

    /**
     * default repository ID's.
     */
    public static final String WEBSITE = "website";

    public static final String USERS = "users";

    public static final String USER_ROLES = "userroles";

    public static final String GROUPS = "groups";

    public static final String CONFIG = "config";

    public static final String[] ALL_REPOSITORIES = new String[]{WEBSITE, USERS, USER_ROLES, CONFIG};

    public static final String DEFAULT_WORKSPACE = "default";

    /**
     * magnolia namespace.
     */
    public static final String NAMESPACE_PREFIX = "mgnl";

    public static final String NAMESPACE_URI = "http://www.magnolia.info/jcr/mgnl";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ContentRepository.class);

    /**
     * repository element string.
     */
    private static final String ELEMENT_REPOSITORY = "Repository";

    private static final String ELEMENT_PARAM = "param";

    private static final String ELEMENT_WORKSPACE = "workspace";

    /**
     * Attribute names.
     */
    private static final String ATTRIBUTE_NAME = "name";

    private static final String ATTRIBUTE_ID = "id";

    private static final String ATTRIBUTE_LOAD_ON_STARTUP = "loadOnStartup";

    private static final String ATTRIBUTE_PROVIDER = "provider";

    private static final String ATTRIBUTE_VALUE = "value";

    /**
     * Magnolia system user.
     */
    private static final String SYSTEM_USER = "magnolia";

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
    private static Map repositoryMappings = new Hashtable();

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
        log.info("System : loading JCR");
        repositories.clear();
        hierarchyManagers.clear();
        try {
            loadRepositories();
            log.info("System : JCR loaded");
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
        for (int j = 0; j < ALL_REPOSITORIES.length; j++) {
            String repository = ALL_REPOSITORIES[j];
            if (log.isDebugEnabled()) {
                log.debug("Checking [" + repository + "] repository.");
            }
            Content startPage = getHierarchyManager(repository).getRoot();
            if (startPage.getChildren(ItemType.NT_CONTENT).size() > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Content found in [" + repository + "].");
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
        log.info("System : reloading JCR");
        ContentRepository.init();
    }

    private static void loadRepositories() throws Exception {
        Document document = buildDocument();
        Element root = document.getRootElement();
        Iterator children = root.getChildren(ContentRepository.ELEMENT_REPOSITORY).iterator();
        while (children.hasNext()) {
            Element element = (Element) children.next();
            String name = element.getAttributeValue(ATTRIBUTE_NAME);
            String id = element.getAttributeValue(ATTRIBUTE_ID);
            String load = element.getAttributeValue(ATTRIBUTE_LOAD_ON_STARTUP);
            String provider = element.getAttributeValue(ATTRIBUTE_PROVIDER);
            RepositoryMapping map = new RepositoryMapping();
            map.setID(id);
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
                    String wspID = workspace.getAttributeValue(ATTRIBUTE_ID);
                    map.addWorkspace(wspID);
                }
            } else {
                map.addWorkspace(DEFAULT_WORKSPACE);
            }
            ContentRepository.repositoryMappings.put(id, map);
            try {
                loadRepository(map);
            }
            catch (Exception e) {
                log.error("System : Failed to load JCR \"" + map.getID() + "\" " + e.getMessage(), e);
            }
        }
    }

    private static void loadRepository(RepositoryMapping map) throws RepositoryNotInitializedException,
        InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("System : loading JCR - " + map.getID());
        Provider handlerClass = (Provider) Class.forName(map.getProvider()).newInstance();
        handlerClass.init(map);
        Repository repository = handlerClass.getUnderlineRepository();
        ContentRepository.repositories.put(map.getID(), repository);
        ContentRepository.repositoryProviders.put(map.getID(), handlerClass);
        if (map.isLoadOnStartup()) {
            /* load hierarchy managers for each workspace */
            Iterator workspaces = map.getWorkspaces().iterator();
            while (workspaces.hasNext()) {
                String wspID = (String) workspaces.next();
                loadHierarchyManager(repository, wspID, map, handlerClass);
            }
        }

    }

    private static void loadHierarchyManager(Repository repository, String wspID, RepositoryMapping map, Provider provider) {
        try {
            SimpleCredentials sc = new SimpleCredentials(ContentRepository.SYSTEM_USER, StringUtils.EMPTY.toCharArray());
            Session session = repository.login(sc, wspID);
            provider.registerNamespace(NAMESPACE_PREFIX, NAMESPACE_URI, session.getWorkspace());
            List acl = getSystemPermissions();
            AccessManagerImpl accessManager = new AccessManagerImpl();
            accessManager.setPermissionList(acl);
            HierarchyManager hierarchyManager = new HierarchyManager(ContentRepository.SYSTEM_USER);
            hierarchyManager.init(session.getRootNode());
            hierarchyManager.setAccessManager(accessManager);
            ContentRepository.hierarchyManagers.put(map.getID()+"_"+wspID, hierarchyManager);
        }
        catch (RepositoryException re) {
            log.error("System : Failed to initialize hierarchy manager for JCR - " + map.getID());
            log.error(re.getMessage(), re);
        }
    }

    private static List getSystemPermissions() {
        List acl = new ArrayList();
        Pattern p = Pattern.compile(RegexWildcardPattern.getMultipleCharPattern());
        Permission permission = new PermissionImpl();
        permission.setPattern(p);
        permission.setPermissions(Permission.ALL);
        acl.add(permission);
        return acl;
    }

    /**
     * Builds JDOM document.
     * @throws IOException
     * @throws JDOMException
     */
    private static Document buildDocument() throws JDOMException, IOException {
        File source = Path.getRepositoriesConfigFile();
        if (!source.exists()) {
            throw new FileNotFoundException("Failed to locate magnolia repositories config file at "
                + source.getAbsolutePath());
        }
        SAXBuilder builder = new SAXBuilder();
        return builder.build(source);
    }

    /**
     * Hierarchy manager as created on startup. Note: this hierarchyManager is created with system rights and has full
     * access on the specified repository.
     */
    public static HierarchyManager getHierarchyManager(String repositoryID) {
        return getHierarchyManager(repositoryID,DEFAULT_WORKSPACE);
    }

    /**
     * Hierarchy manager as created on startup. Note: this hierarchyManager is created with system rights and has full
     * access on the specified repository.
     */
    public static HierarchyManager getHierarchyManager(String repositoryID, String workspaceID) {
        return (HierarchyManager) ContentRepository.hierarchyManagers.get(repositoryID+"_"+workspaceID);
    }

    /**
     * Returns repository specified by the <code>repositoryID</code> as configured in repository config.
     */
    public static Repository getRepository(String repositoryID) {
        return (Repository) ContentRepository.repositories.get(repositoryID);
    }

    /**
     * returns repository mapping as configured.
     */
    public static RepositoryMapping getRepositoryMapping(String repositoryID) {
        return (RepositoryMapping) ContentRepository.repositoryMappings.get(repositoryID);
    }
}
