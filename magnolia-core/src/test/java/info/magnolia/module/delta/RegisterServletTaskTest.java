/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.module.delta;

import static org.junit.Assert.assertEquals;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class RegisterServletTaskTest extends RepositoryTestCase {
    private InstallContextImpl installContext;

    @Override
    @Before
    public void setUp() throws Exception {
        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        ComponentsTestUtil.setInstance(ModuleRegistry.class, moduleRegistry);
        ComponentsTestUtil.setInstance(ModuleManager.class, new ModuleManagerImpl(null, null, null, null, null));
        installContext = new InstallContextImpl(moduleRegistry);
        super.setUp();
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager("config");
        ContentUtil.createPath(hm, "/server/filters/servlets", ItemType.CONTENT);
    }

    @Test
    public void testRegisterServletTaskWithEmptyMappings() throws Exception {
        ServletDefinition sd = new ServletDefinition();
        sd.addMapping("");
        sd.setName("dummy");

        RegisterServletTask task = new RegisterServletTask(sd);
        ModuleDefinition module = new ModuleDefinition();
        module.setName("test");
        installContext.setCurrentModule(module);
        task.execute(installContext);
        assertEquals(1, installContext.getMessages().size());
        final List<InstallContext.Message> messageForTestModule = installContext.getMessages().get(module.toString());
        assertEquals(1, messageForTestModule.size());
        assertEquals("Empty mappings configuration is not supported and servlet was not installed.", messageForTestModule.get(0).getMessage());
    }

    @Test
    public void testRegisterServletTaskWithMappings() throws Exception {
        ServletDefinition sd = new ServletDefinition();
        sd.addMapping(null);
        sd.setName("dummy");

        RegisterServletTask task = new RegisterServletTask(sd);
        ModuleDefinition module = new ModuleDefinition();
        module.setName("test");
        installContext.setCurrentModule(module);
        task.execute(installContext);
        assertEquals(1, installContext.getMessages().size());
        final List<InstallContext.Message> messageForTestModule = installContext.getMessages().get(module.toString());
        assertEquals(1, messageForTestModule.size());
        assertEquals("Empty mappings configuration is not supported and servlet was not installed.", messageForTestModule.get(0).getMessage());
    }
}
