/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.cms.gui.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TreeIconTest {
    
    public static final String ICONDOCROOT = "/.resources/icons/16/"; //$NON-NLS-1$

    public static final String DEFAULT_ICON = ICONDOCROOT + "cubes.gif";

    public static final String DEFAULT_ICON_CONTENT = ICONDOCROOT + "document_plain_earth.gif";

    public static final String DEFAULT_ICON_CONTENTNODE = DEFAULT_ICON;
    
    public static final String DEFAULT_ICON_NODEDATA = ICONDOCROOT + "cube_green.gif";
    
    public static final String DEFAULT_ICON_DELETED = ICONDOCROOT + "document_deleted.gif";
    
    private Tree testTree;
    
    @Before
    public void setUp() throws RepositoryException {
        MockUtil.initMockContext();
        MockSession session = new MockSession("website");
        MockUtil.setSessionAndHierarchyManager(session);
        
        testTree = new Tree("testTree", "website");
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testDeletedIconExistence() {      
        assertNotNull(testTree.getIcon(MgnlNodeType.MIX_DELETED));
    }
    
    @Test
    public void testIconPaths() {
        assertEquals(testTree.getIcon(MgnlNodeType.NT_CONTENT), DEFAULT_ICON_CONTENT);
        assertEquals(testTree.getIcon(MgnlNodeType.NT_CONTENTNODE), DEFAULT_ICON_CONTENTNODE);
        assertEquals(testTree.getIcon(MgnlNodeType.MGNL_NODE_DATA), DEFAULT_ICON_NODEDATA);
        assertEquals(testTree.getIcon(MgnlNodeType.MIX_DELETED), DEFAULT_ICON_DELETED);
    }
}
