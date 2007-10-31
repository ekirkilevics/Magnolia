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
public class IsAuthorInstanceDelegateTaskTest extends TestCase {
    private InstallContext ctx;
    private Task delegIfAuthor;
    private Task delegIfPublic;

    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.initMockContext();
        ctx = createMock(InstallContext.class);
        delegIfAuthor = createStrictMock(Task.class);
        delegIfPublic = createStrictMock(Task.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        FactoryUtil.clear();
        MgnlContext.setInstance(null);
    }

    public void testExecutesAuthorDelegateTaskWhenIsAuthor() throws Exception {
        delegIfAuthor.execute(ctx);
        doTest("server.admin=true");
    }

    public void testExecutesPublicDelegateTaskWhenIsPublic() throws Exception {
        delegIfPublic.execute(ctx);
        doTest("server.admin=false");
    }

    public void testExecutesAuthorDelegateTaskWhenAdminPropertyDoesNotExist() throws Exception {
        ctx.warn("Property \"admin\" was expected to be found at /server but does not exist.");
        delegIfAuthor.execute(ctx);
        doTest("server.bleh=bloh");
    }

    public void testExecutesAuthorDelegateTaskWhenAdminPropertyHasAMeaninglessValue() throws Exception {
        delegIfAuthor.execute(ctx);
        doTest("server.admin=bibaboo");
    }

    private void doTest(String repoContentStr) throws Exception {
        final MockHierarchyManager hm = MockUtil.createAndSetHierarchyManager("config", repoContentStr);
        expect(ctx.getHierarchyManager("config")).andReturn(hm);

        replay(ctx, delegIfAuthor, delegIfPublic);

        final IsAuthorInstanceDelegateTask task = new IsAuthorInstanceDelegateTask("test", "test", delegIfAuthor, delegIfPublic);
        task.execute(ctx);

        verify(ctx, delegIfAuthor, delegIfPublic);
    }

}
