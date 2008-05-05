/**
 * This file Copyright (c) 2008 Magnolia International
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
package info.magnolia.module.cache.filter;

import info.magnolia.module.cache.util.GZipUtil;
import junit.framework.TestCase;

import java.io.IOException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CachedPageTest extends TestCase {
    public void testUnzipsContentIfPassedContentIsGZippedAndNotOfGzipMimeType() throws IOException {
        final String s = "hello";
        final byte[] gzipped = GZipUtil.gzip(s.getBytes());
        final CachedPage p = new CachedPage(gzipped, "text/plain", "cha", 1, null);
        assertEquals(gzipped, p.getDefaultContent());
        assertEquals("hello", new String(p.getUngzippedContent()));
    }

    public void testDoesNotAttemptToUnzipIfMimeTypeSaysTheContentIsGZipEvenIfContentActuallyIsGZipped() throws IOException {
        final byte[] gzipped = GZipUtil.gzip("hello".getBytes());
        final CachedPage p = new CachedPage(gzipped, "application/x-gzip", "cha", 1, null);
        assertEquals(gzipped, p.getDefaultContent());
        assertEquals(null, p.getUngzippedContent());
    }

    public void testDoesNotAttemptToUnzipIfContentIsNotGZipped() throws IOException {
        final CachedPage p = new CachedPage("hello".getBytes(), "foo/bar", "cha", 1, null);
        assertEquals("hello", new String(p.getDefaultContent()));
        assertEquals(null, p.getUngzippedContent());
    }
}
