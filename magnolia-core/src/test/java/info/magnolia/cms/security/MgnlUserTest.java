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
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;
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
        FactoryUtil.setInstance(SecuritySupport.class, sec);
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
