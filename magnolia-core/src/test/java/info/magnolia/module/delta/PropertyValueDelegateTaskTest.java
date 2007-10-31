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
import static org.easymock.EasyMock.*;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PropertyValueDelegateTaskTest extends TestCase {
    private static final String SOMECONTENT = "foo.bar.baz=bleh";
    private InstallContext ctx;
    private Task delegIfTrue;
    private Task delegIfFalse;

    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.initMockContext();

        final MockHierarchyManager hm = MockUtil.createAndSetHierarchyManager("somerepo", SOMECONTENT);
        ctx = createStrictMock(InstallContext.class);
        delegIfTrue = createStrictMock(Task.class);
        delegIfFalse = createStrictMock(Task.class);
        expect(ctx.getHierarchyManager("somerepo")).andReturn(hm);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        FactoryUtil.clear();
        MgnlContext.setInstance(null);
    }

    public void testExecutesDelegateTaskIfPropertyExistsWithExpectedValue() throws Exception {
        delegIfTrue.execute(ctx);
        doTest("baz", "bleh", true);
    }

    public void testExecutesOtherDelegateTaskIfPropertyExistsWithOtherValue() throws Exception {
        delegIfFalse.execute(ctx);
        doTest("baz", "othervalue", true);
    }

    public void testExecutesOtherDelegateTaskIfPropertyMustNotExistAndDoesNot() throws Exception {
        ctx.warn("Property \"nonexistingproperty\" was expected to be found at /foo/bar but does not exist.");
        delegIfFalse.execute(ctx);
        doTest("nonexistingproperty", "bleh", false);
    }

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
