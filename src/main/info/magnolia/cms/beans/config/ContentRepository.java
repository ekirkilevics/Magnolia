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

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.Path;
import info.magnolia.cms.util.regex.RegexWildcardPattern;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;


/**
 * @author Sameer Charles
 * @version 2.01
 */
public class ContentRepository {

    private static Logger log = Logger.getLogger(ContentRepository.class);

    /**
     * default repository ID's
     */
    public static final String WEBSITE = "website";

    public static final String USERS = "users";

    public static final String USER_ROLES = "userroles";

    public static final String GROUPS = "groups";

    public static final String CONFIG = "config";

    /**
     * repository element string
     */
    private static final String ELEMENT_REPOSITORY = "Repository";

    private static final String ELEMENT_PARAM = "param";

    /**
     * Attribute names
     */
    private static final String ATTRIBUTE_NAME = "name";

    private static final String ATTRIBUTE_ID = "id";

    private static final String ATTRIBUTE_LOAD_ON_STARTUP = "loadOnStartup";

    private static final String ATTRIBUTE_PROVIDER = "provider";

    private static final String ATTRIBUTE_VALUE = "value";

    /**
     * magnolia namespace
     */
    public static final String NAMESPACE_PREFIX = "mgnl";

    public static final String NAMESPACE_URI = "http://www.magnolia.info/jcr/mgnl";

    /**
     * magnolia system user
     */
    private static final String SYSTEM_USER = "magnolia";

    /**
     * All available repositories store
     */
    private static Hashtable repositories = new Hashtable();

    /**
     * JCR providers as mapped in repositories.xml
     */
    private static Hashtable repositoryProviders = new Hashtable();

    /**
     * all server hierarchy managers with full access to specific repositories
     */
    private static Hashtable hierarchyManagers = new Hashtable();

    /**
     * repositories configuration as defined in repositories mapping file via attribute <b>magnolia.repositories.config
     * </b>
     */
    private static Hashtable repositoryMappings = new Hashtable();

    /**
     * loads all configured repository using ID as Key, as configured in repositories.xml.
     * 
     * <pre>
     * &lt;Repository name="website"
     *                id="website"
     *                provider="info.magnolia.jackrabbit.ProviderImpl"
     *                loadOnStartup="true" >
     *   &lt;param name="configFile"
     *             value="../webapps/magnolia/WEB-INF/config/repositories/website.xml"/>
     *   &lt;param name="repositoryHome"
     *             value="../webapps/magnolia/repositories/website"/>
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
            log.error("System : Failed to load JCR");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Re-load all configured repositories/
     * </p>
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
            boolean loadOnStartup = (new Boolean(load)).booleanValue();
            map.setLoadOnStartup(loadOnStartup);
            /* load repository parameters */
            Iterator params = element.getChildren(ELEMENT_PARAM).iterator();
            Map parameters = new Hashtable();
            while (params.hasNext()) {
                Element param = (Element) params.next();
                parameters.put((String) param.getAttributeValue(ATTRIBUTE_NAME), (String) param
                    .getAttributeValue(ATTRIBUTE_VALUE));
            }
            map.setParameters(parameters);
            ContentRepository.repositoryMappings.put(id, map);
            loadRepository(map);
        }
    }

    private static void loadRepository(RepositoryMapping map) {
        try {
            log.info("System : loading JCR - " + map.getID());
            Provider handlerClass = (Provider) Class.forName(map.getProvider()).newInstance();
            handlerClass.init(map);
            Repository repository = handlerClass.getUnderlineRepository();
            ContentRepository.repositories.put(map.getID(), repository);
            ContentRepository.repositoryProviders.put(map.getID(), handlerClass);
            if (map.isLoadOnStartup())
                loadHierarchyManager(repository, map, handlerClass);
        }
        catch (Exception re) {
            log.error("System : Failed to load JCR - " + map.getID());
            log.error(re.getMessage(), re);
        }
    }

    private static void loadHierarchyManager(Repository repository, RepositoryMapping map, Provider provider) {
        try {
            SimpleCredentials sc = new SimpleCredentials(ContentRepository.SYSTEM_USER, "".toCharArray());
            Session session = repository.login(sc, null);
            provider.registerNamespace(NAMESPACE_PREFIX, NAMESPACE_URI, session.getWorkspace());
            ArrayList acl = getSystemPermissions();
            AccessManagerImpl accessManager = new AccessManagerImpl();
            accessManager.setPermissionList(acl);
            HierarchyManager hierarchyManager = new HierarchyManager(ContentRepository.SYSTEM_USER);
            hierarchyManager.init(session.getRootNode());
            hierarchyManager.setAccessManager(accessManager);
            ContentRepository.hierarchyManagers.put(map.getID(), hierarchyManager);
        }
        catch (RepositoryException re) {
            log.error("System : Failed to initialize hierarchy manager for JCR - " + map.getID());
            log.error(re.getMessage(), re);
        }
    }

    private static ArrayList getSystemPermissions() {
        ArrayList acl = new ArrayList();
        Pattern p = Pattern.compile(RegexWildcardPattern.getMultipleCharPattern());
        Permission permission = new PermissionImpl();
        permission.setPattern(p);
        permission.setPermissions(Permission.ALL);
        acl.add(permission);
        return acl;
    }

    /**
     * <p>
     * builds JDOM document
     * </p>
     */
    private static Document buildDocument() throws Exception {
        File source = new File(Path.getRepositoriesConfigFilePath());
        if (!source.exists())
            throw new Exception("Failed to locate magnolia repositories config file");
        SAXBuilder builder = new SAXBuilder();
        return builder.build(source);
    }

    /**
     * <p>
     * hierarchy manager as created on startup <br>
     * Note : this hierarchyManager is created with system rights and has full access on the specified repository
     * </p>
     */
    public static HierarchyManager getHierarchyManager(String repositoryID) {
        return (HierarchyManager) ContentRepository.hierarchyManagers.get(repositoryID);
    }

    /**
     * <p>
     * returns repository specified by the <b>repositoryID </b> as configured in repository config
     * </p>
     */
    public static Repository getRepository(String repositoryID) {
        return (Repository) ContentRepository.repositories.get(repositoryID);
    }

    /**
     * <p>
     * returns repository mapping as configured
     * </p>
     */
    public static RepositoryMapping getRepositoryMapping(String repositoryID) {
        return (RepositoryMapping) ContentRepository.repositoryMappings.get(repositoryID);
    }
}
