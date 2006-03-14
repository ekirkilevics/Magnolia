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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.cms.module.ModuleDefinition;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.UrlPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.OrderedMapIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Initialise all configured modules.
 */
public final class ModuleLoader {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ModuleLoader.class);

    /**
     * The instance of the loader
     */
    private static ModuleLoader instance = (ModuleLoader) FactoryUtil.getSingleton(ModuleLoader.class);

    /**
     * magnolia module specific keywords
     */
    public static final String MODULES_NODE = "modules"; //$NON-NLS-1$

    public static final String CONFIG_NODE_REGISTER = "Register"; //$NON-NLS-1$

    public static final String CONFIG_NODE_VIRTUAL_MAPPING = "VirtualURIMapping"; //$NON-NLS-1$

    public static final String CONFIG_NODE_LOCAL_STORE = "Config"; //$NON-NLS-1$

    /**
     * The module instances
     */
    private Map modules = new HashMap();

    /**
     * todo fix this with proper JCR implementation.
     */
    private SimpleCredentials simpleCredentials;

    /**
     * Don't instantiate.
     */
    public ModuleLoader() {
    }

    /**
     * @return Returns the instance.
     */
    public static ModuleLoader getInstance() {
        return instance;
    }

    /**
     * Init the modules.
     * @throws ConfigurationException
     */
    protected void init() throws ConfigurationException {
        // do not initialize if a system restart is needed (could corrupt the system)
        if (ModuleRegistration.getInstance().isRestartNeeded()) {
            log.info("one or more module triggered a system restart, will not initialize the modules");
            return;
        }

        log.info("Loading modules"); //$NON-NLS-1$
        setSudoCredentials();
        try {
            Content modulesNode = getModulesNode();
            init(modulesNode);
            log.info("Finished loading modules"); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("Failed to initialize module loader"); //$NON-NLS-1$
            log.error(e.getMessage(), e);
            throw new ConfigurationException(e.getMessage());
        }
    }

    /**
     * Init the modules
     * @param modulesNode node with the module nodes
     */
    private void init(Content modulesNode) {
        // loop over the definitions (following the dependencies)
        OrderedMap defs = ModuleRegistration.getInstance().getModuleDefinitions();
        for (OrderedMapIterator iter = defs.orderedMapIterator(); !ModuleRegistration.getInstance().isRestartNeeded()
            && iter.hasNext();) {
            iter.next();
            ModuleDefinition def = (ModuleDefinition) iter.getValue();
            try {
                if (modulesNode.hasContent(def.getName())) {
                    Content moduleNode = modulesNode.getContent(def.getName());
                    log.info("initializing module - " + def.getName()); //$NON-NLS-1$
                    load(def, moduleNode);
                    log.info("module : " + def.getName() + " initialized"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else {
                    log.error("can't initialize module ["
                        + def.getName()
                        + "]: node module node in the config repository found");
                }

            }
            catch (RepositoryException e) {
                log.error("can't initialize module [" + def.getName() + "]", e);
            }
        }

        if (ModuleRegistration.getInstance().isRestartNeeded()) {
            log.warn("stopped module initialization since a restart is needed");
        }
    }

    private void load(ModuleDefinition def, Content moduleNode) {
        try {

            Content moduleConfigNode = moduleNode.getContent(CONFIG_NODE_REGISTER);
            ModuleConfig moduleConfig = new ModuleConfig();

            // set the registration definition
            moduleConfig.setModuleDefinition(def);

            moduleConfig
                .setHierarchyManager(getHierarchyManager(moduleConfigNode.getNodeData("repository").getString())); //$NON-NLS-1$
            try {
                Content sharedRepositories = moduleConfigNode.getContent("sharedRepositories"); //$NON-NLS-1$
                moduleConfig.setSharedHierarchyManagers(getSharedHierarchyManagers(sharedRepositories));
            }
            catch (PathNotFoundException e) {
                log.info("Module : no shared repository definition found for - " + moduleNode.getName()); //$NON-NLS-1$
            }

            try {
                Content initParamsNode = moduleConfigNode.getContent("initParams"); //$NON-NLS-1$
                moduleConfig.setInitParameters(getInitParameters(initParamsNode)); //$NON-NLS-1$
            }
            catch (PathNotFoundException e) {
                // no init parameters, that's ok
                moduleConfig.setInitParameters(new HashMap(0));
            }

            /* add local store */
            LocalStore store = LocalStore.getInstance(MODULES_NODE + "/" //$NON-NLS-1$
                + moduleNode.getName() + "/" //$NON-NLS-1$
                + CONFIG_NODE_LOCAL_STORE);
            moduleConfig.setLocalStore(store.getStore());

            Module module = this.getModuleInstance(moduleConfig.getName());

            // instantiate if not yet done (due registraion)
            if (module == null) {
                try {
                    String moduleClassName = moduleConfigNode.getNodeData("class").getString(); //$NON-NLS-1$

                    module = (Module) Class.forName(moduleClassName).newInstance();
                    this.addModuleInstance(moduleConfig.getName(), module);
                }
                catch (InstantiationException ie) {
                    log
                        .error("Module : [ " + moduleConfigNode.getNodeData("moduleName").getString() + " ] failed to load"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    log.error(ie.getMessage());
                }
                catch (IllegalAccessException ae) {
                    log.error(ae.getMessage());
                }
            }

            // init the module
            if (!module.isInitialized()) {
                if (!module.isRestartNeeded()) {
                    log.info("start initialization of module [{}]", def.getName());
                    module.init(moduleConfig);
                }
                else {
                    log.warn("won't initialize the module [" + module.getName() + "] since a system restart is needed");
                }

                if (module.isRestartNeeded()) {
                    ModuleRegistration.getInstance().setRestartNeeded(true);
                }
            }
        }
        catch (Exception e) {
            log.error("can't initialize module " + moduleNode.getHandle(), e); //$NON-NLS-1$
        }
    }

    public void reload() throws ConfigurationException {
        init();
    }

    private Map getInitParameters(Content paramList) {
        Map initParams = new Hashtable();
        Iterator initParameters = paramList.getNodeDataCollection().iterator();
        while (initParameters.hasNext()) {
            NodeData param = (NodeData) initParameters.next();
            initParams.put(param.getName(), param.getString());
        }
        return initParams;
    }

    private HierarchyManager getHierarchyManager(String repositoryName) throws RepositoryException {
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

    private Map getSharedHierarchyManagers(Content sharedRepositoriesNode) throws RepositoryException {
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

    private void setSudoCredentials() {
        simpleCredentials = new SimpleCredentials(ContentRepository.SYSTEM_USER, ContentRepository.SYSTEM_PSWD); //$NON-NLS-1$
    }

    /**
     * Returns the node containing the modules definition in the config repository
     * @return the node
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    public Content getModulesNode() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        Content modulesNode = hm.getContent(MODULES_NODE);
        return modulesNode;
    }

    /**
     * Get the module instance
     * @param name
     * @return the instance
     */
    public Module getModuleInstance(String name) {
        return (Module) this.modules.get(name);
    }

    /**
     * Register this module instance to avoid a second instantiation.
     * @param name
     * @param module
     */
    public void addModuleInstance(String name, Module module) {
        this.modules.put(name, module);
    }

}