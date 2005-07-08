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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.cms.module.ModuleFactory;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.UrlPattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Initialise all configured modules.
 */
public final class ModuleLoader {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ModuleLoader.class);

    /**
     * magnolia module specific keywords
     */
    public static final String CONFIG_PAGE = "modules"; //$NON-NLS-1$

    public static final String CONFIG_NODE_REGISTER = "Register"; //$NON-NLS-1$

    public static final String CONFIG_NODE_VIRTUAL_MAPPING = "VirtualURIMapping"; //$NON-NLS-1$

    public static final String CONFIG_NODE_LOCAL_STORE = "Config"; //$NON-NLS-1$

    /**
     * todo fix this with proper JCR implementation.
     */
    private static SimpleCredentials simpleCredentials;

    /**
     * Utility class, don't instantiate.
     */
    private ModuleLoader() {
        // unused
    }

    protected static void init() throws ConfigurationException {
        log.info("Loading modules"); //$NON-NLS-1$
        setSudoCredentials();
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            Content startPage = hm.getContent(CONFIG_PAGE);
            init(startPage);
            log.info("Finished loading modules"); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.fatal("Failed to initialize module loader"); //$NON-NLS-1$
            log.fatal(e.getMessage(), e);
            throw new ConfigurationException(e.getMessage());
        }
    }

    /**
     * @param startPage
     * @throws IOException
     */
    private static void init(Content startPage) {
        Iterator modules = startPage.getChildren().iterator();
        while (modules.hasNext()) {
            Content module = (Content) modules.next();

            String modulename = module.getName();

            log.info("Initializing module - " + modulename); //$NON-NLS-1$
            load(module);
            VirtualMap.update(CONFIG_PAGE + "/" + modulename + "/" + CONFIG_NODE_VIRTUAL_MAPPING); //$NON-NLS-1$ //$NON-NLS-2$
            log.info("Module : " + modulename + " initialized"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static void load(Content module) {
        try {
            Content moduleConfig = module.getContent(CONFIG_NODE_REGISTER);
            ModuleConfig thisModule = new ModuleConfig();
            thisModule.setModuleName(moduleConfig.getNodeData("moduleName").getString()); //$NON-NLS-1$
            thisModule.setModuleDescription(moduleConfig.getNodeData("moduleDescription").getString()); //$NON-NLS-1$
            thisModule.setHierarchyManager(getHierarchyManager(moduleConfig.getNodeData("repository").getString())); //$NON-NLS-1$
            try {
                Content sharedRepositories = moduleConfig.getContent("sharedRepositories"); //$NON-NLS-1$
                thisModule.setSharedHierarchyManagers(getSharedHierarchyManagers(sharedRepositories));
            }
            catch (PathNotFoundException e) {
                log.info("Module : no shared repository definition found for - " + module.getName()); //$NON-NLS-1$
            }
            thisModule.setInitParameters(getInitParameters(moduleConfig.getContent("initParams"))); //$NON-NLS-1$
            /* add local store */
            LocalStore store = LocalStore.getInstance(CONFIG_PAGE + "/" //$NON-NLS-1$
                + module.getName() + "/" //$NON-NLS-1$
                + CONFIG_NODE_LOCAL_STORE);
            thisModule.setLocalStore(store.getStore());
            try {
                // temporary workaround for compatibility with old repositories (the package "adminInterface" has been
                // renamed to "admininterface" according to java naming standards)
                String moduleClassName = moduleConfig.getNodeData("class").getString(); //$NON-NLS-1$
                if ("info.magnolia.module.adminInterface.Engine".equals(moduleClassName)) { //$NON-NLS-1$
                    moduleClassName = "info.magnolia.module.admininterface.Engine"; //$NON-NLS-1$
                }

                ModuleFactory.initModule(thisModule, moduleClassName);

            }
            catch (InstantiationException ie) {
                log.fatal("Module : [ " + moduleConfig.getNodeData("moduleName").getString() + " ] failed to load"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                log.fatal(ie.getMessage());
            }
            catch (IllegalAccessException ae) {
                log.fatal(ae.getMessage());
            }
        }
        catch (Exception e) {
            log.fatal("can't initialize module " + module.getHandle(), e); //$NON-NLS-1$
        }
    }

    public static void reload() throws ConfigurationException {
        init();
    }

    private static Map getInitParameters(Content paramList) {
        Map initParams = new Hashtable();
        Iterator initParameters = paramList.getNodeDataCollection().iterator();
        while (initParameters.hasNext()) {
            NodeData param = (NodeData) initParameters.next();
            initParams.put(param.getName(), param.getString());
        }
        return initParams;
    }

    private static HierarchyManager getHierarchyManager(String repositoryName) throws RepositoryException {
        if (StringUtils.isEmpty(repositoryName)) {
            return null;
        }
        Session moduleRepositoryTicket = ContentRepository.getRepository(repositoryName).login(simpleCredentials, null);
        List acl = new ArrayList();
        UrlPattern p = UrlPattern.MATCH_ALL;
        Permission permission = new PermissionImpl();
        permission.setPattern(p);
        permission.setPermissions(Permission.ALL);
        acl.add(permission);
        AccessManager accessManager = new AccessManagerImpl();
        accessManager.setPermissionList(acl);
        HierarchyManager hm = new HierarchyManager();
        hm.init(moduleRepositoryTicket.getRootNode(), accessManager);
        return hm;
    }

    private static Map getSharedHierarchyManagers(Content sharedRepositoriesNode) throws RepositoryException {
        Map sharedHierarchy = new Hashtable();
        Iterator repositories = sharedRepositoriesNode.getChildren().iterator();
        List acl = new ArrayList();
        UrlPattern p = UrlPattern.MATCH_ALL;
        while (repositories.hasNext()) {
            Content repositoryConfig = (Content) repositories.next();
            String id = repositoryConfig.getNodeData("id").getString(); //$NON-NLS-1$
            String repositoryName = repositoryConfig.getNodeData("repository").getString(); //$NON-NLS-1$
            Session ticket = ContentRepository.getRepository(repositoryName).login(simpleCredentials, null);
            Permission permission = new PermissionImpl();
            permission.setPattern(p);
            permission.setPermissions(repositoryConfig.getNodeData("permissions").getLong()); //$NON-NLS-1$
            acl.add(permission);
            AccessManager accessManager = new AccessManagerImpl();
            accessManager.setPermissionList(acl);
            HierarchyManager hm = new HierarchyManager();
            hm.init(ticket.getRootNode(), accessManager);
            sharedHierarchy.put(id, hm);
        }
        return sharedHierarchy;
    }

    private static void setSudoCredentials() {
        simpleCredentials = new SimpleCredentials(ContentRepository.SYSTEM_USER, ContentRepository.SYSTEM_PSWD); //$NON-NLS-1$
    }

}