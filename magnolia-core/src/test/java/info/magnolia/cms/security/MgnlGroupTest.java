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

import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.context.MgnlContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;

/**
 * @version $Id$
 */
public class MgnlGroupTest {
    private MgnlGroupManager gman;

    @Before
    public void setUp() throws Exception {
        final SecuritySupportImpl sec = new SecuritySupportImpl();
        sec.setGroupManager(new MgnlGroupManager());
        sec.setRoleManager(new MgnlRoleManager());
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager(RepositoryConstants.USERS, getClass().getResourceAsStream("sample-users.properties"));
        MockUtil.createAndSetHierarchyManager(RepositoryConstants.USER_GROUPS, getClass().getResourceAsStream("sample-usergroups.properties"));
        MockUtil.createAndSetHierarchyManager(RepositoryConstants.USER_ROLES, getClass().getResourceAsStream("sample-userroles.properties"));
        gman = new MgnlGroupManager();
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testGetRoles() throws AccessDeniedException {
        final Collection rolesA = gman.getGroup("groupA").getRoles();
        assertEquals(2, rolesA.size());
        assertTrue(rolesA.contains("roleX"));
        assertTrue(rolesA.contains("roleY"));

        final Collection rolesC = gman.getGroup("groupC").getRoles();
        assertEquals(0, rolesC.size());
    }

    @Test
    public void testGetRolesWithoutDuplicates() throws AccessDeniedException {
        final Collection rolesB = gman.getGroup("groupB").getRoles();
        assertEquals(2, rolesB.size());
        assertTrue(rolesB.contains("roleX"));
        assertTrue(rolesB.contains("roleW"));

        final Collection rolesC = gman.getGroup("groupC").getRoles();
        assertEquals(0, rolesC.size());
    }

    @Test
    public void testGetGroupsReturnsDirectGroups() throws AccessDeniedException {
        final Collection groupsA = gman.getGroup("groupA").getGroups();
        assertEquals(1, groupsA.size());
        assertTrue(groupsA.contains("groupC"));

        final Collection groupsD = gman.getGroup("groupD").getGroups();
        assertEquals(0, groupsD.size());
    }

    @Test
    public void testGetGroupsReturnsDirectGroupsWithoutDuplicates() throws AccessDeniedException {
        final Collection groupsB = gman.getGroup("groupB").getGroups();
        assertEquals(2, groupsB.size());
        assertTrue(groupsB.contains("groupA"));
        assertTrue(groupsB.contains("groupC"));
    }

    @Test
    public void testAddGroupToGroup() throws AccessDeniedException {
        // GIVEN
        Group groupA = gman.getGroup("groupA");

        // WHEN
        gman.addGroup(groupA, "groupB");

        // THEN
        assertTrue(gman.getGroup("groupA").getGroups().contains("groupB"));
    }

    @Test
    public void testAddRoleToGroup() throws AccessDeniedException {
        // GIVEN
        Group groupA = gman.getGroup("groupA");

        // WHEN
        gman.addRole(groupA, "roleW");

        // THEN
        assertTrue(gman.getGroup("groupA").getRoles().contains("roleW"));
    }

    @Test
    public void testGetNotExistGroup() throws AccessDeniedException {
        Group group = gman.getGroup("notExist");

        assertNull(group);
    }
}
