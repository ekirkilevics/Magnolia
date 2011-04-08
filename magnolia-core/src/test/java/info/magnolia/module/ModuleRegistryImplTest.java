/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.DependencyCheckerImpl;
import info.magnolia.setup.CoreModule;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.FixedModuleDefinitionReader;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.List;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleRegistryImplTest extends TestCase {
    @Override
    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testCanGetModuleByClass() {
        final ModuleRegistryImpl reg = new ModuleRegistryImpl();
        final CoreModule module = new CoreModule(null,null,null);
        reg.registerModuleInstance("foo", module);

        // yay, no cast needed.
        final CoreModule result = reg.getModuleInstance(CoreModule.class);
        assertSame(module, result);
    }

    public void testGetModuleByClassThrowExceptionIfMultipleModulesRegisteredWithSameClass() {
        final ModuleRegistryImpl reg = new ModuleRegistryImpl();
        reg.registerModuleInstance("foo", new CoreModule(null,null,null));
        reg.registerModuleInstance("bar", new CoreModule(null,null,null));
        try {
            reg.getModuleInstance(CoreModule.class);
            fail("should have thrown an exception, we have several modules registered for this class");
        } catch (IllegalArgumentException e) {
            assertEquals("Multiple modules registered with class info.magnolia.setup.CoreModule.", e.getMessage());

        }
    }

    public void testGetModuleByClassThrowExceptionIfNoModulesRegisteredWithGivenClass() {
        final ModuleRegistryImpl reg = new ModuleRegistryImpl();
        reg.registerModuleInstance("foo", new HashMap()/*whatever*/);
        try {
            reg.getModuleInstance(CoreModule.class);
            fail("should have thrown an exception, no module registered for this class");
        } catch (IllegalArgumentException e) {
            assertEquals("No module registered with class info.magnolia.setup.CoreModule.", e.getMessage());
        }
    }

    public void testCanGetModuleByNameIfMultipleModulesRegisteredWithSameClass() {
        final ModuleRegistryImpl reg = new ModuleRegistryImpl();
        reg.registerModuleInstance("foo", new CoreModule(null,null,null));
        reg.registerModuleInstance("bar", new CoreModule(null,null,null));

        final CoreModule foo = (CoreModule) reg.getModuleInstance("foo");
        final CoreModule bar = (CoreModule) reg.getModuleInstance("bar");
        assertNotSame(foo, bar);
    }

    public void testThrowsExceptionForUnregisteredModuleName() {
        final ModuleRegistryImpl reg = new ModuleRegistryImpl();
        reg.registerModuleInstance("foo", new CoreModule(null,null,null));
        reg.registerModuleInstance("bar", new CoreModule(null,null,null));
        reg.registerModuleVersionHandler("bar", new DefaultModuleVersionHandler());
        reg.registerModuleDefinition("bar", new ModuleDefinition("bar", Version.parseVersion("1.0"), "foo.bar", DefaultModuleVersionHandler.class));

        try {
            reg.getModuleInstance("chalala");
            fail("should have thrown an exception, no module registered for this name");
        } catch (IllegalArgumentException e) {
            assertEquals("No module registered with name \"chalala\".", e.getMessage());
        }
        try {
            reg.getDefinition("chalala");
            fail("should have thrown an exception, no module registered for this name");
        } catch (IllegalArgumentException e) {
            assertEquals("No module registered with name \"chalala\".", e.getMessage());
        }
        try {
            reg.getVersionHandler("chalala");
            fail("should have thrown an exception, no module registered for this name");
        } catch (IllegalArgumentException e) {
            assertEquals("No module registered with name \"chalala\".", e.getMessage());
        }
    }

    public void testCanCheckIfAModuleExists() {
        final ModuleRegistryImpl reg = new ModuleRegistryImpl();
        reg.registerModuleInstance("foo", new CoreModule(null,null,null));
        reg.registerModuleInstance("bar", new CoreModule(null,null,null));

        assertTrue(reg.isModuleRegistered("bar"));
        assertFalse(reg.isModuleRegistered("chalala"));
    }

    public void testModuleDefinitionsAreListedInDependencyOrder() throws ModuleManagementException {
        final ModuleDefinition a = new ModuleDefinition("a", Version.parseVersion("1.0"), null, null);
        final ModuleDefinition b = new ModuleDefinition("b", Version.parseVersion("1.0"), null, null);
        final ModuleDefinition c = new ModuleDefinition("c", Version.parseVersion("1.0"), null, null);
        b.addDependency(new DependencyDefinition("a", "1.0", false));
        c.addDependency(new DependencyDefinition("a", "1.0", false));
        c.addDependency(new DependencyDefinition("b", "1.0", false));
        // currently ModuleManager loads definitions and registers them in ModuleRegistry - perhaps this should change at some point ...
        // Even if we register/load modules in a bogus order, ModuleRegistry should list them in dependency-order
        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        final ModuleManagerImpl moduleManager = new ModuleManagerImpl(null, new FixedModuleDefinitionReader(c, b, a), moduleRegistry, new DependencyCheckerImpl());
        moduleManager.loadDefinitions();

        final List<ModuleDefinition> result = moduleRegistry.getModuleDefinitions();
        assertEquals(3, result.size());
        assertEquals("a", result.get(0).getName());
        assertEquals("b", result.get(1).getName());
        assertEquals("c", result.get(2).getName());

    }
}
