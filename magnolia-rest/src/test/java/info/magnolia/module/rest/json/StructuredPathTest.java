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

    public void testValueOf() {

        assertEquals("/", path(null).path());
        assertEquals("/", path("").path());
        assertEquals("/", path("/").path());
        assertEquals("/", path("/////").path());
    }

    public void testIsRoot() {

        assertTrue(path(null).isRoot());
        assertTrue(path("").isRoot());
        assertTrue(path("/").isRoot());
        assertTrue(path("//").isRoot());
        assertFalse(path("untitled").isRoot());
        assertFalse(path("/untitled").isRoot());
        assertFalse(path("     ").isRoot());
        assertFalse(path(" /").isRoot());
        assertFalse(path("/ ").isRoot());
    }

    public void testPath() {

        assertEquals("/", path("").path());
        assertEquals("/", path("/").path());
        assertEquals("/untitled", path("untitled").path());
        assertEquals("/untitled", path("/untitled").path());

        assertEquals("/1/2/3/4/5", path("/1/2/3/4/5/").path());
    }

    public void testToString() {

        assertEquals("/", path("").toString());
        assertEquals("/", path("/").toString());
        assertEquals("/untitled", path("untitled").toString());
        assertEquals("/untitled", path("/untitled").toString());
    }

    public void testParentPath() {

        try {
            path("").parentPath();
            fail();
        } catch (IllegalStateException expected) {
        }
        try {
            path("/").parentPath();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals("/untitled", path("untitled/sub").parentPath());
        assertEquals("/untitled", path("/untitled/sub").parentPath());

        assertEquals("/untitled/sub", path("/untitled/sub/node").parentPath());
    }

    public void testParent() {

        try {
            path("").parent();
            fail();
        } catch (IllegalStateException expected) {
        }
        try {
            path("/").parent();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals("/", path("untitled").parent().path());
        assertEquals("/", path("/untitled").parent().path());

        assertEquals("/untitled", path("untitled/sub").parent().path());
        assertEquals("/untitled", path("/untitled/sub").parent().path());

        assertEquals("/untitled/sub", path("/untitled/sub/node").parent().path());
    }

    public void testName() {
        
        assertEquals("/", path("").name());
        assertEquals("untitled", path("/untitled").name());
        assertEquals("sub", path("/untitled/sub").name());
        assertEquals("node", path("/untitled/sub/node").name());

    }

    public void testAppendSegment() {

        StructuredPath path = path("untitled").appendSegment("sub");

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

        StructuredPath.ROOT.appendSegment("qwe");
    }

    public void testRelativeTo() {

        StructuredPath deep = path("/aaa/bbb/ccc/ddd");
        StructuredPath shallow = path("/aaa/bbb");

        assertEquals("/ccc/ddd", deep.relativeTo(shallow).path());

        assertEquals("/", deep.relativeTo(deep).path());

        try {
            shallow.relativeTo(deep);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            path("/foo").relativeTo(path("/bar"));
            fail();
        } catch (IllegalArgumentException expected) {
        }

        assertEquals("/foo", path("/foo").relativeTo(StructuredPath.ROOT).path());

        assertEquals("/", StructuredPath.ROOT.relativeTo(StructuredPath.ROOT).path());
    }

    public void testAppendPath() {

        assertEquals("/foo/bar", path("/foo/bar").appendPath(null).path());
        assertEquals("/foo/bar", path("").appendPath("foo/bar/").path());

        assertEquals("/foo/bar/zed", path("/foo").appendPath("bar/zed").path());
    }

    public void testAppend() {

        assertEquals("/", StructuredPath.ROOT.append(StructuredPath.ROOT).path());
        assertEquals("/", path("").append(path(null)).path());
        assertEquals("/foo", path("").append(path("foo")).path());
        assertEquals("/foo/bar/zed", path("/foo").append(path("bar/zed")).path());
    }

    public void testLength() {
        assertEquals(1, StructuredPath.ROOT.length());
        assertEquals(1, path(null).length());
        assertEquals(1, path("").length());
        assertEquals(4, path("foo").length());
        assertEquals(8, path("foo/bar").length());
    }

    public void testDepth() {
        assertEquals(0, StructuredPath.ROOT.depth());
        assertEquals(0, path(null).depth());
        assertEquals(0, path("").depth());
        assertEquals(1, path("foo").depth());
        assertEquals(2, path("foo/bar").depth());
    }

    private StructuredPath path(String path) {
        return StructuredPath.valueOf(path);
    }
}
