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

public class StructuredPathTest extends TestCase {

    public void testConstructors() {

        assertEquals("/", new StructuredPath(null).path());
        assertEquals("/", new StructuredPath("").path());
        assertEquals("/", new StructuredPath("/").path());
        assertEquals("/", new StructuredPath("/////").path());

        assertEquals("/test", new StructuredPath("", "test").path());
        assertEquals("/foo/bar", new StructuredPath("", "foo/bar").path());
        assertEquals("/foo/bar", new StructuredPath("foo", "bar").path());
        assertEquals("/foo/bar", new StructuredPath("foo/bar", "").path());
        assertEquals("/foo/bar", new StructuredPath("foo/bar", "/").path());
        assertEquals("/foo/bar", new StructuredPath("/foo", "/bar").path());

        assertEquals("/foo/bar", new StructuredPath(new StructuredPath("/foo"), "/bar").path());
    }

    public void testIsRoot() {

        assertTrue(new StructuredPath(null).isRoot());
        assertTrue(new StructuredPath("").isRoot());
        assertTrue(new StructuredPath("/").isRoot());
        assertTrue(new StructuredPath("//").isRoot());
        assertFalse(new StructuredPath("untitled").isRoot());
        assertFalse(new StructuredPath("/untitled").isRoot());
        assertFalse(new StructuredPath("     ").isRoot());
        assertFalse(new StructuredPath(" /").isRoot());
        assertFalse(new StructuredPath("/ ").isRoot());
    }

    public void testPath() {

        assertEquals("/", new StructuredPath("").path());
        assertEquals("/", new StructuredPath("/").path());
        assertEquals("/untitled", new StructuredPath("untitled").path());
        assertEquals("/untitled", new StructuredPath("/untitled").path());

        assertEquals("/1/2/3/4/5", new StructuredPath("/1/2/3/4/5/").path());
    }

    public void testToString() {

        assertEquals("/", new StructuredPath("").toString());
        assertEquals("/", new StructuredPath("/").toString());
        assertEquals("/untitled", new StructuredPath("untitled").toString());
        assertEquals("/untitled", new StructuredPath("/untitled").toString());
    }

    public void testParentPath() {

        try {
            new StructuredPath("").parentPath();
            fail();
        } catch (IllegalStateException expected) {
        }
        try {
            new StructuredPath("/").parentPath();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals("/untitled", new StructuredPath("untitled/sub").parentPath());
        assertEquals("/untitled", new StructuredPath("/untitled/sub").parentPath());

        assertEquals("/untitled/sub", new StructuredPath("/untitled/sub/node").parentPath());
    }

    public void testParent() {

        try {
            new StructuredPath("").parent();
            fail();
        } catch (IllegalStateException expected) {
        }
        try {
            new StructuredPath("/").parent();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals("/", new StructuredPath("untitled").parent().path());
        assertEquals("/", new StructuredPath("/untitled").parent().path());

        assertEquals("/untitled", new StructuredPath("untitled/sub").parent().path());
        assertEquals("/untitled", new StructuredPath("/untitled/sub").parent().path());

        assertEquals("/untitled/sub", new StructuredPath("/untitled/sub/node").parent().path());
    }

    public void testName() {
        
        assertEquals("/", new StructuredPath("").name());
        assertEquals("untitled", new StructuredPath("/untitled").name());
        assertEquals("sub", new StructuredPath("/untitled/sub").name());
        assertEquals("node", new StructuredPath("/untitled/sub/node").name());

    }

    public void testAppendSegment() {

        StructuredPath path = new StructuredPath("untitled").appendSegment("sub");

        assertEquals("/untitled/sub", path.path());
        assertEquals("sub", path.name());
        assertEquals("untitled", path.parent().name());

        assertCantAppendSegment(path, null);
        assertCantAppendSegment(path, "");
        assertCantAppendSegment(path, "/");
        assertCantAppendSegment(path, "paths/are/not/allowed");
    }

    private void assertCantAppendSegment(StructuredPath path, String s) {
        try {
            path.appendSegment(s);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRelativeTo() {

        StructuredPath deep = new StructuredPath("/aaa/bbb/ccc/ddd");
        StructuredPath shallow = new StructuredPath("/aaa/bbb");

        assertEquals("/ccc/ddd", deep.relativeTo(shallow).path());

        assertEquals("/", deep.relativeTo(deep).path());

        try {
            shallow.relativeTo(deep);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            new StructuredPath("/foo").relativeTo(new StructuredPath("/bar"));
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testAppendPath() {

        assertEquals("/foo/bar/zed", new StructuredPath("/foo").appendPath("bar/zed").path());
    }
}
