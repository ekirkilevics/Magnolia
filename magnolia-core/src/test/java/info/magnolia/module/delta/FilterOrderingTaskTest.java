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
package info.magnolia.module.delta;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import info.magnolia.module.InstallContext;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link FilterOrderingTask}.
 */
public class FilterOrderingTaskTest {

    private Session session;
    private InstallContext installContext;

    @Before
    public void setUp() throws Exception {
        session = SessionTestUtil.createSession("config",
                "/server/filters/context",
                "/server/filters/contentType",
                "/server/filters/gzip",
                "/server/filters/cache",
                "/server/filters/test"
        );
        installContext = mock(InstallContext.class);
        when(installContext.getConfigJCRSession()).thenReturn(session);
    }

    @Test(expected = TaskExecutionException.class)
    public void testFailWhenFilterDoesntExist() throws Exception {
        FilterOrderingTask task = new FilterOrderingTask("nonExistingFilter", new String[]{"gzip", ""});
        task.execute(installContext);
    }

    @Test
    public void testOrderFilterAfter() throws Exception {
        FilterOrderingTask task = new FilterOrderingTask("test", new String[]{"gzip"});
        task.execute(installContext);
        assertNodeOrder(session.getNode("/server/filters"), new String[]{"context", "contentType", "gzip", "test", "cache"});
    }

    @Test
    public void testOrderFilterAfterLast() throws Exception {
        FilterOrderingTask task = new FilterOrderingTask("cache", new String[]{"test"});
        task.execute(installContext);
        assertNodeOrder(session.getNode("/server/filters"), new String[]{"context", "contentType", "gzip", "test", "cache"});
    }

    @Test
    public void testDoesNothingIfRequiredFilterMissing() throws Exception {
        FilterOrderingTask task = new FilterOrderingTask("gzip", new String[]{"cache", "missing"});
        task.execute(installContext);
        assertNodeOrder(session.getNode("/server/filters"), new String[]{"context", "contentType", "gzip", "cache", "test"});
    }

    private void assertNodeOrder(Node node, String[] names) throws RepositoryException {
        NodeIterator nodes = node.getNodes();
        for (String name : names) {
            Node childNode = nodes.nextNode();
            if (!childNode.getName().equals(name)) {
                fail("Expected [" + name + "] was [" + childNode.getName() + "]");
            }
        }
    }
}
