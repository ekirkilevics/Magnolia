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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;
import info.magnolia.test.mock.MockUtil;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RenamePropertyAllModulesNodeTaskTest {
    private InstallContext ctx;

    @Before
    public void setUp() throws Exception {
        ctx = mock(InstallContext.class);
    }

    @Test
    public void testRenameWorksIfOldPropertyIsPresentAndNewNot() throws Exception {
        HierarchyManager hm = MockUtil.createAndSetHierarchyManager("config", "/modules");

        final MockContent modulesNode = (MockContent) hm.getContent("/modules");
        final MockContent testModule = new MockContent("test-module");
        modulesNode.addContent(testModule);
        final MockContent foo = new MockContent("foo");
        testModule.addContent(foo);
        foo.addNodeData(new MockNodeData("before", "remainingValue"));

        when(ctx.hasModulesNode()).thenReturn(true);
        when(ctx.getModulesNode()).thenReturn(modulesNode);

        // make sure NodeData "after" does not yet exist
        assertTrue(!foo.hasNodeData("after"));

        final RenamePropertyAllModulesNodeTask task =
                new RenamePropertyAllModulesNodeTask("Name of the Task", "Test-Description", "foo", "before", "after");
        task.doExecute(ctx);

        final Content updatedFoo = hm.getContent("/modules/test-module/foo");
        assertTrue("should no longer exist", !updatedFoo.hasNodeData("before"));
        assertEquals("remainingValue", updatedFoo.getNodeData("after").getString());
    }
}
