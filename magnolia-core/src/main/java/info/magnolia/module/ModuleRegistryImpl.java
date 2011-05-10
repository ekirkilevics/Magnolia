/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.module.model.ModuleDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Keeps references to module descriptors and instances.
 *
 * @author philipp
 * @version $Id$
 */
public class ModuleRegistryImpl implements ModuleRegistry {
    private final Map<String, ModuleEntry> entries;

    public ModuleRegistryImpl() {
        // using a LinkedHashMap : module definitions are registered in dependency-order, and we need to keep that order.
        entries = new LinkedHashMap<String, ModuleEntry>();
    }

    @Override
    public void registerModuleDefinition(String name, ModuleDefinition moduleDefinition) {
        getOrCreateModuleEntry(name).moduleDefinition = moduleDefinition;
    }

    @Override
    public void registerModuleInstance(String name, Object moduleInstance) {
        getOrCreateModuleEntry(name).moduleInstance = moduleInstance;
    }

    @Override
    public void registerModuleVersionHandler(String name, ModuleVersionHandler moduleVersionHandler) {
        getOrCreateModuleEntry(name).moduleVersionHandler = moduleVersionHandler;
    }

    @Override
    public boolean isModuleRegistered(String name) {
        return entries.containsKey(name);
    }

    @Override
    public ModuleDefinition getDefinition(String name) {
        return safeGetModuleEntry(name).moduleDefinition;
    }

    @Override
    public Object getModuleInstance(String name) {
        return safeGetModuleEntry(name).moduleInstance;
    }

    @Override
    public <T> T getModuleInstance(final Class<T> moduleClass) {
        T module = null;
        for (ModuleEntry m : entries.values()) {
            if (m.moduleInstance != null && moduleClass.isAssignableFrom(m.moduleInstance.getClass())) {
                if (module != null) {
                    throw new IllegalArgumentException("Multiple modules registered with " + moduleClass.toString() + ".");
                }
                module = (T) m.moduleInstance;
            }
        }
        if (module != null) {
            return module;
        }
        throw new IllegalArgumentException("No module registered with " + moduleClass.toString() + ".");
    }

    @Override
    public ModuleVersionHandler getVersionHandler(String name) {
        return safeGetModuleEntry(name).moduleVersionHandler;
    }

    @Override
    public Set<String> getModuleNames() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    @Override
    public List<ModuleDefinition> getModuleDefinitions() {
        // TODO - use something like a Transformer from commons-collections ?
        final List<ModuleDefinition> defs = new ArrayList<ModuleDefinition>();
        for (ModuleEntry mod : entries.values()) {
            defs.add(mod.moduleDefinition);
        }
        return Collections.unmodifiableList(defs);
    }

    private ModuleEntry getOrCreateModuleEntry(String name) {
        synchronized (entries) {
            ModuleEntry moduleEntry = entries.get(name);
            if (moduleEntry == null) {
                moduleEntry = new ModuleEntry();
                entries.put(name, moduleEntry);
            }
            return moduleEntry;
        }
    }

    private ModuleEntry safeGetModuleEntry(String name) {
        synchronized (entries) {
            final ModuleEntry moduleEntry = entries.get(name);
            if (moduleEntry == null) {
                throw new IllegalArgumentException("No module registered with name \"" + name + "\".");
            }
            return moduleEntry;
        }
    }


    private static final class ModuleEntry {

        private ModuleDefinition moduleDefinition;

        private Object moduleInstance;

        private ModuleVersionHandler moduleVersionHandler;

    }
}
