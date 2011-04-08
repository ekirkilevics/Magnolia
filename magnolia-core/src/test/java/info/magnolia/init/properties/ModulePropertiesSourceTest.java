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
package info.magnolia.init.properties;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.PropertyDefinition;
import info.magnolia.test.ComponentsTestUtil;
import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModulePropertiesSourceTest extends TestCase {

    @Override
    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testPropertiesCanBeOverriddenUsingDependencyOrderingOfModules() {
        final ModuleDefinition m1 = new ModuleDefinition("m1", null, null, null);
        final PropertyDefinition p1 = new PropertyDefinition();
        p1.setName("myProperty");
        p1.setValue("first value");
        m1.addProperty(p1);
        final ModuleDefinition m2 = new ModuleDefinition("m2", null, null, null);
        final PropertyDefinition p2 = new PropertyDefinition();
        p2.setName("myProperty");
        p2.setValue("second value");
        m2.addProperty(p2);

        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        // gotta register in the "correct" order here, as this is currently done by ModuleRegistryImpl
        moduleRegistry.registerModuleDefinition(m1.getName(), m1);
        moduleRegistry.registerModuleDefinition(m2.getName(), m2);
        final ModulePropertiesSource ps = new ModulePropertiesSource(moduleRegistry);
        assertEquals("second value", ps.getProperty("myProperty"));
    }

    public void testDifferentPropertiesCanRegisteredByDifferentModulesEllipsisDuh() {
        final ModuleDefinition m1 = new ModuleDefinition("m1", null, null, null);
        final PropertyDefinition p1 = new PropertyDefinition();
        p1.setName("firstProperty");
        p1.setValue("first value");
        m1.addProperty(p1);
        final ModuleDefinition m2 = new ModuleDefinition("m2", null, null, null);
        final PropertyDefinition p2 = new PropertyDefinition();
        p2.setName("secondProperty");
        p2.setValue("second value");
        m2.addProperty(p2);

        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        // gotta register in the "correct" order here, as this is currently done by ModuleRegistryImpl
        moduleRegistry.registerModuleDefinition(m1.getName(), m1);
        moduleRegistry.registerModuleDefinition(m2.getName(), m2);
        final ModulePropertiesSource ps = new ModulePropertiesSource(moduleRegistry);
        assertEquals("first value", ps.getProperty("firstProperty"));
        assertEquals("second value", ps.getProperty("secondProperty"));
    }
}
