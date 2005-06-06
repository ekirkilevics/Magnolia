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
package info.magnolia.cms.module;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ModuleLoader;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.jcr.PathNotFoundException;

import org.apache.log4j.Logger;

/**
 * Responsible for initialization and registration of modules.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public final class ModuleFactory {
    
    
    /**
     * Util has no public constructor
     */
    private ModuleFactory() {
    }

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(ModuleFactory.class);

    /**
     * Instantiate a module once.
     */
    private static Map instantiatedModules = new HashMap();

    /**
     * Register all jars with a magnolia module manifets
     * @throws ConfigurationException if a module has an not handled error during registration
     */
    public static void init() throws ConfigurationException {
        log.info("Loading module jars");
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            Content startPage = hm.getContent(ModuleLoader.CONFIG_PAGE);
            init(startPage);
            log.info("Finished loading module jars");
        }
        catch (Exception e) {
            log.fatal("Failed to load the module jar");
            log.fatal(e.getMessage(), e);
            throw new ConfigurationException(e.getMessage());
        }
    }

    /**
     * @param modulesNode root node
     */
    private static void init(Content modulesNode) {
        // load all module jars
        try {
            List jars = getJarFiles();

            for (Iterator iter = jars.iterator(); iter.hasNext();) {
                JarFile jar = (JarFile) iter.next();
                try {
                    Manifest manifest = jar.getManifest();
                    if (manifest != null) {
                        // read from manifest
                        String moduleName = manifest.getMainAttributes().getValue("Magnolia-Module-Name");
                        String version = manifest.getMainAttributes().getValue("Magnolia-Module-Version");
                        String moduleClassName = jar
                            .getManifest()
                            .getMainAttributes()
                            .getValue("Magnolia-Module-Class");
                        
                        // if everything is provided
                        if (moduleName != null && moduleClassName != null && version != null) {
                            try {
                                Module module = (Module) Class.forName(moduleClassName).newInstance();
                                int registerState = Module.REGISTER_STATE_NONE;
                                instantiatedModules.put(moduleName, module);
                                Content moduleNode;

                                try {
                                    moduleNode = modulesNode.getContent(moduleName);
                                    // node exists: is it a new version ?
                                    if (!version.equals(moduleNode.getNodeData("version").getString())) {
                                        registerState = Module.REGISTER_STATE_NEW_VERSION;
                                    }
                                }
                                // first installation
                                catch (PathNotFoundException e1) {
                                    moduleNode = modulesNode.createContent(moduleName);
                                    ModuleLoader.createMinimalConfiguration(
                                        moduleNode,
                                        moduleName,
                                        moduleClassName,
                                        version);
                                    registerState = Module.REGISTER_STATE_INSTALLATION;
                                }

                                try {
                                    // call register: this is always done not only during the first startup
                                    module.register(moduleName, version, moduleNode, jar, registerState);
                                    if (registerState == Module.REGISTER_STATE_NEW_VERSION) {
                                        moduleNode.getNodeData("version", true).setValue(version);
                                    }
                                    modulesNode.save();
                                }
                                catch (RegisterException e) {
                                    switch (registerState) {
                                        case Module.REGISTER_STATE_INSTALLATION :
                                            log.error("can't install module [" + moduleName + "]" + version, e);
                                            break;
                                        case Module.REGISTER_STATE_NEW_VERSION :
                                            log.error(
                                                "can't update module [" + moduleName + "] to version " + version,
                                                e);
                                            break;
                                        default :
                                            log.error("error during registering an already installed module ["
                                                + moduleName
                                                + "]", e);
                                            break;
                                    }
                                }
                            }

                            catch (Exception e) {
                                log.error("can't register module [" + moduleName + "]", e);
                            }
                        }
                    }
                }
                catch (IOException e) {
                    log.error("can't read manifest", e);
                }
            }

        }
        catch (IOException e) {
            log.error("can't load module jars", e);
        }
    }

    /**
     * Returns a single instance of a module
     * @param name module name
     * @return module
     */
    public static Module getModuleInstance(String name) {
        return (Module) instantiatedModules.get(name);
    }

    /**
     * @return all the jar files from the lib directory containingn a magnolia module manifest
     * @throws IOException io exception
     */
    private static List getJarFiles() throws IOException {
        List jars = new ArrayList();

        File dir = new File(Path.getAbsoluteFileSystemPath("WEB-INF/lib"));
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File jarFile = files[i];
                    if (jarFile.getName().endsWith(".jar")) {
                        JarFile jar = new JarFile(jarFile);
                        jars.add(jar);
                    }
                }
            }
        }
        return jars;
    }

    /**
     * Call the init method of a module. If there is not yet an instance a new instance is created.
     * @param config configuration of the module
     * @param moduleClassName classname to initialize
     * @throws InstantiationException exception
     * @throws IllegalAccessException exception
     * @throws ClassNotFoundException exception
     * @throws InvalidConfigException exception
     */
    public static void initModule(ModuleConfig config, String moduleClassName) throws InstantiationException,
        IllegalAccessException, ClassNotFoundException, InvalidConfigException {
        Module module = getModuleInstance(config.getModuleName());
        if (module == null) {
            module = (Module) Class.forName(moduleClassName).newInstance();
        }
        module.init(config);
    }

}