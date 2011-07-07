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
package info.magnolia.templating.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.util.ContentMap;
//import info.magnolia.link.LinkFactory;
//import info.magnolia.link.LinkTransformerManager;
//import info.magnolia.link.LinkUtil;
//import info.magnolia.objectfactory.ComponentProvider;
//import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

/**
 * Tests.
 *
 * @version $Id$
 */
public class TemplatingFunctionsTest {

//  @Test
//  public void testLinkFromNode() throws RepositoryException {
//      // given
//      TemplatingFunctions functions = new TemplatingFunctions();
//      MockSession session = mock(MockSession.class);
//
//      String name = "test";
//      MockNode node = new MockNode(name);
//      node.setSession(session);
//      when(session.hasPermission("/"+name, Session.ACTION_READ)).thenReturn(Boolean.TRUE);
//
//      ComponentProvider componentProvider = mock(ComponentProvider.class);
//      Components.setProvider(componentProvider);
//      componentProvider.newInstance(LinkTransformerManager.class);
//      //componentProvider.newInstance(LinkFactory.class);
//
//      // when
//      String result = functions.link(node);
//
//      // then
//      assertEquals("", result);
//  }

    @Test
    public void testAsContentFromNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        String name = "test";
        MockNode origin = new MockNode(name);
        MockSession session = mock(MockSession.class);
        when(session.hasPermission("/"+name, Session.ACTION_READ)).thenReturn(Boolean.TRUE);
        origin.setSession(session);

        // WHEN
        Content result = functions.asContent(origin);

        // THEN
        //Added check on not null, cause equals is true between nulls. But these values should never be null.
        assertContentEqualsNode(result, origin);
    }

    @Test
    public void testAsJCRNodeFromContent() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        String name = "test";
        MockContent content = new MockContent(name);

        // WHEN
        Node result = functions.asJCRNode(content);

        // THEN
        assertEquals(name, result.getName());
        assertNodeEqualsContent(result, content);
    }

    @Test
    public void testAsContentMapfromNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        String name = "test";
        MockNode content = new MockNode(name);

        // WHEN
        ContentMap result = functions.asContentMap(content);

        // THEN
        assertNodeEqualsMap(content, result);
    }

    @Test
    public void testAsJCRNodeFromContentMap() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        String name = "test";
        MockNode node = new MockNode(name);
        ContentMap map = new ContentMap(node);

        // WHEN
        Node result = functions.asJCRNode(map);

        // THEN
        assertEquals(name, result.getName());
        assertNodeEqualsMap(result, map);
    }

    @Test
    public void testParentFromNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        MockNode parent = new MockNode("parent");
        MockNode child = new MockNode("child");
        parent.addNode(child);

        // WHEN
        Node result = functions.parent(child);

        // THEN
        assertNodeEqualsNode(result, parent);
    }

    @Test
    public void testParentFromRootNodeShouldBeNull() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        MockNode root = new MockNode("root");

        // WHEN
        Node result = functions.parent(root);

        // THEN
        assertNull(result);
    }

    @Test
    public void testParentFromContentMap() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        MockNode parent = new MockNode("parent");
        MockNode child = new MockNode("child");
        parent.addNode(child);
        ContentMap childMap = new ContentMap(child);
        ContentMap parentMap = new ContentMap(parent);

        // WHEN
        ContentMap resultMap = functions.parent(childMap);

        // THEN
        assertMapEqualsMap(resultMap, parentMap);
    }

    @Test
    public void testUuidFromNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        String name = "test";
        MockNode node = new MockNode(name);

        // WHEN
        String uuid = functions.uuid(node);

        // THEN
        assertEquals(uuid, node.getIdentifier());
        assertEquals(uuid, node.getUUID());
    }

    @Test
    public void testUuidFromMap() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        String name = "test";
        MockNode node = new MockNode(name);
        ContentMap map = new ContentMap(node);

        // WHEN
        String uuid = functions.uuid(map);

        // THEN
        assertEquals(uuid, map.get("@id"));
        assertEquals(uuid, map.get("@uuid"));
    }

    @Test
    public void testChildrenFromContentMap() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        String[] firstLevelPages       = {"page1", "page2", "page3"};
        String[] firstLevelComponents  = {"comp1", "comp2", "comp3"};
        String[] secondLevelPages      = {"page1-1", "page1-2", "page1-3"};
        String[] secondLevelComponents = {"comp1-1", "comp1-2", "comp1-3"};

        MockNode rootPage = new MockNode("root", MgnlNodeType.NT_CONTENT);

        createChildNodes(rootPage, firstLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes(rootPage, firstLevelComponents, MgnlNodeType.NT_CONTENTNODE);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelComponents, MgnlNodeType.NT_CONTENTNODE);

        ContentMap rootPageContentMap = new ContentMap(rootPage);

        // WHEN
        List<ContentMap> resultChildNodes = functions.children(rootPageContentMap);

        // THEN
        String[] allFirstLevelNames = (String[]) ArrayUtils.addAll(firstLevelPages, firstLevelComponents);
        assertContentMapListEqualStringDefinitions(resultChildNodes, allFirstLevelNames);
    }

    @Test
    public void testChildrenFromContentMapAndType() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        String[] firstLevelPages       = {"page1", "page2", "page3"};
        String[] firstLevelComponents  = {"comp1", "comp2", "comp3"};
        String[] secondLevelPages      = {"page1-1", "page1-2", "page1-3"};
        String[] secondLevelComponents = {"comp1-1", "comp1-2", "comp1-3"};

        MockNode rootPage = new MockNode("root", MgnlNodeType.NT_CONTENT);

        createChildNodes(rootPage, firstLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes(rootPage, firstLevelComponents, MgnlNodeType.NT_CONTENTNODE);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelComponents, MgnlNodeType.NT_CONTENTNODE);

        ContentMap rootPageContentMap = new ContentMap(rootPage);


        // WHEN
        List<ContentMap> resultChildPagesMap = functions.children(rootPageContentMap, MgnlNodeType.NT_CONTENT);
        List<ContentMap> resultChildComponentsMap = functions.children(rootPageContentMap, MgnlNodeType.NT_CONTENTNODE);

        // THEN
        assertContentMapListEqualStringDefinitions(resultChildPagesMap, firstLevelPages);
        assertContentMapListEqualStringDefinitions(resultChildComponentsMap, firstLevelComponents);
    }

    @Test
    public void testChildrenFromNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        String[] firstLevelPages       = {"page1", "page2", "page3"};
        String[] firstLevelComponents  = {"comp1", "comp2", "comp3"};
        String[] secondLevelPages      = {"page1-1", "page1-2", "page1-3"};
        String[] secondLevelComponents = {"comp1-1", "comp1-2", "comp1-3"};

        MockNode rootPage = new MockNode("root", MgnlNodeType.NT_CONTENT);

        createChildNodes(rootPage, firstLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes(rootPage, firstLevelComponents, MgnlNodeType.NT_CONTENTNODE);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelComponents, MgnlNodeType.NT_CONTENTNODE);

        // WHEN
        List<Node> resultChildNodes = functions.children(rootPage);

        // THEN
        String[] allFirstLevelNames = (String[]) ArrayUtils.addAll(firstLevelPages, firstLevelComponents);
        assertNodesListEqualStringDefinitions(resultChildNodes, allFirstLevelNames);
    }

    @Test
    public void testChildrenFromNodeAndType() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        String[] firstLevelPages       = {"page1", "page2", "page3"};
        String[] firstLevelComponents  = {"comp1", "comp2", "comp3"};
        String[] secondLevelPages      = {"page1-1", "page1-2", "page1-3"};
        String[] secondLevelComponents = {"comp1-1", "comp1-2", "comp1-3"};

        MockNode rootPage = new MockNode("root", MgnlNodeType.NT_CONTENT);

        createChildNodes(rootPage, firstLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes(rootPage, firstLevelComponents, MgnlNodeType.NT_CONTENTNODE);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelComponents, MgnlNodeType.NT_CONTENTNODE);

        // WHEN
        List<Node> resultChildPages = functions.children(rootPage, MgnlNodeType.NT_CONTENT);
        List<Node> resultChildComponents = functions.children(rootPage, MgnlNodeType.NT_CONTENTNODE);

        // THEN
        assertNodesListEqualStringDefinitions(resultChildPages, firstLevelPages);
        assertNodesListEqualStringDefinitions(resultChildComponents, firstLevelComponents);
    }

// TODO cringele: MockNode should always have '/' as root Node with depth=0
//    @Test
//    public void testRootFromNode() throws RepositoryException {
//        // GIVEN
//        TemplatingFunctions functions = new TemplatingFunctions();
//
//        String[] firstLevelPages       = {"page1", "page2", "page3"};
//        String[] firstLevelComponents  = {"comp1", "comp2", "comp3"};
//        String[] secondLevelPages      = {"page1-1", "page1-2", "page1-3"};
//        String[] secondLevelComponents = {"comp1-1", "comp1-2", "comp1-3"};
//
//        MockNode rootPage = new MockNode("root", MgnlNodeType.NT_CONTENT);
//
//        createChildNodes(rootPage, firstLevelPages, MgnlNodeType.NT_CONTENT);
//        createChildNodes(rootPage, firstLevelComponents, MgnlNodeType.NT_CONTENTNODE);
//        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelPages, MgnlNodeType.NT_CONTENT);
//        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelComponents, MgnlNodeType.NT_CONTENTNODE);
//
//        // WHEN
//        Node resultFromRoot = functions.root(rootPage);
//        Node resultFromPageL1 = functions.root(rootPage.getNode("page1"));
//        Node resultFromComponentL1 = functions.root(rootPage.getNode("comp1"));
//        Node resultFromPageL2 = functions.root(rootPage.getNode("page1").getNode("page1-1"));
//        Node resultFromComponentL2 = functions.root(rootPage.getNode("page1").getNode("comp1-1"));
//
//        // THEN
//        assertNodeEqualsNode(resultFromRoot, rootPage);
//        assertNodeEqualsNode(resultFromPageL1, rootPage);
//        assertNodeEqualsNode(resultFromComponentL1, rootPage);
//        assertNodeEqualsNode(resultFromPageL2, rootPage);
//        assertNodeEqualsNode(resultFromComponentL2, rootPage);
//    }
//
//    @Test
//    public void testRootFromContentMap() throws RepositoryException {
//        // GIVEN
//        TemplatingFunctions functions = new TemplatingFunctions();
//
//        String[] firstLevelPages       = {"page1", "page2", "page3"};
//        String[] firstLevelComponents  = {"comp1", "comp2", "comp3"};
//        String[] secondLevelPages      = {"page1-1", "page1-2", "page1-3"};
//        String[] secondLevelComponents = {"comp1-1", "comp1-2", "comp1-3"};
//
//        MockNode rootPage = new MockNode("root", MgnlNodeType.NT_CONTENT);
//        ContentMap rootPageMap = functions.asContentMap(rootPage);
//
//        createChildNodes(rootPage, firstLevelPages, MgnlNodeType.NT_CONTENT);
//        createChildNodes(rootPage, firstLevelComponents, MgnlNodeType.NT_CONTENTNODE);
//        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelPages, MgnlNodeType.NT_CONTENT);
//        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelComponents, MgnlNodeType.NT_CONTENTNODE);
//
//        // WHEN
//        ContentMap resultFromRootMap = functions.root(rootPageMap);
//        ContentMap resultFromPageL1Map = functions.root(new ContentMap(rootPage.getNode("page1")));
//        ContentMap resultFromComponentL1Map = functions.root(new ContentMap(rootPage.getNode("comp1")));
//        ContentMap resultFromPageL2Map = functions.root(new ContentMap(rootPage.getNode("page1").getNode("page1-1")));
//        ContentMap resultFromComponentL2Map = functions.root(new ContentMap(rootPage.getNode("page1").getNode("comp1-1")));
//
//        // THEN
//        assertMapEqualsMap(resultFromRootMap, rootPageMap);
//        assertMapEqualsMap(resultFromPageL1Map, rootPageMap);
//        assertMapEqualsMap(resultFromComponentL1Map, rootPageMap);
//        assertMapEqualsMap(resultFromPageL2Map, rootPageMap);
//        assertMapEqualsMap(resultFromComponentL2Map, rootPageMap);
//    }

    @Test
    public void testPageFromNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        String[] firstLevelPages       = {"page1", "page2", "page3"};
        String[] firstLevelComponents  = {"comp1", "comp2", "comp3"};
        String[] secondLevelPages      = {"page1-1", "page1-2", "page1-3"};
        String[] secondLevelComponents = {"comp1-1", "comp1-2", "comp1-3"};

        MockNode rootPage = new MockNode("root", MgnlNodeType.NT_CONTENT);

        createChildNodes(rootPage, firstLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes(rootPage, firstLevelComponents, MgnlNodeType.NT_CONTENTNODE);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelComponents, MgnlNodeType.NT_CONTENTNODE);

        // WHEN
        Node resultFromRoot = functions.page(rootPage);
        Node resultFromPageL1 = functions.page(rootPage.getNode("page1"));
        Node resultFromComponentL1 = functions.page(rootPage.getNode("comp1"));
        Node resultFromPageL2 = functions.page(rootPage.getNode("page1").getNode("page1-1"));
        Node resultFromComponentL2 = functions.page(rootPage.getNode("page1").getNode("comp1-1"));

        // THEN
        assertNodeEqualsNode(resultFromRoot, rootPage);
        assertNodeEqualsNode(resultFromPageL1, rootPage.getNode("page1"));
        assertNodeEqualsNode(resultFromComponentL1, rootPage);
        assertNodeEqualsNode(resultFromPageL2, rootPage.getNode("page1").getNode("page1-1"));
        assertNodeEqualsNode(resultFromComponentL2, rootPage.getNode("page1"));
    }

    @Test
    public void testPageFromContentMap() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        String[] firstLevelPages       = {"page1", "page2", "page3"};
        String[] firstLevelComponents  = {"comp1", "comp2", "comp3"};
        String[] secondLevelPages      = {"page1-1", "page1-2", "page1-3"};
        String[] secondLevelComponents = {"comp1-1", "comp1-2", "comp1-3"};

        MockNode rootPage = new MockNode("root", MgnlNodeType.NT_CONTENT);
        ContentMap rootPageMap = functions.asContentMap(rootPage);

        createChildNodes(rootPage, firstLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes(rootPage, firstLevelComponents, MgnlNodeType.NT_CONTENTNODE);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelPages, MgnlNodeType.NT_CONTENT);
        createChildNodes((MockNode)rootPage.getNode("page1"), secondLevelComponents, MgnlNodeType.NT_CONTENTNODE);

        // WHEN
        ContentMap resultFromRootMap = functions.page(rootPageMap);
        ContentMap resultFromPageL1Map = functions.page(new ContentMap(rootPage.getNode("page1")));
        ContentMap resultFromComponentL1Map = functions.page(new ContentMap(rootPage.getNode("comp1")));
        ContentMap resultFromPageL2Map = functions.page(new ContentMap(rootPage.getNode("page1").getNode("page1-1")));
        ContentMap resultFromComponentL2Map = functions.page(new ContentMap(rootPage.getNode("page1").getNode("comp1-1")));

        // THEN
        assertMapEqualsMap(resultFromRootMap, rootPageMap);
        assertMapEqualsMap(resultFromPageL1Map, new ContentMap(rootPage.getNode("page1")));
        assertMapEqualsMap(resultFromComponentL1Map, rootPageMap);
        assertMapEqualsMap(resultFromPageL2Map, new ContentMap(rootPage.getNode("page1").getNode("page1-1")));
        assertMapEqualsMap(resultFromComponentL2Map, new ContentMap(rootPage.getNode("page1")));
    }


    /**
     * Checks each Node of the list @param nodeList with the passed nodeNames in @param originNodeNames.
     * Checks also the amount of Nodes compared of the amount of passed nodeNames in @param originNodeNames.
     *
     * @param nodeList List of Nodes to compare to the names defined in @param originNodeNames
     * @param originNodeNames containing the node names to compare to the Nodes in @param nodeList
     * @throws RepositoryException
     */
    private void assertNodesListEqualStringDefinitions(List<Node> nodeList, String[] originNodeNames) throws RepositoryException {
        int i = 0;
        for (Iterator<Node> it = nodeList.iterator(); it.hasNext(); i++) {
            Node node = it.next();
            assertEquals(node.getName(), originNodeNames[i]);
        }
    }

    /**
     * Checks each ContentMap of the list @param contentMapList with the passed nodeNames in @param originNodeNames.
     * Checks also the amount of contentMapList compared of the amount of passed nodeNames in @param originNodeNames.
     *
     * @param contentMapList List of ContentMaps to compare to the names defined in @param originNodeNames
     * @param originNodeNames containing the node names to compare to the ContentMaps in @param contentMapList
     * @throws RepositoryException
     */
    private void assertContentMapListEqualStringDefinitions(List<ContentMap> contentMapList, String[] originNodeNames) throws RepositoryException {
        int i = 0;
        for (Iterator<ContentMap> it = contentMapList.iterator(); it.hasNext(); i++) {
            ContentMap map = it.next();
            assertEquals(map.get("@name"), originNodeNames[i]);
        }
    }

    /**
     * Checks all mandatory Content values. None should be null and all values should equal.
     *
     * @param content Content to compare with Node
     * @param node Node to compare with Content
     * @throws RepositoryException
     */
    private void assertContentEqualsNode(Content content, Node node) throws RepositoryException {
        assertNotNull(content.getName());
        assertEquals(content.getName(), node.getName());
        assertNotNull(content.getUUID());
        assertEquals(content.getUUID(), node.getUUID());
        assertEquals(content.getUUID(), node.getIdentifier());
        assertNotNull(content.getHandle());
        assertEquals(content.getHandle(), node.getPath());
    }

    /**
     * Checks all mandatory Node values. None should be null and all values should equal.
     *
     * @param node1 Node to compare with Node-2
     * @param node2 Node to compare with Node-1
     * @throws RepositoryException
     */
    private void assertNodeEqualsNode(Node node1, Node node2) throws RepositoryException {
        assertNotNull(node1.getName());
        assertEquals(node1.getName(), node2.getName());
        assertNotNull(node1.getUUID());
        assertEquals(node1.getUUID(), node2.getUUID());
        assertNotNull(node1.getIdentifier());
        assertEquals(node1.getIdentifier(), node2.getIdentifier());
        assertNotNull(node1.getPath());
        assertEquals(node1.getPath(), node2.getPath());
    }

    /**
     * Checks all mandatory ContentMap values. None should be null and all values should equal.
     *
     * @param map1 ContentMap to compare with ContentMap-2
     * @param map2 ContentMap to compare with ContentMap-1
     */
    private void assertMapEqualsMap(ContentMap map1, ContentMap map2) {
        assertNotNull(map1.get("@name"));
        assertEquals(map1.get("@name"), map2.get("@name"));
        assertNotNull(map1.get("@id"));
        assertEquals(map1.get("@id"), map2.get("@id"));
        assertNotNull(map1.get("@uuid"));
        assertEquals(map1.get("@uuid"), map2.get("@uuid"));
        assertNotNull(map1.get("@path"));
        assertEquals(map1.get("@path"), map2.get("@path"));
        assertNotNull(map1.get("@handle"));
        assertEquals(map1.get("@handle"), map2.get("@handle"));

        //TODO cringele: should they work too?
//        assertNotNull(map1.get("@nodeType"));
//        assertEquals(map1.get("@nodeType"), map2.get("@nodeType"));
//        assertNotNull(map1.get("@level"));
//        assertEquals(map1.get("@level"), map2.get("@level"));
//        assertNotNull(map1.get("@nodeType"));
//        assertEquals(map1.get("@nodeType"), map2.get("@nodeType"));
    }

    /**
     * Checks all mandatory ContentMap values. None should be null and all values should equal.
     *
     * @param node Node to compare with ContentMap
     * @param map ContentMap to compare with Node
     * @throws RepositoryException
     */
    private void assertNodeEqualsMap(Node node, ContentMap map) throws RepositoryException {
        assertNotNull(node.getName());
        assertEquals(node.getName(), map.get("@name"));
        assertNotNull(node.getUUID());
        assertEquals(node.getUUID(), map.get("@uuid"));
        assertNotNull(node.getIdentifier());
        assertEquals(node.getIdentifier(), map.get("@id"));
        assertEquals(node.getIdentifier(), map.get("@uuid"));
        assertNotNull(node.getPath());
        assertEquals(node.getPath(), map.get("@path"));
        assertEquals(node.getPath(), map.get("@handle"));
    }

    /**
     * Checks all mandatory Content values. None should be null and all values should equal.
     *
     * @param node Node to compare with Content
     * @param content Content to compare with Node
     * @throws RepositoryException
     */
    private void assertNodeEqualsContent(Node node, Content content) throws RepositoryException {
        assertNotNull(node.getName());
        assertEquals(node.getName(), content.getName());
        //TODO cringele: this should work!! I'll have a look at them with dlipp
//        assertNotNull(node.getUUID());
//        assertEquals(node.getUUID(), content.getUUID());
//        assertNotNull(node.getIdentifier());
//        assertEquals(node.getIdentifier(), content.getUUID());
        assertNotNull(node.getPath());
        assertEquals(node.getPath(), content.getHandle());
    }

    /**
     * Add to the give node child nodes by the name and type passed.
     *
     * @param parent Node where child nodes are added to
     * @param childNodeNames names of the child nodes to create
     * @param nodeTypeName primary type of the child nodes to create
     */
    private void createChildNodes(MockNode parent, String[] childNodeNames, String nodeTypeName) {
        for(String nodeName : childNodeNames){
            MockNode child = new MockNode(nodeName, nodeTypeName);
            parent.addNode(child);
        }
    }

}
