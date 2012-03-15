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
package info.magnolia.cms.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;

import java.io.File;

import org.junit.After;
import org.junit.Test;

/**
 * @version $Id$
 */
public class PathTest {

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    /**
     * Test method for {@link info.magnolia.cms.core.Path#isAbsolute(java.lang.String)}.
     */
    @Test
    public void testIsAbsolute() {
        assertTrue(Path.isAbsolute("/test"));
        assertTrue(Path.isAbsolute("d:/test"));
        assertTrue(Path.isAbsolute(File.separator + "test"));
        assertFalse(Path.isAbsolute("test"));
    }

    @Test
    public void testIsCharValid() throws Exception{
        assertTrue(Path.isCharValid('.', null));
        assertFalse(Path.isCharValid(32, null)); // whitespace
        assertFalse(Path.isCharValid('[', null));
        assertFalse(Path.isCharValid(']', null));
        assertFalse(Path.isCharValid('*', null));
        assertFalse(Path.isCharValid('"', null));
        assertFalse(Path.isCharValid('\'', null));
        assertFalse(Path.isCharValid(':', null));
    }

    @Test
    public void testGetValidatedLabel() throws Exception {
        //plain chars tests
        assertEquals("f",Path.getValidatedLabel("f", null));
        assertEquals("fo",Path.getValidatedLabel("fo", null));
        assertEquals("foo",Path.getValidatedLabel("foo", null));

        //dot tests
        assertEquals("foo.bar",Path.getValidatedLabel("foo.bar", null));
        assertEquals("foo..bar",Path.getValidatedLabel("foo..bar", null));
        //local names beginning with dot are not allowed
        assertEquals("-foo",Path.getValidatedLabel(".foo", null));
        assertEquals("-.foo",Path.getValidatedLabel("..foo", null));

        //invalid or special chars tests
        assertEquals("f-oo",Path.getValidatedLabel("f$oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f*oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f[oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f]oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f;oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f:oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f\"oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f'oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f#oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f!oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f+oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f?oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f/oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f%oo", null));
        assertEquals("f-oo",Path.getValidatedLabel("f oo", null)); //if you can't see it, that's a space (ascii code 32)
        assertEquals("f-oo",Path.getValidatedLabel("f-oo", null));
        assertEquals("f_oo",Path.getValidatedLabel("f_oo", null));

        //(alpha)numeric chars tests
        assertEquals("0",Path.getValidatedLabel("0", null));
        assertEquals("0foo",Path.getValidatedLabel("0foo", null));
        assertEquals("123",Path.getValidatedLabel("123", null));
        assertEquals("foo0",Path.getValidatedLabel("foo0", null));

        //uppercase test
        assertEquals("FOO",Path.getValidatedLabel("FOO", null));

        //empty or blank labels
        assertEquals("untitled",Path.getValidatedLabel(null, null));
        assertEquals("untitled",Path.getValidatedLabel("", null));
        assertEquals("----",Path.getValidatedLabel("    ", null));
    }

    @Test
    public void testGetAbsoluteFileSystemPathPrependsApplicationRootDirIfPathIsRelative() throws Exception {
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, "/path/to/magnolia-webapp");
        String relPath = "WEB-INF/config";
        String returnedPath = Path.getAbsoluteFileSystemPath(relPath);
        assertEquals(Path.getAppRootDir().getAbsolutePath() + "/" + relPath, returnedPath);
    }

    @Test
    public void testGetAbsoluteFileSystemPathReturnsArgumentIfPathIsAbsolute() throws Exception {
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, "/path/to/magnolia-webapp");
        String absPath = "/foo/bar";
        String returnedPath = Path.getAbsoluteFileSystemPath(absPath);
        assertEquals(absPath, returnedPath);
    }

}
