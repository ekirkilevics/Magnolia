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

import static org.junit.Assert.fail;

import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DelegatingUserManager}.
 */
public class DelegatingUserManagerTest {

    private ExternalUserManager externalUserManager;
    private DelegatingUserManager delegatingUserManager;
    private Map<String, UserManager> delegate = new HashMap<String, UserManager>();

    @Before
    public void setUp() {
        User user = mock(User.class);
        when(user.getName()).thenReturn("dummy");

        MockContext ctx = MockUtil.initMockContext();
        ctx.setUser(user);

        ComponentsTestUtil.setImplementation(SecuritySupport.class, SecuritySupportImpl.class);

        externalUserManager = new ExternalUserManager();
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testGetUserWillNotThrowErrorIfUserManagerDoesNotSupportGetUserMethod() {
        // GIVEN
        delegate.put("ext", externalUserManager);
        delegatingUserManager = new DelegatingUserManager(delegate);

        try {
            // WHEN
            delegatingUserManager.getUser("test");
        } catch (UnsupportedOperationException e) {
            // THEN
            fail("Should not fail here!");
        }
    }

}
