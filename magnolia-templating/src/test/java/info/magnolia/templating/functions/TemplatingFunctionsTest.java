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
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 *
 * @version $Id$
 */
public class TemplatingFunctionsTest {

    private static final String CONTEXT_PATH = "/manual_set_context_path";

    private static final String DEPTH_1_FIRST_PAGE_NAME      = "page-L1-1";
    private static final String DEPTH_2_FIRST_PAGE_NAME      = "page-L2-1";
    private static final String DEPTH_2_FIRST_COMPONENT_NAME = "comp-L2-1";
    private static final String DEPTH_3_FIRST_PAGE_NAME      = "page-L3-1";
    private static final String DEPTH_3_FIRST_COMPONENT_NAME = "comp-L3-1";

    private static final String[] DEPTH_1_PAGE_NAMES       = {DEPTH_1_FIRST_PAGE_NAME,      "page-L1-2", "page-L1-3"};
    private static final String[] DEPTH_2_PAGE_NAMES       = {DEPTH_2_FIRST_PAGE_NAME,      "page-L2-2", "page-L2-3"};
    private static final String[] DEPTH_2_COMPONENT_NAMES  = {DEPTH_2_FIRST_COMPONENT_NAME, "comp-L2-2", "comp-L2-3"};
    private static final String[] DEPTH_3_PAGE_NAMES       = {DEPTH_3_FIRST_PAGE_NAME,      "page-L3-2", "page-L3-3"};
    private static final String[] DEPTH_3_COMPONENT_NAMES  = {DEPTH_3_FIRST_COMPONENT_NAME, "comp-L3-2", "comp-L3-3"};

    private MockNode root;
    private MockNode topPage;
    private MockNode topPageComponent;
    private MockNode childPage;
    private MockNode childPageComponent;
    private MockNode childPageSubPage;

    private ContentMap rootContentMap;
    private ContentMap topPageContentMap;
    private ContentMap topPageComponentContentMap;
    private ContentMap childPageContentMap;
    private ContentMap childPageComponentContentMap;
    private ContentMap childPageSubPageContentMap;

    @Before
    public void setUpNodeHierarchie() throws PathNotFoundException, RepositoryException{
        root = new MockNode();

        topPage            = createChildNodes(root,       DEPTH_1_PAGE_NAMES,      MgnlNodeType.NT_CONTENT);
        topPageComponent   = createChildNodes(topPage,    DEPTH_2_COMPONENT_NAMES, MgnlNodeType.NT_CONTENTNODE);
        childPage          = createChildNodes(topPage,    DEPTH_2_PAGE_NAMES,      MgnlNodeType.NT_CONTENT);
        childPageComponent = createChildNodes(childPage,  DEPTH_3_COMPONENT_NAMES, MgnlNodeType.NT_CONTENTNODE);
        childPageSubPage   = createChildNodes(childPage,  DEPTH_3_PAGE_NAMES,      MgnlNodeType.NT_CONTENT);

        rootContentMap = new ContentMap(root);
        topPageContentMap = new ContentMap(topPage);
        topPageComponentContentMap = new ContentMap(topPageComponent);
        childPageContentMap = new ContentMap(childPage);
        childPageComponentContentMap = new ContentMap(childPageComponent);
        childPageSubPageContentMap = new ContentMap(childPageSubPage);
    }

    @Test
    public void testAsContentFromNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        MockSession session = mock(MockSession.class);
        when(session.hasPermission(root.getPath()+DEPTH_1_FIRST_PAGE_NAME, Session.ACTION_READ)).thenReturn(Boolean.TRUE);
        topPage.setSession(session);

        // WHEN
        Content result = functions.asContent(topPage);

        // THEN
        assertContentEqualsNode(result, topPage);
    }

    @Test
    public void testAsJCRNodeFromContent() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        MockContent content = new MockContent("test");

        // WHEN
        Node result = functions.asJCRNode(content);

        // THEN
        assertNodeEqualsContent(result, content);
    }

    @Test
    public void testAsContentMapfromNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap result = functions.asContentMap(topPage);

        // THEN
        assertNodeEqualsMap(topPage, result);
    }

    @Test
    public void testAsJCRNodeFromContentMap() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultContenMap = functions.asJCRNode(topPageContentMap);

        // THEN
        assertNodeEqualsMap(resultContenMap, topPageContentMap);
    }

    @Test
    public void testParentFromRootNodeShouldBeNull() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.parent(root);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testParentFromNodeDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.parent(topPage);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testParentFromNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.parent(childPage);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testParentFromContentMapDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.parent(topPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testParentFromContentMapDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.parent(childPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testUuidFromNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        String uuid = functions.uuid(topPage);

        // THEN
        assertEquals(uuid, topPage.getIdentifier());
    }

    @Test
    public void testUuidFromContentMap() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        String uuid = functions.uuid(topPageContentMap);

        // THEN
        assertEquals(uuid, topPageContentMap.get("@id"));
        assertEquals(uuid, topPageContentMap.get("@uuid"));
    }

    @Test
    public void testLinkFromNodeDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        MgnlContext.setInstance(context);

        // WHEN
        String resultLink = functions.link(topPage);

        // THEN
        assertEquals(CONTEXT_PATH+topPage.getPath(), resultLink);
    }

    @Test
    public void testLinkFromNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        MgnlContext.setInstance(context);

        // WHEN
        String resultLink = functions.link(childPage);

        // THEN
        assertEquals(CONTEXT_PATH+childPage.getPath(), resultLink);
    }

    @Test
    public void testLinkFromContentMapDepth1() throws RepositoryException {
        // given
        TemplatingFunctions functions = new TemplatingFunctions();

        MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        MgnlContext.setInstance(context);

        // when
        String resultLink = functions.link(topPage);

        // then
        assertEquals(CONTEXT_PATH+topPage.getPath(), resultLink);
    }

    @Test
    public void testLinkFromContentMapDepth2() throws RepositoryException {
        // given
        TemplatingFunctions functions = new TemplatingFunctions();

        MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        MgnlContext.setInstance(context);

        // when
        String resultLink = functions.link(childPage);

        // then
        assertEquals(CONTEXT_PATH+childPage.getPath(), resultLink);
    }

    @Test
    public void testChildrenFromNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();
        String[] allNamesDepth1 = (String[]) ArrayUtils.addAll(DEPTH_2_COMPONENT_NAMES, DEPTH_2_PAGE_NAMES);

        // WHEN
        List<Node> resultChildNodes = functions.children(topPage);

        // THEN
        assertNodesListEqualStringDefinitions(resultChildNodes, allNamesDepth1);
    }

    @Test
    public void testChildrenFromContentMap() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        List<ContentMap> resultChildNodes = functions.children(topPageContentMap);

        // THEN
        String[] allNamesDepth1 = (String[]) ArrayUtils.addAll(DEPTH_2_COMPONENT_NAMES, DEPTH_2_PAGE_NAMES);
        assertContentMapListEqualStringDefinitions(resultChildNodes, allNamesDepth1);
    }

    @Test
    public void testChildrenFromNodeAndTypePage() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        List<Node> resultChildPages = functions.children(topPage, MgnlNodeType.NT_CONTENT);

        // THEN
        assertNodesListEqualStringDefinitions(resultChildPages, DEPTH_2_PAGE_NAMES);
    }

    @Test
    public void testChildrenFromNodeAndTypeComponent() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        List<Node> resultChildComponents = functions.children(topPage, MgnlNodeType.NT_CONTENTNODE);

        // THEN
        assertNodesListEqualStringDefinitions(resultChildComponents, DEPTH_2_COMPONENT_NAMES);
    }

    @Test
    public void testChildrenFromContentMapAndTypePage() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        List<ContentMap> resultChildPages = functions.children(topPageContentMap, MgnlNodeType.NT_CONTENT);

        // THEN
        assertContentMapListEqualStringDefinitions(resultChildPages, DEPTH_2_PAGE_NAMES);
    }

    @Test
    public void testChildrenFromContentMapAndTypeComponent() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        List<ContentMap> resultChildComponents = functions.children(topPageContentMap, MgnlNodeType.NT_CONTENTNODE);

        // THEN
        assertContentMapListEqualStringDefinitions(resultChildComponents, DEPTH_2_COMPONENT_NAMES);
    }

    @Test
    public void testRootFromPageNodeDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(topPage);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testRootFromPageNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(childPage);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testRootFromComponentNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(topPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testRootFromPageNodeDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(childPageSubPage);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testRootFromComponentNodeDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(childPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testRootFromPageContentMapDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(topPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testRootFromPageContentMapDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(childPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testRootFromComponentContentMapDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(topPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testRootFromPageContentMapDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(childPageSubPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testRootFromComponentContentMapDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(childPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testPageFromPageNodeDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.page(topPage);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testPageFromPageNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.page(childPage);

        // THEN
        assertNodeEqualsNode(resultNode, childPage);
    }

    @Test
    public void testPageFromComponentNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.page(topPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testPageFromPageNodeDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.page(childPageSubPage);

        // THEN
        assertNodeEqualsNode(resultNode, childPageSubPage);
    }

    @Test
    public void testPageFromComponentNodeDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.page(childPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, childPage);
    }

    @Test
    public void testPageFromPageContentMapDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.page(topPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testPageFromPageContentMapDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.page(childPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, childPageContentMap);
    }

    @Test
    public void testPageFromComponentContentMapDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.page(topPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testPageFromPageContentMapDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.page(childPageSubPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, childPageSubPageContentMap);
    }

    @Test
    public void testPageFromComponentContentMapDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.page(childPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, childPageContentMap);
    }

    @Test
    public void testRootPageFromPageNodeDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.rootPage(topPage);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromPageNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.rootPage(childPage);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromComponentNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.rootPage(topPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromPageNodeDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.rootPage(childPageSubPage);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromComponentNodeDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.rootPage(childPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromPageContentMapDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.rootPage(topPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testRootPageFromPageContentMapDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.rootPage(childPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testRootPageFromComponentContentMapDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.rootPage(topPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testRootPageFromPageContentMapDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.rootPage(childPageSubPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testRootPageFromComponentContentMapDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.rootPage(childPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
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
        assertNotNull(node1.getDepth());
        assertEquals(node1.getDepth(), node2.getDepth());
        assertNotNull(node1.getPrimaryNodeType().getName());
        assertEquals(node1.getPrimaryNodeType().getName(), node2.getPrimaryNodeType().getName());
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
        assertNotNull(map1.get("@depth"));
        assertEquals(map1.get("@depth"), map2.get("@depth"));
        //TODO cringele: should ContentMap really return NodeType Object on "@nodeType" or the NodeTypeName?
        assertNotNull(((NodeType)map1.get("@nodeType")).getName());
        assertEquals(((NodeType)map1.get("@nodeType")).getName(), ((NodeType)map1.get("@nodeType")).getName());

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
        assertNotNull(node.getDepth());
        assertEquals(node.getDepth(), map.get("@depth"));
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
     * Add to the give node child nodes by the name and type passed. Returns first created child node.
     *
     * @param parent Node where child nodes are added to
     * @param childNodeNames names of the child nodes to create
     * @param nodeTypeName primary type of the child nodes to create
     * @return first created child node
     * @throws RepositoryException
     * @throws PathNotFoundException
     */
    private MockNode createChildNodes(MockNode parent, String[] childNodeNames, String nodeTypeName) throws PathNotFoundException, RepositoryException {
        for(String nodeName : childNodeNames){
            MockNode child = new MockNode(nodeName, nodeTypeName);
            parent.addNode(child);
        }
        return childNodeNames.length == 0 ? null : (MockNode)parent.getNode(childNodeNames[0]);
    }

}
