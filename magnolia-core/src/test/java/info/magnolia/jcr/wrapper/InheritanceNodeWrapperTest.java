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
package info.magnolia.jcr.wrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;


/**
 * @version $Id$
 */
public class InheritanceNodeWrapperTest {

    private Session session;

    protected void setUpNode(String testName) throws Exception {
        final String fileName = getClass().getSimpleName() + "." + testName + ".properties";
        final InputStream stream = getClass().getResourceAsStream(fileName);
        session = SessionTestUtil.createSession("website", stream);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testRoot() throws Exception {
        setUpNode("testPropertyInheritance");
        Node root = new InheritanceNodeWrapper(session.getRootNode());
        // following will call protected method resolveInnerPath() which in turn calls findAnchor()
        root.getProperty("whateverThatIsNotOnTheNodeItself");
    }

    @Test
    public void testPropertyInheritance() throws Exception {
        setUpNode("testPropertyInheritance");
        Node page11 = getWrapped("/page1/page11");
        Node page12 = getWrapped("/page1/page12");
        // direct node data
        assertEquals("page11", page11.getProperty("nd").getString());
        // inherited from parent
        assertEquals("page1", page12.getProperty("nd").getString());
    }

    @Test
    public void testNestedPropertyInheritance() throws Exception {
        setUpNode("testNestedPropertyInheritance");
        Node comp = getWrapped("/page1/page11/container/comp");
        // inherited from parent
        assertEquals("page1", comp.getProperty("nd").getString());
    }

    @Test
    public void testSingleComponentInheritance() throws Exception {
        setUpNode("testSingleComponentInheritance");
        Node page11 = getWrapped("/page1/page11");
        Node page12 = getWrapped("/page1/page12");
        assertEquals("/page1/page11/comp", page11.getNode("comp").getPath());
        assertEquals("/page1/comp", page12.getNode("comp").getPath());

        assertFalse(((InheritanceNodeWrapper)page11.getNode("comp")).isInherited());
        assertTrue(((InheritanceNodeWrapper)page12.getNode("comp")).isInherited());
    }

    @Test
    public void testNestedComponentInheritance() throws Exception {
        setUpNode("testNestedComponentInheritance");
        Node page11 = getWrapped("/page1/page11");
        Node page12 = getWrapped("/page1/page12");
        Node containerPage11 = page11.getNode("container");
        Node containerPage12 = page12.getNode("container");

        assertEquals("/page1/page11/container/comp", containerPage11.getNode("comp").getPath());
        assertEquals("/page1/container/comp", containerPage12.getNode("comp").getPath());

        assertEquals("/page1/page11/container/comp", page11.getNode("container/comp").getPath());
        assertEquals("/page1/container/comp", page12.getNode("container/comp").getPath());

    }

    @Ignore
    //TODO fgrilli
    public void testCollectionInheritance() throws Exception {
        setUpNode("testCollectionInheritance");
        Node page11 = getWrapped("/page1/page11");
        Node page12 = getWrapped("/page1/page12");
        Node page13 = getWrapped("/page1/page13");

        /*List<Node> col1 = new ArrayList<Node>(page11.getNode("collection").getNodes());
        assertEquals(2, col1.size());
        assertEquals("comp11", col1.get(0).getName());
        assertTrue(((InheritanceNodeWrapper)col1.get(0)).isInherited());
        assertEquals("comp12", col1.get(1).getName());
        assertTrue(((InheritanceNodeWrapper)col1.get(1)).isInherited());

        List<Node> col2 = new ArrayList<Node>(page12.getNode("collection").getNodes());
        assertEquals(4, col2.size());
        assertEquals("comp11", col2.get(0).getName());
        assertTrue(((InheritanceNodeWrapper)col2.get(0)).isInherited());
        assertEquals("comp12", col2.get(1).getName());
        assertTrue(((InheritanceNodeWrapper)col2.get(1)).isInherited());
        assertEquals("comp121", col2.get(2).getName());
        assertFalse(((InheritanceNodeWrapper)col2.get(2)).isInherited());
        assertEquals("comp122", col2.get(3).getName());
        assertFalse(((InheritanceNodeWrapper)col2.get(3)).isInherited());

        // this page has no collection container
        List<Node> col3 = new ArrayList<Node>(page13.getNode("collection").getNodes());
        assertEquals(2, col3.size());
        assertEquals("comp11", col3.get(0).getName());
        assertTrue(((InheritanceNodeWrapper)col3.get(0)).isInherited());
        assertEquals("comp12", col3.get(1).getName());
        assertTrue(((InheritanceNodeWrapper)col3.get(1)).isInherited());
        */

    }

    private Node getWrapped(String absPath) throws RepositoryException {
        Node node = session.getNode(absPath);
        InheritanceNodeWrapper wrapped = new InheritanceNodeWrapper(node);
        return wrapped;
    }
}
