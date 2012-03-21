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
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import java.io.IOException;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
/**
 * NodeUtil test class used to test methods that needs repository definition.
 * @version $Id$
 */
public class NodeUtilRepositoryTest extends RepositoryTestCase {



    @Test
    public void testMoveAndMergeNodesCleanDestination() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n";
        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");
        Node dest = rootNode.getNode("dest");

        // WHEN
        NodeUtil.moveAndMergeNodes(source, dest.getPath(), false);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest/source", "propertyString", "hello");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1", "propertyString", "chield1");
        assertEquals("Should be moved ",rootNode.hasNode("dest/source/chield2"), true);
    }


    @Test(expected= RepositoryException.class)
    public void testMoveAndMergeNodesToSubPathCleanDestination() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n";
        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");
        Node dest = rootNode.getNode("dest");

        // WHEN
        NodeUtil.moveAndMergeNodes(source, dest.getPath()+"/temp", false);

        // THEN

    }

    @Test
    public void testMoveAndMergeNodesDestinationNoConflict() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n" +
            "/dest/chield3.@type=mgnl:content\n" +
            "/dest/chield3.propertyString=destChield3\n";
        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");
        Node dest = rootNode.getNode("dest");

        // WHEN
        NodeUtil.moveAndMergeNodes(source, dest.getPath(), false);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest/source", "propertyString", "hello");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1", "propertyString", "chield1");
        assertNodeExistWithProperty(rootNode, "dest/chield3", "propertyString", "destChield3");
        assertEquals("Should be moved ",rootNode.hasNode("dest/source/chield2"), true);
    }

    @Test
    public void testMoveAndMergeNodesDestinationNoConflictOnChildren() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n" +
            "/dest/source.@type=mgnl:content\n" +
            "/dest/source.propertyString=helloDest\n"+
            "/dest/source/chield3.@type=mgnl:content\n" +
            "/dest/source/chield3.propertyString=chield3\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");
        Node dest = rootNode.getNode("dest");

        // WHEN
        NodeUtil.moveAndMergeNodes(source, dest.getPath(), false);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest/source", "propertyString", "helloDest");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1", "propertyString", "chield1");
        assertNodeExistWithProperty(rootNode, "dest/source/chield3", "propertyString", "chield3");
        assertEquals("Should be moved ",rootNode.hasNode("dest/source/chield2"), true);
    }

    @Test
    public void testMoveAndMergeNodesDestinationNoConflictOnSubChildren() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n" +
            "/dest/source.@type=mgnl:content\n" +
            "/dest/source.propertyString=helloDest\n"+
            "/dest/source/chield3.@type=mgnl:content\n" +
            "/dest/source/chield3.propertyString=chield3\n"+
            "/dest/source/chield1.@type=mgnl:content\n" +
            "/dest/source/chield1.propertyString=DestChield1\n"+
            "/dest/source/chield1/subchield2.@type=mgnl:content\n" +
            "/dest/source/chield1/subchield2.propertyString=DestSubchield2\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");
        Node dest = rootNode.getNode("dest");

        // WHEN
        NodeUtil.moveAndMergeNodes(source, dest.getPath(), false);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest/source", "propertyString", "helloDest");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1", "propertyString", "DestChield1");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1/subchield1", "propertyString", "subchield1");
        assertNodeExistWithProperty(rootNode, "dest/source/chield3", "propertyString", "chield3");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1/subchield2", "propertyString", "DestSubchield2");
        assertEquals("Should be moved ",rootNode.hasNode("dest/source/chield2"), true);
    }


    @Test
    public void testMoveAndMergeNodesDestinationConflictOnSubChildrenNoOverride() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n" +
            "/dest/source.@type=mgnl:content\n" +
            "/dest/source.propertyString=helloDest\n"+
            "/dest/source/chield3.@type=mgnl:content\n" +
            "/dest/source/chield3.propertyString=chield3\n"+
            "/dest/source/chield1.@type=mgnl:content\n" +
            "/dest/source/chield1.propertyString=DestChield1\n"+
            "/dest/source/chield1/subchield1.@type=mgnl:content\n" +
            "/dest/source/chield1/subchield1.propertyString=DestSubchield2\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");
        Node dest = rootNode.getNode("dest");

        // WHEN
        NodeUtil.moveAndMergeNodes(source, dest.getPath(), false);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest/source", "propertyString", "helloDest");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1", "propertyString", "DestChield1");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1/subchield1", "propertyString", "DestSubchield2");
        assertNodeExistWithProperty(rootNode, "dest/source/chield3", "propertyString", "chield3");
        assertEquals("Should be moved ",rootNode.hasNode("dest/source/chield2"), true);

    }


    @Test
    public void testMoveAndMergeNodesDestinationConflictOnSubChildrenOverride() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n" +
            "/dest/source.@type=mgnl:content\n" +
            "/dest/source.propertyString=helloDest\n"+
            "/dest/source/chield3.@type=mgnl:content\n" +
            "/dest/source/chield3.propertyString=chield3\n"+
            "/dest/source/chield1.@type=mgnl:content\n" +
            "/dest/source/chield1.propertyString=DestChield1\n"+
            "/dest/source/chield1/subchield1.@type=mgnl:content\n" +
            "/dest/source/chield1/subchield1.propertyString=DestSubchield2\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");
        Node dest = rootNode.getNode("dest");

        // WHEN
        NodeUtil.moveAndMergeNodes(source, dest.getPath(), true);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest/source", "propertyString", "helloDest");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1", "propertyString", "DestChield1");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1/subchield1", "propertyString", "subchield1");
        assertEquals("Should be moved ",rootNode.hasNode("dest/source/chield2"), true);
        assertNodeExistWithProperty(rootNode, "dest/source/chield3", "propertyString", "chield3");
    }


    @Test
    public void testMoveAndMergeNodesDestinationConflictOnSubNodesDestOverride() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n" +
            "/dest/source.@type=mgnl:content\n" +
            "/dest/source.propertyString=helloDest\n"+
            "/dest/source/chield2.@type=mgnl:content\n" +
            "/dest/source/chield2.propertyString=Destchield2\n"+
            "/dest/source/chield2/subchield2.@type=mgnl:content\n" +
            "/dest/source/chield2/subchield2.propertyString=DestSubchield2\n" +
            "/dest/source/chield1.@type=mgnl:content\n" +
            "/dest/source/chield1.propertyString=DestChield1\n"+
            "/dest/source/chield1/subchield1.@type=mgnl:content\n" +
            "/dest/source/chield1/subchield1.propertyString=DestSubchield2\n" +
            "/dest/source/chield3.@type=mgnl:content\n" +
            "/dest/source/chield3.propertyString=chield3\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");
        Node dest = rootNode.getNode("dest");

        // WHEN
        NodeUtil.moveAndMergeNodes(source, dest.getPath(), true);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest/source", "propertyString", "helloDest");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1", "propertyString", "DestChield1");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1/subchield1", "propertyString", "subchield1");
        assertNodeExistWithProperty(rootNode, "dest/source/chield2", "propertyString", "chield2");
        assertEquals("Should no more exist ",rootNode.hasNode("dest/source/chield2/subchield2"), false);
        assertNodeExistWithProperty(rootNode, "dest/source/chield3", "propertyString", "chield3");
    }

    @Test
    public void testMoveAndMergeNodesDestinationConflictOnSubNodesDestNoOverride() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n" +
            "/dest/source.@type=mgnl:content\n" +
            "/dest/source.propertyString=helloDest\n"+
            "/dest/source/chield2.@type=mgnl:content\n" +
            "/dest/source/chield2.propertyString=Destchield2\n"+
            "/dest/source/chield2/subchield2.@type=mgnl:content\n" +
            "/dest/source/chield2/subchield2.propertyString=DestSubchield2\n" +
            "/dest/source/chield1.@type=mgnl:content\n" +
            "/dest/source/chield1.propertyString=DestChield1\n"+
            "/dest/source/chield1/subchield1.@type=mgnl:content\n" +
            "/dest/source/chield1/subchield1.propertyString=DestSubchield2\n" +
            "/dest/source/chield3.@type=mgnl:content\n" +
            "/dest/source/chield3.propertyString=chield3\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");
        Node dest = rootNode.getNode("dest");

        // WHEN
        NodeUtil.moveAndMergeNodes(source, dest.getPath(), false);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest/source", "propertyString", "helloDest");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1", "propertyString", "DestChield1");
        assertNodeExistWithProperty(rootNode, "dest/source/chield1/subchield1", "propertyString", "DestSubchield2");
        assertNodeExistWithProperty(rootNode, "dest/source/chield2", "propertyString", "Destchield2");
        assertNodeExistWithProperty(rootNode, "dest/source/chield2/subchield2", "propertyString", "DestSubchield2");
        assertNodeExistWithProperty(rootNode, "dest/source/chield3", "propertyString", "chield3");
    }


    @Test
    public void testMoveNode() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/nodeToMove.@type=mgnl:content\n" +
            "/nodeToMove.propertyString=hello\n" +
            "/nodeToMove/chield1.@type=mgnl:content\n" +
            "/nodeToMove/chield1.propertyString=sourceChield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/newParent.propertyString=dest\n" +
            "/newParent/chield2.@type=mgnl:content\n" +
            "/newParent/chield2.propertyString=newParentChield2\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node nodeToMove = rootNode.getNode("nodeToMove");
        Node newParent = rootNode.getNode("newParent");

        // WHEN
        NodeUtil.moveNode(nodeToMove, newParent);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("nodeToMove"), false);
        assertNodeExistWithProperty(rootNode, "newParent/chield2", "propertyString", "newParentChield2");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove", "propertyString", "hello");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove/chield1", "propertyString", "sourceChield1");
    }

    @Test
    public void testMoveNodeBefore() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/nodeToMove.@type=mgnl:content\n" +
            "/nodeToMove.propertyString=hello\n" +
            "/nodeToMove/chield1.@type=mgnl:content\n" +
            "/nodeToMove/chield1.propertyString=sourceChield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/newParent.propertyString=dest\n" +
            "/newParent/chield2.@type=mgnl:content\n" +
            "/newParent/chield2.propertyString=newParentChield2\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node nodeToMove = rootNode.getNode("nodeToMove");
        Node newParent = rootNode.getNode("newParent/chield2");

        // WHEN
        NodeUtil.moveNodeBefore(nodeToMove, newParent);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("nodeToMove"), false);
        assertNodeExistWithProperty(rootNode, "newParent/chield2", "propertyString", "newParentChield2");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove", "propertyString", "hello");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove/chield1", "propertyString", "sourceChield1");

        List<Node> chields = NodeUtil.asList(NodeUtil.collectAllChildren(rootNode.getNode("newParent"),NodeUtil.EXCLUDE_META_DATA_FILTER));
        assertEquals("Should be first ",chields.get(0).getProperty("propertyString").getString(), "hello");
        assertEquals("Should be second ",chields.get(1).getProperty("propertyString").getString(), "newParentChield2");
    }


    @Test
    public void testMoveNodeAfter() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/nodeToMove.@type=mgnl:content\n" +
            "/nodeToMove.propertyString=hello\n" +
            "/nodeToMove/chield1.@type=mgnl:content\n" +
            "/nodeToMove/chield1.propertyString=sourceChield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/newParent.propertyString=dest\n" +
            "/newParent/chield2.@type=mgnl:content\n" +
            "/newParent/chield2.propertyString=newParentChield2\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node nodeToMove = rootNode.getNode("nodeToMove");
        Node newParent = rootNode.getNode("newParent/chield2");

        // WHEN
        NodeUtil.moveNodeAfter(nodeToMove, newParent);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("nodeToMove"), false);
        assertNodeExistWithProperty(rootNode, "newParent/chield2", "propertyString", "newParentChield2");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove", "propertyString", "hello");
        assertNodeExistWithProperty(rootNode, "newParent/nodeToMove/chield1", "propertyString", "sourceChield1");

        List<Node> chields = NodeUtil.asList(NodeUtil.collectAllChildren(rootNode.getNode("newParent"),NodeUtil.EXCLUDE_META_DATA_FILTER));
        assertEquals("Should be first ",chields.get(0).getProperty("propertyString").getString(), "newParentChield2");
        assertEquals("Should be second ",chields.get(1).getProperty("propertyString").getString(), "hello");
    }


    @Test
    public void testRenameNode() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/nodeToRename.@type=mgnl:content\n" +
            "/nodeToRename.propertyString=hello\n" +
            "/nodeToRename/chield1.@type=mgnl:content\n" +
            "/nodeToRename/chield1.propertyString=sourceChield1\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node nodeToRename = rootNode.getNode("nodeToRename");

        // WHEN
        NodeUtil.renameNode(nodeToRename, "newName");

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("nodeToRename"), false);
        assertNodeExistWithProperty(rootNode, "newName", "propertyString", "hello");
        assertNodeExistWithProperty(rootNode, "newName/chield1", "propertyString", "sourceChield1");
    }


    @Test
    public void testRenameAndMergeNodesCleanDestination() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n";
        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");

        // WHEN
        NodeUtil.renameAndMergeNodes(source, "dest", false);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest", "propertyString", "dest");
        assertNodeExistWithProperty(rootNode, "dest/chield2", "propertyString", "chield2");
        assertNodeExistWithProperty(rootNode, "dest/chield1/subchield1", "propertyString", "subchield1");
    }

    @Test
    public void testRenameAndMergeNodesDestinationConflictOnSubChildrenNoOverride() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/source/chield1/subchield2.@type=mgnl:content\n" +
            "/source/chield1/subchield2.propertyString=subchield2\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n" +
            "/dest/chield3.@type=mgnl:content\n" +
            "/dest/chield3.propertyString=chield3\n"+
            "/dest/chield1.@type=mgnl:content\n" +
            "/dest/chield1.propertyString=DestChield1\n"+
            "/dest/chield1/subchield1.@type=mgnl:content\n" +
            "/dest/chield1/subchield1.propertyString=DestSubchield2\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");

        // WHEN
        NodeUtil.renameAndMergeNodes(source, "dest",false);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest", "propertyString", "dest");
        assertNodeExistWithProperty(rootNode, "dest/chield1", "propertyString", "DestChield1");
        assertNodeExistWithProperty(rootNode, "dest/chield1/subchield1", "propertyString", "DestSubchield2");
        assertNodeExistWithProperty(rootNode, "dest/chield1/subchield2", "propertyString", "subchield2");
        assertNodeExistWithProperty(rootNode, "dest/chield2", "propertyString", "chield2");
        assertNodeExistWithProperty(rootNode, "dest/chield3", "propertyString", "chield3");
    }

    @Test
    public void testRenameAndMergeNodesDestinationConflictOnSubChildrenOverride() throws IOException, RepositoryException{
        // GIVEN
        String nodeProperties =
            "/source.@type=mgnl:content\n" +
            "/source.propertyString=hello\n" +
            "/source/chield1.@type=mgnl:content\n" +
            "/source/chield1.propertyString=chield1\n" +
            "/source/chield2.@type=mgnl:content\n" +
            "/source/chield2.propertyString=chield2\n" +
            "/source/chield1/subchield1.@type=mgnl:content\n" +
            "/source/chield1/subchield1.propertyString=subchield1\n" +
            "/source/chield1/subchield2.@type=mgnl:content\n" +
            "/source/chield1/subchield2.propertyString=subchield2\n" +
            "/dest.@type=mgnl:content\n" +
            "/dest.propertyString=dest\n" +
            "/dest/chield3.@type=mgnl:content\n" +
            "/dest/chield3.propertyString=chield3\n"+
            "/dest/chield1.@type=mgnl:content\n" +
            "/dest/chield1.propertyString=DestChield1\n"+
            "/dest/chield1/subchield1.@type=mgnl:content\n" +
            "/dest/chield1/subchield1.propertyString=DestSubchield2\n" +
            "/dest/chield1/subchield3.@type=mgnl:content\n" +
            "/dest/chield1/subchield3.propertyString=DestSubchield3\n";

        Node rootNode = addNodeToRoot(nodeProperties);
        Node source = rootNode.getNode("source");

        // WHEN
        NodeUtil.renameAndMergeNodes(source, "dest",  true);

        // THEN
        assertEquals("Should no more exist ",rootNode.hasNode("source"), false);
        assertNodeExistWithProperty(rootNode, "dest", "propertyString", "dest");
        assertNodeExistWithProperty(rootNode, "dest/chield1", "propertyString", "DestChield1");
        assertNodeExistWithProperty(rootNode, "dest/chield1/subchield1", "propertyString", "subchield1");
        assertNodeExistWithProperty(rootNode, "dest/chield1/subchield2", "propertyString", "subchield2");
        assertNodeExistWithProperty(rootNode, "dest/chield1/subchield3", "propertyString", "DestSubchield3");
        assertNodeExistWithProperty(rootNode, "dest/chield2", "propertyString", "chield2");
        assertNodeExistWithProperty(rootNode, "dest/chield3", "propertyString", "chield3");
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
