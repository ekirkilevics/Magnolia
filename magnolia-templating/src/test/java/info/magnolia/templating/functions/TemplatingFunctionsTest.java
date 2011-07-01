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
import info.magnolia.cms.core.Content;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.jcr.MockNode;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * Tests.
 *
 * @version $Id$
 */
public class TemplatingFunctionsTest {

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


    /**
     * Checks all mandatory ContentMap values. None should be null and all values should equal.
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
        //TODO cringele: this should work!!
//        assertNotNull(result.getUUID());
//        assertEquals(result.getUUID(), origin.getUUID());
//        assertNotNull(result.getIdentifier());
//        assertEquals(result.getIdentifier(), origin.getUUID());
        assertNotNull(result.getPath());
        assertEquals(result.getPath(), origin.getHandle());
    }



}
