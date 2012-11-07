/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.test.mock.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * @version $Id$
 */
public class MockSessionTest {

    @Test
    public void testConstructionProperlyWiresWorkspaceToSession() {
        final MockSession mockSession = new MockSession("test");
        assertEquals(mockSession, mockSession.getWorkspace().getSession());
    }
    @Test
    public void testConstructionProperlyWiresSessionToRoot() throws Exception {
        MockSession session = new MockSession("test");
        MockNode root = (MockNode) session.getRootNode();
        assertEquals(session, root.getSession());
    }
    @Test
    public void testGetNodeFromStringe() throws Exception {
        final MockSession mockSession = new MockSession("test");
        final MockNode parent = (MockNode) mockSession.getRootNode();
        final MockNode child = new MockNode("child");
        parent.addNode(child);

        assertEquals(child, mockSession.getNode("/child"));
    }
    @Test
    public void testGetPropertyFromString() throws Exception {
        final MockSession mockSession = new MockSession("test");
        final MockNode parent = (MockNode) mockSession.getRootNode();
        final MockNode child = new MockNode("child");
        parent.addNode(child);
        final MockNode childOfChild = new MockNode("childOfChild");
        child.addNode(childOfChild);
        childOfChild.setProperty("property", "propertyValue");
        Property property = childOfChild.getProperty("property");

        assertEquals(property, mockSession.getProperty("/child/childOfChild/property"));
    }
    @Test
    public void testNodeExistsFromString() throws Exception {
        final MockSession mockSession = new MockSession("test");
        final MockNode parent = (MockNode) mockSession.getRootNode();
        final MockNode child = new MockNode("child");
        parent.addNode(child);

        assertTrue(mockSession.nodeExists("/child"));
        assertTrue(!mockSession.nodeExists("/notThere"));
    }

    @Test
    public void testGetNodeByIdentifier() throws RepositoryException {
        // given

        // mock workspace with these nodes:
        // /foo/bar/zed
        // /apple
        final MockSession mockSession = new MockSession("test");
        final MockNode parent = (MockNode) mockSession.getRootNode();
        final MockNode foo = new MockNode("foo");
        final MockNode bar = (MockNode) foo.addNode("bar");
        final MockNode zed = new MockNode("zed");
        final MockNode apple = new MockNode("apple");
        parent.addNode(foo);
        foo.addNode(bar);
        bar.addNode(zed);
        parent.addNode(apple);

        assertSame(parent, mockSession.getNodeByIdentifier(parent.getIdentifier()));
        assertSame(foo, mockSession.getNodeByIdentifier(foo.getIdentifier()));
        assertSame(bar, mockSession.getNodeByIdentifier(bar.getIdentifier()));
        assertSame(zed, mockSession.getNodeByIdentifier(zed.getIdentifier()));
        assertSame(apple, mockSession.getNodeByIdentifier(apple.getIdentifier()));

        try {
            mockSession.getNodeByIdentifier("fake");
            fail();
        } catch (ItemNotFoundException expected) {
        }
    }

    @Test
    public void testGetItem() throws Exception {
        MockSession session = new MockSession("test");
        Node rootNode = session.getRootNode();

        Node level1 = rootNode.addNode("level1");

        Node level2 = level1.addNode("level2");
        Property value2 = level2.setProperty("prop2", "value2");

        Node level3 = level2.addNode("level3");
        Property value3 = level3.setProperty("prop3", "value3");

        assertSame(rootNode, session.getItem("/"));
        assertSame(level1, session.getItem("/level1"));
        assertSame(level2, session.getItem("/level1/level2"));
        assertSame(level3, session.getItem("/level1/level2/level3"));
        assertSame(value2, session.getItem("/level1/level2/prop2"));
        assertSame(value3, session.getItem("/level1/level2/level3/prop3"));
    }

    @Test(expected = PathNotFoundException.class)
    public void testGetItemOnNonAbsolutePathFails() throws Exception {
        MockSession session = new MockSession("test");
        Node level1 = session.getRootNode().addNode("level1");
        assertSame(level1, session.getItem("/level1"));
        session.getItem("level1");
    }

    @Test(expected = PathNotFoundException.class)
    public void testGetItemOnNonExistingPathFails() throws Exception {
        MockSession session = new MockSession("test");
        Node level1 = session.getRootNode().addNode("level1");
        assertSame(level1, session.getItem("/level1"));

        // this fails
        session.getItem("/level1/doesNotExist");
    }
}
