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
package info.magnolia.cms.util;

import static org.junit.Assert.*;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * Tests for {@link ContentUtil} which rely on an actual repository, i.e using {@link RepositoryTestCase}.
 *
 * @version $Id$
 */
public class ContentUtilRepoTest extends RepositoryTestCase {

    @Test
    public void testSessionBasedCopy() throws RepositoryException{
        HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        Content src = hm.getRoot().createContent("test");
        src.createContent("subnode");
        ContentUtil.copyInSession(src, "/gugu");
        assertTrue(hm.isExist("/gugu"));
        assertTrue(hm.isExist("/gugu/subnode"));
    }

    @Test
    public void testChangeNodeTypeReplaceFirstOccurrenceOnly() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final Content src = hm.getRoot().createContent("test");
        src.createContent("foo");
        src.createContent("bar");
        final String oldUUID = src.getUUID();

        assertEquals("wrong initial type", ItemType.CONTENT.getSystemName() , src.getNodeTypeName());

        ContentUtil.changeNodeType(src, ItemType.CONTENTNODE, false);

        assertTrue(hm.isExist("/test"));
        assertEquals(oldUUID, hm.getContent("/test").getUUID());
        assertEquals(ItemType.CONTENTNODE.getSystemName(), hm.getContent("/test").getNodeTypeName());
        assertEquals(ItemType.CONTENT.getSystemName(), hm.getContent("/test/bar").getNodeTypeName());
        assertEquals(ItemType.CONTENT.getSystemName(), hm.getContent("/test/foo").getNodeTypeName());
    }

    @Test
    public void testChangeNodeTypeReplaceAllOccurrences() throws Exception {
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final Content src = hm.getRoot().createContent("test");
        src.createContent("foo");
        src.createContent("bar");
        final String oldUUID = src.getUUID();
        assertEquals("wrong initial type", ItemType.CONTENT.getSystemName(), src.getNodeTypeName());
        assertEquals("wrong initial type", ItemType.CONTENT.getSystemName(), hm.getContent("/test/bar").getNodeTypeName());
        assertEquals("wrong initial type", ItemType.CONTENT.getSystemName(), hm.getContent("/test/foo").getNodeTypeName());

        ContentUtil.changeNodeType(src, ItemType.CONTENTNODE, true);

        assertTrue(hm.isExist("/test"));
        assertEquals(oldUUID, hm.getContent("/test").getUUID());
        assertEquals(ItemType.CONTENTNODE.getSystemName(), hm.getContent("/test").getNodeTypeName());
        assertEquals(ItemType.CONTENTNODE.getSystemName(), hm.getContent("/test/foo").getNodeTypeName());
        assertEquals(ItemType.CONTENTNODE.getSystemName(), hm.getContent("/test/bar").getNodeTypeName());
    }

}
