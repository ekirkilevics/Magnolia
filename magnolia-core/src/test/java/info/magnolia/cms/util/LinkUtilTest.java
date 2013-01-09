/**
 * This file Copyright (c) 2012 Magnolia International
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

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

/**
 * Test LinkUtil.
 */
public class LinkUtilTest {

    String link;
    String linkNoExtension;
    Calendar calendar1;
    Calendar calendar2; // A calendar with a different date then calendar1

    @Before
    public void setUp() throws Exception {
        link = "server/path/filename.png";
        linkNoExtension = "server/path/filename";
        calendar1 = new GregorianCalendar();
        calendar1.set(2000, 1, 1, 1, 1, 1); // 2000-02-01 01:01:01

        calendar2 = new GregorianCalendar();
        calendar2.set(2000, 1, 1, 1, 1, 2); // 2000-02-01 01:01:02
    }

    @Test
    /**
     * Basic test of the dash separated date fingerprint.
     * @throws Exception
     */
    public void testAddFingerprintToLink() throws Exception {
        // GIVEN

        // WHEN
        String link1 = LinkUtil.addFingerprintToLink(link, calendar1);

        // THEN
        assertTrue("server/path/filename.2000-02-01-01-01-01.png".equals(link1));
    }

    @Test
    /**
     * There are a few things that any implementation of addFingerprintToLink must do.
     * @throws Exception
     */
    public void testAddFingerprintToLinkFundamentalAssertions() throws Exception {
        // GIVEN

        // WHEN
        String link1 = LinkUtil.addFingerprintToLink(link, calendar1);

        // THEN

        // Did return a different link
        assertFalse(link.equals(link1));
        // Still contains extension
        assertTrue(link1.indexOf(".png") > 0);
    }

    @Test
    /**
     * For any implementation of addFingerprintToLink:
     * two dates should generate two different fingerprints.
     * @throws Exception
     */
    public void testAddFingerprintToLinkCompareTwoDates() throws Exception {
        // GIVEN

        // WHEN
        String link1 = LinkUtil.addFingerprintToLink(link, calendar1);
        String fLinkPast = LinkUtil.addFingerprintToLink(link, calendar2);

        // THEN

        // Different for different dates
        assertFalse(fLinkPast.equals(link1));
    }

    @Test
    public void testAddFingerprintToLinkWithoutExtension() throws Exception {
        // GIVEN

        // WHEN
        String link1 = LinkUtil.addFingerprintToLink(linkNoExtension, calendar1);
        String link2 = LinkUtil.addFingerprintToLink(linkNoExtension, calendar2);

        // THEN

        // Did return a different link
        assertFalse(link.equals(link1));

        // Different for different dates
        assertFalse(link2.equals(link1));
    }

    @Test
    public void testAddFingerprintToLinkWithInvalidDate() throws Exception {
        // GIVEN

        // WHEN
        String link1 = LinkUtil.addFingerprintToLink(link, null);

        // THEN

        // Returns the same link
        assertTrue(link.equals(link1));
    }
}
