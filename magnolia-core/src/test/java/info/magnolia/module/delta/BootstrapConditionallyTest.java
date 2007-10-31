/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.delta;

import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContext;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;
import org.apache.commons.lang.exception.ExceptionUtils;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;
import java.io.IOException;


/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BootstrapConditionallyTest extends TestCase {
    private static final String SOMECONTENT = "foo.bar.baz=bleh";

    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.initMockContext();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        FactoryUtil.clear();
        MgnlContext.setInstance(null);
    }

    public void testExecutesDelegateTaskIfNodeExists() throws Exception {
        final String resourceToBootstrap = "/some-dir/test/foobar/somerepo.foo.bar.xml";
        doTest(resourceToBootstrap, false, true);
    }

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
