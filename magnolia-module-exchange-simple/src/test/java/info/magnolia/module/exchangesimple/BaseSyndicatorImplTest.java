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

import static org.easymock.EasyMock.*;

import java.util.Calendar;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.Rule;
import info.magnolia.test.mock.MockContent;
import junit.framework.TestCase;

/**
 * @version $Id$
 */
public class BaseSyndicatorImplTest extends TestCase {

    private final class DummySyndicator extends BaseSyndicatorImpl {
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
    }

    public void testUpdateMetaDataWhenActivating() throws Exception {
        // GIVEN
        BaseSyndicatorImpl bsi = new DummySyndicator();

        User user = createNiceMock(User.class);
        final String activator = "batman";
        expect(user.getName()).andReturn(activator).anyTimes();
        replay(user);

        Rule rule = new Rule(new String[] {ItemType.CONTENT.getSystemName()});

        bsi.init(user, "repo", "workspace", rule);
        Content content = new MockContent("test");
        Content child = content.createContent("childOfTest");

        MetaData md = content.getMetaData();
        md.setLastActivationActionDate();

        MetaData childsMetaData = child.getMetaData();
        childsMetaData.setLastActivationActionDate();

        // make sure there's a time difference in between the initial setting of the MetaData's and the implicit ones from call to updateMetaData
        Thread.sleep(10);

        assertFalse(md.getIsActivated());
        Calendar lastAction = md.getLastActionDate();

        // WHEN
        bsi.updateMetaData(content, BaseSyndicatorImpl.ACTIVATE);

        // THEN - verify metaData got updated
        verify(user);
        md = content.getMetaData();
        assertTrue(md.getIsActivated());
        assertEquals(activator, md.getActivatorId());
        assertTrue(md.getLastActionDate().getTimeInMillis() > lastAction.getTimeInMillis());

        // ...and know the kid's metadata
        childsMetaData = child.getMetaData();
        assertTrue(childsMetaData.getIsActivated());
        assertEquals(activator, childsMetaData.getActivatorId());
        assertTrue(childsMetaData.getLastActionDate().getTimeInMillis() > lastAction.getTimeInMillis());
    }

    public void testUpdateMetaDataWhenDeactivating() throws Exception {
        // GIVEN
        BaseSyndicatorImpl bsi = new DummySyndicator();

        User user = createNiceMock(User.class);
        final String activator = "batman";
        expect(user.getName()).andReturn(activator).anyTimes();
        replay(user);

        Rule rule = new Rule(new String[] {ItemType.CONTENT.getSystemName()});

        bsi.init(user, "repo", "workspace", rule);
        Content content = new MockContent("test");
        Content child = content.createContent("childOfTest");

        MetaData md = content.getMetaData();
        md.setUnActivated();
        md.setLastActivationActionDate();

        MetaData childsMetaData = child.getMetaData();
        childsMetaData.setUnActivated();
        childsMetaData.setLastActivationActionDate();
        // make sure there's a time difference in between the initial setting of the MetaData's and the implicit ones from call to updateMetaData
        Thread.sleep(1);

        Calendar lastAction = md.getLastActionDate();

        // WHEN
        bsi.updateMetaData(content, BaseSyndicatorImpl.DEACTIVATE);

        // THEN - verify metaData got updated
        verify(user);
        md = content.getMetaData();
        assertFalse(md.getIsActivated());
        assertEquals(activator, md.getActivatorId());
        assertTrue(md.getLastActionDate().getTimeInMillis() > lastAction.getTimeInMillis());

        // ...and know the kid's metadata
        childsMetaData = child.getMetaData();
        assertFalse(childsMetaData.getIsActivated());
        assertEquals(activator, childsMetaData.getActivatorId());
        assertTrue(childsMetaData.getLastActionDate().getTimeInMillis() > lastAction.getTimeInMillis());
    }

    public void testStripPassword() throws Exception {
        // GIVEM
        BaseSyndicatorImpl bsi = new DummySyndicator();

        String testURL = "http://server.com:1234/bla/activation/?something=xxx&mgnlUserID=joey&mgnlUserPSWD=isTheBest";
        String strippedOfURL = "http://server.com:1234/bla/activation/?something=xxx&mgnlUserID=joey";

        // WHEN
        String result = BaseSyndicatorImpl.stripPasswordFromUrl(testURL);

        // THEN
        assertEquals(strippedOfURL, result);
    }

    public void testStripPasswordWithAdditionalParam() throws Exception {
        // GIVEM
        BaseSyndicatorImpl bsi = new DummySyndicator();

        String testURL =
                "http://server.com:1234/bla/activation/?something=xxx&mgnlUserID=joey&mgnlUserPSWD=isTheBest&someOther=bla";
        String strippedOfURL = "http://server.com:1234/bla/activation/?something=xxx&mgnlUserID=joey&someOther=bla";
        // WHEN
        String result = BaseSyndicatorImpl.stripPasswordFromUrl(testURL);

        // THEN
        assertEquals(strippedOfURL, result);
    }

}
