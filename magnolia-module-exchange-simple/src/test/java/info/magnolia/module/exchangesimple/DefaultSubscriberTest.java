/**
 * This file Copyright (c) 2010 Magnolia International
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

import java.net.URLConnection;
import java.util.ArrayList;

import javax.jcr.ImportUUIDBehavior;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.BootstrapUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

public class DefaultSubscriberTest extends RepositoryTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(Subscriber.class, DefaultSubscriber.class);
        ComponentsTestUtil.setImplementation(ActivationManager.class, DefaultActivationManager.class);
        ComponentsTestUtil.setImplementation(Syndicator.class, TestSyndicator.class);
    }

    @Override
    protected void tearDown() throws Exception {
        ActivationManager man = Components.getComponentProvider().newInstance(ActivationManager.class);
        man.setSubscribers(new ArrayList());
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        hm.getContent("/server/activation").delete();
        hm.save();
        super.tearDown();
    }

    public void testDefaultTimeout() throws Exception {
        BootstrapUtil.bootstrap(new String[]{"/mgnl-bootstrap/exchange-simple/config.server.activation.xml"}, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);

        ActivationManager man = ActivationManagerFactory.getActivationManager();
        man.addSubscribers((Subscriber) Content2BeanUtil.toBean(hm.getContent("/server/activation/subscribers/magnoliaPublic8080"), true));
        assertFalse(man.getSubscribers().isEmpty());
        Subscriber subscriber = man.getSubscribers().iterator().next();

        assertEquals(10000, subscriber.getConnectTimeout());
        assertEquals(10000, subscriber.getReadTimeout());
    }

    public void testCustomTimeout() throws Exception {
        BootstrapUtil.bootstrap(new String[]{"/mgnl-bootstrap/exchange-simple/config.server.activation.xml"}, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        Content subContent = hm.getContent("/server/activation/subscribers/magnoliaPublic8080");
        // make sure settings works no matter what data type users use
        subContent.setNodeData("connectTimeout", 5000);
        subContent.setNodeData("readTimeout", "4000");

        ActivationManager man = ActivationManagerFactory.getActivationManager();
        man.addSubscribers((Subscriber) Content2BeanUtil.toBean(hm.getContent("/server/activation/subscribers/magnoliaPublic8080"), true));
        assertFalse(man.getSubscribers().isEmpty());
        Subscriber subscriber = man.getSubscribers().iterator().next();

        assertEquals(5000, subscriber.getConnectTimeout());
        assertEquals(4000, subscriber.getReadTimeout());
    }

    public void testTimeoutSet() throws Exception {
        BootstrapUtil.bootstrap(new String[]{"/mgnl-bootstrap/exchange-simple/config.server.activation.xml"}, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);

        ActivationManager man = ActivationManagerFactory.getActivationManager();
        man.addSubscribers((Subscriber) Content2BeanUtil.toBean(hm.getContent("/server/activation/subscribers/magnoliaPublic8080"), true));
        assertFalse(man.getSubscribers().isEmpty());
        Subscriber subscriber = man.getSubscribers().iterator().next();

        TestSyndicator syndicator= new TestSyndicator();
        URLConnection conn = syndicator.prepareConnection(subscriber);

        assertEquals(10000, conn.getConnectTimeout());
        assertEquals(10000, conn.getReadTimeout());
    }

    public class TestSyndicator extends BaseSyndicatorImpl {

        @Override
        public void activate(ActivationContent activationContent, String nodePath) throws ExchangeException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String activate(Subscriber subscriber, ActivationContent activationContent, String nodePath) throws ExchangeException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void doDeactivate(String nodeUUID, String nodePath) throws ExchangeException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String doDeactivate(Subscriber subscriber, String nodeUUID, String nodePath) throws ExchangeException {
            throw new UnsupportedOperationException();
        }

        @Override
        public URLConnection prepareConnection(Subscriber subscriber) throws ExchangeException {
            return super.prepareConnection(subscriber);
        }
    }
}
