/**
 * This file Copyright (c) 2012-2012 Magnolia International
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
package info.magnolia.jcr.wrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;

/**
 * @version $Id$
 */
public class ExtendingNodeWrapperTest {

    private Session session;

    @Test
    public void testExtendsPropertyIsHidden() throws RepositoryException, IOException{
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/bla/bla.prop2=world\n" +
                "/impl/node.prop1=hello\n" +
                "/impl/node.extends=../../bla/bla\n"
                );

        Node plainNode = session.getNode("impl/node");

        // WHEN
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // THEN
        assertFalse(extendedNode.hasProperty(ExtendingNodeWrapper.EXTENDING_NODE_PROPERTY));

        // WHEN
        try {
            extendedNode.getProperty(ExtendingNodeWrapper.EXTENDING_NODE_PROPERTY);
            fail("Should not get there.");

        // THEN
        } catch (PathNotFoundException e) {
            assertEquals("Cannont access property [/impl/node.extends]", e.getMessage());
        }

        // WHEN
        PropertyIterator it = extendedNode.getProperties();

        // THEN
        while(it.hasNext()) {
            Property prop = (Property) it.next();
            if(prop.getName().equals(ExtendingNodeWrapper.EXTENDING_NODE_PROPERTY)) {
                fail("Found extends property that is supposed to be hidden.");
            }
        }
    }

    @Test
    public void testNodePropertiesAreMerged() throws RepositoryException, IOException{
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/bla/bla.prop2=world\n" +
                "/impl/node.prop1=hello\n" +
                "/impl/node.extends=../../bla/bla\n"
                );

        Node plainNode = session.getNode("impl/node");

        // WHEN
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // THEN
        assertTrue(extendedNode.hasProperty("prop1"));
        assertTrue(extendedNode.hasProperty("prop2"));
        assertFalse(extendedNode.hasProperty("nonexistingProperty"));

        assertEquals("hello", extendedNode.getProperty("prop1").getString());
        assertEquals("world", extendedNode.getProperty("prop2").getString());

        assertEquals(2, extendedNode.getProperties().getSize());
    }

    @Test
    public void testPropertiesCanBeOverriden() throws IOException, RepositoryException{
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/bla/bla.prop1=default\n" +
                "/impl/node.prop1=new\n" +
                "/impl/node.extends=../../bla/bla\n"
                );

        Node plainNode = session.getNode("impl/node");

        // WHEN
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // THEN
        assertEquals("new", extendedNode.getProperty("prop1").getString());
        assertEquals(1, extendedNode.getProperties().getSize());
    }

    @Test
    public void testThatSubNodesAreMerged() throws IOException, RepositoryException{
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/bla/bla/subnode1\n" +
                "/impl/node/subnode2\n" +
                "/impl/node.extends=../../bla/bla\n"
                );

        Node plainNode = session.getNode("impl/node");

        // WHEN
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // THEN
        assertTrue(extendedNode.hasNode("subnode1"));
        assertTrue(extendedNode.hasNode("subnode2"));

        assertNotNull(extendedNode.getNode("subnode1"));
        assertNotNull(extendedNode.getNode("subnode2"));

        assertEquals(2, extendedNode.getNodes().getSize());
    }

    @Test
    public void testThatSubNodesCanBeOverwritten() throws IOException, RepositoryException{
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/bla/bla/subnode.@uuid=1\n" +
                "/impl/node/subnode.@uuid=2\n" +
                "/impl/node.extends=../../bla/bla\n"
                );

        Node plainNode = session.getNode("impl/node");

        // WHEN
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // THEN
        assertTrue(extendedNode.hasNode("subnode"));

        assertEquals("2", extendedNode.getNode("subnode").getIdentifier());
        assertEquals(1, extendedNode.getNodes().getSize());
    }

    @Test
    public void testDeepMerge() throws IOException, RepositoryException{
        // GIVEN
        String [][] expected = new String[][]{
                {"nodeData1","org1"}, // inherited
                {"nodeData2","org2.2"}, // overwritten
                {"nodeData3","org3"}, // added
        };
        session = SessionTestUtil.createSession("test",
                "/base/node/subnode.nodeData1=org1\n" +
                "/base/node/subnode.nodeData2=org2.1\n" +
                "/impl/node.extends=../../base/node\n" +
                "/impl/node/subnode.nodeData2=org2.2\n" +
                "/impl/node/subnode.nodeData3=org3"
                );

        Node plainNode = session.getNode("/impl/node");
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // WHEN
        Node subnode = extendedNode.getNode("subnode");

        // THEN
        for (String[] pair : expected) {
            String name = pair[0];
            String value = pair[1];
            assertTrue(subnode.hasProperty(name));
            assertEquals(value, subnode.getProperty(name).getString());
        }

        // WHEN
        PropertyIterator it = subnode.getProperties();

        // THEN
        assertEquals(3, it.getSize());

        int pos = 0;
        while(it.hasNext()) {
            Property prop = (Property) it.next();
            assertEquals(expected[pos][1], prop.getString());
            pos++;
        }
    }

    @Test
    public void testOrderIsKeptWhileMergingSubNodes() throws IOException, RepositoryException{
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/base/node/subnode1.@uuid=1\n" +
                "/base/node/subnode2.@uuid=2.1\n" +
                "/base/node/subnode3.@uuid=3\n" +
                "/impl/node.extends=../../base/node\n" +
                "/impl/node/subnode2.@uuid=2.2\n" +
                "/impl/node/subnode4.@uuid=4"
                );

        Node plainNode = session.getNode("/impl/node");

        // WHEN
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // THEN
        assertTrue(extendedNode.hasNode("subnode1"));
        assertTrue(extendedNode.hasNode("subnode2"));
        assertTrue(extendedNode.hasNode("subnode3"));
        assertTrue(extendedNode.hasNode("subnode4"));

        assertEquals("1", extendedNode.getNode("subnode1").getIdentifier());
        assertEquals("2.2", extendedNode.getNode("subnode2").getIdentifier());
        assertEquals("3", extendedNode.getNode("subnode3").getIdentifier());
        assertEquals("4", extendedNode.getNode("subnode4").getIdentifier());

        // WHEN
        NodeIterator it = extendedNode.getNodes();

        // THEN
        assertEquals(4, it.getSize());

        int i = 1;
        while (it.hasNext()) {
            Node child = (Node) it.next();
            assertEquals("subnode" + i, child.getName());
            i++;
        }
    }

    @Test
    public void testBasicMultipleInheritance() throws IOException, RepositoryException{
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/superbase/node.nodeData1=org1\n" +
                "/base/node.extends=../../superbase/node\n" +
                "/base/node.nodeData2=org2\n" +
                "/impl/node.extends=../../base/node\n" +
                "/impl/node.nodeData3=org3"
                );

        Node plainNode = session.getNode("/impl/node");

        // WHEN
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // THEN
        assertTrue(extendedNode.hasProperty("nodeData1"));
        assertTrue(extendedNode.hasProperty("nodeData2"));
        assertTrue(extendedNode.hasProperty("nodeData3"));

        assertEquals("org1", extendedNode.getProperty("nodeData1").getString());
        assertEquals("org2", extendedNode.getProperty("nodeData2").getString());
        assertEquals("org3", extendedNode.getProperty("nodeData3").getString());

        assertEquals(3, extendedNode.getProperties().getSize());
    }

    @Test
    public void testComplextMultipleInheritance1() throws IOException, RepositoryException{
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/superbase.nodeData1=org1\n" +
                "/base/node/subnode.nodeData2=org2\n" +
                "/base/node/subnode.extends=/superbase\n" +
                "/impl/node.extends=/base/node\n" +
                "/impl/node/subnode.nodeData3=org3"
                );

        Node plainNode = session.getNode("/impl/node");
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // WHEN
        Node subnode = extendedNode.getNode("subnode");

        // THEN
        assertTrue(subnode.hasProperty("nodeData1"));
        assertTrue(subnode.hasProperty("nodeData2"));
        assertTrue(subnode.hasProperty("nodeData3"));

        assertEquals("org1", subnode.getProperty("nodeData1").getString());
        assertEquals("org2", subnode.getProperty("nodeData2").getString());
        assertEquals("org3", subnode.getProperty("nodeData3").getString());

        assertEquals(3, subnode.getProperties().getSize());
    }

    @Test
    public void testComplextMultipleInheritance2() throws IOException, RepositoryException{
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/superbase.nodeData1=org1\n" +
                "/base/node/subnode.nodeData2=org2\n" +
                "/impl/node.extends=/base/node\n" +
                "/impl/node/subnode.extends=/superbase\n" +
                "/impl/node/subnode.nodeData3=org3\n"
                );

        Node plainNode = session.getNode("/impl/node");
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // WHEN
        Node subnode = extendedNode.getNode("subnode");

        // THEN
        assertTrue(subnode.hasProperty("nodeData1"));
        assertTrue(subnode.hasProperty("nodeData2"));
        assertTrue(subnode.hasProperty("nodeData3"));

        assertEquals("org1", subnode.getProperty("nodeData1").getString());
        assertEquals("org2", subnode.getProperty("nodeData2").getString());
        assertEquals("org3", subnode.getProperty("nodeData3").getString());

        assertEquals(3, subnode.getProperties().getSize());
    }

    @Test
    public void testComplextMultipleInheritanceWithOverride() throws IOException, RepositoryException{
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/superbase.nodeData1=org1\n" +
                "/superbase/uglyChild/withSubChild.nodeDataX=over1\n" +
                "/base/node/subnode.nodeData2=org2\n" +
                "/impl/node.extends=/base/node\n" +
                "/impl/node/subnode.extends=/superbase\n" +
                "/impl/node/subnode/uglyChild.extends=override\n" +
                "/impl/node/subnode.nodeData3=org3"
                );

        Node plainNode = session.getNode("/impl/node");
        Node extendedNode = new ExtendingNodeWrapper(plainNode);

        // WHEN
        Node subnode = extendedNode.getNode("subnode");

        // THEN
        // inherited from superbase:
        assertTrue(subnode.hasProperty("nodeData1"));
        // inherited from base/node:/subnode
        assertTrue(subnode.hasProperty("nodeData2"));
        // subnode's own Node
        assertTrue(subnode.hasProperty("nodeData3"));

        // WHEN
        // override defining node
        Node disinheritedNode = subnode.getNode("uglyChild");

        // THEN
        // we hide extends now
        assertFalse(disinheritedNode.hasProperty(ExtendingNodeWrapper.EXTENDING_NODE_PROPERTY));
        // superbase child node should not be inherited because of override
        assertFalse(disinheritedNode.hasNode("withSubChild"));

        assertEquals("org2", subnode.getProperty("nodeData2").getString());
        assertEquals("org3", subnode.getProperty("nodeData3").getString());

        assertEquals(3, subnode.getProperties().getSize());
    }

    @Test
    public void testExtendsNonAbsolutelyAndNodeIsNotExisting() throws IOException, RepositoryException {
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/superbase.nodeData1=org1\n" +
                "/superbase/uglyChild.nodeDataX=over1\n" +
                "/impl/node.extends=/superbase\n" +
                "/impl/node2.extends=../../superbase/uglyChild\n" +
                "/impl/node3.extends=../../superbase/wrongNode"
                );

        try {
            Node plainNode = session.getNode("/impl/node3");

            // WHEN
            new ExtendingNodeWrapper(plainNode, true);
            fail("Must never get here!");

        // THEN
        } catch (RuntimeException e) {
            assertEquals("Can't find referenced node for value: MockNode [primaryType=mgnl:contentNode, name=node3]", e.getMessage());
        }
    }

    @Test
    public void testExtendsWithEmptyValue() throws Exception {
        // GIVEN
        session = SessionTestUtil.createSession("test", "/impl/node\n");
        Node plainNode = session.getNode("/impl/node");
        plainNode.setProperty(ExtendingNodeWrapper.EXTENDING_NODE_PROPERTY, " ");

        // WHEN
        ExtendingNodeWrapper extendedNode = new ExtendingNodeWrapper(plainNode);

        // THEN
        assertFalse(extendedNode.isExtending());
    }

    @Test
    public void testExtendsAbsolutelyAndNodeIsNotExisting() throws IOException, RepositoryException {
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/impl/node.extends=/base/node\n" +
                "/impl/node.nodeData2=org2"
                );

        Node plainNode = session.getNode("/impl/node");

        // WHEN
        try {
            new ExtendingNodeWrapper(plainNode, true);
            fail("should never get here...");

        // THEN
        } catch (RuntimeException e) {
            assertEquals("Can't find referenced node for value: MockNode [primaryType=mgnl:contentNode, name=node]", e.getMessage());
        }
    }

    @Test
    public void testGetPath() throws RepositoryException {
        // GIVEN
        MockNode plainNode = new MockNode("impl");
        Node subNode = plainNode.addNode("node");
        Node extendedNode = new ExtendingNodeWrapper(subNode);

        // WHEN
        final String result = extendedNode.getPath();

        // THEN
        assertEquals("/impl/node", result);
    }

    @Test
    public void testGetPropertiesByNamePattern() throws IOException, RepositoryException {
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/superbase/node.nodeData1=org1\n" +
                "/base/node.extends=../../superbase/node\n" +
                "/base/node.nodeData2=org2\n" +
                "/impl/node.extends=../../base/node\n" +
                "/impl/node.nodeData3=org3"
                );

        Node plainNode = session.getNode("/impl/node");
        Node extendedNode = new ExtendingNodeWrapper(plainNode);
        List<String> propertyNames = new ArrayList<String>();
        propertyNames.add("nodeData1");
        propertyNames.add("nodeData2");
        propertyNames.add("nodeData3");

        // WHEN
        PropertyIterator it = extendedNode.getProperties("node*");

        // THEN
        assertEquals(3, it.getSize());

        while (it.hasNext()) {
            Property prop = (Property) it.next();
            assertTrue(propertyNames.contains(prop.getName()));
        }

        // again but pass array instead of string
        // WHEN
        it = extendedNode.getProperties(new String[] {"node*"});

        // THEN
        assertEquals(3, it.getSize());

        while (it.hasNext()) {
            Property prop = (Property) it.next();
            assertTrue(propertyNames.contains(prop.getName()));
        }
    }

    @Test
    public void testGetNodesByNamePattern() throws IOException, RepositoryException {
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/base/node/subnode1\n" +
                "/base/node/subnode2\n" +
                "/impl/node.extends=/base/node\n" +
                "/impl/node/subnode2\n" +
                "/impl/node/newnode"
                );

        Node plainNode = session.getNode("/impl/node");
        Node extendedNode = new ExtendingNodeWrapper(plainNode);
        List<String> nodeNames = new ArrayList<String>();
        nodeNames.add("subnode1");
        nodeNames.add("subnode2");
        nodeNames.add("newnode");

        // WHEN
        NodeIterator it = extendedNode.getNodes("sub* | newnode");

        // THEN
        assertEquals(3, it.getSize());

        int i = 0;
        while (it.hasNext()) {
            Node node = (Node) it.next();
            assertEquals(nodeNames.get(i), node.getName());
            i++;
        }

        // again but pass array instead of string
        // WHEN
        it = extendedNode.getNodes(new String[] {"sub*", "newnode"});

        // THEN
        assertEquals(3, it.getSize());

        i = 0;
        while (it.hasNext()) {
            Node node = (Node) it.next();
            assertEquals(nodeNames.get(i), node.getName());
            i++;
        }
    }

    @Test
    public void testSubNodesAreWrapped() throws RepositoryException, IOException {
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/parent.string=Hello\n" +
                "/parent.integer=10\n" +
                "/parent/beans/sub1.string=foo\n" +
                "/parent/beans/sub2.string=bar\n" +
                "/parent/beans/sub3.string=baz\n" +
                "/sub/bean.string=World\n" +
                "/sub/bean.integer=999\n" +
                "/sub/bean.extends=/parent\n" +
                "/another/sub/bean.extends=../../../sub/bean\n" +
                "/another/sub/bean/beans/sub3.string=bla\n" +
                "/another/sub/bean/beans/sub4.string=blah\n"
                );
        ExtendingNodeWrapper node = new ExtendingNodeWrapper(session.getNode("/another/sub/bean"));

        // WHEN
        List<Node> nodes = (List<Node>) NodeUtil.getSortedCollectionFromNodeIterator(node.getNode("beans").getNodes());

        // THEN
        assertEquals(4, nodes.size());

        for (Node n : nodes) {
            assertTrue(n instanceof ExtendingNodeWrapper);
        }

        assertEquals("foo", nodes.get(0).getProperty("string").getString());
        assertEquals("bar", nodes.get(1).getProperty("string").getString());
        assertEquals("bla", nodes.get(2).getProperty("string").getString());
        assertEquals("blah", nodes.get(3).getProperty("string").getString());
    }

    @Test
    public void testNodeCannotSelfExtend() throws IOException, RepositoryException {
        // GIVEN
        session = SessionTestUtil.createSession("test",
                "/parent.extends=../parent\n"
                );

        // WHEN
        ExtendingNodeWrapper node = new ExtendingNodeWrapper(session.getNode("/parent"));

        // THEN
        assertFalse(node.isExtending());
    }
}
