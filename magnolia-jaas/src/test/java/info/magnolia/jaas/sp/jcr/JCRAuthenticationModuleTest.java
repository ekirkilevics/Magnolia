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
package info.magnolia.jaas.sp.jcr;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.security.auth.login.FailedLoginException;

import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import org.apache.commons.codec.binary.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @version $Id$
 */
public class JCRAuthenticationModuleTest {

    protected static final String FOO_USERNAME = "foo";
    protected static final String BASE64_ENCODED_SUPERUSER_PSWD = new String(Base64.encodeBase64(UserManager.SYSTEM_PSWD.getBytes()));
    protected static final String PLAIN_TXT_FOO_PSWD = "bar";
    protected static final String BASE64_ENCODED_FOO_PSWD = new String(Base64.encodeBase64(PLAIN_TXT_FOO_PSWD.getBytes()));

    @Before
    public void setUp() throws Exception {
        final SecuritySupportImpl sec = new SecuritySupportImpl();

        final User superuser = mock(User.class);
        when(superuser.getName()).thenReturn(UserManager.SYSTEM_USER);
        when(superuser.getPassword()).thenReturn(BASE64_ENCODED_SUPERUSER_PSWD);
        when(superuser.isEnabled()).thenReturn(true);

        final User foo = mock(User.class);
        when(foo.getName()).thenReturn(FOO_USERNAME);
        when(foo.getPassword()).thenReturn(BASE64_ENCODED_FOO_PSWD);
        when(foo.isEnabled()).thenReturn(true);

        UserManager uman = mock(UserManager.class);
        when(uman.getUser(UserManager.SYSTEM_USER)).thenReturn(superuser);
        when(uman.getUser(FOO_USERNAME)).thenReturn(foo);

        sec.addUserManager(Realm.REALM_SYSTEM.getName(), uman);
        sec.setRoleManager(new MgnlRoleManager());
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        MockUtil.initMockContext();
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testValidateUserPassesIfPasswordsMatch() throws Exception {
        JCRAuthenticationModule authenticationModule = new JCRAuthenticationModule();
        authenticationModule.name = FOO_USERNAME;
        authenticationModule.pswd = PLAIN_TXT_FOO_PSWD.toCharArray();
        authenticationModule.validateUser();

        authenticationModule.name = UserManager.SYSTEM_USER;
        authenticationModule.pswd = UserManager.SYSTEM_PSWD.toCharArray();
        authenticationModule.validateUser();
    }

    @Test(expected=FailedLoginException.class)
    public void testValidateUserFailsIfPasswordsDoNotMatch() throws Exception {
        JCRAuthenticationModule authenticationModule = new JCRAuthenticationModule();
        authenticationModule.name = FOO_USERNAME;
        authenticationModule.pswd = "hghgh".toCharArray();
        authenticationModule.validateUser();

        authenticationModule.name = UserManager.SYSTEM_USER;
        authenticationModule.pswd = "blah".toCharArray();
        authenticationModule.validateUser();
    }

}
