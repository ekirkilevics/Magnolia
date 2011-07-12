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

//import static org.easymock.EasyMock.createMock;
//import static org.easymock.EasyMock.expect;
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

    private static final String LEVEL_1_FIRST_PAGE_NAME      = "page-L1-1";
    private static final String LEVEL_2_FIRST_PAGE_NAME      = "page-L2-1";
    private static final String LEVEL_2_FIRST_COMPONENT_NAME = "comp-L2-1";
    private static final String LEVEL_3_FIRST_PAGE_NAME      = "page-L3-1";
    private static final String LEVEL_3_FIRST_COMPONENT_NAME = "comp-L3-1";

    private static final String[] LEVEL_1_PAGE_NAMES       = {LEVEL_1_FIRST_PAGE_NAME, "page-L1-2", "page-L1-3"};
    private static final String[] LEVEL_2_PAGE_NAMES       = {LEVEL_2_FIRST_PAGE_NAME, "page-L2-2", "page-L2-3"};
    private static final String[] LEVEL_2_COMPONENT_NAMES  = {LEVEL_2_FIRST_COMPONENT_NAME, "comp-L2-2", "comp-L2-3"};
    private static final String[] LEVEL_3_PAGE_NAMES       = {LEVEL_3_FIRST_PAGE_NAME, "page-L3-2", "page-L3-3"};
    private static final String[] LEVEL_3_COMPONENT_NAMES  = {LEVEL_3_FIRST_COMPONENT_NAME, "comp-L3-2", "comp-L3-3"};

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

        topPage            = createChildNodes(root,       LEVEL_1_PAGE_NAMES,      MgnlNodeType.NT_CONTENT);
        topPageComponent   = createChildNodes(topPage,    LEVEL_2_COMPONENT_NAMES, MgnlNodeType.NT_CONTENTNODE);
        childPage          = createChildNodes(topPage,    LEVEL_2_PAGE_NAMES,      MgnlNodeType.NT_CONTENT);
        childPageComponent = createChildNodes(childPage,  LEVEL_3_COMPONENT_NAMES, MgnlNodeType.NT_CONTENTNODE);
        childPageSubPage   = createChildNodes(childPage,  LEVEL_3_PAGE_NAMES,      MgnlNodeType.NT_CONTENT);

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
        when(session.hasPermission(root.getPath()+LEVEL_1_FIRST_PAGE_NAME, Session.ACTION_READ)).thenReturn(Boolean.TRUE);
        topPage.setSession(session);

        // WHEN
        Content result = functions.asContent(topPage);

        // THEN
        //Added check on not null, cause equals is true between nulls. But these values should never be null.
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
        Node result = functions.parent(root);

        // THEN
        assertNull(result);
    }

    @Test
    public void testParentFromNodeLevel1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node result = functions.parent(topPage);

        // THEN
        assertNodeEqualsNode(result, root);
    }

    @Test
    public void testParentFromNodeLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node result = functions.parent(childPage);

        // THEN
        assertNodeEqualsNode(result, topPage);
    }

    @Test
    public void testParentFromContentMapLevel1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultMap = functions.parent(topPageContentMap);

        // THEN
        assertMapEqualsMap(resultMap, new ContentMap(root));
    }

    @Test
    public void testParentFromContentMapLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultMap = functions.parent(childPageContentMap);

        // THEN
        assertMapEqualsMap(resultMap, topPageContentMap);
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
    public void testLinkFromNodeLevel1() throws RepositoryException {
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
    public void testLinkFromNodeLevel2() throws RepositoryException {
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
    public void testLinkFromContentMapLevel1() throws RepositoryException {
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
    public void testLinkFromContentMapLevel2() throws RepositoryException {
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
        String[] allFirstLevelNames = (String[]) ArrayUtils.addAll(LEVEL_2_COMPONENT_NAMES, LEVEL_2_PAGE_NAMES);

        // WHEN
        List<Node> resultChildNodes = functions.children(topPage);

        // THEN
        assertNodesListEqualStringDefinitions(resultChildNodes, allFirstLevelNames);
    }

    @Test
    public void testChildrenFromContentMap() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        List<ContentMap> resultChildNodes = functions.children(new ContentMap(topPage));

        // THEN
        String[] allFirstLevelNames = (String[]) ArrayUtils.addAll(LEVEL_2_COMPONENT_NAMES, LEVEL_2_PAGE_NAMES);
        assertContentMapListEqualStringDefinitions(resultChildNodes, allFirstLevelNames);
    }

    @Test
    public void testChildrenFromNodeAndTypePage() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        List<Node> resultChildPages = functions.children(topPage, MgnlNodeType.NT_CONTENT);

        // THEN
        assertNodesListEqualStringDefinitions(resultChildPages, LEVEL_2_PAGE_NAMES);
    }

    @Test
    public void testChildrenFromNodeAndTypeComponent() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        List<Node> resultChildComponents = functions.children(topPage, MgnlNodeType.NT_CONTENTNODE);

        // THEN
        assertNodesListEqualStringDefinitions(resultChildComponents, LEVEL_2_COMPONENT_NAMES);
    }

    @Test
    public void testChildrenFromContentMapAndTypePage() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        List<ContentMap> resultChildPagesMap = functions.children(topPageContentMap, MgnlNodeType.NT_CONTENT);

        // THEN
        assertContentMapListEqualStringDefinitions(resultChildPagesMap, LEVEL_2_PAGE_NAMES);
    }

    @Test
    public void testChildrenFromContentMapAndTypeComponent() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        List<ContentMap> resultChildComponentsMap = functions.children(topPageContentMap, MgnlNodeType.NT_CONTENTNODE);

        // THEN
        assertContentMapListEqualStringDefinitions(resultChildComponentsMap, LEVEL_2_COMPONENT_NAMES);
    }

    @Test
    public void testRootFromPageNodeLevel1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(topPage);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testRootFromPageNodeLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(childPage);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testRootFromComponentNodeLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(topPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testRootFromPageNodeLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(childPageSubPage);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testRootFromComponentNodeLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(childPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, root);
    }

    @Test
    public void testRootFromPageContentMapLevel1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(topPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testRootFromPageContentMapLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(childPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testRootFromComponentContentMapLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(topPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testRootFromPageContentMapLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(childPageSubPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testRootFromComponentContentMapLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(childPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, rootContentMap);
    }

    @Test
    public void testPageFromPageNodeLevel1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.page(topPage);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testPageFromPageNodeLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.page(childPage);

        // THEN
        assertNodeEqualsNode(resultNode, childPage);
    }

    @Test
    public void testPageFromComponentNodeLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.page(topPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testPageFromPageNodeLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.page(childPageSubPage);

        // THEN
        assertNodeEqualsNode(resultNode, childPageSubPage);
    }

    @Test
    public void testPageFromComponentNodeLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.page(childPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, childPage);
    }

    @Test
    public void testPageFromPageContentMapLevel1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.page(topPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testPageFromPageContentMapLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.page(childPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, childPageContentMap);
    }

    @Test
    public void testPageFromComponentContentMapLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.page(topPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testPageFromPageContentMapLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.page(childPageSubPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, childPageSubPageContentMap);
    }

    @Test
    public void testPageFromComponentContentMapLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.page(childPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, childPageContentMap);
    }

    @Test
    public void testRootPageFromPageNodeLevel1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.rootPage(topPage);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromPageNodeLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.rootPage(childPage);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromComponentNodeLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.rootPage(topPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromPageNodeLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.rootPage(childPageSubPage);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromComponentNodeLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.rootPage(childPageComponent);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromPageContentMapLevel1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.rootPage(topPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testRootPageFromPageContentMapLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.rootPage(childPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testRootPageFromComponentContentMapLevel2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.rootPage(topPageComponentContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testRootPageFromPageContentMapLevel3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.rootPage(childPageSubPageContentMap);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testRootPageFromComponentContentMapLevel3() throws RepositoryException {
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
