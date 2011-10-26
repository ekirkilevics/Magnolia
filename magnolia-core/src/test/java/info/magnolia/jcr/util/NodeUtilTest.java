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
package info.magnolia.jcr.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.version.VersionedNode;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class NodeUtilTest {

    private static final String FIRST_CHILD = "1";
    private static final String SECOND_CHILD = "2";
    private static final String THIRD_CHILD = "3";

    private MockNode root;
    private Node first;
    private Node second;
    private Node third;

    @Before
    public void setUpTestStructure() throws RepositoryException {
        root = new MockNode();
        first = root.addNode(FIRST_CHILD);
        second = root.addNode(SECOND_CHILD);
        third = root.addNode(THIRD_CHILD);
    }

    @Test
    public void testHasMixin() throws Exception {
        final String mixin1 = "mixin1";
        root.addMixin(mixin1);

        assertTrue(NodeUtil.hasMixin(root, mixin1));
        assertFalse(NodeUtil.hasMixin(root, "mixin2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasMixinFailsWithEmptyMixin() throws Exception {
        NodeUtil.hasMixin(root, null);
    }

    @Test
    public void testUnwrap() throws Exception {
        final Version version = mock(Version.class);
        when(version.getNode(MgnlNodeType.JCR_FROZENNODE)).thenReturn(root);
        final VersionedNode wrapper = new VersionedNode(version);

        assertEquals(root, NodeUtil.unwrap(wrapper));
    }

    @Test
    public void testOrderBeforeWithExistingNodeAndSibling() throws Exception {
        NodeUtil.orderBefore(third, FIRST_CHILD);

        NodeIterator kidsIterator = root.getNodes();
        assertEquals(third, kidsIterator.next());
        assertEquals(first, kidsIterator.next());
        assertEquals(second, kidsIterator.next());
    }

    @Test
    public void testOrderBeforeWithNullSibling() throws Exception {
        // should result in putting firstChild at the end of the children
        NodeUtil.orderBefore(first, null);

        NodeIterator orderedKids = root.getNodes();
        assertEquals(second, orderedKids.next());
        assertEquals(third, orderedKids.next());
        assertEquals(first, orderedKids.next());
    }

    @Test
    public void testOrderAfterWithExistingNodeAndSibling() throws Exception {
        NodeUtil.orderAfter(third, FIRST_CHILD);

        NodeIterator kidsIterator = root.getNodes();
        assertEquals(first, kidsIterator.next());
        assertEquals(third, kidsIterator.next());
        assertEquals(second, kidsIterator.next());
    }

    @Test
    public void testOrderAfterWithNullSibling() throws RepositoryException {
        // should result in putting thirdChild at the begin of the children
        NodeUtil.orderAfter(third, null);
        NodeIterator orderedKids = root.getNodes();
        assertEquals(third, orderedKids.next());
        assertEquals(first, orderedKids.next());
        assertEquals(second, orderedKids.next());
    }

    @Test
    public void testOrderFirst() throws RepositoryException {
        NodeUtil.orderFirst(second);
        NodeIterator orderedKids = root.getNodes();
        assertEquals(second, orderedKids.next());
        assertEquals(first, orderedKids.next());
        assertEquals(third, orderedKids.next());
    }

    @Test
    public void testOrderLast() throws RepositoryException {
        NodeUtil.orderLast(second);
        NodeIterator orderedKids = root.getNodes();
        assertEquals(first, orderedKids.next());
        assertEquals(third, orderedKids.next());
        assertEquals(second, orderedKids.next());
    }

    @Test
    public void testCreatePath() throws RepositoryException {
        final String pathToCreate = "/xxx/yyy/zzz";
        Node zzz = NodeUtil.createPath(root, pathToCreate, PropertyType.TYPENAME_STRING);
        assertNotNull(zzz);
        assertEquals(PropertyType.TYPENAME_STRING, zzz.getPrimaryNodeType().getName());
    }

    @Test
    public void testCreatePathDoesntCreateNewWhenExisting() throws RepositoryException {
        Node returnedNode = NodeUtil.createPath(root, FIRST_CHILD, PropertyType.TYPENAME_STRING);
        assertNotNull(returnedNode);
        assertEquals("createPath was called with existing subpath: existing node should be returned - not a new instance!", first, returnedNode);
    }

    @Test
    public void testVisit() throws RepositoryException {
        final AtomicInteger counter = new AtomicInteger(0);
        NodeUtil.visit(root, new NodeVisitor() {
            @Override
            public void visit(Node node) throws RepositoryException {
                counter.incrementAndGet();
            }
        });
        assertEquals(4, counter.get());
    }

    @Test
    public void testPostVisit() throws RepositoryException {

        final AtomicInteger counter1 = new AtomicInteger(0);
        final AtomicInteger counter2 = new AtomicInteger(0);
        final List<String> names = new ArrayList<String>();
        NodeUtil.visit(root, new PostNodeVisitor() {
            @Override
            public void visit(Node node) throws RepositoryException {
                counter1.incrementAndGet();
            }

            @Override
            public void postVisit(Node node) throws RepositoryException {
                counter2.incrementAndGet();
                names.add(node.getName());
            }
        });
        assertEquals(4, counter1.get());
        assertEquals(4, counter2.get());
        assertEquals(4, names.size());
        assertEquals(FIRST_CHILD, names.get(0));
        assertEquals(SECOND_CHILD, names.get(1));
        assertEquals(THIRD_CHILD, names.get(2));
        assertEquals(MockNode.ROOT_NODE_NAME, names.get(3));
    }

    @Test
    public void testGetNodes() throws RepositoryException {
        first.addNode("alpha", MgnlNodeType.NT_CONTENT);
        first.addNode("meta", MgnlNodeType.NT_METADATA);
        first.addNode("gamma", MgnlNodeType.NT_CONTENT);

        Iterable<Node> iterable = NodeUtil.getNodes(first);
        Iterator<Node> iterator = iterable.iterator();
        assertEquals("alpha", iterator.next().getName());
        assertEquals("gamma", iterator.next().getName());
        assertTrue(!iterator.hasNext());
    }

    @Test
    public void testGetNodeWithContentType() throws RepositoryException {
        root.addNode("alpha", MgnlNodeType.NT_CONTENT);
        root.addNode("beta", MgnlNodeType.NT_FOLDER);
        root.addNode("gamma", MgnlNodeType.NT_CONTENT);

        Iterable<Node> iterable = NodeUtil.getNodes(root, MgnlNodeType.NT_CONTENT);
        Iterator<Node> iterator = iterable.iterator();
        assertEquals("alpha", iterator.next().getName());
        assertEquals("gamma", iterator.next().getName());
        assertTrue(!iterator.hasNext());
    }

    @Test
    public void testGetNodesWithNodeFilter() throws RepositoryException {
        first.addNode("alpha", MgnlNodeType.NT_CONTENT);
        first.addNode("beta", MgnlNodeType.JCR_CONTENT);

        Iterable<Node> iterable = NodeUtil.getNodes(first, NodeUtil.MAGNOLIA_FILTER);
        Iterator<Node> iterator = iterable.iterator();

        assertEquals("alpha", iterator.next().getName());
        assertTrue(!iterator.hasNext());
    }

    @Test
    public void testGetNameFromNode() {
        assertEquals(FIRST_CHILD, NodeUtil.getName(first));
    }

    @Test(expected = RuntimeRepositoryException.class)
    public void testGetNameFromNodeThrowsRuntimeRepositoryException() {
        Node node = mock(Node.class);
        try {
            when(node.getName()).thenThrow(new RepositoryException());
        } catch (RepositoryException e) {
            fail();
        }
        NodeUtil.getName(node);
    }

    @Test
    public void testGetNodeByIdentifier() throws RepositoryException {
        //INIT
        try {
            // GIVEN
            MockUtil.initMockContext();
            MockSession session = new MockSession("website");
            MockUtil.setSessionAndHierarchyManager(session);
            Node rootNode = session.getRootNode();
            Node addedNode = rootNode.addNode(FIRST_CHILD);
            String identifier = addedNode.getIdentifier();

            // WHEN
            Node res = NodeUtil.getNodeByIdentifier("website", identifier);

            //THEN
            assertEquals("Both Node should be Identical ", addedNode, res);

        } finally {
            MgnlContext.setInstance(null);
        }
    }

    @Test
    public void testGetNodeByIdentifierMissingParam() throws RepositoryException {
        //INIT
        try {
            //WHEN
            Node res = NodeUtil.getNodeByIdentifier("website", null);
            //THEN
            assertEquals("Both Node should be Identical ", null, res);

        } finally {
            MgnlContext.setInstance(null);
        }
    }

    @Test(expected = RepositoryException.class)
    public void testGetNodeByIdentifierNoNodeFound() throws RepositoryException {
        //INIT
        try {
            //GIVEN
            MockUtil.initMockContext();
            MockSession session = new MockSession("website");
            MockUtil.setSessionAndHierarchyManager(session);
            Node rootNode = session.getRootNode();
            Node addedNode = rootNode.addNode(FIRST_CHILD);
            String identifier = addedNode.getIdentifier();

            //WHEN
            NodeUtil.getNodeByIdentifier("website", identifier + 1);

            assertTrue("Should get an Exception ", false);
        } finally {
            MgnlContext.setInstance(null);
        }
    }

    @Test
    public void testPathIfPossible() throws RepositoryException {
        // GIVEN

        // WHEN
        String res = NodeUtil.getPathIfPossible(first);

        // THEN
        assertEquals("Should be /testNode  ", "/" + FIRST_CHILD, res);
    }


    @Test
    public void testGetStringIfPossibleTwoArgs() throws RepositoryException {
        // GIVEN
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        first.setProperty(propertyName, propertyValue);

        // WHEN
        String res = NodeUtil.getStringIfPossible(first, propertyName);

        // THEN
        assertEquals(propertyValue, res);
    }

    @Test
    public void testGetStringIfPossibleThreeArgs() throws RepositoryException {
        // GIVEN
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        String defaultValue = "defaultValue";
        first.setProperty(propertyName, propertyValue);

        // WHEN
        String res = NodeUtil.getStringIfPossible(first, propertyName,defaultValue);

        // THEN
        assertEquals(propertyValue, res);
    }

    @Test
    public void testGetStringIfPossibleThreeArgsBadNodeName() throws RepositoryException {
        // GIVEN
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        String defaultValue = "defaultValue";
        first.setProperty(propertyName, propertyValue);

        // WHEN
        String res = NodeUtil.getStringIfPossible(first, propertyName+1,defaultValue);

        // THEN
        assertEquals(defaultValue, res);
    }

    @Test
    public void testGetStringIfPossibleThreeArgsBadNodeType() throws RepositoryException {
        // GIVEN
        String propertyName = "propertyName";
        BigDecimal propertyValue = BigDecimal.ONE;
        String defaultValue = "defaultValue";
        first.setProperty(propertyName, propertyValue);

        // WHEN
        String res = NodeUtil.getStringIfPossible(first, propertyName,defaultValue);

        // THEN
        assertEquals(defaultValue, res);
    }

    @Test
    public void testGetBooleanIfPossible() throws RepositoryException {
        // GIVEN
        boolean defaultValue = false;
        boolean propertyValue = true;
        String propertyName = "propertyName";
        first.setProperty(propertyName, propertyValue);

        // WHEN
        boolean res = NodeUtil.getBooleanIfPossible(first, propertyName,defaultValue);

        // THEN
        assertEquals(propertyValue, res);
    }

    @Test
    public void testGetBooleanIfPossibleBadNodeName() throws RepositoryException {
        // GIVEN
        boolean defaultValue = false;
        boolean propertyValue = true;
        String propertyName = "propertyName";
        first.setProperty(propertyName, propertyValue);

        // WHEN
        boolean res = NodeUtil.getBooleanIfPossible(first, propertyName+1,defaultValue);

        // THEN
        assertEquals(defaultValue, res);
    }

    @Test
    public void testGetBooleanIfPossibleBadNodeType() throws RepositoryException {
        // GIVEN
        boolean defaultValue = false;
        BigDecimal propertyValue = BigDecimal.ONE;
        String propertyName = "propertyName";
        first.setProperty(propertyName, propertyValue);

        // WHEN
        boolean res = NodeUtil.getBooleanIfPossible(first, propertyName,defaultValue);

        // THEN
        assertEquals(defaultValue, res);
    }


}
