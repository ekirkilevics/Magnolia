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
package info.magnolia.cms.util;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;

/**
 * Tests for info.magnolia.cms.util.SimpleUrlPattern
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class SimpleUrlPatternTest extends TestCase {

    public void testDotDoPattern() {
       final SimpleUrlPattern sup = new SimpleUrlPattern("*.do");
       assertFalse(sup.match("/.resources/enterprise-css/registration.css"));
    }

    /**
     * Test without wildcards.
     */
    public void testNoWildcardsMatch() {
        assertTrue(new SimpleUrlPattern("/test/url.html").match("/test/url.html"));
    }

    /**
     * Test without wildcards.
     */
    public void testNoWildcardsNoMatch() {
        assertFalse(new SimpleUrlPattern("/test/url.html").match("/test/secondurl.html"));
    }

    /**
     * Test with the <code>*</code> wildcard.
     */
    public void testStarMatch() {
        assertTrue(new SimpleUrlPattern("/test/*.html").match("/test/url.html"));
    }

    /**
     * Test with the <code>*</code> wildcard.
     */
    public void testStarNoMatch() {
        assertFalse(new SimpleUrlPattern("/test/*.html").match("/other/url.html"));
    }

    /**
     * Test with the <code>*</code> wildcard.
     */
    public void testStarMatch2() {
        assertTrue(new SimpleUrlPattern("/*/*.html").match("/test/url.html"));
    }

    /**
     * Test with the <code>*</code> wildcard.
     */
    public void testStarNoMatch2() {
        assertFalse(new SimpleUrlPattern("/*/*.html").match("/test/url.jsp"));
    }

    /**
     * Test with the <code>*</code> wildcard.
     */
    public void testStarMatch3() {
        assertTrue(new SimpleUrlPattern("/**/*.html").match("/test/url.html"));
    }

    /**
     * Test with the <code>*</code> wildcard.
     */
    public void testStarNoMatch3() {
        assertFalse(new SimpleUrlPattern("/**/*.html").match("/test/url.jsp"));
    }

    /**
     * Test with the <code>*</code> wildcard.
     */
    public void testStarMatch4() {
        assertTrue(new SimpleUrlPattern("/**/*.html").match("/test/dir/dir/url.html"));
    }

    /**
     * Test with the <code>*</code> wildcard.
     */
    public void testStarNoMatch4() {
        assertFalse(new SimpleUrlPattern("/**/*.html").match("/test/dir/dir/url.jsp"));
    }

    /**
     * Test with the <code>?</code> wildcard.
     */
    public void testQuestionMarkMatch() {
        assertTrue(new SimpleUrlPattern("/test/num?page.html").match("/test/num2page.html"));
    }

    /**
     * Test with the <code>*</code> and <code>?</code> wildcards.
     */
    public void testWildcardsMatch() {
        assertTrue(new SimpleUrlPattern("/*/num?page.html").match("/*/num2page.html"));
    }
    
    /**
     * Test with the <code>*</code> and <code>?</code> wildcards.
     */
    public void testWildcardsWithSpecialChars() {
        assertTrue(new SimpleUrlPattern("/*").match("/*/page‘.html"));
    }
    
    /**
     * Test with the <code>*</code> and <code>?</code> wildcards.
     */
    public void testWildcardsWithNewline() {
        assertTrue(new SimpleUrlPattern("/*").match("/page\nxxx\nbbb.html"));
    }

    /**
     * Test with the <code>*</code> and <code>?</code> wildcards.
     */
    public void testGroupMatch() {
        assertTrue(new SimpleUrlPattern("/[a,b]/num/*").match("/b/num/blah"));
    }

    private static final byte[] MACROMAN_BYTES = new byte[]{47, 121, -118, -118, -118, -118, -118};
    private static final byte[] NFC_BYTES = new byte[]{47, 121, -61, -92, -61, -92, -61, -92, -61, -92, -61, -92};
    private static final byte[] NFD_BYTES = new byte[]{47, 121, 97, -52, -120, 97, -52, -120, 97, -52, -120, 97, -52, -120, 97, -52, -120};
    private static final String MACROMAN;
    private static final String NFC;
    private static final String NFD;

    static {
        try {
            MACROMAN = new String(MACROMAN_BYTES, "MacRoman");
            NFC = new String(NFC_BYTES, "UTF-8");
            NFD = new String(NFD_BYTES, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests with variously encoded paths.
     */
    public void testEncodedMatch() {
        // url-encoded path:
        assertTrue(new SimpleUrlPattern("/*").match("/magnoliaAuthor/dms/M--ller_PP-Praesentation/M%E2%94%9C%E2%95%9Dller_PP-Praesentation.doc"));
        // decoded url (should match):
        assertTrue(new SimpleUrlPattern("/*").match("/dms/M--ller_PP-Praesentation/M\u00FCller_PP-Praesentation.doc"));

        assertTrue(new SimpleUrlPattern("/*").match(NFC));
        assertTrue(new SimpleUrlPattern("/*").match(NFD));
        assertTrue(new SimpleUrlPattern("/*").match(MACROMAN));
        
        assertTrue(new SimpleUrlPattern("/*").match("/£"));
        
    }

}
