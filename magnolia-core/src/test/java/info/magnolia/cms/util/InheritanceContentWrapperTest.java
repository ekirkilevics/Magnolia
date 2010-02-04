/**
 * This file Copyright (c) 2008-2009 Magnolia International
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

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.ArrayUtils;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.test.mock.MockUtil;
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

        Collection col1 = page11.getContent("collection").getChildren();
        asertEquals(col1, new String[]{"para11","para12"});

        Collection col2 = page12.getContent("collection").getChildren();
        asertEquals(col2, new String[]{"para11","para12", "para121", "para122"});

        // this page has no collection container
        Collection col3 = page13.getContent("collection").getChildren();
        asertEquals(col3, new String[]{"para11","para12"});

    }

    private void asertEquals(Collection col, String[] names) {
        final String msg = "wrong set of paragraphs found. expected <" + ArrayUtils.toString(names)+ "> actual:<" + col + ">";

        if(col.size() != names.length){
            fail(msg);
        }
        int i = 0;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Content found = (Content) iterator.next();
            if(!found.getName().equals(names[i])){
                fail(msg);
            }
            i++;
        }
    }

    private InheritanceContentWrapper getWrapped(String path) throws RepositoryException {
        Content node = hm.getContent(path);
        InheritanceContentWrapper wrapped = new InheritanceContentWrapper(node);
        return wrapped;
    }
}
