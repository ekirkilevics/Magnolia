/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.module.exchangesimple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for subscription implementation.
 * 
 * @version $Id$
 * 
 */
public class DefaultSubscriptionTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testVoteEnabled() {
        DefaultSubscription subscription = new DefaultSubscription();
        // enabled by defauls
        assertTrue(subscription.isEnabled());
        subscription.setEnabled(false);
        assertFalse(subscription.isEnabled());
    }

    @Test
    public void testVoteNull() {
        DefaultSubscription subscription = new DefaultSubscription();
        subscription.setFromURI(null);
        subscription.setToURI(null);
        assertEquals(-1, subscription.vote("/bla/boo"));
    }

    @Test
    public void testVoteFail() {
        DefaultSubscription subscription = new DefaultSubscription();
        subscription.setFromURI("/foo/bar");
        assertEquals(-1, subscription.vote("/bla/boo"));
    }

    @Test
    public void testVoteMatch() {
        DefaultSubscription subscription = new DefaultSubscription();
        subscription.setFromURI("/foo/bar");
        // match value is determined by length
        assertEquals(8, subscription.vote("/foo/bar"));
        assertEquals(9, subscription.vote("/foo/bar/"));
        subscription.setFromURI("/foo/bar/");
        // this will not match!! we do not strip slash from set uri
        assertEquals(-1, subscription.vote("/foo/bar"));
        assertEquals(9, subscription.vote("/foo/bar/"));
    }

}
