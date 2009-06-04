/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
import org.apache.commons.collections.MapUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Keeps references to module descriptors and instances.
 *
 * @author philipp
 * @version $Id$
 */
public class ModuleRegistryImpl implements ModuleRegistry {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleRegistryImpl.class);

    private final Map<String, ModuleEntry> entries;

    public ModuleRegistryImpl() {
        entries = MapUtils.lazyMap(new HashMap<String, ModuleEntry>(), new org.apache.commons.collections.Factory() {
            public Object create() {
                return new ModuleEntry();
            }
        });
    }

    public void registerModuleDefinition(String name, ModuleDefinition moduleDefinition) {
        entries.get(name).moduleDefinition = moduleDefinition;
    }

    public void registerModuleInstance(String name, Object moduleInstance) {
        entries.get(name).moduleInstance = moduleInstance;
    }

    public void registerModuleVersionHandler(String name, ModuleVersionHandler moduleVersionHandler) {
        entries.get(name).moduleVersionHandler = moduleVersionHandler;
    }

    public ModuleDefinition getDefinition(String name) {
        final ModuleDefinition moduleDefinition = entries.get(name).moduleDefinition;
        if (moduleDefinition == null) {
            // only logging a debug message here, because some components use this method to determine if a module is present
            log.debug("No module registered with name {}", name);
        }
        return moduleDefinition;
    }

    public Object getModuleInstance(String name) {
        final Object moduleInstance = entries.get(name).moduleInstance;
        if (moduleInstance == null) {
            log.warn("No module registered with name {}", name);
        }
        return moduleInstance;
    }

    public ModuleVersionHandler getVersionHandler(String name) {
        final ModuleVersionHandler moduleVersionHandler = entries.get(name).moduleVersionHandler;
        if (moduleVersionHandler == null) {
            // ModuleManagerImpl currently *always* registers a version handler, even if the module does not specify one
            log.warn("No module registered with name {}", name);
        }
        return moduleVersionHandler;
    }

    public Set<String> getModuleNames() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    private static final class ModuleEntry {

        private ModuleDefinition moduleDefinition;

        private Object moduleInstance;

        private ModuleVersionHandler moduleVersionHandler;

    }
}
