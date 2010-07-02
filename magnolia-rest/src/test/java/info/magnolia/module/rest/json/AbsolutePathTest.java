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
package info.magnolia.module.rest.json;

import junit.framework.TestCase;

public class AbsolutePathTest extends TestCase {

    public void testIsRoot() {

        assertTrue(new AbsolutePath("").isRoot());
        assertTrue(new AbsolutePath("/").isRoot());
        assertFalse(new AbsolutePath("untitled").isRoot());
        assertFalse(new AbsolutePath("/untitled").isRoot());
    }

    public void testPath() {

        assertEquals("/", new AbsolutePath("").path());
        assertEquals("/", new AbsolutePath("/").path());
        assertEquals("/untitled", new AbsolutePath("untitled").path());
        assertEquals("/untitled", new AbsolutePath("/untitled").path());

        assertEquals("/1/2/3/4/5", new AbsolutePath("/1/2/3/4/5/").path());
    }

    public void testToString() {

        assertEquals("/", new AbsolutePath("").toString());
        assertEquals("/", new AbsolutePath("/").toString());
        assertEquals("/untitled", new AbsolutePath("untitled").toString());
        assertEquals("/untitled", new AbsolutePath("/untitled").toString());
    }

    public void testParentPath() {

        try {
            new AbsolutePath("").parentPath();
            fail();
        } catch (IllegalStateException expected) {
        }
        try {
            new AbsolutePath("/").parentPath();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals("/untitled", new AbsolutePath("untitled/sub").parentPath());
        assertEquals("/untitled", new AbsolutePath("/untitled/sub").parentPath());

        assertEquals("/untitled/sub", new AbsolutePath("/untitled/sub/node").parentPath());
    }

    public void testParent() {

        try {
            new AbsolutePath("").parent();
            fail();
        } catch (IllegalStateException expected) {
        }
        try {
            new AbsolutePath("/").parent();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals("/", new AbsolutePath("untitled").parent().path());
        assertEquals("/", new AbsolutePath("/untitled").parent().path());

        assertEquals("/untitled", new AbsolutePath("untitled/sub").parent().path());
        assertEquals("/untitled", new AbsolutePath("/untitled/sub").parent().path());

        assertEquals("/untitled/sub", new AbsolutePath("/untitled/sub/node").parent().path());
    }

    public void testName() {
        
        assertEquals("/", new AbsolutePath("").name());
        assertEquals("untitled", new AbsolutePath("/untitled").name());
        assertEquals("sub", new AbsolutePath("/untitled/sub").name());
        assertEquals("node", new AbsolutePath("/untitled/sub/node").name());

    }

    public void testAppend() {

        AbsolutePath path = new AbsolutePath("untitled").append("sub");

        assertEquals("/untitled/sub", path.path());
        assertEquals("sub", path.name());
        assertEquals("untitled", path.parent().name());

        try {
            path.append("not/allowed");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }
}
