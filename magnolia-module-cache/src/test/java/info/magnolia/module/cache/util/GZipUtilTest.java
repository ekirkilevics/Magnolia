/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
package info.magnolia.module.cache.util;

import junit.framework.TestCase;

import java.io.IOException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class GZipUtilTest extends TestCase {
    public void testGzippedEmptyByteArrayShouldBeExactly20BytesLong() throws IOException {
        final byte[] gzipped = GZipUtil.gzip(new byte[0]);
        assertEquals(20, gzipped.length);
        final byte[] unzipped = GZipUtil.ungzip(gzipped);
        assertEquals(0, unzipped.length);

        assertFalse(GZipUtil.isGZipped(unzipped));
        assertTrue(GZipUtil.isGZipped(gzipped));
    }

    public void testGzipped1ByteArrayShouldNotBe20BytesLong() throws IOException {
        final byte[] gzipped = GZipUtil.gzip(new byte[]{'a'});
        assertEquals(21, gzipped.length);
        final byte[] unzipped = GZipUtil.ungzip(gzipped);
        assertEquals(1, unzipped.length);
        assertEquals('a', unzipped[0]);

        assertFalse(GZipUtil.isGZipped(unzipped));
        assertTrue(GZipUtil.isGZipped(gzipped));
    }

    public void testGzipIsSymetric() throws IOException {
        final byte[] gzipped = GZipUtil.gzip("azerty".getBytes());
        final byte[] unzipped = GZipUtil.ungzip(gzipped);
        assertEquals(6, unzipped.length);
        assertEquals("azerty", new String(unzipped));

        assertFalse(GZipUtil.isGZipped(unzipped));
        assertTrue(GZipUtil.isGZipped(gzipped));
    }

    public void testGzipActuallyCompressesAtSomePoint() throws IOException {
        final String s1 = "qwertzuiopasdfghjklyxcvbnm";
        final String s = s1 + s1;// + s1 + s1 + s1 + s1 + s1 + s1 + s1 + s1;
        assertEquals(52, s.length()); // 260
        final byte[] gzipped = GZipUtil.gzip(s.getBytes());
        assertTrue(gzipped.length < s.length());
        final byte[] unzipped = GZipUtil.ungzip(gzipped);
        assertEquals(s, new String(unzipped));

        assertFalse(GZipUtil.isGZipped(unzipped));
        assertTrue(GZipUtil.isGZipped(gzipped));
    }
}
