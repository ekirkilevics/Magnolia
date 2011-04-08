/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import junit.framework.TestCase;


/**
 * @author pbracher
 * @version $Id$
 */
public class InheritanceContentWrapperTest extends TestCase {

    private HierarchyManager hm;

    protected void setUpContent(String testName) throws Exception {
        final String fileName = getClass().getSimpleName() + "." + testName + ".properties";
        final InputStream stream = getClass().getResourceAsStream(fileName);
        hm = MockUtil.createHierarchyManager(stream);
    }

    @Override
    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testRoot() throws Exception {
        setUpContent("testPropertyInheritance");
        Content root = new InheritanceContentWrapper(hm.getRoot());
        // following will call protected method resolveInnerPath() which in turn calls findAnchor()
        root.getNodeData("whateverThatIsNotOnTheNodeItself");
    }

    public void testPropertyInheritance() throws Exception {
        setUpContent("testPropertyInheritance");
        Content page11 = getWrapped("/page1/page11");
        Content page12 = getWrapped("/page1/page12");
        // direct node data
        assertEquals("page11", page11.getNodeData("nd").getString());
        // inherited from parent
        assertEquals("page1", page12.getNodeData("nd").getString());
    }

    public void testNestedPropertyInheritance() throws Exception {
        setUpContent("testNestedPropertyInheritance");
        Content para = getWrapped("/page1/page11/container/para");
        // inherited from parent
        assertEquals("page1", para.getNodeData("nd").getString());
    }

    public void testSingleParagraphInheritance() throws Exception {
        setUpContent("testSingleParagraphInheritance");
        Content page11 = getWrapped("/page1/page11");
        Content page12 = getWrapped("/page1/page12");
        assertEquals("/page1/page11/para", page11.getContent("para").getHandle());
        assertEquals("/page1/para", page12.getContent("para").getHandle());

        assertFalse(((InheritanceContentWrapper)page11.getContent("para")).isInherited());
        assertTrue(((InheritanceContentWrapper)page12.getContent("para")).isInherited());
    }

    public void testNestedParagraphInheritance() throws Exception {
        setUpContent("testNestedParagraphInheritance");
        Content page11 = getWrapped("/page1/page11");
        Content page12 = getWrapped("/page1/page12");
        Content containerPage11 = page11.getContent("container");
        Content containerPage12 = page12.getContent("container");

        assertEquals("/page1/page11/container/para", containerPage11.getContent("para").getHandle());
        assertEquals("/page1/container/para", containerPage12.getContent("para").getHandle());

        assertEquals("/page1/page11/container/para", page11.getContent("container/para").getHandle());
        assertEquals("/page1/container/para", page12.getContent("container/para").getHandle());

    }

    public void testCollectionInheritance() throws Exception {
        setUpContent("testCollectionInheritance");
        Content page11 = getWrapped("/page1/page11");
        Content page12 = getWrapped("/page1/page12");
        Content page13 = getWrapped("/page1/page13");

        List<Content> col1 = new ArrayList<Content>(page11.getContent("collection").getChildren());
        assertEquals(2, col1.size());
        assertEquals("para11", col1.get(0).getName());
        assertTrue(((InheritanceContentWrapper)col1.get(0)).isInherited());
        assertEquals("para12", col1.get(1).getName());
        assertTrue(((InheritanceContentWrapper)col1.get(1)).isInherited());

        List<Content> col2 = new ArrayList<Content>(page12.getContent("collection").getChildren());
        assertEquals(4, col2.size());
        assertEquals("para11", col2.get(0).getName());
        assertTrue(((InheritanceContentWrapper)col2.get(0)).isInherited());
        assertEquals("para12", col2.get(1).getName());
        assertTrue(((InheritanceContentWrapper)col2.get(1)).isInherited());
        assertEquals("para121", col2.get(2).getName());
        assertFalse(((InheritanceContentWrapper)col2.get(2)).isInherited());
        assertEquals("para122", col2.get(3).getName());
        assertFalse(((InheritanceContentWrapper)col2.get(3)).isInherited());

        // this page has no collection container
        List<Content> col3 = new ArrayList<Content>(page13.getContent("collection").getChildren());
        assertEquals(2, col3.size());
        assertEquals("para11", col3.get(0).getName());
        assertTrue(((InheritanceContentWrapper)col3.get(0)).isInherited());
        assertEquals("para12", col3.get(1).getName());
        assertTrue(((InheritanceContentWrapper)col3.get(1)).isInherited());

    }

    private InheritanceContentWrapper getWrapped(String path) throws RepositoryException {
        Content node = hm.getContent(path);
        InheritanceContentWrapper wrapped = new InheritanceContentWrapper(node);
        return wrapped;
    }
}
