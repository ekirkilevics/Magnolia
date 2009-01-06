/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
        assertEquals(expected, new VersionRange(range).contains(Version.parseVersion(otherVersion)));
    }

}
