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

import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContext;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;
import java.io.IOException;


/**
 * @version $Id$
 */
public class BootstrapConditionallyTest {
    private static final String SOMECONTENT = "foo.bar.baz=bleh";

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testExecutesDelegateTaskIfNodeExists() throws Exception {
        final String resourceToBootstrap = "/some-dir/test/foobar/somerepo.foo.bar.xml";
        doTest(resourceToBootstrap, false, true);
    }

    @Test
    public void testBootstrapsIfNodeDoesNotExist() throws Exception {
        final String resourceToBootstrap = "/some-dir/test/foobar/somerepo.bleh.blih.xml";
        doTest(resourceToBootstrap, true, false);
    }

    private void doTest(String resourceToBootstrap, boolean shouldBootstrap, boolean shouldExecuteDelegateTask) throws IOException, RepositoryException, TaskExecutionException {
        final MockHierarchyManager hm = MockUtil.createAndSetHierarchyManager("somerepo", SOMECONTENT);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        final Task delegateTask = createStrictMock(Task.class);
        expect(ctx.getHierarchyManager("somerepo")).andReturn(hm);
        if (shouldExecuteDelegateTask) {
            delegateTask.execute(ctx);
        }

        replay(ctx, delegateTask);
        final BootstrapConditionally task = new BootstrapConditionally("test", "test", resourceToBootstrap, delegateTask);
        try {
            task.execute(ctx);
            // TODO : ugly hack until BootstrapUtil is refactored / mockable / testable
        } catch (TaskExecutionException e) {
            if (shouldBootstrap) {
                assertEquals("Could not bootstrap: Can't find resource to bootstrap at /some-dir/test/foobar/somerepo.bleh.blih.xml", e.getMessage());
            } else {
                fail("Failed! NullPointerException: " + e.getMessage() + " : " + ExceptionUtils.getFullStackTrace(e));
            }

        }
        verify(ctx, delegateTask);
    }
}
