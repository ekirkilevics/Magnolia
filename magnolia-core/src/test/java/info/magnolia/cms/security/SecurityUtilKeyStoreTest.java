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
package info.magnolia.cms.security;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockWebContext;

import java.io.File;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SecurityUtilKeyStoreTest extends MgnlTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ModuleRegistry registry = mock(ModuleRegistry.class);
        Session session = mock(Session.class);
        Node node = mock(Node.class);
        ContextFactory factory = mock(ContextFactory.class);
        SystemContext context = mock(SystemContext.class);
        MockWebContext mctx = new MockWebContext();

        ComponentsTestUtil.setInstance(ModuleRegistry.class, registry);
        ComponentsTestUtil.setInstance(ContextFactory.class, factory);

        when(factory.getSystemContext()).thenReturn(context);
        when(context.getJCRSession("config")).thenReturn(session);
        when(session.getNode("/server/activation")).thenReturn(node);

        mctx.setUser(new DummyUser());
        MgnlContext.setInstance(mctx);
    }

    @Test
    public void testKeyStoreInCurrentFolder() throws Exception {
        //GIVEN
        SystemProperty.setProperty("magnolia.author.key.location", "key.store.properties");
        MgnlKeyPair keys = SecurityUtil.generateKeyPair(1024);
        //WHEN
        SecurityUtil.updateKeys(keys);
        //THEN no exception occurs and
        assertTrue(new File(SystemProperty.getProperty("magnolia.author.key.location")).exists());
    }

    @Test
    public void testKeyStoreInSomeOtherFolder() throws Exception {
        //GIVEN
        SystemProperty.setProperty("magnolia.author.key.location", "someFolder/key.store.properties");
        MgnlKeyPair keys = SecurityUtil.generateKeyPair(1024);
        //WHEN
        SecurityUtil.updateKeys(keys);
        //THEN
        assertTrue(new File(SystemProperty.getProperty("magnolia.author.key.location")).exists());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        File keystore = new File(SystemProperty.getProperty("magnolia.author.key.location"));
        super.tearDown();
        keystore.delete();
        try {
             keystore.getParentFile().delete();
        } catch (NullPointerException e) { //parent folder doesn't exists
        }
    }
}
