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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.wrapper.InheritanceNodeWrapper;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
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
    public void setUpNodeHierarchy() throws PathNotFoundException, RepositoryException{
        root = new MockNode();
        root.setSession(new MockSession("website"));

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

        LinkTransformerManager linkTransformerManager = new LinkTransformerManager();
        linkTransformerManager.setAddContextPathToBrowserLinks(true);
        ComponentsTestUtil.setInstance(LinkTransformerManager.class, linkTransformerManager);

        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
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
    public void testLinkFromContentMapDepth2() throws RepositoryException {
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
    public void testPageFromComponentNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.parent(topPageComponent, MgnlNodeType.NT_CONTENT);

        // THEN
        assertEquals(resultNode, topPage);
    }

    @Test
    public void testPageFromComponentNodeDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.parent(childPageComponent, MgnlNodeType.NT_CONTENT);

        // THEN
        assertNodeEqualsNode(resultNode, childPage);
    }

    @Test
    public void testPageFromPageNode() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.parent(childPage, MgnlNodeType.NT_CONTENT);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testPageFromPageContentMap() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.parent(childPageContentMap, MgnlNodeType.NT_CONTENT);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testPageFromComponentContentMapDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.parent(topPageComponentContentMap, MgnlNodeType.NT_CONTENT);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testPageFromComponentContentMapDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.parent(childPageComponentContentMap, MgnlNodeType.NT_CONTENT);

        // THEN
        assertMapEqualsMap(resultContentMap, childPageContentMap);
    }

    @Test
    public void testRootPageFromPageNodeDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(topPage, MgnlNodeType.NT_CONTENT);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testRootPageFromPageNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(childPage, MgnlNodeType.NT_CONTENT);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromComponentNodeDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(topPageComponent, MgnlNodeType.NT_CONTENT);

        // THEN
        assertNodeEqualsNode(resultNode, topPage);
    }

    @Test
    public void testRootPageFromPageNodeDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(childPageSubPage, MgnlNodeType.NT_CONTENT);

        // THEN
        assertNodeEqualsNode(resultNode, childPage);
    }

    @Test
    public void testRootPageFromComponentNodeDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        Node resultNode = functions.root(childPageComponent, MgnlNodeType.NT_CONTENT);

        // THEN
        assertNodeEqualsNode(resultNode, childPage);
    }

    @Test
    public void testRootPageFromPageContentMapDepth1() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(topPageContentMap, MgnlNodeType.NT_CONTENT);

        // THEN
        assertNull(resultContentMap);
    }

    @Test
    public void testRootPageFromPageContentMapDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(childPageContentMap, MgnlNodeType.NT_CONTENT);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testRootPageFromComponentContentMapDepth2() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(topPageComponentContentMap, MgnlNodeType.NT_CONTENT);

        // THEN
        assertMapEqualsMap(resultContentMap, topPageContentMap);
    }

    @Test
    public void testRootPageFromPageContentMapDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(childPageSubPageContentMap, MgnlNodeType.NT_CONTENT);

        // THEN
        assertMapEqualsMap(resultContentMap, childPageContentMap);
    }

    @Test
    public void testRootPageFromComponentContentMapDepth3() throws RepositoryException {
        // GIVEN
        TemplatingFunctions functions = new TemplatingFunctions();

        // WHEN
        ContentMap resultContentMap = functions.root(childPageComponentContentMap, MgnlNodeType.NT_CONTENT);

        // THEN
        assertMapEqualsMap(resultContentMap, childPageContentMap);
    }

     @Test
     public void testAncestorsFromComponentNodeDepth1() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         List<Node> resultList = functions.ancestors(topPage);

         // THEN
         assertEquals(resultList.size(), 0);
     }

     @Test
     public void testAncestorsFromNodeDepth1() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         List<Node> resultList = functions.ancestors(topPage);

         // THEN
         assertEquals(resultList.size(), 0);
     }

     @Test
     public void testAncestorPagesFromNodeDepth1() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         List<Node> resultList = functions.ancestors(topPage, MgnlNodeType.NT_CONTENT);

         // THEN
         assertEquals(resultList.size(), 0);
     }

     @Test
     public void testAncestorPagesFromComponentNodeDepth4() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();
         List<Node> compareList = new ArrayList<Node>();
         compareList.add(topPage);
         compareList.add(childPage);
         Iterator<Node> itCompare = compareList.iterator();

         // WHEN
         // gets into endless loop.
         List<Node> resultList = functions.ancestors(childPageComponent, MgnlNodeType.NT_CONTENT);

         // THEN
         assertEquals(resultList.size(), compareList.size());
         for(Iterator<Node> itResult=resultList.iterator(); itResult.hasNext();){
            assertNodeEqualsNode(itResult.next(), itCompare.next());
         }
     }

     @Test
     public void testAncestorsFromComponentNodeDepth5() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();
         MockNode subComponent = new MockNode("subComponent", MgnlNodeType.NT_CONTENT);
         childPageComponent.addNode(subComponent);

         List<Node> compareList = new ArrayList<Node>();
         compareList.add(topPage);
         compareList.add(childPage);
         compareList.add(childPageComponent);
         Iterator<Node> itCompare = compareList.iterator();

         // WHEN
         List<Node> resultList = functions.ancestors(subComponent);

         // THEN
         assertEquals(resultList.size(), compareList.size());
         for(Iterator<Node> itResult=resultList.iterator(); itResult.hasNext();){
            assertNodeEqualsNode(itResult.next(), itCompare.next());
         }
     }

     @Test
     public void testNodeIsInherited() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         InheritanceNodeWrapper inheritedNode = new InheritanceNodeWrapper(childPage);

         // THEN
         assertTrue(functions.isInherited(inheritedNode.getNode("comp-L2-1")));
         assertTrue(functions.isInherited(inheritedNode.getNode("comp-L2-2")));
         assertTrue(functions.isInherited(inheritedNode.getNode("comp-L2-3")));
    }

     @Test
     public void testNodeIsFromCurrentPage() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         InheritanceNodeWrapper inheritedNode = new InheritanceNodeWrapper(childPage);

         // THEN
         assertTrue(functions.isFromCurrentPage(inheritedNode.getNode("comp-L3-1")));
     }

     @Test
     public void testContentMapIsInherited() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         InheritanceNodeWrapper inheritedNode = new InheritanceNodeWrapper(childPage);

         // THEN
         assertTrue(functions.isInherited(new ContentMap(inheritedNode.getNode("comp-L2-1"))));
         assertTrue(functions.isInherited(new ContentMap(inheritedNode.getNode("comp-L2-2"))));
         assertTrue(functions.isInherited(new ContentMap(inheritedNode.getNode("comp-L2-3"))));
     }

     @Test
     public void testContentMapIsFromCurrentPage() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         InheritanceNodeWrapper inheritedNode = new InheritanceNodeWrapper(childPage);

         // THEN
         assertTrue(functions.isFromCurrentPage(new ContentMap(inheritedNode.getNode("comp-L3-1"))));
     }

     @Test
     public void testInheritFromNode() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         Node node = functions.inherit(childPage, "comp-L3-1");

         // THEN
         assertNodeEqualsNode(node, childPageComponent);
     }

     @Test
     public void testInheritedNodeIsUnwrapped() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         Node node = functions.inherit(childPage, "comp-L3-1");

         // THEN
         assertFalse(node instanceof InheritanceNodeWrapper);
     }

     @Test
     public void testInheritFromContentMap() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         ContentMap contentMap = functions.inherit(childPageContentMap, "comp-L3-1");

         // THEN
         assertMapEqualsMap(contentMap, childPageComponentContentMap);
     }

     @Test
     public void testNonExistingInheritedRelPathShouldReturnNull() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         ContentMap resultContentMap = functions.inherit(childPageContentMap, "iMaybeExistSomewhereElseButNotHere");
         Node node = functions.inherit(childPage, "iMaybeExistSomewhereElseButNotHere");

         // THEN
         assertNull(resultContentMap);
         assertNull(node);
     }

     @Test
     public void testNonExistingInheritedPropertyShouldReturnNull() throws RepositoryException {
         // GIVEN
         TemplatingFunctions functions = new TemplatingFunctions();

         // WHEN
         Property property = functions.inheritProperty(topPage, "iMaybeExistSomewhereElseButNotHere");
         Property property2 = functions.inheritProperty(topPageComponent, "iMaybeExistSomewhereElseButNotHere");

         // THEN
         assertNull(property);
         assertNull(property2);
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
     * Checks all mandatory Node values. None should be null and all values should equal.
     *
     * @param node1 Node to compare with Node-2
     * @param node2 Node to compare with Node-1
     * @throws RepositoryException
     */
    private void assertNodeEqualsNode(Node node1, Node node2) throws RepositoryException {
        assertNotNull(node1.getName());
        assertEquals(node1.getName(), node2.getName());
        assertNotNull(node1.getIdentifier());
        assertEquals(node1.getIdentifier(), node2.getIdentifier());
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
        //TODO cringele: change when SCRUM-303 is solved
        assertNotNull(((NodeType)map1.get("@nodeType")).getName());
        assertEquals(((NodeType)map1.get("@nodeType")).getName(), ((NodeType)map2.get("@nodeType")).getName());
        //TODO cringele: correct code when SCRUM-303 is solved
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
        assertNotNull(node.getIdentifier());
        assertEquals(node.getIdentifier(), map.get("@uuid"));
        assertNotNull(node.getIdentifier());
        assertEquals(node.getIdentifier(), map.get("@id"));
        assertEquals(node.getIdentifier(), map.get("@uuid"));
        assertNotNull(node.getPath());
        assertEquals(node.getPath(), map.get("@path"));
        assertEquals(node.getPath(), map.get("@handle"));
        assertNotNull(node.getDepth());
        assertEquals(node.getDepth(), map.get("@depth"));
        assertNotNull(node.getPrimaryNodeType().getName());
        assertEquals(node.getPrimaryNodeType().getName(), ((NodeType)map.get("@nodeType")).getName());
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
