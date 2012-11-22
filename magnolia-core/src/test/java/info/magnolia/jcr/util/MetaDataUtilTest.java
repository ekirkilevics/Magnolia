/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
import info.magnolia.test.RepositoryTestCase;

import java.util.Date;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class MetaDataUtilTest extends RepositoryTestCase {

    private Node root;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        root = session.getRootNode();
    }
    @Test
    public void testGetMetaData() throws Exception {

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

        MetaData metaData = MetaDataUtil.getMetaData(root);

        // WHEN
        MetaDataUtil.updateMetaData(root);

        // THEN
        Date lastMod = metaData.getModificationDate().getTime();
        assertTrue("lastMod hast not been updated in the last 100ms!" , System.currentTimeMillis() - lastMod.getTime() < 100);

        String authorId = metaData.getAuthorId();
        assertEquals(testUserName,authorId);
    }

    @Override
    @After
    public void tearDown(){
        MgnlContext.setInstance(null);
    }
}
