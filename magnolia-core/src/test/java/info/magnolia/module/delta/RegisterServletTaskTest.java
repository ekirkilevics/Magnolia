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
package info.magnolia.module.delta;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

/**
 * 
 * @author ochytil
 * @version $Revision: $ ($Author: $)
 */
public class RegisterServletTaskTest extends RepositoryTestCase {

    protected void setUp() throws Exception {
        ComponentsTestUtil.setInstance(ModuleRegistry.class, new ModuleRegistryImpl());
        ComponentsTestUtil.setInstance(ModuleManager.class, new ModuleManagerImpl());
        super.setUp();
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager("config");
        ContentUtil.createPath(hm, "/server/filters/servlets", ItemType.CONTENT);
    }

    public void testRegisterServletTaskWithEmptyMappings() throws Exception {
        ServletDefinition sd = new ServletDefinition();
        sd.addMapping("");
        sd.setName("dummy");

        RegisterServletTask task = new RegisterServletTask(sd);
        InstallContextImpl ctx = new InstallContextImpl();
        ModuleDefinition module = new ModuleDefinition();
        module.setName("test");
        ctx.setCurrentModule(module);
        task.execute(ctx);
        assertEquals("Empty mappings configuration is not supported and sevlet was not installed.", ctx.getMessages().values().iterator().next().get(0)
                .getMessage());
    }

    public void testRegisterServletTaskWithMappings() throws Exception {
        ServletDefinition sd = new ServletDefinition();
        sd.addMapping(null);
        sd.setName("dummy");

        RegisterServletTask task = new RegisterServletTask(sd);
        InstallContextImpl ctx = new InstallContextImpl();
        ModuleDefinition module = new ModuleDefinition();
        module.setName("test");
        ctx.setCurrentModule(module);
        task.execute(ctx);
        assertEquals("Empty mappings configuration is not supported and sevlet was not installed.", ctx.getMessages().values().iterator().next().get(0)
                .getMessage());
    }
}