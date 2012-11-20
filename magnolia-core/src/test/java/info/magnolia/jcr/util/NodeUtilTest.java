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
package info.magnolia.jcr.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.version.VersionedNode;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
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
        final VersionedNode wrapper = new VersionedNode(version, root);

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
        // root's name is ""
        assertEquals("", names.get(3));
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
    public void testGetPathIfPossible() {
        // GIVEN

        // WHEN
        String res = NodeUtil.getPathIfPossible(first);

        // THEN
        assertEquals("Should be /testNode  ", "/" + FIRST_CHILD, res);
    }

    @Test
    public void testCollectAllChildren_DefaultPredicate_Simple() throws RepositoryException {
        // GIVEN

        // WHEN
        Iterable<Node> res = NodeUtil.collectAllChildren(root);

        // THEN
        List<Node> resAsList = NodeUtil.asList(res);
        assertEquals("Should have 3 nodes  ", 3 , resAsList.size());
    }

    @Test
    public void testCollectAllChildren_DefaultPredicate_FilterType() throws RepositoryException {
        // GIVEN
        second.setPrimaryType("toto:data");

        // WHEN
        Iterable<Node> res = NodeUtil.collectAllChildren(root);

        // THEN
        List<Node> resAsList = NodeUtil.asList(res);
        assertEquals("Should have 2 nodes  ", 2 , resAsList.size());
    }

    @Test
    public void testCollectAllChildren_DefaultPredicate_FilterType_ThreeLevel() throws RepositoryException {
        // GIVEN
        Node secondL11 = second.addNode("L11");
        Node secondL12 = second.addNode("L12");
        secondL11.addNode("L111");
        Node secondL112 = secondL11.addNode("L112");
        secondL12.addNode("L121");

        second.setPrimaryType("toto:data");
        secondL12.setPrimaryType("toto:data");
        secondL112.setPrimaryType("toto:data");
        // WHEN
        Iterable<Node> res = NodeUtil.collectAllChildren(root);

        // THEN
        List<Node> resAsList = NodeUtil.asList(res);
        assertEquals("Should have 5 nodes  ", 5 , resAsList.size());
    }

    @Test
    public void testCollectAllChildren_SpecificPredicate_FilterType_ThreeLevel() throws RepositoryException {
        // GIVEN
        Node secondL11 = second.addNode("L11");
        Node secondL12 = second.addNode("L12");
        secondL11.addNode("L111");
        Node secondL112 = secondL11.addNode("L112");
        secondL12.addNode("L121");

        second.setPrimaryType("toto:data");
        secondL12.setPrimaryType("toto:data");
        secondL112.setPrimaryType("toto:data");

        AbstractPredicate<Node> predicate =new AbstractPredicate<Node>() {
            @Override
            public boolean evaluateTyped(Node node) {
                try {
                    return node.getPrimaryNodeType().getName().equals("toto:data");
                } catch (RepositoryException e) {
                    return false;
                }
            }
        };


        // WHEN
        Iterable<Node> res = NodeUtil.collectAllChildren(root,predicate);

        // THEN
        List<Node> resAsList = NodeUtil.asList(res);
        assertEquals("Should have 3 nodes  ", 3 , resAsList.size());
    }


    @Test
    public void testGetAncestors_Level0() throws RepositoryException {
        // GIVEN

        // WHEN
        Collection<Node> res = NodeUtil.getAncestors(root);

        // THEN
        List<Node> resAsList = NodeUtil.asList(res);
        assertEquals("Should have no nodes  ", 0, resAsList.size());
    }

    @Test
    public void testGetAncestors_Level3() throws RepositoryException {
        // GIVEN
        Node subFirst = first.addNode("subFirst");

        // WHEN
        Collection<Node> res = NodeUtil.getAncestors(subFirst);

        // THEN
        List<Node> resAsList = NodeUtil.asList(res);
        assertEquals("Should have 2 nodes  ", 2, resAsList.size());
    }

    @Test
    public void testAreSiblingsTrue() throws RepositoryException {
        // GIVEN
        Node subFirst1 = first.addNode("subFirst1");
        Node subFirst2 = first.addNode("subFirst1");

        // WHEN
        boolean areSiblings  = NodeUtil.isSameNameSiblings(subFirst1, subFirst2);

        // THEN
        assertEquals("Should be Siblings  ", true, areSiblings);
    }

    @Test
    public void testAreSiblingsFalse() throws RepositoryException {
        // GIVEN
        Node subFirst1 = first.addNode("subFirst1");
        Node subFirst2 = first.addNode("subFirst2");

        // WHEN
        boolean areSiblings  = NodeUtil.isSameNameSiblings(subFirst1, subFirst2);

        // THEN
        assertEquals("Should not be Siblings  ", false, areSiblings);
    }
}
