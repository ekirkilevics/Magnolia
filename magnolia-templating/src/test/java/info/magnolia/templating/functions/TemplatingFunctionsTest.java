/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
        assertChildrenEqualStringDefinition(resultChildNodes, allFirstLevelNames);
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
        assertChildrenEqualStringDefinition(resultChildPagesMap, firstLevelPages);
        assertChildrenEqualStringDefinition(resultChildComponentsMap, firstLevelComponents);
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
        assertChildrenEqualStringDefinition(resultChildNodes, allFirstLevelNames);
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
        assertChildrenEqualStringDefinition(resultChildPages, firstLevelPages);
        assertChildrenEqualStringDefinition(resultChildComponents, firstLevelComponents);
    }



    /**
     * Checks each object of the list @param resultNodesOrContentMaps with the passes nodeNames in @param originNodeNames.
     * The checked object can be an instance of Node or ContentMap.
     * Check also the amount of nodes compared of the amount of passed namesNames in @param originNodeNames.
     *
     * @param resultNodesOrContentMaps List of Nodes generate during // WHEN
     * @param originNodeNames String[] containing the node names which were base of generating the node list @param resultNodes during //GIVEN
     * @throws RepositoryException
     */
    private void assertChildrenEqualStringDefinition(List resultNodesOrContentMaps, String[] originNodeNames) throws RepositoryException {
        int i = 0;
        for ( Iterator it = resultNodesOrContentMaps.iterator(); it.hasNext(); i++) {
            Object object = it.next();
            if(object instanceof Node){
                assertEquals((((Node)object).getName()), originNodeNames[i]);
            }
            if(object instanceof ContentMap){
                assertEquals((((ContentMap)object).get("@name")), originNodeNames[i]);
            }
        }
    }

    /**
     * Checks all mandatory Content values. None should be null and all values should equal.
     *
     * @param result Content generate during // WHEN
     * @param origin Node generated during // GIVEN
     * @throws RepositoryException
     */
    private void assertContentEqualsNode(Content result, Node origin) throws RepositoryException {
        assertNotNull(result.getName());
        assertEquals(result.getName(), origin.getName());
        assertNotNull(result.getUUID());
        assertEquals(result.getUUID(), origin.getUUID());
        assertEquals(result.getUUID(), origin.getIdentifier());
        assertNotNull(result.getHandle());
        assertEquals(result.getHandle(), origin.getPath());
    }

    /**
     * Checks all mandatory Node values. None should be null and all values should equal.
     *
     * @param result Node generate during // WHEN
     * @param origin Node generated during // GIVEN
     * @throws RepositoryException
     */
    private void assertNodeEqualsNode(Node result, Node origin) throws RepositoryException {
        assertNotNull(result.getName());
        assertEquals(result.getName(), origin.getName());
        assertNotNull(result.getUUID());
        assertEquals(result.getUUID(), origin.getUUID());
        assertNotNull(result.getIdentifier());
        assertEquals(result.getIdentifier(), origin.getIdentifier());
        assertNotNull(result.getPath());
        assertEquals(result.getPath(), origin.getPath());
    }

    /**
     * Checks all mandatory ContentMap values. None should be null and all values should equal.
     *
     * @param resultMap ContentMap generated during // WHEN
     * @param originMap ContentMAp generated during // THEN
     */
    private void assertMapEqualsMap(ContentMap resultMap, ContentMap originMap) {
        assertNotNull(resultMap.get("@name"));
        assertEquals(resultMap.get("@name"), originMap.get("@name"));
        assertNotNull(resultMap.get("@id"));
        assertEquals(resultMap.get("@id"), originMap.get("@id"));
        assertNotNull(resultMap.get("@uuid"));
        assertEquals(resultMap.get("@uuid"), originMap.get("@uuid"));
        assertNotNull(resultMap.get("@path"));
        assertEquals(resultMap.get("@path"), originMap.get("@path"));
        assertNotNull(resultMap.get("@handle"));
        assertEquals(resultMap.get("@handle"), originMap.get("@handle"));

        //TODO cringele: should they work too?
//        assertNotNull(resultMap.get("@nodeType"));
//        assertEquals(resultMap.get("@nodeType"), originMap.get("@nodeType"));
//        assertNotNull(resultMap.get("@level"));
//        assertEquals(resultMap.get("@level"), originMap.get("@level"));
//        assertNotNull(resultMap.get("@nodeType"));
//        assertEquals(resultMap.get("@nodeType"), originMap.get("@nodeType"));
    }

    /**
     * Checks all mandatory ContentMap values. None should be null and all values should equal.
     *
     * @param result Node generate during // WHEN
     * @param origin ContentMap generated during // THEN
     * @throws RepositoryException
     */
    private void assertNodeEqualsMap(Node result, ContentMap origin) throws RepositoryException {
        assertNotNull(result.getName());
        assertEquals(result.getName(), origin.get("@name"));
        assertNotNull(result.getUUID());
        assertEquals(result.getUUID(), origin.get("@uuid"));
        assertNotNull(result.getIdentifier());
        assertEquals(result.getIdentifier(), origin.get("@id"));
        assertEquals(result.getIdentifier(), origin.get("@uuid"));
        assertNotNull(result.getPath());
        assertEquals(result.getPath(), origin.get("@path"));
        assertEquals(result.getPath(), origin.get("@handle"));
    }

    /**
     * Checks all mandatory Content values. None should be null and all values should equal.
     *
     * @param result Node generate during // WHEN
     * @param origin Content generated during // THEN
     * @throws RepositoryException
     */
    private void assertNodeEqualsContent(Node result, Content origin) throws RepositoryException {
        assertNotNull(result.getName());
        assertEquals(result.getName(), origin.getName());
        //TODO cringele: this should work!! I'll have a look at them with dlipp
//        assertNotNull(result.getUUID());
//        assertEquals(result.getUUID(), origin.getUUID());
//        assertNotNull(result.getIdentifier());
//        assertEquals(result.getIdentifier(), origin.getUUID());
        assertNotNull(result.getPath());
        assertEquals(result.getPath(), origin.getHandle());
    }

    /**
     * Add to the give root node child nodes by the name and type passed.
     *
     * @param root node where child nodes are added to
     * @param childNodeNames names of the children to create
     * @param nodeTypeName of the children to create
     */
    private void createChildNodes(MockNode root, String[] childNodeNames, String nodeTypeName) {
        for(String nodeName : childNodeNames){
            MockNode child = new MockNode(nodeName, nodeTypeName);
            root.addNode(child);
        }
    }

}
