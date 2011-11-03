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
package info.magnolia.jcr.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class MetaDataUtilTest {

    private MockNode root;

    @Before
    public void setUp() {
        MgnlContext.setInstance(null);
        MockSession session = new MockSession("MetaDataTest");
        root = (MockNode) session.getRootNode();
    }
    @Test
    public void testGetMetaData() throws Exception {
        // GIVEN
        root.addNode(MetaData.DEFAULT_META_NODE);

        // WHEN
        MetaData md = MetaDataUtil.getMetaData(root);

        // THEN
        assertNotNull(md);
    }

    @Test
    public void testUpdateMetaData() throws RepositoryException{
        // GIVEN
        final String testUserName = "test";
        final Context ctx = mock(Context.class);
        final User user = mock(User.class);
        MgnlContext.setInstance(ctx);

        when(ctx.getUser()).thenReturn(user);
        when(user.getName()).thenReturn(testUserName);

        Node metaDataNode = root.addNode(MetaData.DEFAULT_META_NODE);

        // WHEN
        MetaDataUtil.updateMetaData(root);

        // THEN
        Property lastModProperty = metaDataNode.getProperty(RepositoryConstants.NAMESPACE_PREFIX + ":" + MetaData.LAST_MODIFIED);
        assertTrue(System.currentTimeMillis() - lastModProperty.getDate().getTimeInMillis() < 100);

        Property authorProperty = metaDataNode.getProperty(RepositoryConstants.NAMESPACE_PREFIX + ":" + MetaData.AUTHOR_ID);
        assertEquals(testUserName,authorProperty.getString());
    }
}
