/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Subscription;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.BootstrapUtil;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

import java.net.URLConnection;
import java.util.ArrayList;

import javax.jcr.ImportUUIDBehavior;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class DefaultSubscriberTest extends RepositoryTestCase {

    private Node2BeanProcessor n2b;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(Subscriber.class, DefaultSubscriber.class);
        ComponentsTestUtil.setImplementation(ActivationManager.class, DefaultActivationManager.class);
        ComponentsTestUtil.setImplementation(Syndicator.class, TestSyndicator.class);
        ComponentsTestUtil.setImplementation(Subscription.class, DefaultSubscription.class);
        n2b = new Node2BeanProcessorImpl(new TypeMappingImpl(), new Node2BeanTransformerImpl());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        ActivationManager man = Components.getComponentProvider().newInstance(ActivationManager.class);
        man.setSubscribers(new ArrayList());
        HierarchyManager hm = MgnlContext.getHierarchyManager(RepositoryConstants.CONFIG);
        hm.getContent("/server/activation").delete();
        hm.save();
        super.tearDown();
    }

    @Test
    public void testDefaultTimeout() throws Exception {
        BootstrapUtil.bootstrap(new String[] { "/info/magnolia/module/exchangesimple/setup/config.server.activation.xml" }, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
        HierarchyManager hm = MgnlContext.getHierarchyManager(RepositoryConstants.CONFIG);

        ActivationManager man = ActivationManagerFactory.getActivationManager();
        man.addSubscribers((Subscriber) n2b.toBean(hm.getContent("/server/activation/subscribers/magnoliaPublic8080").getJCRNode()));
        assertFalse(man.getSubscribers().isEmpty());
        Subscriber subscriber = man.getSubscribers().iterator().next();

        assertEquals(10000, subscriber.getConnectTimeout());
        assertEquals(600000, subscriber.getReadTimeout());
    }

    @Test
    public void testCustomTimeout() throws Exception {
        BootstrapUtil.bootstrap(new String[] { "/info/magnolia/module/exchangesimple/setup/config.server.activation.xml" }, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
        HierarchyManager hm = MgnlContext.getHierarchyManager(RepositoryConstants.CONFIG);
        Content subContent = hm.getContent("/server/activation/subscribers/magnoliaPublic8080");
        // make sure settings works no matter what data type users use
        subContent.setNodeData("connectTimeout", 5000);
        subContent.setNodeData("readTimeout", "4000");

        ActivationManager man = ActivationManagerFactory.getActivationManager();
        man.addSubscribers((Subscriber) n2b.toBean(hm.getContent("/server/activation/subscribers/magnoliaPublic8080").getJCRNode()));
        assertFalse(man.getSubscribers().isEmpty());
        Subscriber subscriber = man.getSubscribers().iterator().next();

        assertEquals(5000, subscriber.getConnectTimeout());
        assertEquals(4000, subscriber.getReadTimeout());
    }

    @Test
    public void testTimeoutSet() throws Exception {
        BootstrapUtil.bootstrap(new String[] { "/info/magnolia/module/exchangesimple/setup/config.server.activation.xml" }, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
        HierarchyManager hm = MgnlContext.getHierarchyManager(RepositoryConstants.CONFIG);

        ActivationManager man = ActivationManagerFactory.getActivationManager();
        man.addSubscribers((Subscriber) n2b.toBean(hm.getContent("/server/activation/subscribers/magnoliaPublic8080").getJCRNode()));
        assertFalse(man.getSubscribers().isEmpty());
        Subscriber subscriber = man.getSubscribers().iterator().next();

        TestSyndicator syndicator= new TestSyndicator();
        String url = syndicator.getActivationURL(subscriber);
        URLConnection conn = syndicator.prepareConnection(subscriber, url);

        assertEquals(10000, conn.getConnectTimeout());
        assertEquals(600000, conn.getReadTimeout());
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
        public URLConnection prepareConnection(Subscriber subscriber, String url) throws ExchangeException {
            return super.prepareConnection(subscriber, url);
        }
    }
}
