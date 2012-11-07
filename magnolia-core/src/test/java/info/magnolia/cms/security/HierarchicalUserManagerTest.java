/**
 * This file Copyright (c) 2003-2012 Magnolia International
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

import java.io.InputStream;
import java.util.Collection;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import info.magnolia.importexport.DataTransporter;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * HierarchicalUserManagerTest.
 */
public class HierarchicalUserManagerTest extends RepositoryTestCase{
    private HierarchicalUserManager hm;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        hm = new HierarchicalUserManager();
        hm.setRealmName("public");

        final SecuritySupportImpl sec = new SecuritySupportImpl();
        sec.setGroupManager(new MgnlGroupManager());
        sec.setRoleManager(new MgnlRoleManager());
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);

        InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("users.public.test_public.xml");
        DataTransporter.importXmlStream(
            xmlStream,
            "users",
            "/public",
            "name matters only when importing a file that needs XSL transformation",
            false,
            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW,
            true,
            true);

        xmlStream = this.getClass().getClassLoader().getResourceAsStream("usergroups.test_group.xml");
        DataTransporter.importXmlStream(
            xmlStream,
            "usergroups",
            "/",
            "name matters only when importing a file that needs XSL transformation",
            false,
            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW,
            true,
            true);

        xmlStream = this.getClass().getClassLoader().getResourceAsStream("userroles.test_role.xml");
        DataTransporter.importXmlStream(
            xmlStream,
            "userroles",
            "/",
            "name matters only when importing a file that needs XSL transformation",
            false,
            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW,
            true,
            true);
    }

    @Test
    public void testParentPathIsRealmIfNameShorterThan3Chars() {
        assertEquals("/public", hm.getParentPath("ab"));
    }

    @Test
    public void testParentPathShouldReflectFirstLettersOfNameAndIncludeRealmName() {
        assertEquals("/public/c/ca", hm.getParentPath("casimir"));
    }

    @Test
    public void testParentPathShouldBeLowercased() {
        assertEquals("/public/c/ca", hm.getParentPath("Casimir"));
        assertEquals("/public/c/ca", hm.getParentPath("CASIMIR"));
    }
    
    @Test
    public void testGroupAddingAndRemoving() throws PathNotFoundException, RepositoryException{

        User user = hm.getUser("test_public");

        Collection<String> groups = user.getGroups();
        assertTrue(groups.contains("test_group"));
        
        user = hm.removeGroup(user, "test_group");

        groups = user.getGroups();
        assertFalse(groups.contains("test_group"));
        
        user = hm.addGroup(user, "test_group");
        
        groups = user.getGroups();
        assertTrue(groups.contains("test_group"));
    }
    
    @Test
    public void testRoleAddingAndRemoving() throws PathNotFoundException, RepositoryException{

        User user = hm.getUser("test_public");

        Collection<String> roles = user.getRoles();
        assertTrue(roles.contains("test_role"));
        
        user = hm.removeRole(user, "test_role");

        roles = user.getRoles();
        assertFalse(roles.contains("test_role"));
        
        user = hm.addRole(user, "test_role");
        
        roles = user.getRoles();
        assertTrue(roles.contains("test_role"));
    }
}
