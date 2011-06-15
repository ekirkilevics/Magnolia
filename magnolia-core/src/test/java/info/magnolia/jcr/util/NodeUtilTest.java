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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.version.VersionedNode;
import info.magnolia.test.mock.jcr.MockNode;

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

    public static final String ROOT = "root";
    private static final String FIRST_CHILD = "1";
    private static final String SECOND_CHILD = "2";
    private static final String THIRD_CHILD = "3";

    private MockNode root;
    private Node first;
    private Node second;
    private Node third;

    @Before
    public void setUpTestStructure() throws RepositoryException {
        root = new MockNode(ROOT);
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

    @Test(expected=IllegalArgumentException.class)
    public void testHasMixinFailsWithEmptyMixin() throws Exception {
        NodeUtil.hasMixin(root, null);
    }

    @Test
    public void testUnwrap() throws Exception {
        final Version version = mock(Version.class);
        when(version.getNode(ItemType.JCR_FROZENNODE)).thenReturn(root);
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
        NodeUtil.visit(root, new NodeVisitor(){
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
        NodeUtil.visit(root, new PostNodeVisitor(){
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
        assertEquals(ROOT, names.get(3));
    }

    @Test
    public void testGetChildren() throws RepositoryException {
        Node parent = new MockNode("");
        parent.addNode("alpha", MgnlNodeType.NT_CONTENT);
        parent.addNode("beta", MgnlNodeType.NT_CONTENTNODE);
        parent.addNode("gamma", MgnlNodeType.NT_CONTENTNODE);
        parent.addNode("metaData", MgnlNodeType.NT_METADATA);
        parent.addNode("jcr:system", MgnlNodeType.NT_FILE);

        assertEquals(1, NodeUtil.getChildren(parent, MgnlNodeType.NT_CONTENT).size());
        assertEquals(2, NodeUtil.getChildren(parent, MgnlNodeType.NT_CONTENTNODE).size());
        assertEquals(3, NodeUtil.getChildren(parent).size());

        assertEquals(4, NodeUtil.getChildren(parent, NodeUtil.ALL_NODES_EXCEPT_JCR_FILTER).size());
        assertEquals(5, NodeUtil.getChildren(parent, NodeUtil.ALL_NODES_FILTER).size());
        assertEquals(4, NodeUtil.getChildren(parent, NodeUtil.MAGNOLIA_FILTER).size());
    }
}
