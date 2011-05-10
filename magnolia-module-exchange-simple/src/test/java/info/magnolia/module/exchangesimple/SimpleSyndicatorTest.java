/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.collections.CollectionUtils;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.Content.ContentFilter;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Subscription;
import info.magnolia.cms.security.User;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

/**
 * Basic test for checking the sending end of the activation.
 * @author had
 * @version $Id:$
 */
public class SimpleSyndicatorTest extends TestCase {

    private ActivationManager actMan;
    private WebContext ctx;
    private SystemContext sysctx;
    private BaseSyndicatorImpl syndicator;
    private User user;
    private Content content;
    private HierarchyManager hm;
    private MetaData meta;
    private Collection<Subscriber> subscribers;
    private List<Object> allMocks;

    @Override
    public void setUp() {
        actMan = createStrictMock(ActivationManager.class);
        ComponentsTestUtil.setInstance(ActivationManager.class, actMan);
        ctx = createStrictMock(WebContext.class);
        MgnlContext.setInstance(ctx);
        sysctx = createStrictMock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, sysctx);
        syndicator = new SimpleSyndicator();
        user = createStrictMock(User.class);
        syndicator.user = user;
        content = createStrictMock(Content.class);
        hm = createStrictMock(HierarchyManager.class);
        meta = new MetaData() {};
        subscribers = new ArrayList<Subscriber>();
        allMocks = new ArrayList<Object>();
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, ".");
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR, ".");
        final Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_METADATA);
        rule.addAllowType(ItemType.NT_RESOURCE);
        syndicator.contentFilterRule = rule;
    }

    @Override
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.setInstance(ActivationManager.class, null);
        ComponentsTestUtil.setInstance(SystemContext.class, null);
        SystemProperty.getProperties().remove(SystemProperty.MAGNOLIA_APP_ROOTDIR);
        SystemProperty.getProperties().remove(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR);
    }

    public void testDeactivateWithNoSubscriber() throws Exception {
        runDeactivateTest(new Runnable() {
            @Override
            public void run() {
                try {
                    // TODO: shouldn't repo be set even tho there is no  subscriber???
                    expect(sysctx.getHierarchyManager(null,null)).andReturn(hm);
                    expect(hm.getContentByUUID("some-real-uuid")).andReturn(content);
                    expect(content.getMetaData()).andReturn(meta);
                    //meta.setUnActivated();
                    expect(user.getName()).andReturn("Dummy");
                    //meta.setActivatorId("Dummy");
                    //meta.setLastActivationActionDate();
                    expect(content.getChildren((ContentFilter) anyObject())).andReturn(CollectionUtils.EMPTY_COLLECTION);
                    content.save();
                    expect(content.getItemType()).andReturn(ItemType.CONTENT);
                    expect(content.getHandle()).andReturn("/path");
                    expect(ctx.getUser()).andReturn(user).times(2);
                    expect(user.getName()).andReturn("Dummy");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }});

    }

    public void testDeactivateWithNoActiveSubscriber() throws Exception {
        runDeactivateTest(new Runnable() {
            @Override
            public void run() {
                try {
                    Subscriber subscriber = createStrictMock(Subscriber.class);
                    subscribers.add(subscriber);
                    // TODO: shouldn't repo be set even tho there is no  subscriber???
                    expect(sysctx.getHierarchyManager(null,null)).andReturn(hm);
                    expect(hm.getContentByUUID("some-real-uuid")).andReturn(content);
                    expect(subscriber.isActive()).andReturn(false);
                    expect(content.getMetaData()).andReturn(meta);
                    //meta.setUnActivated();
                    expect(user.getName()).andReturn("Dummy");
                    //meta.setActivatorId("Dummy");
                    //meta.setLastActivationActionDate();
                    expect(content.getChildren((ContentFilter) anyObject())).andReturn(CollectionUtils.EMPTY_COLLECTION);
                    content.save();
                    expect(content.getItemType()).andReturn(ItemType.CONTENT);
                    expect(content.getHandle()).andReturn("/path");
                    expect(ctx.getUser()).andReturn(user).times(2);
                    expect(user.getName()).andReturn("Dummy");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }});

    }

    public void testActivateLooongName() throws Exception {
        // name of the content has to be more then 256 chars to make sure that if used somewhere to create files it will fail the test.
        StringBuilder sb = new StringBuilder("/");
        for (int i = 0; i < 256; i++) {
            sb.append(i % 10 == 0 ? 'A' : 'x');
        }
        String path = sb.toString();
        expect(content.getHandle()).andReturn(path);
        expect(actMan.getSubscribers()).andReturn(subscribers);

        Subscriber subscriber = createStrictMock(Subscriber.class);
        makeThreadSafe(subscriber, true);
        Subscription subscription = createStrictMock(Subscription.class);
        allMocks.add(subscription);
        subscribers.add(subscriber);
        // TODO: shouldn't repo be set even tho there is no  subscriber???
        expect(subscriber.isActive()).andReturn(true);
        expect(subscriber.getMatchedSubscription(path, null)).andReturn(subscription);

        expect(content.getUUID()).andReturn("some-real-uuid");
        Workspace wks = createStrictMock(Workspace.class);
        expect(content.getWorkspace()).andReturn(wks).times(2);
        Session session = createStrictMock(Session.class);
        expect(wks.getSession()).andReturn(session);
        expect(content.getHandle()).andReturn("/whatever/just/for/the/logs");
        expect(content.getUUID()).andReturn("some-real-uuid");
        // activating non versioned nodes!
        expect(content.isNodeType("nt:frozenNode")).andReturn(false);
        expect(wks.getName()).andReturn("dummy-wks");
        final boolean isFile = false;
        expect(content.isNodeType("nt:file")).andReturn(isFile);
        Node jcr = createStrictMock(Node.class);
        expect(content.getJCRNode()).andReturn(jcr);
        expect(jcr.getPath()).andReturn(path);
        session.exportSystemView(eq(path), isA(GZIPOutputStream.class), eq(false), eq(!isFile));
        expect(content.getName()).andReturn(path.substring(1));

        expect(content.getChildren((ContentFilter) anyObject())).andReturn(CollectionUtils.EMPTY_COLLECTION);
        expect(content.hasMixin("mgnl:deleted")).andReturn(false);

        //expect(hm.getContentByUUID("some-real-uuid")).andReturn(content);
        // proceed with activation
        expect(subscription.getToURI()).andReturn("/");
        expect(subscription.getFromURI()).andReturn("/");
        expect(subscriber.getName()).andReturn("aSubscriber");
        expect(subscriber.getURL()).andReturn("prot://dummyURL");
        expect(subscriber.getAuthenticationMethod()).andReturn("basic");
        expect(subscriber.getName()).andReturn("aSubscriber");
        // and don't update the status ...

        expect(user.getName()).andReturn("Dummy");

        allMocks.addAll(subscribers);
        allMocks.addAll(Arrays.asList(content, actMan, ctx, sysctx, hm, user, wks, session, jcr));
        replay(allMocks.toArray());
        try {
            syndicator.activate("/", content);
        } catch (ExchangeException e) {
            // and fail because the activation target doesn't exist.
            assertEquals("info.magnolia.cms.exchange.ExchangeException: 1 error detected: \nIncorrect URL for subscriber EasyMock for interface info.magnolia.cms.exchange.Subscriber[prot://dummyURL/.magnolia/activation] on aSubscriber", e.getMessage());
        }
        verify(allMocks.toArray());
    }

    public void runDeactivateTest(Runnable run) throws Exception {
        expect(content.getUUID()).andReturn("some-real-uuid");
        expect(content.getHandle()).andReturn("/path");
        expect(actMan.getSubscribers()).andReturn(subscribers);
        run.run();
        allMocks.addAll(subscribers);
        allMocks.addAll(Arrays.asList(content, actMan, ctx, sysctx, hm, user));
        replay(allMocks.toArray());
        syndicator.deactivate(content);
        verify(allMocks.toArray());
    }

}
