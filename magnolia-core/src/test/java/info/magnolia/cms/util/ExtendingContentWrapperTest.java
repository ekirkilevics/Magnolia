/**
 * This file Copyright (c) 2010-2010 Magnolia International
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
 * is available at http://www.magnolia.info/mna.html
 * 
 * Any modifications to this file must keep this entire header
 * intact.
 * 
 */
package info.magnolia.cms.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;


/**
 * @author pbaerfuss
 * @version $Id$
 *
 */
public class ExtendingContentWrapperTest extends MgnlTestCase {
    
    public void testThatNodeDatasAreMerged() throws IOException, RepositoryException{
        HierarchyManager hm = MockUtil.createHierarchyManager(
            "/base/node/nodeData1=org1\n" +
            "/impl/node/extends=../../base/node\n" +
            "/impl/node/nodeData2=org2");
        Content plainContent = hm.getContent("/impl/node");
        Content extendedContent = new ExtendingContentWrapper(plainContent);
        
        assertTrue(extendedContent.hasNodeData("nodeData1"));
        assertTrue(extendedContent.hasNodeData("nodeData2"));
        assertFalse(extendedContent.hasNodeData("nodeData3"));
        
        assertEquals("org1", extendedContent.getNodeData("nodeData1").getString());
        assertEquals("org2", extendedContent.getNodeData("nodeData2").getString());
        
        assertEquals(3, extendedContent.getNodeDataCollection().size());
    }

    public void testThatNodeDatasCanBeOvwerWritten() throws IOException, RepositoryException{        
        HierarchyManager hm = MockUtil.createHierarchyManager(
            "/base/node/nodeData=org1\n" +
            "/impl/node/extends=../../base/node\n" +
            "/impl/node/nodeData=new");
        Content plainContent = hm.getContent("/impl/node");
        Content extendedContent = new ExtendingContentWrapper(plainContent);
        
        assertEquals("new", extendedContent.getNodeData("nodeData").getString());
        assertEquals(2, extendedContent.getNodeDataCollection().size());    }
    
    public void testThatSubNodesAreMerged() throws IOException, RepositoryException{        
        HierarchyManager hm = MockUtil.createHierarchyManager(
            "/base/node/subnode1\n" +
            "/impl/node/extends=../../base/node\n" +
            "/impl/node/subnode2");
        Content plainContent = hm.getContent("/impl/node");
        Content extendedContent = new ExtendingContentWrapper(plainContent);
        
        assertTrue(extendedContent.hasContent("subnode1"));
        assertTrue(extendedContent.hasContent("subnode2"));

        assertNotNull(extendedContent.getContent("subnode1"));
        assertNotNull(extendedContent.getContent("subnode2"));
        
        assertEquals(2, extendedContent.getChildren().size());
    }

    public void testThatSubNodesCanBeOverwritten() throws IOException, RepositoryException{        
        HierarchyManager hm = MockUtil.createHierarchyManager(
            "/base/node/subnode@uuid=1\n" +
            "/impl/node/extends=../../base/node\n" +
            "/impl/node/subnode@uuid=2");
        Content plainContent = hm.getContent("/impl/node");
        Content extendedContent = new ExtendingContentWrapper(plainContent);
        
        assertTrue(extendedContent.hasContent("subnode"));
        
        assertEquals("2", extendedContent.getContent("subnode").getUUID());
        
        assertEquals(1, extendedContent.getChildren().size());
    }
    
    public void testDeepMerge() throws IOException, RepositoryException{        
        String [][] expected = new String[][]{
            {"nodeData1","org1"}, // inherited
            {"nodeData2","org2.2"}, // overwritten
            {"nodeData3","org3"}, // added
        };
        HierarchyManager hm = MockUtil.createHierarchyManager(
            "/base/node/subnode/nodeData1=org1\n" +
            "/base/node/subnode/nodeData2=org2.1\n" +
            "/impl/node/extends=../../base/node\n" +
            "/impl/node/subnode/nodeData2=org2.2\n" +
            "/impl/node/subnode/nodeData3=org3");

        Content plainContent = hm.getContent("/impl/node");
        Content extendedContent = new ExtendingContentWrapper(plainContent);
        
        Content subnode = extendedContent.getContent("subnode");
        
        for (String[] pair : expected) {
            String name = pair[0];
            String value = pair[1];
            assertTrue(subnode.hasNodeData(name));
            assertEquals(value, subnode.getNodeData(name).getString());
        }
        
        Collection<NodeData> nodeDataCollection = subnode.getNodeDataCollection();
        assertEquals(3, nodeDataCollection.size());

        // test content of the collection
        int pos = 0;
        for (NodeData nodeData : nodeDataCollection) {
            assertEquals(expected[pos][1], nodeData.getString());
            pos ++;
        }
    }
    
    public void testThatOrderIsKeptWhileMergingSubNodes() throws IOException, RepositoryException{        
        HierarchyManager hm = MockUtil.createHierarchyManager(
            "/base/node/subnode1@uuid=1\n" +
            "/base/node/subnode2@uuid=2.1\n" +
            "/base/node/subnode3@uuid=3\n" +
            "/impl/node/extends=../../base/node\n" +
            "/impl/node/subnode2@uuid=2.2\n" +
            "/impl/node/subnode4@uuid=4");

        Content plainContent = hm.getContent("/impl/node");
        Content extendedContent = new ExtendingContentWrapper(plainContent);
        
        assertTrue(extendedContent.hasContent("subnode1"));
        assertTrue(extendedContent.hasContent("subnode2"));
        assertTrue(extendedContent.hasContent("subnode3"));
        assertTrue(extendedContent.hasContent("subnode4"));

        assertEquals("1", extendedContent.getContent("subnode1").getUUID());
        assertEquals("2.2", extendedContent.getContent("subnode2").getUUID());
        assertEquals("3", extendedContent.getContent("subnode3").getUUID());
        assertEquals("4", extendedContent.getContent("subnode4").getUUID());
        
        List<Content> subnodes = new ArrayList<Content>(extendedContent.getChildren());
        for (int i = 0; i < 4; i++) {
            assertEquals("subnode" + (i+1), subnodes.get(i).getName());
        }
        
        assertEquals(4, subnodes.size());
    }
        
    public void testBasicMultipleInheritance() throws IOException, RepositoryException{        
        HierarchyManager hm = MockUtil.createHierarchyManager(
            "/superbase/node/nodeData1=org1\n" +
            "/base/node/extends=../../superbase/node\n" +
            "/base/node/nodeData2=org2\n" +
            "/impl/node/extends=../../base/node\n" +
            "/impl/node/nodeData3=org3");
        Content plainContent = hm.getContent("/impl/node");
        Content extendedContent = new ExtendingContentWrapper(plainContent);
        
        assertTrue(extendedContent.hasNodeData("nodeData1"));
        assertTrue(extendedContent.hasNodeData("nodeData2"));
        assertTrue(extendedContent.hasNodeData("nodeData3"));
 
        assertEquals("org1", extendedContent.getNodeData("nodeData1").getString());
        assertEquals("org2", extendedContent.getNodeData("nodeData2").getString());
        assertEquals("org3", extendedContent.getNodeData("nodeData3").getString());
        
        //3 + extends node data
        assertEquals(4, extendedContent.getNodeDataCollection().size());
    }

    public void testComplextMultipleInheritance1() throws IOException, RepositoryException{        
        HierarchyManager hm = MockUtil.createHierarchyManager(
            "/superbase/nodeData1=org1\n" +
            "/base/node/subnode/nodeData2=org2\n" +
            "/base/node/subnode/extends=/superbase\n" +
            "/impl/node/extends=/base/node\n" +
            "/impl/node/subnode/nodeData3=org3");
        Content plainContent = hm.getContent("/impl/node");
        Content extendedContent = new ExtendingContentWrapper(plainContent);
        
        Content subnode = extendedContent.getContent("subnode");
        assertTrue(subnode.hasNodeData("nodeData1"));
        assertTrue(subnode.hasNodeData("nodeData2"));
        assertTrue(subnode.hasNodeData("nodeData3"));
 
        assertEquals("org1", subnode.getNodeData("nodeData1").getString());
        assertEquals("org2", subnode.getNodeData("nodeData2").getString());
        assertEquals("org3", subnode.getNodeData("nodeData3").getString());
        
        //3 + extends node data
        assertEquals(4, subnode.getNodeDataCollection().size());
    }

    public void testComplextMultipleInheritance2() throws IOException, RepositoryException{        
        HierarchyManager hm = MockUtil.createHierarchyManager(
            "/superbase/nodeData1=org1\n" +
            "/base/node/subnode/nodeData2=org2\n" +
            "/impl/node/extends=/base/node\n" +
            "/impl/node/subnode/extends=/superbase\n" +
            "/impl/node/subnode/nodeData3=org3");
        Content plainContent = hm.getContent("/impl/node");
        Content extendedContent = new ExtendingContentWrapper(plainContent);
        
        Content subnode = extendedContent.getContent("subnode");
        assertTrue(subnode.hasNodeData("nodeData1"));
        assertTrue(subnode.hasNodeData("nodeData2"));
        assertTrue(subnode.hasNodeData("nodeData3"));
 
        assertEquals("org1", subnode.getNodeData("nodeData1").getString());
        assertEquals("org2", subnode.getNodeData("nodeData2").getString());
        assertEquals("org3", subnode.getNodeData("nodeData3").getString());

        //3 + extends node data
        assertEquals(4, subnode.getNodeDataCollection().size());
    }
    
    public void testComplextMultipleInheritanceWithOverride() throws IOException, RepositoryException{
        HierarchyManager hm = MockUtil.createHierarchyManager(
            "/superbase/nodeData1=org1\n" +
            "/superbase/uglyChild/withSubChild/nodeDataX=over1\n" +
            "/base/node/subnode/nodeData2=org2\n" +
            "/impl/node/extends=/base/node\n" +
            "/impl/node/subnode/extends=/superbase\n" +
            "/impl/node/subnode/uglyChild/extends=override\n" +
            "/impl/node/subnode/nodeData3=org3");

        Content plainContent = hm.getContent("/impl/node");
        Content extendedContent = new ExtendingContentWrapper(plainContent);

        Content subnode = extendedContent.getContent("subnode");
        assertTrue(subnode.hasNodeData("nodeData1"));
        assertTrue(subnode.hasNodeData("nodeData2"));
        assertTrue(subnode.hasNodeData("nodeData3"));

        Content disinheritedNode = subnode.getContent("uglyChild");
        assertTrue(disinheritedNode.hasNodeData("extends"));
        assertFalse(disinheritedNode.hasContent("withSubChild"));

        assertEquals("org2", subnode.getNodeData("nodeData2").getString());
        assertEquals("org3", subnode.getNodeData("nodeData3").getString());

        //3 + extends node data
        assertEquals(4, subnode.getNodeDataCollection().size());
    }    
}
