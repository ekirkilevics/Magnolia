/**
 * This file Copyright (c) 2013 Magnolia International
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

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.jackrabbit.value.StringValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for MgnlUserManager with real repository.
 */
public class MgnlUserManagerRepositoryTest extends RepositoryTestCase{

    private MgnlUserManager um;

    @Before
    @Override
    public void setUp() throws Exception{
        super.setUp();
        Session session = MgnlContext.getJCRSession(RepositoryConstants.USERS);
        session.getRootNode().addNode("admin");
        um = new MgnlUserManager();
        um.setRealmName("admin");
        ComponentsTestUtil.setImplementation(SecuritySupport.class, SecuritySupportImpl.class);
        um.createUser("test", "test");
    }

    @Test
    public void testUserPasswordAfterCodeCreation() throws LoginException, RepositoryException{
        User user = um.getUser("test");
        String password = user.getPassword();
        assertTrue(SecurityUtil.matchBCrypted("test", password));
    }

    @Test
    public void testUserPasswordAfterChangingByJCRProperty(){
        User user = um.getUser("test");
        um.setProperty(user, "pswd", "test");
        user = um.getUser("test");
        String password = user.getPassword();
        assertTrue(SecurityUtil.matchBCrypted("test", password));
    }

    @Test
    public void testUserPasswordAfterChangingByJCRPropertyValueType(){
        User user = um.getUser("test");
        Value value = new StringValue("test");
        um.setProperty(user, "pswd", value);
        user = um.getUser("test");
        String password = user.getPassword();
        assertTrue(SecurityUtil.matchBCrypted("test", password));
    }

    @Test
    public void testDoNotCreateUserWhenExistInAnotherRealm() throws PathNotFoundException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.USERS);
        session.getRootNode().addNode("public");
        final MgnlUserManager userManagerPublicRealm = new MgnlUserManager();
        userManagerPublicRealm.setRealmName("public");

        SecuritySupportImpl securityManager = (SecuritySupportImpl) Security.getSecuritySupport();
        securityManager.addUserManager("admin", um);
        securityManager.addUserManager("public", userManagerPublicRealm);

        // WHEN - THEN
        try {
            userManagerPublicRealm.createUser("test", "test");
        } catch (IllegalArgumentException e) {
            assertEquals("User with name test already exists.", e.getMessage());
            assertFalse(session.getRootNode().hasNode("public/test"));
            assertNull(userManagerPublicRealm.getUser("test"));
        }
    }

    @Test
    public void testDoNotCreateUserWhenExistInAnotherRealmAndCrossRealmDuplicateNamesAreNotAllowed() throws PathNotFoundException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.USERS);
        session.getRootNode().addNode("public");
        final MgnlUserManager userManagerPublicRealm = new MgnlUserManager();
        userManagerPublicRealm.setRealmName("public");

        SecuritySupportImpl securityManager = (SecuritySupportImpl) Security.getSecuritySupport();
        securityManager.addUserManager("admin", um);
        securityManager.addUserManager("public", userManagerPublicRealm);

        // WHEN - THEN
        try {
            userManagerPublicRealm.createUser("test", "test");
        } catch (IllegalArgumentException e) {
            assertEquals("User with name test already exists.", e.getMessage());
            assertFalse(session.getRootNode().hasNode("public/test"));
            assertNull(userManagerPublicRealm.getUser("test"));
        }
    }

    @Test
    public void testCreateUserWhenExistInAnotherRealmAndCrossRealmDuplicateNamesAreAllowed() throws PathNotFoundException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.USERS);
        session.getRootNode().addNode("public");
        final MgnlUserManager userManagerPublicRealm = new MgnlUserManager();
        userManagerPublicRealm.setRealmName("public");
        userManagerPublicRealm.setAllowCrossRealmDuplicateNames(true);

        SecuritySupportImpl securityManager = (SecuritySupportImpl) Security.getSecuritySupport();
        securityManager.addUserManager("admin", um);
        securityManager.addUserManager("public", userManagerPublicRealm);

        // WHEN
        userManagerPublicRealm.createUser("test", "test");

        // THEN
        assertNotNull(session.getNode("/public/test"));
        assertNotNull(userManagerPublicRealm.getUser("test"));
    }

    @After
    @Override
    public void tearDown() throws Exception{
        super.tearDown();
        ComponentsTestUtil.clear();
    }
}
