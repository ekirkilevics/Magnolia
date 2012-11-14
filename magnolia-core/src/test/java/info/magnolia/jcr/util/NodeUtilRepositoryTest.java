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

import static org.junit.Assert.assertEquals;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.MgnlPropertyNames;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import sun.util.BuddhistCalendar;

/**
 * NodeUtil test class used to test methods that needs repository definition.
 */
public class NodeUtilRepositoryTest extends RepositoryTestCase {

    @Test
    public void testMoveNode() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/nodeToMove.@type=mgnl:content\n" +
            "/nodeToMove.propertyString=hello\n" +
            "/nodeToMove/child1.@type=mgnl:content\n" +
            "/nodeToMove/child1.propertyString=sourceChild1\n" +
            "/dest.@type=mgnl:content\n" +
            "/newParent.propertyString=dest\n" +
            "/newParent/child2.@type=mgnl:content\n" +
            "/newParent/child2.propertyString=newParentChild2\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node nodeToMove = rootNode.getNode("nodeToMove");
        Node newParent = rootNode.getNode("newParent");

        // WHEN
        NodeUtil.moveNode(nodeToMove, newParent);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("nodeToMove"), false);
        assertNodeExistWithProperty(rootNode, "newParent/child2", "propertyString", "newParentChild2");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove", "propertyString", "hello");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove/child1", "propertyString", "sourceChild1");
    }

    @Test
    public void testMoveNodeBefore() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/nodeToMove.@type=mgnl:content\n" +
            "/nodeToMove.propertyString=hello\n" +
            "/nodeToMove/child1.@type=mgnl:content\n" +
            "/nodeToMove/child1.propertyString=sourceChild1\n" +
            "/dest.@type=mgnl:content\n" +
            "/newParent.propertyString=dest\n" +
            "/newParent/child2.@type=mgnl:content\n" +
            "/newParent/child2.propertyString=newParentChild2\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node nodeToMove = rootNode.getNode("nodeToMove");
        Node newParent = rootNode.getNode("newParent/child2");

        // WHEN
        NodeUtil.moveNodeBefore(nodeToMove, newParent);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("nodeToMove"), false);
        assertNodeExistWithProperty(rootNode, "newParent/child2", "propertyString", "newParentChild2");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove", "propertyString", "hello");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove/child1", "propertyString", "sourceChild1");

        List<Node> children = NodeUtil.asList(NodeUtil.collectAllChildren(rootNode.getNode("newParent"),NodeUtil.EXCLUDE_META_DATA_FILTER));
        assertEquals("Should be first ", children.get(0).getProperty("propertyString").getString(), "hello");
        assertEquals("Should be second ", children.get(1).getProperty("propertyString").getString(), "newParentChild2");
    }


    @Test
    public void testMoveNodeAfter() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/nodeToMove.@type=mgnl:content\n" +
            "/nodeToMove.propertyString=hello\n" +
            "/nodeToMove/child1.@type=mgnl:content\n" +
            "/nodeToMove/child1.propertyString=sourceChild1\n" +
            "/dest.@type=mgnl:content\n" +
            "/newParent.propertyString=dest\n" +
            "/newParent/child2.@type=mgnl:content\n" +
            "/newParent/child2.propertyString=newParentChild2\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node nodeToMove = rootNode.getNode("nodeToMove");
        Node newParent = rootNode.getNode("newParent/child2");

        // WHEN
        NodeUtil.moveNodeAfter(nodeToMove, newParent);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("nodeToMove"), false);
        assertNodeExistWithProperty(rootNode, "newParent/child2", "propertyString", "newParentChild2");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove", "propertyString", "hello");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove/child1", "propertyString", "sourceChild1");

        List<Node> children = NodeUtil.asList(NodeUtil.collectAllChildren(rootNode.getNode("newParent"),NodeUtil.EXCLUDE_META_DATA_FILTER));
        assertEquals("Should be first ",children.get(0).getProperty("propertyString").getString(), "newParentChild2");
        assertEquals("Should be second ",children.get(1).getProperty("propertyString").getString(), "hello");
    }


    @Test
    public void testRenameNode() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/nodeToRename.@type=mgnl:content\n" +
            "/nodeToRename.propertyString=hello\n" +
            "/nodeToRename/child1.@type=mgnl:content\n" +
            "/nodeToRename/child1.propertyString=sourceChild1\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node nodeToRename = rootNode.getNode("nodeToRename");

        // WHEN
        NodeUtil.renameNode(nodeToRename, "newName");

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("nodeToRename"), false);
        assertNodeExistWithProperty(rootNode, "newName", "propertyString", "hello");
        assertNodeExistWithProperty(rootNode, "newName/child1", "propertyString", "sourceChild1");
    }


    @Test
    public void testCalendar() throws Exception {
        // GIVEN
        String nodeProperties = "/node.@type=mgnl:content";

        Node rootNode = addNodeToRoot(nodeProperties);
        final Calendar now = new BuddhistCalendar(TimeZone.getDefault());
        rootNode.setProperty(MgnlPropertyNames.LAST_MODIFIED, now);

        // WHEN
        final Calendar result = NodeUtil.getLastModified(rootNode);

        // THEN
        assertEquals(now.getTimeInMillis(), result.getTimeInMillis());


    }

    /**
     * Common check.
     */
    private void assertNodeExistWithProperty(Node rootNode, String nodePathToTest, String propertyName, String propertyValue) throws RepositoryException {
        assertEquals(nodePathToTest+" should have been moved ",rootNode.hasNode(nodePathToTest), true);
        assertEquals(nodePathToTest+" should have property "+propertyName,rootNode.getNode(nodePathToTest).hasProperty(propertyName), true);
        assertEquals(nodePathToTest+" should have property "+propertyName+" with the same value "+propertyValue,(rootNode.getNode(nodePathToTest).getProperty(propertyName)).getValue().getString().equals(propertyValue), true);
    }

    /**
     * Add Nodes to the rootNode.
     */
    private Node addNodeToRoot(String nodeProperties) throws IOException, RepositoryException {
        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        new PropertiesImportExport().createNodes(session.getRootNode(), IOUtils.toInputStream(nodeProperties));
        session.save();
        Node node = session.getRootNode();
        return node;
    }

}
