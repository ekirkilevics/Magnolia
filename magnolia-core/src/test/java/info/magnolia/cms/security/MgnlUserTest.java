/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import java.util.Collection;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MgnlUserTest extends TestCase {
    private MgnlUserManager uman;

    protected void setUp() throws Exception {
        super.setUp();
        final SecuritySupportImpl sec = new SecuritySupportImpl();
        sec.setGroupManager(new MgnlGroupManager());
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager(ContentRepository.USERS, getClass().getResourceAsStream("sample-users.properties"));
        MockUtil.createAndSetHierarchyManager(ContentRepository.USER_GROUPS, getClass().getResourceAsStream("sample-usergroups.properties"));
        MockUtil.createAndSetHierarchyManager(ContentRepository.USER_ROLES, getClass().getResourceAsStream("sample-userroles.properties"));
        uman = new MgnlUserManager() {
            {
                // realm name
                setName("test");
            }

            protected Content findUserNode(String realm, String name) throws RepositoryException {
                return getHierarchyManager().getContent("/" + realm + "/" + name);
            }
        };
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        super.tearDown();
    }

    public void testGetGroupsReturnsOnlyDirectGroups() {
        final Collection g = uman.getUser("julien").getGroups();
        assertEquals(1, g.size());
        assertEquals("groupC", g.iterator().next());
    }

    public void testGetGroupsReturnsDirectGroupsWithoutDuplicates() {
        final Collection groups = uman.getUser("georges").getGroups();
        assertEquals(2, groups.size());
        assertTrue(groups.contains("groupA"));
        assertTrue(groups.contains("groupB"));
    }

    public void testGetAllGroupsReturnsDirectAndInheritedGroups() {
        final Collection groups = uman.getUser("georges").getAllGroups();
        assertEquals(4, groups.size());
        assertTrue(groups.contains("groupA"));
        assertTrue(groups.contains("groupB"));
        assertTrue(groups.contains("groupC"));
        assertTrue(groups.contains("groupD"));
    }

    public void testGetRolesReturnsDirectRoles() {
        final Collection roles = uman.getUser("georges").getRoles();
        assertEquals(3, roles.size());
        assertTrue(roles.contains("roleV"));
        assertTrue(roles.contains("roleW"));
        assertTrue(roles.contains("roleX"));
    }

    public void testGetRolesReturnsDirectRolesWithoutDuplicates() {
        final Collection roles = uman.getUser("julien").getRoles();
        assertEquals(2, roles.size());
        assertTrue(roles.contains("roleY"));
        assertTrue(roles.contains("roleX"));
    }

    public void testGetAllRolesReturnsDirectAndInheritedRoles() throws AccessDeniedException {
        final Collection rolesG = uman.getUser("georges").getAllRoles();
        assertEquals(5, rolesG.size());
        assertTrue(rolesG.contains("roleV"));
        assertTrue(rolesG.contains("roleW"));
        assertTrue(rolesG.contains("roleX"));
        assertTrue(rolesG.contains("roleY"));
        assertTrue(rolesG.contains("roleZ"));

        final Collection rolesJ = uman.getUser("julien").getAllRoles();
        assertEquals(3, rolesJ.size());
        assertTrue(rolesJ.contains("roleX"));
        assertTrue(rolesJ.contains("roleY"));
        assertTrue(rolesJ.contains("roleZ"));
    }

}
