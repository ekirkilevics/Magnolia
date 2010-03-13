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
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.context.MgnlContext;
import junit.framework.TestCase;

import java.util.Collection;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MgnlGroupTest extends TestCase {
    private MgnlGroupManager gman;

    protected void setUp() throws Exception {
        super.setUp();
        final SecuritySupportImpl sec = new SecuritySupportImpl();
        sec.setGroupManager(new MgnlGroupManager());
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager(ContentRepository.USERS, getClass().getResourceAsStream("sample-users.properties"));
        MockUtil.createAndSetHierarchyManager(ContentRepository.USER_GROUPS, getClass().getResourceAsStream("sample-usergroups.properties"));
        MockUtil.createAndSetHierarchyManager(ContentRepository.USER_ROLES, getClass().getResourceAsStream("sample-userroles.properties"));
        gman = new MgnlGroupManager();
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testGetRoles() throws AccessDeniedException {
        final Collection rolesA = gman.getGroup("groupA").getRoles();
        assertEquals(2, rolesA.size());
        assertTrue(rolesA.contains("roleX"));
        assertTrue(rolesA.contains("roleY"));

        final Collection rolesC = gman.getGroup("groupC").getRoles();
        assertEquals(0, rolesC.size());
    }

    public void testGetRolesWithoutDuplicates() throws AccessDeniedException {
        final Collection rolesB = gman.getGroup("groupB").getRoles();
        assertEquals(2, rolesB.size());
        assertTrue(rolesB.contains("roleX"));
        assertTrue(rolesB.contains("roleW"));

        final Collection rolesC = gman.getGroup("groupC").getRoles();
        assertEquals(0, rolesC.size());
    }

    public void testGetGroupsReturnsDirectGroups() throws AccessDeniedException {
        final Collection groupsA = gman.getGroup("groupA").getGroups();
        assertEquals(1, groupsA.size());
        assertTrue(groupsA.contains("groupC"));

        final Collection groupsD = gman.getGroup("groupD").getGroups();
        assertEquals(0, groupsD.size());
    }

    public void testGetGroupsReturnsDirectGroupsWithoutDuplicates() throws AccessDeniedException {
        final Collection groupsB = gman.getGroup("groupB").getGroups();
        assertEquals(2, groupsB.size());
        assertTrue(groupsB.contains("groupA"));
        assertTrue(groupsB.contains("groupC"));
    }
}
