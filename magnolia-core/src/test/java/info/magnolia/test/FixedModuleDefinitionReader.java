/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.test;

import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.module.model.reader.ModuleDefinitionReader;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link info.magnolia.module.model.reader.ModuleDefinitionReader} which simply returns a given list <tt>ModuleDefinition</tt>s.
 */
public class FixedModuleDefinitionReader implements ModuleDefinitionReader {
    private List<ModuleDefinition> modules;

    /**
     * Factory method which loads the given resource paths as module descriptors, but don't use the "discovery" mechanism
     * implemented by {@link info.magnolia.module.model.reader.BetwixtModuleDefinitionReader#readAll()}.
     */
    public static FixedModuleDefinitionReader with(String... resourcePaths) throws ModuleManagementException {
        // load the given resources using the default impl, but don't let them be discovered
        final BetwixtModuleDefinitionReader delegate = new BetwixtModuleDefinitionReader();
        final List<ModuleDefinition> modules = new ArrayList<ModuleDefinition>();
        for (String resourcePath : resourcePaths) {
            modules.add(delegate.readFromResource(resourcePath));
        }
        return new FixedModuleDefinitionReader(modules);
    }

    public FixedModuleDefinitionReader(List<ModuleDefinition> modules) {
        this.modules = modules;
    }

    public FixedModuleDefinitionReader(ModuleDefinition... modules) {
        this.modules = Arrays.asList(modules);
    }

    public Map<String, ModuleDefinition> readAll() throws ModuleManagementException {
        Map<String, ModuleDefinition> all = new LinkedHashMap<String, ModuleDefinition>();
        for (ModuleDefinition module : modules) {
            all.put(module.getName(), module);
        }
        return all;
    }

    public ModuleDefinition read(Reader in) throws ModuleManagementException {
        throw new IllegalStateException("should not be called");
    }

    public ModuleDefinition readFromResource(String resourcePath) throws ModuleManagementException {
        throw new IllegalStateException("should not be called");
    }
}
