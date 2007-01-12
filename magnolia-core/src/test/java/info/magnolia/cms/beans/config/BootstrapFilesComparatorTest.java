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
