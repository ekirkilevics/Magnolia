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
package info.magnolia.cms.security;

import java.util.ArrayList;
import javax.security.auth.Subject;

import org.junit.Test;

import info.magnolia.cms.security.auth.PrincipalCollectionImpl;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test case for PrincipalUtil.
 *
 * @version $Id$
 */
public class PrincipalUtilTest {

    @Test
    public void testFindPrincipal() throws Exception {

        Subject subject = new Subject();
        User user = mock(User.class);
        subject.getPrincipals().add(user);

        assertSame(user, PrincipalUtil.findPrincipal(subject, User.class));
    }

    @Test
    public void testFindPrincipalInCollection() throws Exception {

        Subject subject = new Subject();
        User user = mock(User.class);

        PrincipalCollectionImpl collection = new PrincipalCollectionImpl();
        collection.add(user);
        subject.getPrincipals().add(collection);

        assertSame(user, PrincipalUtil.findPrincipal(subject, User.class));
    }

    @Test
    public void testFindACLByName() throws Exception {

        Subject subject = new Subject();

        PrincipalCollectionImpl collection = new PrincipalCollectionImpl();
        ACLImpl acl = new ACLImpl("website", new ArrayList<Permission>());
        collection.add(acl);
        subject.getPrincipals().add(collection);

        assertSame(acl, PrincipalUtil.findAccessControlList(subject, "website"));
        assertSame(acl, PrincipalUtil.findAccessControlList(subject.getPrincipals(), "website"));
    }

    @Test
    public void testRemovePrincipal() throws Exception {

        Subject subject = new Subject();
        User user = mock(User.class);
        subject.getPrincipals().add(user);

        assertSame(user, PrincipalUtil.removePrincipal(subject.getPrincipals(), User.class));
        assertTrue(subject.getPrincipals().isEmpty());
    }

    @Test
    public void testRemovePrincipalFromCollection() throws Exception {

        Subject subject = new Subject();
        User user = mock(User.class);

        PrincipalCollectionImpl collection = new PrincipalCollectionImpl();
        collection.add(user);
        subject.getPrincipals().add(collection);

        assertSame(user, PrincipalUtil.removePrincipal(subject.getPrincipals(), User.class));
        assertFalse(collection.contains(user));
    }
}
