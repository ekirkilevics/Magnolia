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
package info.magnolia.module;

import java.util.Set;

import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.module.model.ModuleDefinition;

/**
 * Holds instances and definitions of modules.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ModuleRegistry {

    void registerModuleDefinition(String name, ModuleDefinition moduleDefinition);

    void registerModuleInstance(String name, Object moduleInstance);

    void registerModuleVersionHandler(String name, ModuleVersionHandler moduleVersionHandler);

    /**
     * Returns the module's instance.
     */
    Object getModuleInstance(String name);

    ModuleVersionHandler getVersionHandler(String name);

    ModuleDefinition getDefinition(String name);

    /**
     * Returns the names of configured modules (strings)
     * @return unmodifiable set of module names
     */
    Set getModuleNames();

    /**
     * Use this to retrieve the configured singleton impl of ModuleRegistry.
     */
    public class Factory {
        public static ModuleRegistry getInstance() {
            return (ModuleRegistry) FactoryUtil.getSingleton(ModuleRegistry.class);
        }
    }

}
