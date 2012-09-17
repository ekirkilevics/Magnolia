/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.ItemType;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import info.magnolia.cms.core.MgnlNodeType;
import org.junit.Test;

/**
 * Tests for MgnlUserManager.
 */
public class MgnlUserManagerTest {

    @Test
    public void testUsernameIsValidatedUponCreation() {
        final String justCheckingIfValidateUsernameIsCalledMessage = "Yes! I wanted this method to be called !";
        final MgnlUserManager hm = new MgnlUserManager() {
            @Override
            protected void validateUsername(String name) {
                throw new IllegalArgumentException(justCheckingIfValidateUsernameIsCalledMessage);
            }
        };
        try {
            hm.createUser("bleh", "blah");
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            assertEquals(justCheckingIfValidateUsernameIsCalledMessage, e.getMessage());
        }
    }

    @Test
    public void testUsernameCantBeNull() {
        try {
            new MgnlUserManager().validateUsername(null);
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            assertEquals("null is not a valid username.", e.getMessage());
        }
    }

    @Test
    public void testUsernameCantBeEmpty() {
        try {
            new MgnlUserManager().validateUsername("");
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            assertEquals(" is not a valid username.", e.getMessage());
        }
    }

    @Test
    public void testUsernameCantBeBlank() {
        try {
            new MgnlUserManager().validateUsername("   ");
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            assertEquals("    is not a valid username.", e.getMessage());
        }
    }


    @Test
    public void testFindPrincipalNode() throws RepositoryException {
        // GIVEN
        final Session session = mock(Session.class);
        final Workspace workspace = mock(Workspace.class);
        final QueryManager qm = mock(QueryManager.class);
        final Query query = mock(Query.class);
        final QueryResult result = mock(QueryResult.class);
        final NodeIterator nodeIterator = mock(NodeIterator.class);
        final Node node = mock(Node.class);

        when(session.getWorkspace()).thenReturn(workspace);
        when(workspace.getQueryManager()).thenReturn(qm);
        when(qm.createQuery("select * from [mgnl:user] where name() = 'test'", Query.JCR_SQL2)).thenReturn(query);
        when(query.execute()).thenReturn(result);
        when(result.getNodes()).thenReturn(nodeIterator);
        when(nodeIterator.hasNext()).thenReturn(true).thenReturn(false);
        when(nodeIterator.nextNode()).thenReturn(node);
        when(node.isNodeType(MgnlNodeType.USER)).thenReturn(true);

        MgnlUserManager um = new MgnlUserManager();
        // Realm "all"
        um.setRealmName(Realm.REALM_ALL.getName());

        // WHEN
        final Node principal = um.findPrincipalNode("test", session);

        // THEN
        assertNotNull(principal);
    }

    @Test
    public void testFindPrincipalNodeWithOtherRealm() throws RepositoryException {
        // GIVEN
        final Session session = mock(Session.class);
        final Workspace workspace = mock(Workspace.class);
        final QueryManager qm = mock(QueryManager.class);
        final Query query = mock(Query.class);
        final QueryResult result = mock(QueryResult.class);
        final NodeIterator nodeIterator = mock(NodeIterator.class);
        final Node node = mock(Node.class);

        when(session.getWorkspace()).thenReturn(workspace);
        when(workspace.getQueryManager()).thenReturn(qm);
        when(
                qm.createQuery("select * from [mgnl:user] where name() = 'test' and isdescendantnode(['/otherRealm'])",
                        Query.JCR_SQL2)).thenReturn(query);
        when(query.execute()).thenReturn(result);
        when(result.getNodes()).thenReturn(nodeIterator);
        when(nodeIterator.hasNext()).thenReturn(true).thenReturn(false);
        when(nodeIterator.nextNode()).thenReturn(node);
        when(node.isNodeType(ItemType.USER.getSystemName())).thenReturn(true);

        final MgnlUserManager um = new MgnlUserManager();

        um.setRealmName("otherRealm");

        // WHEN
        final Node principal = um.findPrincipalNode("test", session);

        // THEN
        assertNotNull(principal);
    }
}
