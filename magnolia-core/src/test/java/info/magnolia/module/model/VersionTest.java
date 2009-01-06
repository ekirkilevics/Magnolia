/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.model;

import info.magnolia.module.model.Version.UndefinedDevelopmentVersion;
import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class VersionTest extends TestCase {
    public void testShouldSupportSingleDigitVersions() {
        assertVersion(3, 0, 0, null, Version.parseVersion("3"));
    }

    public void testShouldSupportTwoDigitVersions() {
        assertVersion(3, 0, 0, null, Version.parseVersion("3.0"));
        assertVersion(3, 1, 0, null, Version.parseVersion("3.1"));
    }

    public void testShouldSupportThreeDigitVersions() {
        assertVersion(3, 0, 0, null, Version.parseVersion("3.0.0"));
        assertVersion(3, 2, 0, null, Version.parseVersion("3.2.0"));
        assertVersion(3, 4, 5, null, Version.parseVersion("3.4.5"));
    }

    public void testShouldSupportAlphanumericClassifiers() {
        assertVersion(3, 0, 0, "x", Version.parseVersion("3.0.0-x"));
        assertVersion(3, 0, 0, "Y", Version.parseVersion("3.0.0-Y"));
        assertVersion(3, 0, 0, "5", Version.parseVersion("3.0.0-5"));
        assertVersion(3, 0, 0, "20060622gregYO", Version.parseVersion("3.0.0-20060622gregYO"));
    }

    public void testShouldSupportUnderscoresAndDashesInClassifiersToo() {
        assertVersion(3, 4, 5, "20060622-greg-YO", Version.parseVersion("3.4.5-20060622-greg-YO"));
        assertVersion(3, 4, 5, "20071102_fixed", Version.parseVersion("3.4.5-20071102_fixed"));
    }

    public void testShouldRejectInvalidCharsInClassifiers() {
        try {
            Version.parseVersion("3.0.0-/slash+plus");
            fail("should have failed");
        } catch (RuntimeException e) {
            assertEquals("Invalid classifier: \"/slash+plus\" in version \"3.0.0-/slash+plus\"", e.getMessage());
        }
    }

    public void testShouldSupportClassifierIndependentlyOfTheVersionNumberPrecision() {
        assertVersion(3, 0, 0, "foo", Version.parseVersion("3-foo"));
        assertVersion(3, 0, 0, "foo", Version.parseVersion("3.0-foo"));
        assertVersion(3, 0, 0, "foo", Version.parseVersion("3.0.0-foo"));
        assertVersion(3, 1, 0, "foo", Version.parseVersion("3.1-foo"));
        assertVersion(3, 1, 0, "foo", Version.parseVersion("3.1.0-foo"));
        assertVersion(3, 1, 7, "foo", Version.parseVersion("3.1.7-foo"));
    }

    public void testShouldTrimInput() {
        assertVersion(3, 1, 7, "foo", Version.parseVersion(" 3.1.7-foo\t\n "));
    }

    public void testShouldRejectInvalidInput() {
        assertInvalidVersion("Invalid major revision: \"\" in version \".4\"", ".4");
        assertInvalidVersion("Invalid major revision: \"\" in version \".4.2\"", ".4.2");
        assertInvalidVersion("Invalid major revision: \"\" in version \".4.2.3\"", ".4.2.3");
        assertInvalidVersion("Invalid major revision: \"\" in version \".4.2.3-foo\"", ".4.2.3-foo");
        assertInvalidVersion("Invalid major revision: \"X\" in version \"X\"", "X");
        assertInvalidVersion("Invalid major revision: \"X\" in version \"X.3.2\"", "X.3.2");
        assertInvalidVersion("Invalid major revision: \"X\" in version \"X.3.2-SNAPSHOT\"", "X.3.2-SNAPSHOT");
        assertInvalidVersion("Invalid minor revision: \"\" in version \"3..\"", "3..");
        assertInvalidVersion("Invalid minor revision: \"Y\" in version \"3.Y\"", "3.Y");
        assertInvalidVersion("Invalid patch revision: \"Z\" in version \"3.4.Z\"", "3.4.Z");
        assertInvalidVersion("Invalid patch revision: \"Z3\" in version \"3.4.Z3\"", "3.4.Z3");
        assertInvalidVersion("Invalid classifier: \"\" in version \"3.4.3-\"", "3.4.3-");
        assertInvalidVersion("Invalid classifier: \"?=)\" in version \"3.4.3-?=)\"", "3.4.3-?=)");
    }

    public void test3and300shouldBeEquivalent() {
        assertTrue(Version.parseVersion("3").isEquivalent(Version.parseVersion("3.0.0")));
        assertFalse(Version.parseVersion("3.0.1").isEquivalent(Version.parseVersion("3.0.0")));
        assertFalse(Version.parseVersion("3.1.1").isEquivalent(Version.parseVersion("3.0.0")));
        assertFalse(Version.parseVersion("4.1.1").isEquivalent(Version.parseVersion("3.0.0")));
        assertFalse(Version.parseVersion("3.0.1").isEquivalent(Version.parseVersion("3.0.2")));
        assertFalse(Version.parseVersion("3.0.1").isEquivalent(Version.parseVersion("3.2.2")));
        assertFalse(Version.parseVersion("3.0.1").isEquivalent(Version.parseVersion("2.2.2")));
    }

    public void testClassifiersShouldBeIgnoredInEquivalenceComparison() {
        assertTrue(Version.parseVersion("3-foo").isEquivalent(Version.parseVersion("3.0.0")));
        assertTrue(Version.parseVersion("3.0.0").isEquivalent(Version.parseVersion("3.0.0-bar")));
        assertTrue(Version.parseVersion("3.0.0-baz").isEquivalent(Version.parseVersion("3.0.0-bar")));
    }

    public void testStrictlyAfter() {
        doTestStrictlyAfter(true, "3.0.0", "2.0.0");
        doTestStrictlyAfter(true, "3.0.0", "2.5.0");
        doTestStrictlyAfter(true, "3.0.0", "2.5.8");
        doTestStrictlyAfter(true, "4.0", "3.0.0");
        doTestStrictlyAfter(true, "4.0.0", "3.0.0");
        doTestStrictlyAfter(true, "3.1.0", "3.0.0");
        doTestStrictlyAfter(true, "4.0.0", "3.0.0");
        doTestStrictlyAfter(true, "3.0.1", "3.0.0");

        doTestStrictlyAfter(false, "3.0.0", "3.0.0");
        doTestStrictlyAfter(false, "3.0.0", "3");
        doTestStrictlyAfter(false, "3.0.0", "3.0");
        doTestStrictlyAfter(false, "3", "3.0");
        doTestStrictlyAfter(false, "3.0", "3.0.0");
        doTestStrictlyAfter(false, "3.1", "3.1.1");
        doTestStrictlyAfter(false, "3.1", "3.2");

        doTestStrictlyAfter(false, "3.1", "4.0");

        doTestStrictlyAfter(false, "2.3.4", "3.4.2");
        doTestStrictlyAfter(true, "3.4.2", "2.3.4");
    }

    public void testStrictlyAfterShouldIgnoreClassifiers() {
        doTestStrictlyAfter(true, "4-foo", "3.0-bar");
        doTestStrictlyAfter(true, "3.1.0", "3.0.0-bar");
        doTestStrictlyAfter(true, "3.1.0-foo", "3.0.0");
    }

    public void testBeforeOrEqu() {
        doTestBefore(false, "3.0.0", "2.0.0");
        doTestBefore(false, "3.0.0", "2.5.0");
        doTestBefore(false, "3.0.0", "2.5.8");
        doTestBefore(false, "4.0", "3.0.0");
        doTestBefore(false, "4.0.0", "3.0.0");
        doTestBefore(false, "3.1.0", "3.0.0");
        doTestBefore(false, "4.0.0", "3.0.0");
        doTestBefore(false, "3.0.1", "3.0.0");

        doTestBefore(true, "3.0.0", "3.0.0");
        doTestBefore(true, "3.0.0", "3");
        doTestBefore(true, "3.0.0", "3.0");
        doTestBefore(true, "3", "3.0");
        doTestBefore(true, "3.0", "3.0.0");
        doTestBefore(true, "3.1", "3.1.1");
        doTestBefore(true, "3.1", "3.2");

        doTestBefore(true, "3.1", "4.0");

        doTestBefore(true, "2.3.4", "3.4.2");
        doTestBefore(false, "3.4.2", "2.3.4");
    }

    public void testBeforeOrEquShouldIgnoreClassifiers() {
        doTestBefore(false, "4-foo", "3.0-bar");
        doTestBefore(false, "3.1.0", "3.0.0-bar");
        doTestBefore(false, "3.1.0-foo", "3.0.0");
        doTestBefore(true, "2-foo", "3.0-bar");
        doTestBefore(true, "2.0.0", "3.1.0-bar");
        doTestBefore(true, "3.0.0-foo", "3.1.0");
    }

    public void testUndefinedDeveloperVersion() {
        Version realVersion = new Version(3, 1, 1);
        assertTrue(Version.parseVersion(Version.UndefinedDevelopmentVersion.KEY) instanceof UndefinedDevelopmentVersion);
        assertTrue(Version.UNDEFINED_DEVELOPMENT_VERSION.isEquivalent(realVersion));
        assertTrue(Version.UNDEFINED_DEVELOPMENT_VERSION.isBeforeOrEquivalent(realVersion));
        assertFalse(Version.UNDEFINED_DEVELOPMENT_VERSION.isStrictlyAfter(realVersion));
        assertTrue(realVersion.isEquivalent(Version.UNDEFINED_DEVELOPMENT_VERSION));
        assertTrue(realVersion.isBeforeOrEquivalent(Version.UNDEFINED_DEVELOPMENT_VERSION));
        assertFalse(realVersion.isStrictlyAfter(Version.UNDEFINED_DEVELOPMENT_VERSION));
        assertTrue(Version.UNDEFINED_DEVELOPMENT_VERSION.isEquivalent(Version.UNDEFINED_DEVELOPMENT_VERSION));
        assertTrue(Version.UNDEFINED_DEVELOPMENT_VERSION.isBeforeOrEquivalent(Version.UNDEFINED_DEVELOPMENT_VERSION));
        assertFalse(Version.UNDEFINED_DEVELOPMENT_VERSION.isStrictlyAfter(Version.UNDEFINED_DEVELOPMENT_VERSION));
    }

    private void doTestStrictlyAfter(boolean expected, String v1, String arg) {
        assertEquals(expected, Version.parseVersion(v1).isStrictlyAfter(Version.parseVersion(arg)));
    }

    private void doTestBefore(boolean expected, String v1, String arg) {
        assertEquals(expected, Version.parseVersion(v1).isBeforeOrEquivalent(Version.parseVersion(arg)));
    }

    public static void assertInvalidVersion(String expectedMessage, String versionStr) {
        try {
            Version.parseVersion(versionStr);
            fail("Should have failed for input " + versionStr);
        } catch (RuntimeException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    public static void assertVersion(int expectedMajor, int expectedMinor, int expectedPatch, String expectedClassifier, Version actual) {
        assertEquals("major revision:", expectedMajor, actual.getMajor());
        assertEquals("minor revision:", expectedMinor, actual.getMinor());
        assertEquals("minor revision:", expectedPatch, actual.getPatch());
        assertEquals("classifier:", expectedClassifier, actual.getClassifier());
    }

}
