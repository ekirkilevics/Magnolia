/**
 * This file Copyright (c) 2012-2012 Magnolia International
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

import java.security.Principal;
import java.util.Iterator;

import javax.security.auth.Subject;

import info.magnolia.cms.security.auth.PrincipalCollectionImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for Security class.
 *
 * @version $Id$
 */
public class SecurityTest {
    
    private final class DummyUserManager extends MgnlUserManager{
        
        @Override
        public User getAnonymousUser() {
            return getUser("anonymous");
        }
    }
    
    @Before
    public void setUp() throws Exception {
        final SecuritySupportImpl sec = new SecuritySupportImpl();
        sec.addUserManager(Realm.REALM_SYSTEM.getName(), new DummyUserManager());
        sec.setRoleManager(new MgnlRoleManager());
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager(RepositoryConstants.USERS, getClass().getResourceAsStream("anonymous-user.properties"));
        MockUtil.createAndSetHierarchyManager(RepositoryConstants.USER_ROLES, getClass().getResourceAsStream("anonymous-userroles.properties"));
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }
    
    @Test
    public void testMergePrincipals() throws AccessDeniedException {
        Subject subject = Security.getAnonymousSubject();
        assertEquals("anonymous", subject.getPrincipals(User.class).iterator().next().getName());
        
        Iterator<PrincipalCollectionImpl> principalCollectionIter = subject.getPrincipals(PrincipalCollectionImpl.class).iterator();
        Iterator<Principal> principalIter = principalCollectionIter.next().iterator();
        assertEquals("website",principalIter.next().getName());
        assertEquals("data",principalIter.next().getName());
        assertEquals("imaging",principalIter.next().getName());
        assertFalse(principalIter.hasNext());
    }
}
