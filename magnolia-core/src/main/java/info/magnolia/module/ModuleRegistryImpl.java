/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module;

import info.magnolia.module.model.ModuleDefinition;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Keeps references to module descriptors and instances
 * @author philipp
 * @version $Id$
 */
public class ModuleRegistryImpl implements ModuleRegistry {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ModuleRegistryImpl.class);

    private Map entries;

    public ModuleRegistryImpl() {

        entries = MapUtils.lazyMap(new HashMap(), new org.apache.commons.collections.Factory(){
            public Object create() {
                return new ModuleEntry();
            }
        });
    }

    public void registerModuleDefinition(String name, ModuleDefinition moduleDefinition) {
        ((ModuleEntry) entries.get(name)).moduleDefinition = moduleDefinition;
    }

    public void registerModuleInstance(String name, Object moduleInstance) {
        ((ModuleEntry) entries.get(name)).moduleInstance = moduleInstance;
    }

    public void registerModuleVersionHandler(String name, ModuleVersionHandler moduleVersionHandler) {
        ((ModuleEntry) entries.get(name)).moduleVersionHandler = moduleVersionHandler;
    }

    public ModuleDefinition getDefinition(String name) {
        return ((ModuleEntry) entries.get(name)).moduleDefinition;
    }

    public Object getModuleInstance(String name) {
        return ((ModuleEntry) entries.get(name)).moduleInstance;
    }

    public ModuleVersionHandler getVersionHandler(String name) {
        return ((ModuleEntry) entries.get(name)).moduleVersionHandler;
    }

    private static final class ModuleEntry {

        private ModuleDefinition moduleDefinition;

        private Object moduleInstance;

        private ModuleVersionHandler moduleVersionHandler;

    }
}
