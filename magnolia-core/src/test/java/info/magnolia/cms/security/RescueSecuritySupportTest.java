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
package info.magnolia.cms.security;

import static org.junit.Assert.*;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.RescueSecuritySupport.RescueUserManager;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class RescueSecuritySupportTest extends MgnlTestCase{

    private RescueSecuritySupport securitySupport;

    //this is our GIVEN
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        securitySupport = new RescueSecuritySupport();
        MockUtil.createAndSetHierarchyManager(ContentRepository.USERS, getClass().getResourceAsStream("sample-users.properties"));
        MockUtil.createAndSetHierarchyManager(ContentRepository.USER_GROUPS, getClass().getResourceAsStream("sample-usergroups.properties"));
        MockUtil.createAndSetHierarchyManager(ContentRepository.USER_ROLES, getClass().getResourceAsStream("sample-userroles.properties"));
    }

    @Test
    public void testUserManagerIsAnInstanceOfRescueUserManager() throws Exception {
        //WHEN
        UserManager uman = securitySupport.getUserManager();

        //THEN
        assertTrue(uman instanceof RescueUserManager);
    }

    @Test
    public void testUserManagerRealmIsSystemRealm() throws Exception {
        //WHEN
        RescueUserManager uman = (RescueUserManager) securitySupport.getUserManager();

        //THEN
        assertEquals(Realm.REALM_SYSTEM.getName(), uman.getRealmName());
    }

    @Test
    public void testUserManagerReturnsCorrectAnonymousUser() throws Exception {
        //WHEN
        User user = securitySupport.getUserManager().getAnonymousUser();

        //THEN
        assertEquals(UserManager.ANONYMOUS_USER, user.getName());
        assertEquals("", user.getPassword());
    }

    @Test
    public void testUserManagerReturnsEnabledRescueUser() throws Exception {
        //WHEN
        User user = securitySupport.getUserManager().getUser("");

        //THEN
        assertTrue(user.isEnabled());
    }

    @Test
    public void testUserManagerReturnsRescueUserWithEnglishAsDefaultLanguage() throws Exception {
        //WHEN
        User user = securitySupport.getUserManager().getUser("");

        //THEN
        assertEquals("en", user.getLanguage());
    }

    @Test
    public void testUserManagerReturnsCorrectSystemUser() throws Exception {
        //WHEN
        User user = securitySupport.getUserManager().getSystemUser();

        //THEN
        assertEquals(UserManager.SYSTEM_USER, user.getName());
        assertEquals(UserManager.SYSTEM_PSWD, user.getPassword());
    }

    @Test
    public void testUserManagerReturnsCorrectSystemUserByName() throws Exception {
        //WHEN
        User user = securitySupport.getUserManager().getUser(UserManager.SYSTEM_USER);

        //THEN
        assertEquals(UserManager.SYSTEM_USER, user.getName());
        assertEquals(UserManager.SYSTEM_PSWD, user.getPassword());
    }

    @Test
    public void testUserManagerReturnsAnonymousUserByAnyNameExcludedSystemUserName() throws Exception {
        //WHEN
        User user = securitySupport.getUserManager().getUser("foo");

        //THEN
        assertEquals(UserManager.ANONYMOUS_USER, user.getName());
        assertEquals("", user.getPassword());
    }

    @Test
    public void testUserManagerReturnsSystemUserWithCorrectRole() throws Exception {
        //WHEN
        User user = securitySupport.getUserManager().getSystemUser();

        //THEN
        assertTrue(user.hasRole("superuser"));
    }

    @Test
    public void testUserManagerReturnsSystemUserWithCorrectGroup() throws Exception {
        //WHEN
        User user = securitySupport.getUserManager().getSystemUser();

        //THEN
        assertTrue(user.inGroup("publishers"));
    }

    //TODO (maybe one fine day). Commented out as it fails with java.lang.SecurityException: Unable to locate a login configuration at com.sun.security.auth.login.ConfigFile.<init>(ConfigFile.java:93)
    //public void testAuthenticateReturnsSuccessfully() throws Exception {
    ////WHEN
    //   LoginResult loginResult = securitySupport.authenticate(new CredentialsCallbackHandler(), "magnolia");
    //
    //   //THEN
    //assertTrue(loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED);
    //}

}
