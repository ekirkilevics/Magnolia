/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for EscapeUtil.
 */
public class EscapeUtilTest  {

    @Test
    public void testEscapeUnescape() {
        //GIVEN
        final String originalString = "<>&\"'&quot;";
        //WHEN
        final String escapedStr = EscapeUtil.escapeXss(originalString);
        //THEN
        assertEquals("&lt;&gt;&amp;&quot;'&quot;" ,escapedStr);

        //WHEN
        final String unescapedStr = EscapeUtil.unescapeXss(escapedStr);
        //THEN
        assertEquals("<>&\"'\"" , unescapedStr);
    }

    @Test
    public void testEscapeUnescapeArray() {
        // GIVEN
        final String[] stringArray = {"<>&\"'", "someOtherString", null};
        // WHEN
        final String[] escapedArray = EscapeUtil.escapeXss(stringArray);
        // THEN
        assertEquals("&lt;&gt;&amp;&quot;'" , escapedArray[0]);
        assertEquals("someOtherString" , escapedArray[1]);
        assertEquals(null , escapedArray[2]);

        //WHEN
        final String[] unescapedArray = EscapeUtil.unescapeXss(escapedArray);
        //THEN
        assertEquals("<>&\"'", unescapedArray[0]);
        assertEquals("someOtherString" , unescapedArray[1]);
        assertEquals(null , unescapedArray[2]);
    }
}
