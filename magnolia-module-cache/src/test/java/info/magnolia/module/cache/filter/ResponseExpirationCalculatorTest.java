/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.cache.filter;

import java.util.Date;

import org.apache.commons.httpclient.util.DateUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link ResponseExpirationCalculator}.
 *
 * @version $Id$
 */
public class ResponseExpirationCalculatorTest {

    @Test
    public void testConsumesCacheHeaders() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();

        assertTrue(negotiator.addHeader("Cache-Control", ""));
        assertTrue(negotiator.addHeader("Expires", ""));
        assertTrue(negotiator.addHeader("Pragma", "no-cache"));

        assertFalse(negotiator.addHeader("Pragma", "extension"));
        assertFalse(negotiator.addHeader("Content-Type", ""));
        assertFalse(negotiator.addHeader("Accept", ""));
        assertFalse(negotiator.addHeader("Vary", ""));
    }

    @Test
    public void testWontCacheWhenPragmaNoCache() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Pragma", "no-cache");
        assertEquals(0, negotiator.getMaxAgeInSeconds());
    }

    @Test
    public void testWontCacheWhenCacheControlPrivate() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Cache-Control", "private");
        assertEquals(0, negotiator.getMaxAgeInSeconds());
    }

    @Test
    public void testWontCacheWhenCacheControlNoCache() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Cache-Control", "no-cache");
        assertEquals(0, negotiator.getMaxAgeInSeconds());
    }

    @Test
    public void testDetectsSharedCacheMaxAge() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Cache-Control", "s-maxage=15");
        assertEquals(15, negotiator.getMaxAgeInSeconds());
    }

    @Test
    public void testDetectsCacheControlMaxAge() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Cache-Control", "max-age=15");
        assertEquals(15, negotiator.getMaxAgeInSeconds());
    }

    @Test
    public void testDetectsExpiresAsLong() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Expires", System.currentTimeMillis() + 1000L);
        assertTrue(negotiator.getMaxAgeInSeconds() > 100);
    }

    @Test
    public void testDetectsExpiresAsInt() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Expires", (int) (System.currentTimeMillis() + 1000));
        assertTrue(negotiator.getMaxAgeInSeconds() > 100);
    }

    @Test
    public void testDetectsExpiresAsString() throws Exception {

        String expiresString = DateUtil.formatDate(new Date(System.currentTimeMillis() + 1000), DateUtil.PATTERN_RFC1123);

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Expires", expiresString);
        assertTrue(negotiator.getMaxAgeInSeconds() > 100);
    }

    @Test
    public void testSharedCacheMaxAgeOverridesCacheControlMaxAge() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Cache-Control", "max-age=5, s-maxage=15");
        assertEquals(15, negotiator.getMaxAgeInSeconds());
    }

    @Test
    public void testSharedCacheMaxAgeOverridesExpires() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Cache-Control", "max-age=5, s-maxage=15");
        negotiator.addHeader("Expires", "0");
        assertEquals(15, negotiator.getMaxAgeInSeconds());
    }

    @Test
    public void testCacheControlMaxAgeOverridesExpires() throws Exception {

        ResponseExpirationCalculator negotiator = new ResponseExpirationCalculator();
        negotiator.addHeader("Cache-Control", "max-age=5");
        negotiator.addHeader("Expires", "0");
        assertEquals(5, negotiator.getMaxAgeInSeconds());
    }
}
