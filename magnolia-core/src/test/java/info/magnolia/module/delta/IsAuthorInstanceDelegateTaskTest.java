/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
        ComponentsTestUtil.clear();
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
