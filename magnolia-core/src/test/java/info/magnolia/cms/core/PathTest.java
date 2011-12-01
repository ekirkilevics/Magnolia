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

}
