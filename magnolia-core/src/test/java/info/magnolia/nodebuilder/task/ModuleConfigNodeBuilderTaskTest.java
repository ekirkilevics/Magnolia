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
package info.magnolia.nodebuilder.task;

import static info.magnolia.nodebuilder.Ops.addProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.junit.Test;


/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleConfigNodeBuilderTaskTest extends RepositoryTestCase {
    
    @Test
    public void testModuleConfigNodeIsCreatedIfNeeded() throws Exception {
        // given
        ContentUtil.createPath(MgnlContext.getHierarchyManager("config"), "/modules/test-module", true);
        assertPathNotFoundExceptionFor("modules/test-module/config");
        final ModuleConfigNodeBuilderTask task = new ModuleConfigNodeBuilderTask("Test", "Tests", ErrorHandling.strict,
                addProperty("Hello", "World")
        );

        // when
        final InstallContextImpl ctx = new InstallContextImpl(null);
        ctx.setCurrentModule(new ModuleDefinition("test-module", null, null, null));
        task.execute(ctx);

        // then
        assertEquals("World", MgnlContext.getHierarchyManager("config").getNodeData("/modules/test-module/config/Hello").getString());
    }

    @Test
    public void testModuleNodeIsCreatedIfNeeded() throws Exception {
        // given
        ContentUtil.createPath(MgnlContext.getHierarchyManager("config"), "/modules", true);
        assertPathNotFoundExceptionFor("modules/test-module");
        final ModuleConfigNodeBuilderTask task = new ModuleConfigNodeBuilderTask("Test", "Tests", ErrorHandling.strict,
                addProperty("Hello", "World")
        );

        // when
        final InstallContextImpl ctx = new InstallContextImpl(null);
        ctx.setCurrentModule(new ModuleDefinition("test-module", null, null, null));
        task.execute(ctx);

        // then
        assertEquals("World", MgnlContext.getHierarchyManager("config").getNodeData("/modules/test-module/config/Hello").getString());
    }

    @Test
    public void testUsesExistingModuleConfigNode() throws Exception {
        // given
        ContentUtil.createPath(MgnlContext.getHierarchyManager("config"), "/modules/test-module/config", true);
        final ModuleConfigNodeBuilderTask task = new ModuleConfigNodeBuilderTask("Test", "Tests", ErrorHandling.strict,
                addProperty("Hello", "World")
        );

        // when
        final InstallContextImpl ctx = new InstallContextImpl(null);
        ctx.setCurrentModule(new ModuleDefinition("test-module", null, null, null));
        task.execute(ctx);

        // then
        assertEquals("World", MgnlContext.getHierarchyManager("config").getNodeData("/modules/test-module/config/Hello").getString());
    }

    private void assertPathNotFoundExceptionFor(final String path) throws RepositoryException {
        try {
            MgnlContext.getHierarchyManager("config").getContent(path);
            fail("should have failed");
        } catch (PathNotFoundException e) {
            assertEquals(path, e.getMessage());
        }
    }

}
