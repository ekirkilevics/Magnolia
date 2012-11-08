/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.SiblingsHelper;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.inheritance.InheritanceNodeWrapper;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.rendering.template.configured.ConfiguredInheritance;
import info.magnolia.templating.inheritance.DefaultInheritanceContentDecorator;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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

    private static final String DEPTH_1_FIRST_PAGE_NAME = "page-L1-1";
    private static final String DEPTH_2_FIRST_PAGE_NAME = "page-L2-1";
    private static final String DEPTH_2_FIRST_COMPONENT_NAME = "comp-L2-1";
    private static final String DEPTH_3_FIRST_PAGE_NAME = "page-L3-1";
    private static final String DEPTH_3_FIRST_COMPONENT_NAME = "comp-L3-1";

    private static final String[] DEPTH_1_PAGE_NAMES = {DEPTH_1_FIRST_PAGE_NAME, "page-L1-2", "page-L1-3"};
    private static final String[] DEPTH_2_PAGE_NAMES = {DEPTH_2_FIRST_PAGE_NAME, "page-L2-2", "page-L2-3"};
    private static final String[] DEPTH_2_COMPONENT_NAMES = {DEPTH_2_FIRST_COMPONENT_NAME, "comp-L2-2", "comp-L2-3"};
    private static final String[] DEPTH_3_PAGE_NAMES = {DEPTH_3_FIRST_PAGE_NAME, "page-L3-2", "page-L3-3"};
    private static final String[] DEPTH_3_COMPONENT_NAMES = {DEPTH_3_FIRST_COMPONENT_NAME, "comp-L3-2", "comp-L3-3"};

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

    private TemplatingFunctions functions;

    @Before
    public void setUpNodeHierarchy() throws PathNotFoundException, RepositoryException {
        MockSession session = new MockSession("website");
        root = (MockNode) session.getRootNode();

        topPage = createChildNodes(root, DEPTH_1_PAGE_NAMES, MgnlNodeType.NT_PAGE);
        topPageComponent = createChildNodes(topPage, DEPTH_2_COMPONENT_NAMES, MgnlNodeType.NT_COMPONENT);
        childPage = createChildNodes(topPage, DEPTH_2_PAGE_NAMES, MgnlNodeType.NT_PAGE);
        childPageComponent = createChildNodes(childPage, DEPTH_3_COMPONENT_NAMES, MgnlNodeType.NT_COMPONENT);
        childPageSubPage = createChildNodes(childPage, DEPTH_3_PAGE_NAMES, MgnlNodeType.NT_PAGE);

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
        MockContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
        Provider<AggregationState> aggregationProvider = new Provider<AggregationState>() {
            @Override
            public AggregationState get() {
                return MgnlContext.getAggregationState();
            }
        };
        functions = new TemplatingFunctions(aggregationProvider);
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testAsContentMapfromNode() throws RepositoryException {
        // GIVEN

        // WHEN
        ContentMap result = functions.asContentMap(topPage);

        // THEN
        assertMapEqualsNode(topPage, result);
    }

    @Test
    public void testAsJCRNodeFromContentMap() throws RepositoryException {
        // GIVEN

        // WHEN
        Node resultContenMap = functions.asJCRNode(topPageContentMap);

        // THEN
        assertNodeEqualsMap(topPageContentMap, resultContenMap);
    }



    @Test
    public void testChildrenFromNode() throws RepositoryException {
        // GIVEN
        String[] expectedNamesDepth1 = (String[]) ArrayUtils.addAll(DEPTH_2_COMPONENT_NAMES, DEPTH_2_PAGE_NAMES);

        // WHEN
        List<Node> resultChildNodes = functions.children(topPage);

        // THEN
        assertNodesListEqualStringDefinitions(expectedNamesDepth1, resultChildNodes);
    }

    @Test
    public void testChildrenFromNodeAndTypePage() throws RepositoryException {
        // GIVEN

        // WHEN
        List<Node> resultChildPages = functions.children(topPage, MgnlNodeType.NT_PAGE);

        // THEN
        assertNodesListEqualStringDefinitions(DEPTH_2_PAGE_NAMES, resultChildPages);
    }

    @Test
    public void testChildrenFromNodeAndTypeComponent() throws RepositoryException {
        // GIVEN

        // WHEN
        List<Node> resultChildComponents = functions.children(topPage, MgnlNodeType.NT_COMPONENT);

        // THEN
        assertNodesListEqualStringDefinitions(DEPTH_2_COMPONENT_NAMES, resultChildComponents);
    }

    @Test
    public void testChildrenFromContentMap() throws RepositoryException {
        // GIVEN
        String[] expectedNamesDepth1 = (String[]) ArrayUtils.addAll(DEPTH_2_COMPONENT_NAMES, DEPTH_2_PAGE_NAMES);

        // WHEN
        List<ContentMap> resultChildNodes = functions.children(topPageContentMap);

        // THEN
        assertContentMapListEqualStringDefinitions(expectedNamesDepth1, resultChildNodes);
    }

    @Test
    public void testChildrenFromContentMapAndTypePage() throws RepositoryException {
        // GIVEN

        // WHEN
        List<ContentMap> resultChildPages = functions.children(topPageContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertContentMapListEqualStringDefinitions(DEPTH_2_PAGE_NAMES, resultChildPages);
    }

    @Test
    public void testChildrenFromContentMapAndTypeComponent() throws RepositoryException {
        // GIVEN

        // WHEN
        List<ContentMap> resultChildComponents = functions.children(topPageContentMap, MgnlNodeType.NT_COMPONENT);

        // THEN
        assertContentMapListEqualStringDefinitions(DEPTH_2_COMPONENT_NAMES, resultChildComponents);
    }

    @Test
    public void testRootFromPageNodeDepth1() throws RepositoryException {
        // GIVEN

        // WHEN
        Node resultNode = functions.root(topPage);

        // THEN
        assertNodeEqualsNode(root, resultNode);
    }

    @Test
    public void testRootFromPageNodeDepth2() throws RepositoryException {
        // GIVEN

        // WHEN
        Node resultNode = functions.root(childPage);

        // THEN
        assertNodeEqualsNode(root, resultNode);
    }

    @Test
    public void testRootFromPageNodeDepth3() throws RepositoryException {
        // GIVEN

        // WHEN
        Node resultNode = functions.root(childPageSubPage);

        // THEN
        assertNodeEqualsNode(root, resultNode);
    }

    @Test
    public void testRootFromComponentNodeDepth1() throws RepositoryException {
        // GIVEN

        // WHEN
        Node resultNode = functions.root(topPageComponent);

        // THEN
        assertNodeEqualsNode(root, resultNode);
    }

    @Test
    public void testRootFromComponentNodeDepth2() throws RepositoryException {
        // GIVEN

        // WHEN
        Node resultNode = functions.root(childPageComponent);

        // THEN
        assertNodeEqualsNode(root, resultNode);
    }

    @Test
    public void testRootPageFromPageNodeDepth1() throws RepositoryException {
        // GIVEN

        // WHEN
        Node resultNode = functions.root(topPage, MgnlNodeType.NT_PAGE);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testRootPageFromPageNodeDepth2() throws RepositoryException {
        // GIVEN

        // WHEN
        Node resultNode = functions.root(childPage, MgnlNodeType.NT_PAGE);

        // THEN
        assertNodeEqualsNode(topPage, resultNode);
    }

    @Test
    public void testRootPageFromPageNodeDepth3() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.root(childPageSubPage, MgnlNodeType.NT_PAGE);

        // THEN
        assertNodeEqualsNode(topPage, resultNode);
    }

    @Test
    public void testRootPageFromComponentNodeDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.root(topPageComponent, MgnlNodeType.NT_PAGE);

        // THEN
        assertNodeEqualsNode(topPage, resultNode);
    }

    @Test
    public void testRootPageFromComponentNodeDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.root(childPageComponent, MgnlNodeType.NT_PAGE);

        // THEN
        assertNodeEqualsNode(topPage, resultNode);
    }

    @Test
    public void testRootFromPageContentMapDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.root(topPageContentMap);

        // THEN
        assertMapEqualsMap(rootContentMap, resultContentMap);
    }

    @Test
    public void testRootFromPageContentMapDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.root(childPageContentMap);

        // THEN
        assertMapEqualsMap(rootContentMap, resultContentMap);
    }

    @Test
    public void testRootFromPageContentMapDepth3() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.root(childPageSubPageContentMap);

        // THEN
        assertMapEqualsMap(rootContentMap, resultContentMap);
    }

    @Test
    public void testRootFromComponentContentMapDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.root(topPageComponentContentMap);

        // THEN
        assertMapEqualsMap(rootContentMap, resultContentMap);
    }

    @Test
    public void testRootFromComponentContentMapDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.root(childPageComponentContentMap);

        // THEN
        assertMapEqualsMap(rootContentMap, resultContentMap);
    }

    @Test
    public void testRootPageFromPageContentMapDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.root(topPageContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertNull(resultContentMap);
    }

    @Test
    public void testRootPageFromPageContentMapDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.root(childPageContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertMapEqualsMap(topPageContentMap, resultContentMap);
    }

    @Test
    public void testRootPageFromPageContentMapDepth3() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.root(childPageSubPageContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertMapEqualsMap(topPageContentMap, resultContentMap);
    }

    @Test
    public void testRootPageFromComponentContentMapDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.root(topPageComponentContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertMapEqualsMap(topPageContentMap, resultContentMap);
    }

    @Test
    public void testRootPageFromComponentContentMapDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.root(childPageComponentContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertMapEqualsMap(topPageContentMap, resultContentMap);
    }

    @Test
    public void testParentFromRootNodeShouldBeNull() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.parent(root);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testParentFromNodeDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.parent(topPage);

        // THEN
        assertNodeEqualsNode(root, resultNode);
    }

    @Test
    public void testParentFromNodeDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.parent(childPage);

        // THEN
        assertNodeEqualsNode(topPage, resultNode);
    }

    @Test
    public void testParentFromRootContentMapShouldBeNull() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.parent(root);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testParentFromContentMapDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.parent(topPageContentMap);

        // THEN
        assertMapEqualsMap(rootContentMap, resultContentMap);
    }

    @Test
    public void testParentFromContentMapDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.parent(childPageContentMap);

        // THEN
        assertMapEqualsMap(topPageContentMap, resultContentMap);
    }

    @Test
    public void testParentPageFromPageNode() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.parent(childPage, MgnlNodeType.NT_PAGE);

        // THEN
        assertNodeEqualsNode(topPage, resultNode);
    }

    @Test
    public void testParentComponentFromPageNode() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.parent(childPage, MgnlNodeType.NT_COMPONENT);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testParentPageFromNodeDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.parent(topPage, MgnlNodeType.NT_PAGE);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testParentPageFromComponentNodeDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.parent(topPageComponent, MgnlNodeType.NT_PAGE);

        // THEN
        assertEquals(topPage, resultNode);
    }

    @Test
    public void testParentPageFromComponentNodeDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.parent(childPageComponent, MgnlNodeType.NT_PAGE);

        // THEN
        assertEquals(childPage, resultNode);
    }

    @Test
    public void testParentPageFromComponentNodeDepth3() throws RepositoryException {
        // GIVEN
        Node childPageSubSubComponent = childPageComponent.addNode("subSubComponent", MgnlNodeType.NT_COMPONENT);

        // WHEN
        Node resultNode = functions.parent(childPageSubSubComponent, MgnlNodeType.NT_PAGE);

        // THEN
        assertNodeEqualsNode(childPage, resultNode);
    }

    @Test
    public void testParentPageFromPageContentMap() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.parent(childPageContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertMapEqualsMap(topPageContentMap, resultContentMap);
    }

    @Test
    public void testParentComponentFromPageContentMap() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultNode = functions.parent(childPageContentMap, MgnlNodeType.NT_COMPONENT);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testParentPageFromContentMapDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultNode = functions.parent(topPageContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testParentPageFromComponentContentMapDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.parent(topPageComponentContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertMapEqualsMap(topPageContentMap, resultContentMap);
    }

    @Test
    public void testParentPageFromComponentContentMapDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.parent(childPageComponentContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertMapEqualsMap(childPageContentMap, resultContentMap);
    }

    @Test
    public void testParentPageFromComponentContentMapDepth3() throws RepositoryException {
        // GIVEN
        ContentMap childPageSubSubComponentContentMap = new ContentMap(childPageComponent.addNode("subSubComponent", MgnlNodeType.NT_COMPONENT));

        // WHEN
        ContentMap resultContentMap = functions.parent(childPageSubSubComponentContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertMapEqualsMap(childPageContentMap, resultContentMap);
    }

    @Test
    public void testPageFromRootShouldBeNull() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.page(root);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testPageHavingNoParentPageShouldBeNull() throws RepositoryException {
        // GIVEN
        Node componentWithoutPage = root.addNode("onlyComponent", MgnlNodeType.NT_COMPONENT);

        // WHEN
        Node resultNode = functions.page(componentWithoutPage);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testPageFromNodeDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.page(topPage);

        // THEN
        assertNodeEqualsNode(topPage, resultNode);
    }

    @Test
    public void testPageFromNodeDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.page(childPage);

        // THEN
        assertNodeEqualsNode(childPage, resultNode);
    }

    @Test
    public void testPageFromComponentNodeDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        Node resultNode = functions.page(childPageComponent);

        // THEN
        assertNodeEqualsNode(childPage, resultNode);
    }

    @Test
    public void testPageFromComponentNodeDepth3() throws RepositoryException {
        // GIVEN
        Node subSubComponent = childPageComponent.addNode("subSubCompoent", MgnlNodeType.NT_COMPONENT);

        // WHEN
        Node resultNode = functions.page(subSubComponent);

        // THEN
        assertNodeEqualsNode(childPage, resultNode);
    }

    @Test
    public void testPageContentMapFromRootShouldBeNull() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultNode = functions.page(rootContentMap);

        // THEN
        assertNull(resultNode);
    }

    @Test
    public void testPageContentMapHavingNoParentPageShouldBeNull() throws RepositoryException {
        // GIVEN
        Node componentWithoutPage = root.addNode("onlyComponent", MgnlNodeType.NT_COMPONENT);

        // WHEN
        ContentMap resultContentMap = functions.page(new ContentMap(componentWithoutPage));

        // THEN
        assertNull(resultContentMap);
    }

    @Test
    public void testPageFromContentMapDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.page(topPageContentMap);

        // THEN
        assertMapEqualsMap(topPageContentMap, resultContentMap);
    }

    @Test
    public void testPageFromContentMapDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.page(childPageContentMap);

        // THEN
        assertMapEqualsMap(childPageContentMap, resultContentMap);
    }

    @Test
    public void testPageFromComponentContentMapDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap resultContentMap = functions.page(childPageComponentContentMap);

        // THEN
        assertMapEqualsMap(childPageContentMap, resultContentMap);
    }

    @Test
    public void testPageFromComponentContentMapDepth3() throws RepositoryException {
        // GIVEN
        Node subSubComponent = childPageComponent.addNode("subSubCompoent", MgnlNodeType.NT_COMPONENT);

        // WHEN
        ContentMap resultContentMap = functions.page(new ContentMap(subSubComponent));

        // THEN
        assertMapEqualsMap(childPageContentMap, resultContentMap);
    }

    @Test
    public void testAncestorsFromNodeDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        List<Node> resultList = functions.ancestors(topPage);

        // THEN
        assertEquals(resultList.size(), 0);
    }

    @Test
    public void testAncestorPagesFromNodeDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        List<Node> resultList = functions.ancestors(topPage, MgnlNodeType.NT_PAGE);

        // THEN
        assertEquals(resultList.size(), 0);
    }

    @Test
    public void testAncestorsFromComponentNodeDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        List<Node> resultList = functions.ancestors(topPageComponent);

        // THEN
        assertEquals(resultList.size(), 1);
        assertNodeEqualsNode(topPage, resultList.get(0));
    }

    @Test
    public void testAncestorPagesFromComponentNodeDepth4() throws RepositoryException {
        // GIVEN
        List<Node> compareList = new ArrayList<Node>();
        compareList.add(topPage);
        compareList.add(childPage);
        Iterator<Node> itCompare = compareList.iterator();

        // WHEN
        List<Node> resultList = functions.ancestors(childPageComponent, MgnlNodeType.NT_PAGE);

        // THEN
        assertEquals(compareList.size(), resultList.size());
        for (Iterator<Node> itResult = resultList.iterator(); itResult.hasNext();) {
            assertNodeEqualsNode(itResult.next(), itCompare.next());
        }
    }

    @Test
    public void testAncestorsFromSubComponentNodeDepth5() throws RepositoryException {
        // GIVEN
        Node subSubComponent = childPageComponent.addNode("subSubComponent", MgnlNodeType.NT_COMPONENT);

        List<Node> compareList = new ArrayList<Node>();
        compareList.add(topPage);
        compareList.add(childPage);
        compareList.add(childPageComponent);
        Iterator<Node> itCompare = compareList.iterator();

        // WHEN
        List<Node> resultList = functions.ancestors(subSubComponent);

        // THEN
        assertEquals(compareList.size(), resultList.size());
        for (Iterator<Node> itResult = resultList.iterator(); itResult.hasNext();) {
            assertNodeEqualsNode(itResult.next(), itCompare.next());
        }
    }

    @Test
    public void testAncestorPagesFromSubComponentNodeDepth5() throws RepositoryException {
        // GIVEN
        Node subSubComponent = childPageComponent.addNode("subSubComponent", MgnlNodeType.NT_COMPONENT);

        List<Node> compareList = new ArrayList<Node>();
        compareList.add(topPage);
        compareList.add(childPage);
        Iterator<Node> itCompare = compareList.iterator();

        // WHEN
        List<Node> resultList = functions.ancestors(subSubComponent, MgnlNodeType.NT_PAGE);

        // THEN
        assertEquals(compareList.size(), resultList.size());
        for (Iterator<Node> itResult = resultList.iterator(); itResult.hasNext();) {
            assertNodeEqualsNode(itResult.next(), itCompare.next());
        }
    }

    @Test
    public void testAncestorsFromContentMapDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        List<ContentMap> resultList = functions.ancestors(topPageContentMap);

        // THEN
        assertEquals(resultList.size(), 0);
    }

    @Test
    public void testAncestorPagesFromContentMapDepth1() throws RepositoryException {
        // GIVEN
        // WHEN
        List<ContentMap> resultList = functions.ancestors(topPageContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertEquals(resultList.size(), 0);
    }

    @Test
    public void testAncestorsFromComponentContentMapDepth2() throws RepositoryException {
        // GIVEN
        // WHEN
        List<ContentMap> resultList = functions.ancestors(topPageComponentContentMap);

        // THEN
        assertEquals(resultList.size(), 1);
        assertMapEqualsNode(topPage, resultList.get(0));
    }

    @Test
    public void testAncestorPagesFromComponentContentMapDepth4() throws RepositoryException {
        // GIVEN
        List<Node> compareList = new ArrayList<Node>();
        compareList.add(topPage);
        compareList.add(childPage);
        Iterator<Node> itCompare = compareList.iterator();

        // WHEN
        List<ContentMap> resultList = functions.ancestors(childPageComponentContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertEquals(compareList.size(), resultList.size());
        for (Iterator<ContentMap> itResult = resultList.iterator(); itResult.hasNext();) {
            assertMapEqualsNode(itCompare.next(), itResult.next());
        }
    }

    @Test
    public void testAncestorsFromSubComponentConentMapDepth5() throws RepositoryException {
        // GIVEN
        ContentMap subComponentContentMap = new ContentMap(childPageComponent.addNode("subSubComponent", MgnlNodeType.NT_COMPONENT));

        List<Node> compareList = new ArrayList<Node>();
        compareList.add(topPage);
        compareList.add(childPage);
        compareList.add(childPageComponent);
        Iterator<Node> itCompare = compareList.iterator();

        // WHEN
        List<ContentMap> resultList = functions.ancestors(subComponentContentMap);

        // THEN
        assertEquals(compareList.size(), resultList.size());
        for (Iterator<ContentMap> itResult = resultList.iterator(); itResult.hasNext();) {
            assertMapEqualsNode(itCompare.next(), itResult.next());
        }
    }

    @Test
    public void testAncestorPagesFromSubComponentConentMapDepth5() throws RepositoryException {
        // GIVEN
        ContentMap subComponentContentMap = new ContentMap(childPageComponent.addNode("subSubComponent", MgnlNodeType.NT_COMPONENT));

        List<Node> compareList = new ArrayList<Node>();
        compareList.add(topPage);
        compareList.add(childPage);
        Iterator<Node> itCompare = compareList.iterator();

        // WHEN
        List<ContentMap> resultList = functions.ancestors(subComponentContentMap, MgnlNodeType.NT_PAGE);

        // THEN
        assertEquals(compareList.size(), resultList.size());
        for (Iterator<ContentMap> itResult = resultList.iterator(); itResult.hasNext();) {
            assertMapEqualsNode(itCompare.next(), itResult.next());
        }
    }

    @Test
    public void testNodeIsInherited() throws RepositoryException {
        // GIVEN
        // WHEN
        InheritanceNodeWrapper inheritedNode = wrapNodeForInheritance(childPageSubPage);

        // THEN
        assertTrue(functions.isInherited(inheritedNode.getNode("comp-L2-1")));
        assertTrue(functions.isInherited(inheritedNode.getNode("comp-L2-2")));
        assertTrue(functions.isInherited(inheritedNode.getNode("comp-L2-3")));
    }

    @Test
    public void testNodeIsFromCurrentPage() throws RepositoryException {
        // GIVEN
        // WHEN
        InheritanceNodeWrapper inheritedNode = wrapNodeForInheritance(childPage);

        // THEN
        assertTrue(functions.isFromCurrentPage(inheritedNode.getNode("comp-L3-1")));
    }

    @Test
    public void testContentMapIsInherited() throws RepositoryException {
        // GIVEN
        // WHEN
        InheritanceNodeWrapper inheritedNode = wrapNodeForInheritance(childPageSubPage);

        // THEN
        assertTrue(functions.isInherited(new ContentMap(inheritedNode.getNode("comp-L2-1"))));
        assertTrue(functions.isInherited(new ContentMap(inheritedNode.getNode("comp-L2-2"))));
        assertTrue(functions.isInherited(new ContentMap(inheritedNode.getNode("comp-L2-3"))));
    }

    @Test
    public void testContentMapIsFromCurrentPage() throws RepositoryException {
        // GIVEN
        // WHEN
        InheritanceNodeWrapper inheritedNode = wrapNodeForInheritance(childPage);

        // THEN
        assertTrue(functions.isFromCurrentPage(new ContentMap(inheritedNode.getNode("comp-L3-1"))));
    }

    @Test
    public void testInheritFromNode() throws RepositoryException {
        // GIVEN
        // WHEN
        Node node = functions.inherit(childPage, "comp-L3-1");

        // THEN
        assertNodeEqualsNode(childPageComponent, node);
    }

    @Test
    public void testInheritFromNodeNoContent() throws RepositoryException {
        // GIVEN
        Node content = null;

        // WHEN
        Node node = functions.inherit(content, "comp-L3-1");

        // THEN
        assertEquals(node, null);
    }

    @Test
    public void testInheritFromNodeOnlyContent() throws RepositoryException {
        // GIVEN
        // WHEN
        Node node = functions.inherit(childPage);

        // THEN
        assertEquals(node.getPath(), childPage.getPath());
        assertEquals(node.getIdentifier(), childPage.getIdentifier());
    }

    @Test
    public void testInheritedNodeIsUnwrapped() throws RepositoryException {
        // GIVEN
        // WHEN
        Node node = functions.inherit(childPage, "comp-L3-1");

        // THEN
        assertFalse(node instanceof InheritanceNodeWrapper);
    }

    @Test
    public void testInheritFromContentMap() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap contentMap = functions.inherit(childPageContentMap, "comp-L3-1");

        // THEN
        assertMapEqualsMap(childPageComponentContentMap, contentMap);
    }

    @Test
    public void testInheritFromContentMapOnlyContentMap() throws RepositoryException {
        // GIVEN
        // WHEN
        ContentMap contentMap = functions.inherit(childPageContentMap);

        // THEN
        assertEquals(childPageContentMap.getJCRNode().getParent().getPath(), contentMap.getJCRNode().getParent().getPath());
        assertEquals(childPageContentMap.getJCRNode().getPath(), contentMap.getJCRNode().getPath());
        assertEquals(childPageContentMap.getJCRNode().getIdentifier(), contentMap.getJCRNode().getIdentifier());
    }

    @Test
    public void testNonExistingInheritedRelPathShouldReturnNull() throws RepositoryException {
        // GIVEN
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
        // WHEN
        Property property = functions.inheritProperty(topPage, "iMaybeExistSomewhereElseButNotHere");
        Property property2 = functions.inheritProperty(topPageComponent, "iMaybeExistSomewhereElseButNotHere");

        // THEN
        assertNull(property);
        assertNull(property2);
    }

    @Test
    public void testLinkForPropertyFromNodeDepth1() throws RepositoryException {
        // GIVEN
        String value = "value";
        String name = "myProperty";
        topPage.setProperty(name, value);

        MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        MgnlContext.setInstance(context);

        // WHEN
        String resultLink = functions.link(PropertyUtil.getPropertyOrNull(topPage,name));

        // THEN
        assertEquals(CONTEXT_PATH + topPage.getPath() + "/" + name, resultLink);
    }

    @Test
    public void testLinkForPropertyFromNodeDepth2() throws RepositoryException {
        // GIVEN
        String value = "value";
        String name = "myProperty";
        childPage.setProperty(name, value);

        MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        MgnlContext.setInstance(context);

        // WHEN
        String resultLink = functions.link(PropertyUtil.getPropertyOrNull(childPage,name));

        // THEN
        assertEquals(CONTEXT_PATH + childPage.getPath() + "/" + name, resultLink);
    }

    @Test
    public void testLanguage() throws RepositoryException {
        // GIVEN
        String langue = "fr";
        MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        Locale locale = new Locale(langue);
        context.setLocale(locale);
        MgnlContext.setInstance(context);


        // WHEN
        String langueRes = functions.language();

        // THEN
        assertEquals(langue,langueRes);
    }

    @Test
    public void testLinkForIdentifierFromNodeDepth1() throws RepositoryException {
        // GIVEN
        // TemplatingFunctions functions = new TemplatingFunctions();
        //
        // String identifier = NodeUtil.getNodeIdentifierIfPossible(topPage);
        //
        // // WHEN
        // String resultLink = functions.link(REPOSITORY,identifier);
        //
        // // THEN
        // assertEquals(CONTEXT_PATH + topPage.getPath(), resultLink);

        // FIXME To be implemented when core will commit the new Test Utility
    }

    @Test
    public void testLinkForIdentifierFromNodeDepth2() throws RepositoryException {
        // GIVEN
        // TemplatingFunctions functions = new TemplatingFunctions();
        //
        // String identifier = NodeUtil.getNodeIdentifierIfPossible(topPage);
        //
        // // WHEN
        // String resultLink = functions.link(REPOSITORY,identifier);
        //
        // // THEN
        // assertEquals(CONTEXT_PATH + topPage.getPath(), resultLink);

        // FIXME To be implemented when core will commit the new Test Utility
    }

    @Test
    public void testLinkFromNodeDepth1() throws RepositoryException {
        // GIVEN
                MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        MgnlContext.setInstance(context);

        // WHEN
        String resultLink = functions.link(topPage);

        // THEN
        assertEquals(CONTEXT_PATH + topPage.getPath(), resultLink);
    }

    @Test
    public void testLinkFromNodeDepth2() throws RepositoryException {
        // GIVEN
                MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        MgnlContext.setInstance(context);

        // WHEN
        String resultLink = functions.link(childPage);

        // THEN
        assertEquals(CONTEXT_PATH + childPage.getPath(), resultLink);
    }

    @Test
    public void testLinkFromContentMapDepth1() throws RepositoryException {
        // GIVEN
                MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        MgnlContext.setInstance(context);

        // WHEN
        String resultLink = functions.link(topPageContentMap);

        // THEN
        assertEquals(CONTEXT_PATH + topPageContentMap.get("@path"), resultLink);
    }

    @Test
    public void testLinkFromContentMapDepth2() throws RepositoryException {
        // GIVEN
                MockWebContext context = new MockWebContext();
        context.setContextPath(CONTEXT_PATH);
        MgnlContext.setInstance(context);

        // WHEN
        String resultLink = functions.link(childPageContentMap);

        // THEN
        assertEquals(CONTEXT_PATH + childPageContentMap.get("@path"), resultLink);
    }

    @Test
    public void testExternalLinkFromNodeNoProtocol() {
        // GIVEN
        topPage.setProperty("link", "www.external.ch");
        // WHEN
        String link = functions.externalLink(topPage, "link");

        // THEN
        assertEquals("http://www.external.ch", link);
    }

    @Test
    public void testExternalLinkFromNodeWithProtocol() {
        // GIVEN
        topPage.setProperty("link", "http://www.external.ch");
        // WHEN
        String link = functions.externalLink(topPage, "link");

        // THEN
        assertEquals("http://www.external.ch", link);
    }

    @Test
    public void testExternalLinkTitleFromNodeWithTitle() {
        // GIVEN
        topPage.setProperty("link", "www.external.ch");
        topPage.setProperty("linkTitle", "Link Title");
        // WHEN
        String linkTitle = functions.externalLinkTitle(topPage, "link", "linkTitle");

        // THEN
        assertEquals("Link Title", linkTitle);
    }

    @Test
    public void testExternalLinkTitleFromNodeNoTitleSet() {
        // GIVEN
        topPage.setProperty("link", "www.external.ch");
        // WHEN
        String linkTitle = functions.externalLinkTitle(topPage, "link", "linkTitle");

        // THEN
        assertEquals("http://www.external.ch", linkTitle);
    }

    @Test
    public void testExternalLinkFromContentMapNoProtocol() {
        // GIVEN
        topPage.setProperty("link", "www.external.ch");
        // WHEN
        String link = functions.externalLink(new ContentMap(topPage), "link");

        // THEN
        assertEquals("http://www.external.ch", link);
    }

    @Test
    public void testExternalLinkFromContentMapWithProtocol() {
        // GIVEN
        topPage.setProperty("link", "http://www.external.ch");
        // WHEN
        String link = functions.externalLink(new ContentMap(topPage), "link");

        // THEN
        assertEquals("http://www.external.ch", link);
    }

    @Test
    public void testExternalLinkTitleFromContentMapWithTitle() {
        // GIVEN
        topPage.setProperty("link", "www.external.ch");
        topPage.setProperty("linkTitle", "Link Title");
        // WHEN
        String linkTitle = functions.externalLinkTitle(new ContentMap(topPage), "link", "linkTitle");

        // THEN
        assertEquals("Link Title", linkTitle);
    }

    @Test
    public void testExternalLinkTitleFromContentMapNoTitleSet() {
        // GIVEN
        topPage.setProperty("link", "www.external.ch");
        // WHEN
        String linkTitle = functions.externalLinkTitle(new ContentMap(topPage), "link", "linkTitle");

        // THEN
        assertEquals("http://www.external.ch", linkTitle);
    }

    @Test
    public void testAsNodeList() throws RepositoryException {
        // GIVEN
        List<Node> compareNodeList = new ArrayList<Node>();
        compareNodeList.add(topPage);
        compareNodeList.add(childPage);
        compareNodeList.add(childPageSubPage);
        Iterator<Node> itCompare = compareNodeList.iterator();

        // WHEN
        List<ContentMap> resultMapList = functions.asContentMapList(compareNodeList);

        // THEN
        assertEquals(compareNodeList.size(), resultMapList.size());
        for (Iterator<ContentMap> itResult = resultMapList.iterator(); itResult.hasNext();) {
            assertMapEqualsNode(itCompare.next(), itResult.next());
        }
    }

    @Test
    public void testAsContentMapList() throws RepositoryException {
        // GIVEN
        List<ContentMap> compareNodeList = new ArrayList<ContentMap>();
        compareNodeList.add(topPageContentMap);
        compareNodeList.add(childPageContentMap);
        compareNodeList.add(childPageSubPageContentMap);
        Iterator<ContentMap> itCompare = compareNodeList.iterator();

        // WHEN
        List<Node> resultMapList = functions.asNodeList(compareNodeList);

        // THEN
        assertEquals(compareNodeList.size(), resultMapList.size());
        for (Iterator<Node> itResult = resultMapList.iterator(); itResult.hasNext();) {
            assertNodeEqualsMap(itCompare.next(), itResult.next());
        }
    }

    @Test
    public void testIsEditModeAuthorAndPreview() {
        // GIVEN
        boolean res = false;
        setAdminMode(true);
        setEditMode(true);

        // WHEN
        res = functions.isEditMode();

        // THEN
        assertFalse("Should not be in Edit Mode ", res);
    }

    @Test
    public void testIsEditModeNotAuthorAndPreview() {
        // GIVEN
        boolean res = false;
        setAdminMode(false);
        setEditMode(true);
        // WHEN

        res = functions.isEditMode();

        // THEN
        assertFalse("Should not be in Edit Mode ", res);
    }

    @Test
    public void testIsEditModeAuthorAndNotPreview() {
        // GIVEN
        boolean res = false;
        setAdminMode(true);
        setEditMode(false);
        // WHEN

        res = functions.isEditMode();

        // THEN
        assertTrue("Should be in Edit Mode ", res);
    }

    @Test
    public void testIsEditModeNotAuthorAndNotPreview() {
        // GIVEN
        boolean res = false;
        setAdminMode(false);
        setEditMode(false);
        // WHEN

        res = functions.isEditMode();

        // THEN
        assertFalse("Should not be in Edit Mode ", res);
    }

    @Test
    public void testIsPreviewModeTrue() {
        // GIVEN
        boolean res = false;
        setEditMode(true);
        // WHEN
        res = functions.isPreviewMode();

        // THEN
        assertTrue("Should be in Preview Mode ", res);
    }

    @Test
    public void testIsPreviewModeFalse() {
        // GIVEN
        boolean res = false;
        setEditMode(false);

        // WHEN
        res = functions.isPreviewMode();

        // THEN
        assertFalse("Should Not be in Preview Mode ", res);
    }

    @Test
    public void testIsAuthorInstanceTrue() {
        // GIVEN
        boolean res = false;
        setAdminMode(true);

        // WHEN
        res = functions.isAuthorInstance();
        // THEN
        assertTrue("should be Author ", res);
    }

    @Test
    public void testIsAuthorInstanceFalse() {
        // GIVEN
        boolean res = true;
        setAdminMode(false);

        // WHEN
        res = functions.isAuthorInstance();
        // THEN
        assertFalse("should not be Author ", res);
    }

    @Test
    public void testIsPublicInstanceTrue() {
        // GIVEN
        boolean res = true;
        setAdminMode(true);

        // WHEN
        res = functions.isPublicInstance();
        // THEN
        assertFalse("should be Public ", res);
    }

    @Test
    public void testIsPublicInstanceFalse() {
        // GIVEN
        boolean res = false;
        setAdminMode(false);

        // WHEN
        res = functions.isPublicInstance();
        // THEN
        assertTrue("should not be Public ", res);
    }


    @Test
    public void testCreateHtmlAttribute(){
       // GIVEN
       String value = " value ";
       String name = "name";

       // WHEN
       String res = functions.createHtmlAttribute(name, value);
       // THEN
       assertEquals(name+"=\""+value.trim()+"\"", res);
    }

    @Test
    public void testCreateHtmlAttributeNoValue(){
        // GIVEN
        String value = "";
        String name = "name";

        // WHEN
        String res = functions.createHtmlAttribute(name, value);
        // THEN
        assertEquals("", res);
    }

    @Test
    public void testSiblings() throws RepositoryException{
        // GIVEN

        // WHEN
        SiblingsHelper s = functions.siblings(topPage);
        assertEquals("Indexes are 0-based ", 0, s.getIndex());
        s.next();
        s.next();
        assertEquals("Should have skipped nodes of different type.",2, s.getIndex());
    }

    @Test
    public void testSiblingsContentMap() throws RepositoryException{
        // GIVEN

        // WHEN
        SiblingsHelper s = functions.siblings(topPageContentMap);
        assertEquals("Indexes are 0-based ", 0, s.getIndex());
        s.next();
        s.next();
        assertEquals("Should have skipped nodes of different type.",2, s.getIndex());
    }

    @Test
    public void testMetaDataProperty() throws RepositoryException{
        // GIVEN
        Node myNode = new MockNode();
        myNode.addNode(MetaData.DEFAULT_META_NODE);
        MetaData md = MetaDataUtil.getMetaData(myNode);
        md.setProperty("foo", "bar");
        // WHEN
        String property = functions.metaData(myNode, "foo");
        //THEN
        assertEquals("bar", property);
    }

    @Test
    public void testGetContentByIdentifier() throws RepositoryException{
        // GIVEN
        String id = childPage.getIdentifier();
        Session session = childPage.getSession();
        MockUtil.setSessionAndHierarchyManager(session);
        String repository = session.getWorkspace().getName();

        //THEN

        //get content by identifier when repository was provided
        Node returnedNode1 = functions.contentByIdentifier(repository, id);
        assertEquals(childPage, returnedNode1);

        //get content by identifier when repository wasn't provided -> will taken the default (website)
        Node returnedNode2 = functions.contentByIdentifier(id);
        assertEquals(childPage, returnedNode2);
    }


    /**
     * Set the Server to Admin Mode
     */
    private void setAdminMode(boolean isAdmin) {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);
        if (isAdmin) {
            serverConfiguration.setAdmin(true);
        }
    }

    /**
     * Set the WebContext in Edit Mode
     */
    private void setEditMode(boolean isInEditMode) {
        if (isInEditMode) {
            MgnlContext.getAggregationState().setPreviewMode(true);
        }
    }

    /**
     * Checks each {@link Node} of the @param actualNodeList {@link List} with the names in @param expectedNames {@link String} array.
     * Compares also the size of @param expectedNames {@link String} array compared to the size of the @param actualNodeList {@link List}.
     *
     * @param expectedNames {@link String} array of {@link Node} names compared the names defined in @param actualNodeList {@link List}
     * @param actualNodeList {@link List} containing the {@link ContentMap} names to compare to @param expectedNames {@link String} array
     * @throws RepositoryException
     */
    private void assertNodesListEqualStringDefinitions(String[] expectedNames, List<Node> actualNodeList) throws RepositoryException {
        int i = 0;
        for (Iterator<Node> it = actualNodeList.iterator(); it.hasNext(); i++) {
            Node node = it.next();
            assertEquals(expectedNames[i], node.getName());
        }
    }

    /**
     * Checks each {@link ContentMap} of the @param actualMapList {@link List} with the names in @param expectedNames {@link String} array.
     * Compares also the size of @param expectedNames {@link String} array compared to the size of the @actualMapList {@link List}.
     *
     * @param expectedNames {@link String} array of {@link ContentMap} names compared the names defined in @param actualMapList {@link List}
     * @param actualMapList {@link List} containing the {@link ContentMap} names to compare to @param expectedNames {@link String} array
     * @throws RepositoryException
     */
    private void assertContentMapListEqualStringDefinitions(String[] expectedNames, List<ContentMap> actualMapList) throws RepositoryException {
        int i = 0;
        for (Iterator<ContentMap> it = actualMapList.iterator(); it.hasNext(); i++) {
            ContentMap actualMap = it.next();
            assertEquals(expectedNames[i], actualMap.get("@name"));
        }
    }

    /**
     * Checks all mandatory {@link info.magnolia.nodebuilder.NodeOperationException} values. None should be null and all values should equal.
     *
     * @param expected {@link Node} to compare with @param actual {@link Node}
     * @param actual {@link Node} to compare with @param expected {@link Node}
     * @throws RepositoryException
     */
    private void assertNodeEqualsNode(Node expected, Node actual) throws RepositoryException {
        assertNotNull(actual.getName());
        assertEquals(expected.getName(), actual.getName());
        assertNotNull(actual.getIdentifier());
        assertEquals(expected.getIdentifier(), actual.getIdentifier());
        assertNotNull(actual.getPath());
        assertEquals(expected.getPath(), actual.getPath());
        assertNotNull(actual.getDepth());
        assertEquals(expected.getDepth(), actual.getDepth());
        assertNotNull(actual.getPrimaryNodeType().getName());
        assertEquals(expected.getPrimaryNodeType().getName(), actual.getPrimaryNodeType().getName());
    }

    /**
     * Checks all mandatory {@link ContentMap} values. None should be null and all values should equal.
     *
     * @param expected {@link ContentMap} to compare with @param actual {@link ContentMap}
     * @param actual {@link ContentMap} to compare with @param expected {@link ContentMap}
     */
    private void assertMapEqualsMap(ContentMap expected, ContentMap actual) {
        assertNotNull(actual.get("@name"));
        assertEquals(expected.get("@name"), actual.get("@name"));
        assertNotNull(actual.get("@id"));
        assertEquals(expected.get("@id"), actual.get("@id"));
        assertNotNull(actual.get("@uuid"));
        assertEquals(expected.get("@uuid"), actual.get("@uuid"));
        assertNotNull(actual.get("@path"));
        assertEquals(expected.get("@path"), actual.get("@path"));
        assertNotNull(actual.get("@handle"));
        assertEquals(expected.get("@handle"), actual.get("@handle"));
        assertNotNull(actual.get("@depth"));
        assertEquals(expected.get("@depth"), actual.get("@depth"));
        // TODO cringele: change when SCRUM-303 is solved
        assertNotNull(((NodeType) actual.get("@nodeType")).getName());
        assertEquals(((NodeType) expected.get("@nodeType")).getName(), ((NodeType) actual.get("@nodeType")).getName());
        // TODO cringele: correct code when SCRUM-303 is solved
        // assertNotNull(map1.get("@nodeType"));
        // assertEquals(map1.get("@nodeType"), map2.get("@nodeType"));

    }

    /**
     * Checks all mandatory {@link Node} values. None should be null and all values should equal.
     *
     * @param actual {@link Node} to compare with @param expected {@link ContentMap}
     * @param expected {@link ContentMap} to compare with @param actual {@link Node}
     * @throws RepositoryException
     */
    private void assertNodeEqualsMap(ContentMap expected, Node actual) throws RepositoryException {
        assertNotNull(actual.getName());
        assertEquals(expected.get("@name"), actual.getName());
        assertNotNull(actual.getIdentifier());
        assertEquals(expected.get("@uuid"), actual.getIdentifier());
        assertEquals(expected.get("@id"), actual.getIdentifier());
        assertNotNull(actual.getPath());
        assertEquals(expected.get("@path"), actual.getPath());
        assertEquals(expected.get("@handle"), actual.getPath());
        assertNotNull(actual.getDepth());
        assertEquals(expected.get("@depth"), actual.getDepth());
        assertNotNull(actual.getPrimaryNodeType().getName());
        assertEquals(((NodeType) expected.get("@nodeType")).getName(), actual.getPrimaryNodeType().getName());
    }

    /**
     * Checks all mandatory {@link ContentMap} values. No values should be null and all values should equal.
     *
     * @param expected Expected {@link Node} to compare with @param actual {@link ContentMap}
     * @param actual {@link ContentMap} to compare with @param expected {@link Node}
     * @throws RepositoryException
     */
    private void assertMapEqualsNode(Node expected, ContentMap actual) throws RepositoryException {
        assertNotNull(actual.get("@name"));
        assertEquals(expected.getName(), actual.get("@name"));
        assertNotNull(actual.get("@uuid"));
        assertEquals(expected.getIdentifier(), actual.get("@uuid"));
        assertNotNull(actual.get("@id"));
        assertEquals(expected.getIdentifier(), actual.get("@id"));
        assertNotNull(actual.get("@path"));
        assertEquals(expected.getPath(), actual.get("@path"));
        assertNotNull(actual.get("@handle"));
        assertEquals(expected.getPath(), actual.get("@handle"));
        assertNotNull(actual.get("@depth"));
        assertEquals(expected.getDepth(), actual.get("@depth"));
        assertNotNull(((NodeType) actual.get("@nodeType")).getName());
        assertEquals(expected.getPrimaryNodeType().getName(), ((NodeType) actual.get("@nodeType")).getName());
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
        for (String nodeName : childNodeNames) {
            MockNode child = new MockNode(nodeName, nodeTypeName);
            parent.addNode(child);
        }
        return childNodeNames.length == 0 ? null : (MockNode) parent.getNode(childNodeNames[0]);
    }

    private InheritanceNodeWrapper wrapNodeForInheritance(Node destination) throws RepositoryException {
        ConfiguredInheritance configuration = new ConfiguredInheritance();
        configuration.setEnabled(true);
        configuration.setProperties(ConfiguredInheritance.PROPERTIES_ALL);
        configuration.setComponents(ConfiguredInheritance.COMPONENTS_ALL);
        return (InheritanceNodeWrapper) new DefaultInheritanceContentDecorator(destination, configuration).wrapNode(destination);
    }
}
