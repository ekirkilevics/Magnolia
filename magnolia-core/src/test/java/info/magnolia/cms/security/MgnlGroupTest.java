/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.test.mock.MockUtil;
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
        FactoryUtil.setInstance(SecuritySupport.class, sec);
        MockUtil.initMockContext();
        MockUtil.createAndSetHierarchyManager(ContentRepository.USERS, getClass().getResourceAsStream("sample-users.properties"));
        MockUtil.createAndSetHierarchyManager(ContentRepository.USER_GROUPS, getClass().getResourceAsStream("sample-usergroups.properties"));
        MockUtil.createAndSetHierarchyManager(ContentRepository.USER_ROLES, getClass().getResourceAsStream("sample-userroles.properties"));
        gman = new MgnlGroupManager();
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
