/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module;

import java.util.Set;

import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.Components;

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

    boolean isModuleRegistered(String name);

    /**
     * Returns the module's instance. This is useful for modules which use a common class (i.e content or theme modules, which
     * can typically co-exist in a system and have the same module class).
     * @see #getModuleInstance(Class) for a type-safer method to get module instances
     * @throws IllegalArgumentException if no such module is registered.
     */
    Object getModuleInstance(String name);

    /**
     * Returns the module's instance. This is useful for modules of known type.
     * @throws IllegalArgumentException if no such module is registered or if multiple modules are registered for this class.
     */
    <T> T getModuleInstance(Class<T> moduleClass);

    /**
     * @throws IllegalArgumentException if no such module is registered.
     */
    ModuleVersionHandler getVersionHandler(String name);

    /**
     * @throws IllegalArgumentException if no such module is registered.
     */
    ModuleDefinition getDefinition(String name);

    /**
     * Returns the names of configured modules.
     * @return unmodifiable set of module names
     */
    Set<String> getModuleNames();

    /**
     * Use this to retrieve the configured singleton impl of ModuleRegistry.
     */
    public class Factory {
        public static ModuleRegistry getInstance() {
            return Components.getSingleton(ModuleRegistry.class);
        }
    }

}
