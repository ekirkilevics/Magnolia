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
public class VersionRangeTest extends TestCase {
    public void testBasicRangeParsing() {
        final VersionRange range = new VersionRange("1.2.3/4.5.6");
        VersionTest.assertVersion(1, 2, 3, null, range.getFrom());
        VersionTest.assertVersion(4, 5, 6, null, range.getTo());
        assertTrue(range.getFrom().isBeforeOrEquivalent(range.getTo()));
    }

    public void testInputIsTrimmedAndSeparatorCanBeSurroundBySpaces() {
        final VersionRange range = new VersionRange("\n\t  1.2.3  /\t\n4.5.6    \n");
        VersionTest.assertVersion(1, 2, 3, null, range.getFrom());
        VersionTest.assertVersion(4, 5, 6, null, range.getTo());
        assertTrue(range.getFrom().isBeforeOrEquivalent(range.getTo()));
    }

    public void testClassifiersAreAccepted() {
        final VersionRange range = new VersionRange("1.2.3-foo/4.5.6-bar");
        VersionTest.assertVersion(1, 2, 3, "foo", range.getFrom());
        VersionTest.assertVersion(4, 5, 6, "bar", range.getTo());
        assertTrue(range.getFrom().isBeforeOrEquivalent(range.getTo()));
    }

    public void testFromAndToCanBeSame() {
        final VersionRange range = new VersionRange("3.0.0/3.0.0");
        VersionTest.assertVersion(3, 0, 0, null, range.getFrom());
        VersionTest.assertVersion(3, 0, 0, null, range.getTo());
        assertTrue(range.getFrom().isEquivalent(range.getTo()));
    }

    public void testClassifiersAreAcceptedEvenIfFromAndToAreSame() {
        final VersionRange range = new VersionRange("3.0.0-pouet/3.0.0-tralala");
        VersionTest.assertVersion(3, 0, 0, "pouet", range.getFrom());
        VersionTest.assertVersion(3, 0, 0, "tralala", range.getTo());
        assertTrue(range.getFrom().isEquivalent(range.getTo()));
    }

    public void testFromCanNotBeAfterTo() {
        try {
            new VersionRange("4.0.0/3.4.5");
            fail("should have failed");
        } catch (RuntimeException e) {
            assertEquals("Invalid range: 4.0.0/3.4.5", e.getMessage());
        }
    }

    public void testFromCanNotBeAfterToAndClassifiersAreStillIgnored() {
        try {
            new VersionRange("4.0.0-foo/3.4.5-bar");
            fail("should have failed");
        } catch (RuntimeException e) {
            assertEquals("Invalid range: 4.0.0-foo/3.4.5-bar", e.getMessage());
        }
    }

    public void testSingleVersionCanBeSpecified() {
        final VersionRange range = new VersionRange("1.2.3");
        VersionTest.assertVersion(1, 2, 3, null, range.getFrom());
        VersionTest.assertVersion(1, 2, 3, null, range.getTo());
        assertTrue(range.getFrom().isEquivalent(range.getTo()));
    }

    public void testFromCanBeWildcard() {
        final VersionRange range = new VersionRange("*/1.2.3");
        assertEquals(Version.UNDEFINED_FROM, range.getFrom());
        VersionTest.assertVersion(1, 2, 3, null, range.getTo());
    }

    public void testToCanBeWildcard() {
        final VersionRange range = new VersionRange("1.2.3/*");
        VersionTest.assertVersion(1, 2, 3, null, range.getFrom());
        assertEquals(Version.UNDEFINED_TO, range.getTo());
    }

    public void testToAndFromCanBeWildcards() {
        final VersionRange range = new VersionRange("*/*");
        assertEquals(Version.UNDEFINED_FROM, range.getFrom());
        assertEquals(Version.UNDEFINED_TO, range.getTo());
    }

    public void testSingleVersionCanBeWildcard() {
        final VersionRange range = new VersionRange("*");
        assertEquals(Version.UNDEFINED_FROM, range.getFrom());
        assertEquals(Version.UNDEFINED_TO, range.getTo());
    }

    public void testChecksIfAVersionIsInRange() {
        doTestIsInRange(true, "1.2.3/3.4.5", "2.0.0");
        doTestIsInRange(false, "1.2.3/3.4.5", "4.0.0");
    }

    public void testChecksIfAVersionIsInRangeWithFromWildcard() {
        doTestIsInRange(true, "*/3.4.5", "1.2.3");
        doTestIsInRange(false, "*/3.4.5", "3.4.6");
        doTestIsInRange(false, "*/3.4.5", "4.6");
        doTestIsInRange(true, "*/3.4.5", "3.4.5");
    }

    public void testChecksIfAVersionIsInRangeWithToWildcard() {
        doTestIsInRange(true, "1.2.3/*", "3.4.5");
        doTestIsInRange(false, "1.2.3/*", "1.0.0");
        doTestIsInRange(false, "1.2.3/*", "0.1");
        doTestIsInRange(true, "1.2.3/*", "1.2.3");
    }

    public void testSingleVersionRangeShouldContainItSelf() {
        doTestIsInRange(true, "1.2.3", "1.2.3");
    }

    private void doTestIsInRange(boolean expected, String range, String otherVersion) {
        assertEquals(expected, new VersionRange(range).contains(new Version(otherVersion)));
    }

}
