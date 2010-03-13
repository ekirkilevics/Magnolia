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

import info.magnolia.cms.util.SimpleUrlPattern;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class AccessManagerImplTest extends TestCase {

    private static final String TEST = "/admin/test";
    private static final String TEST_LANG = "/admin/test/language";


    public void testGetPermissions() {
        final Permission accessChildrenPermission = new PermissionImpl();
        accessChildrenPermission.setPattern(new SimpleUrlPattern("/admin/test/*"));
        accessChildrenPermission.setPermissions(8);
        final List permissionList = new ArrayList();
        permissionList.add(accessChildrenPermission);
        AccessManagerImpl ami = new AccessManagerImpl();
        // no permission to access given path by default
        assertEquals(0, ami.getPermissions(TEST));
        assertEquals(0, ami.getPermissions(TEST_LANG));
        // permission to children doesn't give rights to access the parent
        ami.setPermissionList(permissionList);
        assertEquals(0, ami.getPermissions(TEST));
        assertEquals(8, ami.getPermissions(TEST_LANG));

        permissionList.clear();
        final Permission accessParentPermission = new PermissionImpl();
        accessParentPermission.setPattern(new SimpleUrlPattern("/admin/test"));
        accessParentPermission.setPermissions(8);
        permissionList.add(accessParentPermission);
        assertEquals(8, ami.getPermissions(TEST));
        assertEquals(0, ami.getPermissions(TEST_LANG));

        permissionList.clear();
        final Permission accessAllPermission = new PermissionImpl();
        // actually while this is a valid pattern it would give access to all the nodes that start with given prefix ... dangerous when used in user management
        accessAllPermission.setPattern(new SimpleUrlPattern("/admin/test*"));
        accessAllPermission.setPermissions(8);
        permissionList.add(accessAllPermission);
        assertEquals(8, ami.getPermissions(TEST));
        assertEquals(8, ami.getPermissions(TEST_LANG));
    }

}
