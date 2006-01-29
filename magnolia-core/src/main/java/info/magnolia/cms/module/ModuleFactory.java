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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private static Logger log = LoggerFactory.getLogger(ModuleFactory.class);

    /**
     * Instantiate a module once.
     */
    private static Map instantiatedModules = new HashMap();

    /**
     * Register all jars with a magnolia module manifets
     * @throws ConfigurationException if a module has an not handled error during registration
     */
    public static void init() throws ConfigurationException {
        log.info("Loading module jars"); //$NON-NLS-1$
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            Content startPage = hm.getContent(ModuleLoader.CONFIG_PAGE);
            init(startPage);
            log.info("Finished loading module jars"); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("Failed to load the module jar"); //$NON-NLS-1$
            log.error(e.getMessage(), e);
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
                        String moduleName = manifest.getMainAttributes().getValue("Magnolia-Module-Name"); //$NON-NLS-1$
                        String version = manifest.getMainAttributes().getValue("Magnolia-Module-Version"); //$NON-NLS-1$
                        String moduleClassName = jar
                            .getManifest()
                            .getMainAttributes()
                            .getValue("Magnolia-Module-Class"); //$NON-NLS-1$

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
                                    if (!version.equals(moduleNode.getNodeData("version").getString())) { //$NON-NLS-1$
                                        registerState = Module.REGISTER_STATE_NEW_VERSION;
                                    }
                                }
                                // first installation
                                catch (PathNotFoundException e1) {
                                    moduleNode = modulesNode.createContent(moduleName);
                                    ModuleUtil.createMinimalConfiguration(
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
                                        moduleNode.createNodeData("version").setValue(version); //$NON-NLS-1$
                                    }
                                    modulesNode.save();
                                }
                                catch (RegisterException e) {
                                    switch (registerState) {
                                        case Module.REGISTER_STATE_INSTALLATION:
                                            log.error("can't install module [" + moduleName + "]" + version, e); //$NON-NLS-1$ //$NON-NLS-2$
                                            break;
                                        case Module.REGISTER_STATE_NEW_VERSION:
                                            log.error("can't update module [" + moduleName + "] to version " + version, //$NON-NLS-1$ //$NON-NLS-2$
                                                e);
                                            break;
                                        default:
                                            log.error("error during registering an already installed module [" //$NON-NLS-1$
                                                + moduleName
                                                + "]", e); //$NON-NLS-1$
                                            break;
                                    }
                                }
                            }

                            catch (Exception e) {
                                log.error("can't register module [" + moduleName + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                }
                catch (IOException e) {
                    log.error("can't read manifest", e); //$NON-NLS-1$
                }
            }

        }
        catch (IOException e) {
            log.error("can't load module jars", e); //$NON-NLS-1$
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

        File dir = new File(Path.getAbsoluteFileSystemPath("WEB-INF/lib")); //$NON-NLS-1$
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File jarFile = files[i];
                    if (jarFile.getName().endsWith(".jar")) { //$NON-NLS-1$
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