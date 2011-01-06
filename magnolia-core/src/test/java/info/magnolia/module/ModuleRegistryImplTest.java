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

import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.setup.CoreModule;
import junit.framework.TestCase;

import java.util.HashMap;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleRegistryImplTest extends TestCase {
    public void testCanGetModuleByClass() {
        final ModuleRegistryImpl reg = new ModuleRegistryImpl();
        final CoreModule module = new CoreModule();
        reg.registerModuleInstance("foo", module);

        // yay, no cast needed.
        final CoreModule result = reg.getModuleInstance(CoreModule.class);
        assertSame(module, result);
    }

    public void testGetModuleByClassThrowExceptionIfMultipleModulesRegisteredWithSameClass() {
        final ModuleRegistryImpl reg = new ModuleRegistryImpl();
        reg.registerModuleInstance("foo", new CoreModule());
        reg.registerModuleInstance("bar", new CoreModule());
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
        reg.registerModuleInstance("foo", new CoreModule());
        reg.registerModuleInstance("bar", new CoreModule());

        final CoreModule foo = (CoreModule) reg.getModuleInstance("foo");
        final CoreModule bar = (CoreModule) reg.getModuleInstance("bar");
        assertNotSame(foo, bar);
    }

    public void testThrowsExceptionForUnregisteredModuleName() {
        final ModuleRegistryImpl reg = new ModuleRegistryImpl();
        reg.registerModuleInstance("foo", new CoreModule());
        reg.registerModuleInstance("bar", new CoreModule());
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
        reg.registerModuleInstance("foo", new CoreModule());
        reg.registerModuleInstance("bar", new CoreModule());

        assertTrue(reg.isModuleRegistered("bar"));
        assertFalse(reg.isModuleRegistered("chalala"));
    }
}
