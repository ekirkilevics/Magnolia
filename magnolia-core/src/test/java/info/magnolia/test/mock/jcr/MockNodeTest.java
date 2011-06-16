/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class MockNodeTest {

    private MockNode root;

    @Before
    public void init() {
        root = new MockNode("root");
    }

    @Test
    public void testConstructionFromNamePropertiesAndChildren() throws Exception {
        Map<String, MockValue> properties = new LinkedHashMap<String, MockValue>();
        Map<String, MockNode> children = new LinkedHashMap<String, MockNode>();
        final String nodeName = "name";
        final MockNode node = new MockNode(nodeName, properties, children);
        assertTrue(!node.hasProperties());
        assertTrue(!node.hasNodes());

        // now add property
        MockValue sampleValue = new MockValue("test");
        final String propertyName = "property";
        properties.put(propertyName, sampleValue);
        final MockNode nodeWithProperty = new MockNode(nodeName, properties, children);
        assertTrue(!nodeWithProperty.hasNodes());
        assertTrue(nodeWithProperty.hasProperties());
        assertEquals(sampleValue, nodeWithProperty.getProperty(propertyName).getValue());

        // and finally a child
        final String childName = "childOne";
        final MockNode child = new MockNode(childName);
        children.put(childName, child);

        final MockNode nodeWithPropertyAndChild = new MockNode(nodeName, properties, children);
        assertTrue(nodeWithPropertyAndChild.hasProperties());
        assertEquals(sampleValue, nodeWithPropertyAndChild.getProperty(propertyName).getValue());
        assertTrue(nodeWithPropertyAndChild.hasNodes());
        assertEquals(child, nodeWithPropertyAndChild.getNode(childName));
    }

    @Test
    public void testAddNodeWithParamFakeJCRNode() throws Exception {
        final MockNode child = new MockNode("child");
        root.addNode(child);

        assertEquals(root, child.getParent());
        assertEquals(root.getChildren().get("child"), child);
    }

    @Test
    public void testAddNodeWithParamString() throws Exception {
        final MockNode child = (MockNode) root.addNode("child");

        assertEquals(root, child.getParent());
        assertEquals(root.getChildren().get("child"), child);
    }

    @Test
    public void testAddNodeWithParamStringString() throws Exception {
        final MockNode child = (MockNode) root.addNode("child", "primaryNodeTypeName");

        assertEquals(root, child.getParent());
        assertEquals(root.getChildren().get("child"), child);
        assertEquals("primaryNodeTypeName", child.getPrimaryNodeType().getName());
    }

    @Test
    public void testGetNodeWithExistingPath() throws Exception {
        final MockNode child = (MockNode) root.addNode("child");
        final MockItem childOfChild = (MockItem) child.addNode("childOfChild");

        assertEquals(child, root.getNode("child"));
        assertEquals(childOfChild, root.getNode("child/childOfChild"));
    }

    @Test(expected = PathNotFoundException.class)
    public void testGetNodeWithFalsePath() throws Exception {
        root.getNode("does/not/exist");
    }

    @Test
    public void testHasNode() throws Exception {
        final MockNode child = (MockNode) root.addNode("child");
        child.addNode("childOfChild");

        assertTrue(root.hasNode("child/childOfChild"));
        assertTrue(!root.hasNode("childOfChild"));
        assertTrue(!root.hasNode("does/not/exist"));
    }

    @Test
    public void testHasNodes() throws Exception {
        final MockNode child = new MockNode("child");
        root.addNode(child);

        assertTrue(root.hasNodes());
        assertTrue(!child.hasNodes());
    }

    @Test
    public void testHasProperties() throws Exception {
        assertTrue(!root.hasProperties());

        root.setProperty("property", "string");
        assertTrue(root.hasProperties());
    }

    @Test
    public void testGetProperties() throws Exception {
        root.setProperty("property1", "string");
        root.setProperty("property2", BigDecimal.TEN);

        PropertyIterator iterator = root.getProperties();
        assertTrue(iterator.hasNext());
        Property current = iterator.nextProperty();
        assertEquals("property1", current.getName());
        assertEquals("string", current.getString());

        current = iterator.nextProperty();
        assertEquals("property2", current.getName());
        assertEquals(BigDecimal.TEN, current.getDecimal());

        assertTrue(!iterator.hasNext());
    }


    @Test
    public void testSetPropertyWithStringAndBoolean() throws Exception {
        root.setProperty("boolean", false);

        assertEquals(false, root.getProperty("boolean").getValue().getBoolean());
    }

    @Test
    public void testSetPropertyWithStringAndValue() throws Exception {
        final MockValue value = new MockValue("stringValue");
        root.setProperty("string", value);

        assertEquals(value, root.getProperty("string").getValue());
    }

    @Test
    public void testChildNodesAndPropertiesGetProperSession() throws Exception {
        MockSession session = new MockSession("test");

        MockNode child = (MockNode) session.getRootNode().addNode("child");
        MockProperty property = (MockProperty) child.setProperty("property", "propertyValue");

        assertEquals(session, child.getSession());
        assertEquals(session, property.getSession());
    }

    @Test
    public void testGetMixingNodeTypes() throws Exception {
        root.addMixin("mixin1");

        NodeType[] nodeTypes = root.getMixinNodeTypes();

        assertEquals(1, nodeTypes.length);
        assertEquals("mixin1", nodeTypes[0].getName());
    }

    @Test
    public void testOrderBeforeWithBothNamesValid() throws Exception {
        final String firstChild = "1";
        final String secondChild = "2";
        final String thirdChild = "3";

        final Node first = root.addNode(firstChild);
        final Node second = root.addNode(secondChild);
        final Node third = root.addNode(thirdChild);

        root.orderBefore(secondChild, firstChild);

        assertEquals(3, root.getChildren().values().size());
        Iterator<MockNode> orderedKids = root.getChildren().values().iterator();
        assertEquals(second, orderedKids.next());
        assertEquals(first, orderedKids.next());
        assertEquals(third, orderedKids.next());
    }

    @Test
    public void testOrderBeforeWithNullBeforeName() throws Exception {
        final String firstChild = "1";
        final String secondChild = "2";
        final String thirdChild = "3";

        final Node first = root.addNode(firstChild);
        final Node second = root.addNode(secondChild);
        final Node third = root.addNode(thirdChild);

        // should result in putting firstChild at the end of the children
        root.orderBefore(firstChild, null);

        Iterator<MockNode> orderedKids = root.getChildren().values().iterator();
        assertEquals(second, orderedKids.next());
        assertEquals(third, orderedKids.next());
        assertEquals(first, orderedKids.next());
    }

    @Test
    public void testAccept() throws RepositoryException{
        ItemVisitor visitor = mock(ItemVisitor.class);
        root.accept(visitor);

        verify(visitor).visit(root);
    }
}
