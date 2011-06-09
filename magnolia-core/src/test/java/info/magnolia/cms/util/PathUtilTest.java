/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import junit.framework.TestCase;

/**
 * Tests for {@link PathUtil}.
 *
 * @version $Id$
 */
public class PathUtilTest extends TestCase {

    public void testCreatePath() throws Exception {
        assertEquals("/foo/bar", PathUtil.createPath("/foo", "bar"));
        assertEquals("/bar", PathUtil.createPath("/", "bar"));
        assertEquals("/bar", PathUtil.createPath("", "bar"));
        assertEquals("/bar", PathUtil.createPath(null, "bar"));
        try {
            PathUtil.createPath(null, null);
            fail();
        } catch (NullPointerException expected) {
        }

        // Trailing slash not supported
        assertEquals("/foo//bar", PathUtil.createPath("/foo/", "bar"));

        assertEquals("/foo/bar/zed", PathUtil.createPath("/foo", "bar/zed"));
        assertEquals("/foo//bar/zed", PathUtil.createPath("/foo", "/bar/zed"));
        assertEquals("/foo///bar/zed", PathUtil.createPath("/foo/", "/bar/zed"));
    }

    public void testAddLeadingSlash() throws Exception {
        assertEquals("/", PathUtil.addLeadingSlash("/"));
        assertEquals("/", PathUtil.addLeadingSlash(""));
        assertEquals("/foo", PathUtil.addLeadingSlash("foo"));
        assertEquals("/foo", PathUtil.addLeadingSlash("/foo"));
        try {
            PathUtil.addLeadingSlash(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testGetFolder() throws Exception {
        assertEquals("/", PathUtil.getFolder(""));
        assertEquals("/", PathUtil.getFolder("/"));
        assertEquals("/", PathUtil.getFolder(null));
        assertEquals("/foo", PathUtil.getFolder("/foo/bar"));
        assertEquals("foo", PathUtil.getFolder("foo/bar"));

        // Relative paths with only one segment not supported
        assertEquals("foobar", PathUtil.getFolder("foobar"));

        // Trailing slash not supported
        assertEquals("foo", PathUtil.getFolder("foo/"));
        assertEquals("foo/bar", PathUtil.getFolder("foo/bar/"));
        assertEquals("/foo/bar", PathUtil.getFolder("/foo/bar/"));
    }

    public void testGetFileName() throws Exception {
        assertEquals("bar", PathUtil.getFileName("/foo/bar"));
        assertEquals("bar", PathUtil.getFileName("foo/bar"));
        assertEquals("foo", PathUtil.getFileName("/foo"));
        try {
            PathUtil.getFileName(null);
            fail();
        } catch (NullPointerException expected) {
        }

        // Trailing slash not supported
        assertEquals("", PathUtil.getFileName("foo/"));
        assertEquals("", PathUtil.getFileName("/foo/bar/"));
        assertEquals("", PathUtil.getFileName("foo/bar/"));
    }

    public void testGetExtension() throws Exception {
        assertEquals("html", PathUtil.getExtension("index.html"));
        assertEquals("", PathUtil.getExtension("index"));
        assertEquals("html", PathUtil.getExtension("/home/index.html"));
        assertEquals("html", PathUtil.getExtension("home/index.html"));
        assertEquals("", PathUtil.getExtension(""));
        assertEquals("", PathUtil.getExtension("/"));
        assertEquals(null, PathUtil.getExtension(null));
    }

    public void testStripExtension() {
        assertEquals("index", PathUtil.stripExtension("index.html"));
        assertEquals("index", PathUtil.stripExtension("index"));
        assertEquals("/home/index", PathUtil.stripExtension("/home/index.html"));
        assertEquals("home/index", PathUtil.stripExtension("home/index.html"));
        assertEquals("", PathUtil.stripExtension(""));
        assertEquals("/", PathUtil.stripExtension("/"));
        assertEquals(null, PathUtil.stripExtension(null));
    }
}
