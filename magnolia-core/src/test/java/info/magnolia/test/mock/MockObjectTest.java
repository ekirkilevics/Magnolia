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
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;

import java.util.GregorianCalendar;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class MockObjectTest extends TestCase {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MockObjectTest.class);

    public void testRootNodeOfHierarchyManger() throws RepositoryException {
        MockHierarchyManager hm = new MockHierarchyManager();
        Content root = hm.getRoot();
        assertEquals(root.getName(), "jcr:root");
    }

    public void testCreatingANode() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        MockHierarchyManager hm = new MockHierarchyManager();
        hm.createContent("/", "test1", ItemType.CONTENTNODE.getSystemName());
        assertEquals(hm.getContent("/test1").getName(), "test1");
    }

    public void testCreatingASubNode() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        MockHierarchyManager hm = new MockHierarchyManager();
        hm.createContent("/test/sub", "test1", ItemType.CONTENTNODE.getSystemName());
        assertEquals(hm.getContent("/test/sub/test1").getName(), "test1");
    }

    public void testGetANodeAddedToASubNode() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        MockHierarchyManager hm = new MockHierarchyManager();
        Content parent = hm.createContent("/test/sub", "test1", ItemType.CONTENTNODE.getSystemName());
        parent.createContent("other", new ItemType("mgnl:test"));
        Content node = hm.getContent("/test/sub/test1/other");

        assertEquals(node.getName(), "other");
        assertEquals(node.getItemType().getSystemName(), "mgnl:test");
        assertEquals(node.getHandle(), "/test/sub/test1/other");
        assertEquals(((MockContent) node).getHierarchyManager(), hm);
    }

    public void testSetABooleanValueOnANodeData() {
        MockNodeData nd = new MockNodeData("test", new Boolean(true));
        assertEquals(true, nd.getBoolean());
    }

    public void testSetAnObjectAndResolvePropertyType() {
        MockNodeData ndBoolean = new MockNodeData("test", new Boolean(true));
        MockNodeData ndLong = new MockNodeData("test", new Long(5));
        MockNodeData ndDate = new MockNodeData("test", new GregorianCalendar(2007, 2, 14));

        assertEquals(true, ndBoolean.getBoolean());
        assertEquals(PropertyType.BOOLEAN, ndBoolean.getType());

        assertEquals(5, ndLong.getLong());
        assertEquals(PropertyType.LONG, ndLong.getType());

        assertEquals(new GregorianCalendar(2007, 2, 14), ndDate.getDate());
        assertEquals(PropertyType.DATE, ndDate.getType());
    }

    public void testDeletingReallyWorks() throws Exception {
        MockHierarchyManager hm = new MockHierarchyManager();
        Content node = hm.createContent("/test/sub", "test1", ItemType.CONTENTNODE.getSystemName());
        assertEquals(node, hm.getContent("/test/sub/test1"));
        node.delete();
        try {
            hm.getContent("/test/sub").getContent("test1");
            fail("should have failed");
        } catch (PathNotFoundException e) {
            assertEquals("test1", e.getMessage());
        }
        try {
            hm.getContent("/test/sub/test1");
            fail("should have failed");
        } catch (PathNotFoundException e) {
            assertEquals("test1", e.getMessage());
        }
    }

}
