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
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import junit.framework.TestCase;

/**
 * Basic tests for BaseSyndicatorImpl.
 * 
 * @author had
 * 
 */
public class BaseSyndicatorImplTest extends TestCase {

    public void testStripPassword() throws Exception {
        BaseSyndicatorImpl bsi = new BaseSyndicatorImpl() {

            @Override
            public void activate(ActivationContent activationContent, String nodePath) throws ExchangeException {
            }

            @Override
            public void doDeactivate(String nodeUUID, String nodePath) throws ExchangeException {
            }

            @Override
            public String doDeactivate(Subscriber subscriber, String nodeUUID, String nodePath) throws ExchangeException {
                return null;
            }
        };

        String testURL = "http://server.com:1234/bla/activation/?something=xxx&mgnlUserID=joey&mgnlUserPSWD=isTheBest";
        String strippedOfURL = "http://server.com:1234/bla/activation/?something=xxx&mgnlUserID=joey";
        assertEquals(strippedOfURL, bsi.stripPasswordFromUrl(testURL));

        testURL = "http://server.com:1234/bla/activation/?something=xxx&mgnlUserID=joey&mgnlUserPSWD=isTheBest&someOther=bla";
        strippedOfURL = "http://server.com:1234/bla/activation/?something=xxx&mgnlUserID=joey&someOther=bla";
        assertEquals(strippedOfURL, bsi.stripPasswordFromUrl(testURL));
    }
}
