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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.jcr.Session;

import org.junit.Test;

/**
 * @version $Id$
 */
public class PermissionUtilTest {

    @Test
    public void testConvertPermissions() {
        assertEquals(Session.ACTION_READ, PermissionUtil.convertPermissions(Permission.READ));
        assertEquals("add_node,read,set_property", PermissionUtil.convertPermissions(Permission.WRITE));
        assertEquals(Session.ACTION_REMOVE, PermissionUtil.convertPermissions(Permission.REMOVE));
        assertEquals(Session.ACTION_SET_PROPERTY, PermissionUtil.convertPermissions(Permission.SET));
        assertEquals("add_node,read,remove,set_property", PermissionUtil.convertPermissions(Permission.ALL));
        // and any combo of the above
        assertEquals("read,set_property", PermissionUtil.convertPermissions(Permission.READ | Permission.SET));
    }

    @Test
    public void testFailsOnCustomPermissions() {
        try {
            final String result = PermissionUtil.convertPermissions(64);
            fail("Should have failed - but returned ["+result+"] instead.");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown permissions: 64", e.getMessage());
        }
    }

    @Test
    public void testSuccessOnCustomPermissionAndNormalPermission() {
        final String result = PermissionUtil.convertPermissions(64 | Permission.READ);
        assertEquals("read", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsGrantedForEmptyPermissionString() {
        PermissionUtil.isGranted((Session) null, "ignored", "");
        // TODO: reenable this test once we can support mock subject for anonymous user access
        // assertEquals("Empty-string must not be granted.", false, PermissionUtil.isGranted("config", "ignored", ""));
    }
}
