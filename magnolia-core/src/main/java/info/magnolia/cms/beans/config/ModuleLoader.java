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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleDefinition;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.core.HierarchyManager;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.OrderedMapIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Initialise all configured modules.
 * @deprecated since 3.1, use ModuleManager and/or ModuleRegistry
 * @see info.magnolia.module.ModuleManager
 * @see info.magnolia.module.ModuleRegistry
 */
public class ModuleLoader {

    /**
     * Logger.
     */
    protected static Logger log = LoggerFactory.getLogger(ModuleLoader.class);

    /**
     * magnolia module specific keywords
     */
    public static final String MODULES_NODE = "modules"; //$NON-NLS-1$

    public static final String CONFIG_NODE_VIRTUAL_MAPPING = "virtualURIMapping"; //$NON-NLS-1$

    public static final String CONFIG_NODE = "config"; //$NON-NLS-1$

    /**
     * The module instances
     */
    private Map modules = new HashMap();

    /**
     * Don't instantiate.
     */
    public ModuleLoader() {
    }

    /**
     * @return Returns the instance.
     */
    public static ModuleLoader getInstance() {
        return (ModuleLoader) FactoryUtil.getSingleton(ModuleLoader.class);
    }

    /**
     * Init the modules.
     * @throws ConfigurationException
     */
    protected void init() throws ConfigurationException {
        log.info("Loading modules"); //$NON-NLS-1$
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
        for (OrderedMapIterator iter = defs.orderedMapIterator(); iter.hasNext();) {
            iter.next();
            ModuleDefinition def = (ModuleDefinition) iter.getValue();
            try {
                if (modulesNode.hasContent(def.getName())) {
                    Content moduleNode = modulesNode.getContent(def.getName());
                    load(def, moduleNode);
                }
                else {
                    log.error("can't initialize module [{}]: no module node in the config repository found", def
                        .getName());
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
            Module module = this.getModuleInstance(def.getName());

            // TODO : this should be removed !
            // instantiate if not yet done (due registraion)
            if (module == null) {
                try {
                    String moduleClassName = moduleNode.getNodeData("class").getString(); //$NON-NLS-1$

                    module = (Module) ClassUtil.newInstance(moduleClassName);
                    this.addModuleInstance(def.getName(), module);
                }
                catch (InstantiationException ie) {
                    log.error("Module {} failed to load", moduleNode.getName()); //$NON-NLS-1$ 
                    log.error(ie.getMessage());
                }
                catch (IllegalAccessException ae) {
                    log.error(ae.getMessage());
                }
            }

            // init the module
            if (!module.isInitialized()) {
                if (!module.isRestartNeeded()) {
                    log.info("start initialization of module {}", def.getName());
                    Content moduleConfigNode = ContentUtil.getCaseInsensitive(moduleNode, CONFIG_NODE);
                    module.init(moduleConfigNode);
                    log.info("module {} initialized", def.getName()); //$NON-NLS-1$
                }
                else {
                    log.warn("won't initialize the module {} since a system restart is needed", module.getName());
                }

                if (module.isRestartNeeded()) {
                    ModuleRegistration.getInstance().setRestartNeeded(true);
                }
            }
            final Module m = module;
            // add destroy method as a shutdown task
            ShutdownManager.addShutdownTask(new ShutdownTask() {

                public boolean execute(info.magnolia.context.Context context) {
                    log.info("Shutting down module: " + m.getName());
                    m.destroy();
                    return true;
                }

                public String toString() {
                    return getClass().getName() + " " + m;
                }
            });

        }
        catch (Exception e) {
            log.error("can't initialize module " + moduleNode.getHandle(), e); //$NON-NLS-1$
        }
    }

    public void reload() throws ConfigurationException {
        init();
    }

    /**
     * Returns the node containing the modules definition in the config repository
     * @return the node
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     *
     * @deprecated ModuleManager does this.
     */
    public Content getModulesNode() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        if (!hm.isExist("/" + MODULES_NODE)) {
            hm.createContent("/", MODULES_NODE, ItemType.CONTENT.getSystemName());
        }
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
     * @return the map containing the modules
     */
    public Map getModuleInstances() {
        return this.modules;
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