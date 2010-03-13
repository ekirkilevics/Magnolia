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
package info.magnolia.importexport;

import junit.framework.TestCase;

import java.io.File;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BootstrapFilesComparatorTest extends TestCase {
    public void testXmlShouldBeBeforeProperties() {
        assertStrictOrder("foo.xml", "foo.properties");
        assertStrictOrder("/foo/bar/baz.xml", "/bar/bar/abc.properties");
        assertStrictOrder("/foo/bar/baz.xml", "/bar/bar/baz.properties");
        assertStrictOrder("/foo/bar/abc.xml", "/bar/bar/abc.properties");
    }

    public void testZipAndGzShouldBeIgnoredInSortingXmlBeforeProperties() {
        assertStrictOrder("foo.xml.zip", "foo.properties");
        assertStrictOrder("foo.xml.zip", "foo.properties.zip");
        assertStrictOrder("foo.xml.zip", "foo.properties.gz");
        assertStrictOrder("foo.xml.gz", "foo.properties");
        assertStrictOrder("foo.xml.gz", "foo.properties.zip");
        assertStrictOrder("foo.xml.gz", "foo.properties.gz");
        assertStrictOrder("foo.xml", "foo.properties.zip");
        assertStrictOrder("foo.xml", "foo.properties.gz");
    }

    public void testFilesAreOrderedByFileNameLength() {
        // this is not the actual desired behaviour, see MAGNOLIA-1541
        assertStrictOrder("mama.xml", "bebebebebebebe.xml");

        // this is definitely the opposite of what we want - but is ok since bootstrap files are all in one single directory
        assertStrictOrder("/foo/b.xml", "/foo.xml");
        assertStrictOrder("/a/a/a/a/a/a/a/a/a.xml", "/z/z/z.xml");
        assertStrictOrder("/z/z/z.xml", "/a/a/a/a/a/a/a/a/aa.xml");

        // this is what we want:
        assertStrictOrder("foo.xml", "foo.b.xml");

        // this makes no sense:
        assertStrictOrder("foo.xyz.xml", "foo.aaaaaaaa.xml");
    }

    /**
     * Asserts that pathA is sorted strictly before pathB.
     */
    private void assertStrictOrder(String pathA, String pathB) {
        BootstrapFilesComparator c = new BootstrapFilesComparator();
        File a = new File(pathA);
        File b = new File(pathB);
        assertTrue(c.compare(a, b) < 0);
        assertTrue(c.compare(b, a) > 0);
    }


}
