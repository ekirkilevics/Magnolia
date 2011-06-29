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
        TemplatingFunctions functions = new TemplatingFunctions();
        String name = "test";
        MockContent content = new MockContent(name);
        //the tested function
        Node result = functions.asJCRNode(content);

        assertEquals(name, result.getName());
    }

    @Test
    public void testAsJCRNodeFromContentMap() throws RepositoryException {
        TemplatingFunctions functions = new TemplatingFunctions();
        String name = "test";
        MockNode root = new MockNode(name);
        ContentMap map = new ContentMap(root);
        //the tested function
        Node result = functions.asJCRNode(map);

        assertEquals(name, result.getName());
    }

    @Test
    public void testParentFromNode() throws RepositoryException {
        TemplatingFunctions functions = new TemplatingFunctions();
        MockNode parent = new MockNode("parent");
        MockNode child = new MockNode("child");
        parent.addNode(child);
        //the tested function
        Node result = functions.parent(parent);

        assertEquals(parent.getName(), result.getName());
        assertEquals(parent.getUUID(), result.getUUID());
        assertEquals(parent.getIdentifier(), result.getIdentifier());
        assertEquals(parent.getPath(), result.getPath());
    }

    @Test
    public void testParentFromRootNodeShouldBeNull() throws RepositoryException {
        TemplatingFunctions functions = new TemplatingFunctions();
        MockNode parent = new MockNode("parent");
        //the tested function
        Node result = functions.parent(parent);
        assertEquals(result, null);
    }

    @Test
    public void testParentFromContentMap() throws RepositoryException {
        TemplatingFunctions functions = new TemplatingFunctions();
        MockNode parent = new MockNode("parent");
        MockNode child = new MockNode("child");
        parent.addNode(child);
        ContentMap childMap = new ContentMap(child);
        ContentMap parentMap = new ContentMap(parent);
        //the tested function
        ContentMap resultMap = functions.parent(childMap);

        assertNotNull(resultMap.get("@name"));
        assertEquals(parentMap.get("@name"), resultMap.get("@name"));
        assertNotNull(resultMap.get("@id"));
        assertEquals(parentMap.get("@id"), resultMap.get("@id"));
        assertNotNull(resultMap.get("@path"));
        assertEquals(parentMap.get("@path"), resultMap.get("@path"));
        //TODO cringele: should they work too?
//        assertNotNull(resultMap.get("@level"));
//        assertEquals(parentMap.get("@level"), resultMap.get("@level"));
//        assertNotNull(resultMap.get("@nodeType"));
//        assertEquals(parentMap.get("@nodeType"), resultMap.get("@nodeType"));
    }

}
