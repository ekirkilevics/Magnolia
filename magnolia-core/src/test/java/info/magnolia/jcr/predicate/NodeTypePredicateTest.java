/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.jcr.predicate;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Test;

import info.magnolia.test.mock.jcr.MockNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test case for NodeTypePredicate.
 */
public class NodeTypePredicateTest {

    @Test
    public void testEvaluate() {
        NodeTypePredicate predicate = new NodeTypePredicate("bogusNodeType");
        MockNode node1 = new MockNode();
        node1.setPrimaryType("bogusNodeType");
        MockNode node2 = new MockNode();
        node2.setPrimaryType("anotherNodeType");

        assertTrue(predicate.evaluate(node1));
        assertFalse(predicate.evaluate(node2));
    }

    @Test
    public void testEvaluateReturnsFalseOnException() {
        NodeTypePredicate predicate = new NodeTypePredicate("bogusNodeType");
        Node node = mock(Node.class);
        try {
            when(node.getProperty(anyString())).thenThrow(new RepositoryException());
        } catch (RepositoryException e) {
            fail();
        }

        assertFalse(predicate.evaluate(node));
    }

    @Test
    public void testToString() {
        NodeTypePredicate predicate = new NodeTypePredicate("bogusNodeType");
        assertEquals("NodeTypePredicate for type bogusNodeType", predicate.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        NodeTypePredicate predicate1 = new NodeTypePredicate("bogusNodeType");
        NodeTypePredicate predicate2 = new NodeTypePredicate("bogusNodeType");
        NodeTypePredicate predicate3 = new NodeTypePredicate("anotherNodeType");

        assertTrue(predicate1.equals(predicate1));
        assertEquals(predicate1.hashCode(), predicate2.hashCode());

        assertTrue(predicate1.equals(predicate2));
        assertEquals(predicate1.hashCode(), predicate2.hashCode());

        assertFalse(predicate1.equals(predicate3));
        assertFalse(predicate1.hashCode() == predicate3.hashCode());

        assertFalse(predicate1.equals(null));
        assertFalse(predicate1.equals("string"));
    }
}
