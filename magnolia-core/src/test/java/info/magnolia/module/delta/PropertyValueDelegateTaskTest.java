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

import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContext;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PropertyValueDelegateTaskTest {
    private static final String SOMECONTENT = "foo.bar.baz=bleh";
    private InstallContext ctx;
    private Task delegIfTrue;
    private Task delegIfFalse;

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();

        final MockHierarchyManager hm = MockUtil.createAndSetHierarchyManager("somerepo", SOMECONTENT);
        ctx = createStrictMock(InstallContext.class);
        delegIfTrue = createStrictMock(Task.class);
        delegIfFalse = createStrictMock(Task.class);
        expect(ctx.getHierarchyManager("somerepo")).andReturn(hm);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testExecutesDelegateTaskIfPropertyExistsWithExpectedValue() throws Exception {
        delegIfTrue.execute(ctx);
        doTest("baz", "bleh", true);
    }

    @Test
    public void testExecutesOtherDelegateTaskIfPropertyExistsWithOtherValue() throws Exception {
        delegIfFalse.execute(ctx);
        doTest("baz", "othervalue", true);
    }

    @Test
    public void testExecutesOtherDelegateTaskIfPropertyMustNotExistAndDoesNot() throws Exception {
        ctx.warn("Property \"nonexistingproperty\" was expected to be found at /foo/bar but does not exist.");
        delegIfFalse.execute(ctx);
        doTest("nonexistingproperty", "bleh", false);
    }

    @Test
    public void testThrowsExceptionIfPropertyMustExistAndDoesNot() throws Exception {
        try {
            doTest("nonexistingproperty", "bleh", true);
            fail();
        } catch (TaskExecutionException e) {
            assertEquals("Property \"nonexistingproperty\" was expected to exist at /foo/bar", e.getMessage());
        }
    }

    private void doTest(String propertyName, String expectedValue, boolean propertyMustExist) throws TaskExecutionException {
        replay(ctx, delegIfTrue, delegIfFalse);

        try {
            final PropertyValueDelegateTask task = new PropertyValueDelegateTask("test", "test", "somerepo", "/foo/bar", propertyName, expectedValue, propertyMustExist, delegIfTrue, delegIfFalse);
            task.execute(ctx);
        } finally {
            verify(ctx, delegIfTrue, delegIfFalse);
        }
    }

}
