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

import static info.magnolia.cms.core.ItemType.CONTENT;
import static info.magnolia.cms.core.ItemType.CONTENTNODE;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;

import org.junit.Test;

/**
 * Basic test for checking indication status.
 * @author mdivilek
 * @version $Id:$
 */
public class IndicationStatusTest extends RepositoryTestCase{

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

    @Test
    public void testIndicationOfStatusAfterEditDuringActivation() throws Exception{
        BaseSyndicatorImpl bsi = new DummySyndicator();
        bsi.setResouceCollector(new ResourceCollector());

        User user = createNiceMock(User.class);
        final String activator = "user";
        expect(user.getName()).andReturn(activator).anyTimes();
        replay(user);

        Rule rule = new Rule(new String[] {ItemType.CONTENT.getSystemName()});

        bsi.init(user, ContentRepository.WEBSITE, ContentRepository.WEBSITE, rule);

        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

        Content homePage = hm.createContent("/", "page", CONTENT.getSystemName());
        Content subPage1 = homePage.createContent("subpage1", CONTENT.getSystemName());
        Content subPage2 = homePage.createContent("subpage2", CONTENT.getSystemName());
        hm.save();

        MetaData homepageMetaData = homePage.getMetaData();
        MetaData subpage1MetaData = subPage1.getMetaData();
        MetaData subpage2MetaData = subPage2.getMetaData();

        homePage.addVersion(new Rule(new String[]{CONTENTNODE.getSystemName()}));
        subPage1.addVersion(new Rule(new String[]{CONTENTNODE.getSystemName()}));
        subPage2.addVersion(new Rule(new String[]{CONTENTNODE.getSystemName()}));
        ContentVersion versionedHomePage = homePage.getVersionedContent("1.0");

        Thread.sleep(1);
        homepageMetaData.setModificationDate();
        subpage2MetaData.setModificationDate();

        assertFalse(homepageMetaData.getIsActivated());

        bsi.activate("/", versionedHomePage);

        assertTrue(homepageMetaData.getIsActivated());
        assertTrue(homepageMetaData.getModificationDate().after(homepageMetaData.getLastActionDate()));
        assertEquals(MetaData.ACTIVATION_STATUS_MODIFIED, homepageMetaData.getActivationStatus());

        assertTrue(subpage1MetaData.getIsActivated());
        assertFalse(subpage1MetaData.getModificationDate().after(subpage1MetaData.getLastActionDate()));
        assertEquals(MetaData.ACTIVATION_STATUS_ACTIVATED, subpage1MetaData.getActivationStatus());

        assertTrue(subpage2MetaData.getIsActivated());
        assertTrue(subpage2MetaData.getModificationDate().after(subpage2MetaData.getLastActionDate()));
        assertEquals(MetaData.ACTIVATION_STATUS_MODIFIED, subpage2MetaData.getActivationStatus());
    }

    @Test
    public void testIndicationOfStatus() throws Exception{

        BaseSyndicatorImpl bsi = new DummySyndicator();
        bsi.setResouceCollector(new ResourceCollector());

        User user = createNiceMock(User.class);
        final String activator = "user";
        expect(user.getName()).andReturn(activator).anyTimes();
        replay(user);

        Rule rule = new Rule(new String[] {ItemType.CONTENT.getSystemName()});

        bsi.init(user, ContentRepository.WEBSITE, ContentRepository.WEBSITE, rule);

        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

        Content homePage = hm.createContent("/", "home", CONTENT.getSystemName());
        Content anotherPage = hm.createContent("/", "page", CONTENT.getSystemName());
        hm.save();

        MetaData homepageMetaData = homePage.getMetaData();
        MetaData anotherpageMetaData = anotherPage.getMetaData();

        homePage.addVersion(new Rule(new String[]{CONTENTNODE.getSystemName()}));
        anotherPage.addVersion(new Rule(new String[]{CONTENTNODE.getSystemName()}));
        ContentVersion versionedHomePage = homePage.getVersionedContent("1.0");

        assertFalse(homepageMetaData.getIsActivated());

        bsi.activate("/", versionedHomePage);

        assertTrue(homepageMetaData.getIsActivated());
        assertFalse(homepageMetaData.getModificationDate().after(homepageMetaData.getLastActionDate()));
        assertEquals(MetaData.ACTIVATION_STATUS_ACTIVATED, homepageMetaData.getActivationStatus());

        assertFalse(anotherpageMetaData.getIsActivated());
        assertEquals(MetaData.ACTIVATION_STATUS_NOT_ACTIVATED, anotherpageMetaData.getActivationStatus());

        bsi.deactivate(versionedHomePage);

        assertFalse(homepageMetaData.getIsActivated());
        assertEquals(MetaData.ACTIVATION_STATUS_NOT_ACTIVATED, homepageMetaData.getActivationStatus());
    }
}
