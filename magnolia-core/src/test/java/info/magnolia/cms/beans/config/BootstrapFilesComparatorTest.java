/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

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

    // this is probably not the actual desired behaviour
    public void testFilesAreOrderedByFileNameLength() {
        assertStrictOrder("mama.xml", "bebebebebebebe.xml");
        assertStrictOrder("/a/a/a/a/a/a/a/a/a.xml", "/z/z/z.xml");
        assertStrictOrder("/z/z/z.xml", "/a/a/a/a/a/a/a/a/aa.xml");

        // this is definitely the opposite of what we want - but is ok since bootstrap files are all in one single directory
        assertStrictOrder("/foo/b.xml", "/foo.xml");

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
