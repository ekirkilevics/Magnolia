/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */




package info.magnolia.cms.beans.config;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.apache.log4j.Logger;
import org.apache.slide.jcr.core.RepositoryFactory;
import org.apache.slide.jcr.core.AccessManagerImpl;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.io.File;

import info.magnolia.cms.util.Path;
import info.magnolia.cms.util.regex.RegexWildcardPattern;
import info.magnolia.cms.beans.runtime.Permission;
import info.magnolia.cms.core.HierarchyManager;

import javax.jcr.Repository;
import javax.jcr.SimpleCredentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Date: Jul 8, 2004
 * Time: 2:09:45 PM
 *
 * @author Sameer Charles
 * @version 2.0
 */



public class ContentRepository {


    private static Logger log = Logger.getLogger(ContentRepository.class);


    /**
     * default repository ID's
     *
     * */
    public static final String WEBSITE = "website";
    public static final String USERS = "users";
    public static final String USER_ROLES = "userroles";
    public static final String CONFIG = "config";


    /**
     * repository element string
     * */
    private static final String ELEMENT_REPOSITORY = "Repository";


    /**
     * Attribute names
     * */
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_LOAD_ON_STARTUP = "loadOnStartup";


    /**
     * magnolia system user
     * */
    private static final String SYSTEM_USER = "magnolia";


    /**
     * All available repositories store
     * */
    private static Hashtable repositories = new Hashtable();


    /**
     * all server hierarchy managers with full access to specific repositories
     * */
    private static Hashtable hierarchyManagers = new Hashtable();


    /**
     * repositories configuration as defined in repositories mapping file
     * via attribute <b>magnolia.repositories.config</b>
     * */
    private static Hashtable repositoryMappings = new Hashtable();


    /**
     * using org.apache.slide.jcr.core.RepositoryFactory for the time being
     * todo use magnolia implementation independent dynamic repository factory
     * */
    private static RepositoryFactory repositoryFactory;



    /**
     * <p>
     * loads all configured repository using ID as Key<br>
     * &nbsp; - <i><Repository name="virtualName" id="<b>uniqueID</b>" loadOnStartup="true" /></i>
     *
     * </p>
     *
     * */
    public static void init() {
        log.info("System : loading JCR");
        repositories.clear();
        hierarchyManagers.clear();
        try {
            repositoryFactory = RepositoryFactory.create();
            loadRepositories();
            log.info("System : JCR loaded");
        } catch (Exception e) {
            log.error("System : Failed to load JCR");
            log.error(e.getMessage(), e);
        }
    }



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
            String name = element.getAttributeValue(ContentRepository.ATTRIBUTE_NAME);
            String id = element.getAttributeValue(ContentRepository.ATTRIBUTE_ID);
            String load = element.getAttributeValue(ContentRepository.ATTRIBUTE_LOAD_ON_STARTUP);
            RepositoryMapping map = new RepositoryMapping();
            map.setID(id);
            map.setName(name);
            boolean loadOnStartup = (new Boolean(load)).booleanValue();
            map.setLoadOnStartup(loadOnStartup);
            ContentRepository.repositoryMappings.put(id,map);
            loadRepository(map);
        }
    }



    private static void loadRepository(RepositoryMapping map) {
        try {
            log.info("System : loading JCR - "+map.getID());
            Repository repository = ContentRepository.repositoryFactory.getRepository(map.getName());
            ContentRepository.repositories.put(map.getID(),repository);
            if (map.isLoadOnStartup())
                loadHierarchyManager(repository, map);
        } catch (RepositoryException re) {
            log.error("System : Failed to load JCR - "+map.getID());
            log.error(re.getMessage(), re);
        }
    }


    private static void loadHierarchyManager(Repository repository, RepositoryMapping map) {
        try {
            SimpleCredentials sc = new SimpleCredentials(ContentRepository.SYSTEM_USER,"".toCharArray());
            Session ticket = repository.login(sc, null);
            ArrayList acl = getSystemPermissions();
            ((AccessManagerImpl)ticket.getWorkspace().getAccessManager()).setUserPermissions(acl);
            HierarchyManager hierarchyManager = new HierarchyManager(ContentRepository.SYSTEM_USER);
            hierarchyManager.init(ticket.getRootNode());
            ContentRepository.hierarchyManagers.put(map.getID(), hierarchyManager);
        } catch (RepositoryException re) {
            log.error("System : Failed to initialize hierarchy manager for JCR - "+map.getID());
            log.error(re.getMessage(), re);
        }
    }



    private static ArrayList getSystemPermissions() {
        ArrayList acl = new ArrayList();
        Pattern p = Pattern.compile(RegexWildcardPattern.getMultipleCharPattern());
        Permission permission = new Permission();
        permission.setPattern(p);
        permission.setPermissions(Permission.ALL_PERMISSIONS);
        acl.add(permission);
        return acl;
    }



    /**
     * <p>
     * builds JDOM document
     * </p>
     * */
    private static Document buildDocument() throws Exception {
        File source = new File(Path.getRepositoriesConfigFilePath());
        if (!source.exists())
            throw new Exception("Failed to locate magnolia repositories config file");
        SAXBuilder builder = new SAXBuilder();
        return builder.build(source);
    }



    /**
     * <p>
     * hierarchy manager as created on startup<br>
     * Note : this hierarchyManager is created with system ticket and has full access on
     * the specified repository
     *
     * </p>
     *
     * */
    public static HierarchyManager getHierarchyManager(String repositoryID) {
        return (HierarchyManager) ContentRepository.hierarchyManagers.get(repositoryID);
    }



    /**
     * <p>
     * returns repository specified by the <b>repositoryID</b> as configured in repository config
     * </p>
     *
     * */
    public static Repository getRepository(String repositoryID) {
        return (Repository) ContentRepository.repositories.get(repositoryID);
    }



    /**
     * <p>
     * returns repository mapping as configured
     * </p>
     *
     * */
    public static RepositoryMapping getRepositoryMapping(String repositoryID) {
        return (RepositoryMapping) ContentRepository.repositoryMappings.get(repositoryID);
    }



}


