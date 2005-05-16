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

import javax.jcr.PathNotFoundException;

import org.apache.log4j.Logger;


/**
 * @author philipp
 */
public class ModuleFactory {

    private static Logger log = Logger.getLogger(ModuleFactory.class);

    private static Map instantiatedModules = new HashMap();

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

    private static void init(Content modulesNode) throws InstantiationException, IllegalAccessException,
        ClassNotFoundException {
        Map registeredModules = new HashMap();

        // load all module jars
        try {
            List jars = getJarFiles();

            for (Iterator iter = jars.iterator(); iter.hasNext();) {
                JarFile jar = (JarFile) iter.next();
                try {
                    String moduleName = jar.getManifest().getMainAttributes().getValue("Magnolia-Module-Name");
                    String moduleClassName = jar.getManifest().getMainAttributes().getValue("Magnolia-Module-Class");
                    if (moduleName != null && moduleClassName != null) {
                        registeredModules.put(moduleName, moduleClassName);
                        log.info("module loaded [" + moduleName + "]");
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

        // create instance and call register()
        for (Iterator iter = registeredModules.keySet().iterator(); iter.hasNext();) {
            String moduleName = (String) iter.next();
            String className = (String) registeredModules.get(moduleName);

            Module module = (Module) Class.forName(className).newInstance();

            instantiatedModules.put(moduleName, module);
            Content moduleNode;
            try {
                try {
                    moduleNode = modulesNode.getContent(moduleName);
                }
                catch (PathNotFoundException e1) {
                    moduleNode = modulesNode.createContent(moduleName);
                    modulesNode.save();
                }
                module.register(moduleNode);
            }

            catch (Exception e) {
                log.error(e);
            }
        }
    }

    public static Module getModuleInstance(String name) {
        return (Module) instantiatedModules.get(name);
    }

    private static List getJarFiles() throws IOException {
        ArrayList jars = new ArrayList();

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

    public static void initModule(ModuleConfig thisModule, String moduleClassName) throws InstantiationException,
        IllegalAccessException, ClassNotFoundException, InvalidConfigException {
        Module module = getModuleInstance(thisModule.getModuleName());
        if (module == null) {
            module = (Module) Class.forName(moduleClassName).newInstance();
        }
        module.init(thisModule);
    }

}