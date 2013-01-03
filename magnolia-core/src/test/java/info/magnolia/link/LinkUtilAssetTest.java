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
package info.magnolia.link;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

/**
 * Test LinkUtilAsset.
 */
public class LinkUtilAssetTest {

    String link;
    String linkNoExtension;
    Calendar now;
    Calendar lastYear;

    @Before
    public void setUp() throws Exception {
        link = "server/path/filename.png";
        linkNoExtension = "server/path/filename";
        now = new GregorianCalendar();
        lastYear = new GregorianCalendar();
        lastYear.set(Calendar.YEAR, now.get(Calendar.YEAR)-1);
    }

    @Test
    public void testAddAssetCacheFingerprintToLink() throws Exception {
        // GIVEN

        // WHEN
        String fLinkNow = LinkUtilAsset.addAssetCacheFingerprintToLink(link, now);
        String fLinkPast = LinkUtilAsset.addAssetCacheFingerprintToLink(link, lastYear);

        // THEN

        // Did return a different link
        assertFalse(link.equals(fLinkNow));
        // Still contains extension
        assertTrue(fLinkNow.indexOf(".png")>0);

        // Different for different dates
        assertFalse(fLinkPast.equals(fLinkNow));
    }

    @Test
    public void testAddAssetCacheFingerprintToLinkWithoutExtension() throws Exception {
        // GIVEN

        // WHEN
        String fLinkNow = LinkUtilAsset.addAssetCacheFingerprintToLink(linkNoExtension, now);
        String fLinkPast = LinkUtilAsset.addAssetCacheFingerprintToLink(linkNoExtension, lastYear);

        // THEN

        // Did return a different link
        assertFalse(link.equals(fLinkNow));

        // Different for different dates
        assertFalse(fLinkPast.equals(fLinkNow));
    }

    @Test
    public void testAddAssetCacheFingerprintToLinkWithInvalidDate() throws Exception {
        // GIVEN

        // WHEN
        String fLinkNow = LinkUtilAsset.addAssetCacheFingerprintToLink(link, null);

        // THEN

        // Returns the same link
        assertTrue(link.equals(fLinkNow));
    }
}
