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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
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
        IMocksControl control = EasyMock.createControl();
        HierarchyManager hm = control.createMock(HierarchyManager.class);
        Session session = control.createMock(Session.class);
        Workspace workspace = control.createMock(Workspace.class);
        QueryManager qm = control.createMock(QueryManager.class);
        Query query = control.createMock(Query.class);
        QueryResult result = control.createMock(QueryResult.class);
        NodeIterator nodeIterator = control.createMock(NodeIterator.class);
        Node node = control.createMock(Node.class);

        expect(session.getWorkspace()).andReturn(workspace);
        expect(workspace.getQueryManager()).andReturn(qm);
        expect(qm.createQuery("select * from [mgnl:user] where name() = 'test'", Query.JCR_SQL2)).andReturn(query);
        expect(query.execute()).andReturn(result);
        expect(result.getNodes()).andReturn(nodeIterator);
        expect(nodeIterator.hasNext()).andReturn(true).times(1);
        expect(nodeIterator.hasNext()).andReturn(false);
        expect(nodeIterator.nextNode()).andReturn(node);
        expect(node.isNodeType(ItemType.USER.getSystemName())).andReturn(true);

        control.replay();
        MgnlUserManager um = new MgnlUserManager();
        // Realm "all"
        um.setRealName(Realm.REALM_ALL.getName());
        Node principal = um.findPrincipalNode("test", session);
        assertNotNull(principal);
        control.verify();

        control = EasyMock.createControl();
        hm = control.createMock(HierarchyManager.class);
        session = control.createMock(Session.class);
        workspace = control.createMock(Workspace.class);
        qm = control.createMock(QueryManager.class);
        query = control.createMock(Query.class);
        result = control.createMock(QueryResult.class);
        nodeIterator = control.createMock(NodeIterator.class);
        node = control.createMock(Node.class);

        expect(session.getWorkspace()).andReturn(workspace);
        expect(workspace.getQueryManager()).andReturn(qm);
        expect(
                qm.createQuery("select * from [mgnl:user] where name() = 'test' and isdescendantnode(['/otherRealm'])",
                        Query.JCR_SQL2)).andReturn(query);
        expect(query.execute()).andReturn(result);
        expect(result.getNodes()).andReturn(nodeIterator);
        expect(nodeIterator.hasNext()).andReturn(true).times(1);
        expect(nodeIterator.hasNext()).andReturn(false);
        expect(nodeIterator.nextNode()).andReturn(node);
        expect(node.isNodeType(ItemType.USER.getSystemName())).andReturn(true);

        control.replay();
        um = new MgnlUserManager();

        um.setRealName("otherRealm");
        principal = um.findPrincipalNode("test", session);
        assertNotNull(principal);
        control.verify();
    }
}
