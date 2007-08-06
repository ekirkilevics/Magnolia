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
package info.magnolia.module.model;

import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class VersionComparatorTest extends TestCase {
    private static final Version V100 = Version.parseVersion("1.0.0");
    private static final Version V101 = Version.parseVersion("1.0.1");
    private static final Version V110 = Version.parseVersion("1.1.0");
    private static final Version V200 = Version.parseVersion("2.0.0");

    public void testBasic() {
        final VersionComparator vc = new VersionComparator();
        assertTrue(vc.compare(V101, V100) > 0);
        assertTrue(vc.compare(V110, V100) > 0);
        assertTrue(vc.compare(V200, V100) > 0);
        assertTrue(vc.compare(V110, V101) > 0);
        assertTrue(vc.compare(V200, V101) > 0);
        assertTrue(vc.compare(V200, V110) > 0);

        assertTrue(vc.compare(V100, V100) == 0);
        assertTrue(vc.compare(V101, V101) == 0);
        assertTrue(vc.compare(V110, V110) == 0);
        assertTrue(vc.compare(V200, V200) == 0);

        assertTrue(vc.compare(V100, V101) < 0);
        assertTrue(vc.compare(V100, V110) < 0);
        assertTrue(vc.compare(V100, V200) < 0);
        assertTrue(vc.compare(V101, V110) < 0);
        assertTrue(vc.compare(V101, V200) < 0);
        assertTrue(vc.compare(V110, V200) < 0);
    }

    public void testClassifiersAreIgnored() {
        final VersionComparator vc = new VersionComparator();
        assertTrue(vc.compare(Version.parseVersion("1.0-foo"), V100) == 0);
        assertTrue(vc.compare(V100, Version.parseVersion("1.0-bar")) == 0);
        assertTrue(vc.compare(V100, Version.parseVersion("2.0-bar")) < 0);
        assertTrue(vc.compare(Version.parseVersion("2.0.5-foo"), V101) > 0);
    }
}
