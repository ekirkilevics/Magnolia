/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Subscription;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

/**
 * @author had
 * @version $Id:$
 */
public class SimpleSyndicatorTest extends TestCase {

    ActivationManager actMan;
    WebContext ctx;
    SimpleSyndicator ss;
    User user;
    Content cnt;
    HierarchyManager hm;
    MetaData meta;
    Collection subscribers;
    List all;

    public void setUp() {
        actMan = createStrictMock(ActivationManager.class);
        FactoryUtil.setInstance(ActivationManager.class, actMan);
        ctx = createStrictMock(WebContext.class);
        MgnlContext.setInstance(ctx);
        ss = new SimpleSyndicator();
        user = createStrictMock(User.class);
        ss.user = user;
        cnt = createStrictMock(Content.class);
        hm = createStrictMock(HierarchyManager.class);
        meta = new MetaData() {};
        subscribers = new ArrayList();
        all = new ArrayList();
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, ".");
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR, ".");
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_METADATA);
        rule.addAllowType(ItemType.NT_RESOURCE);
        ss.contentFilterRule = rule;
    }

    public void tearDown() {
        MgnlContext.setInstance(null);
        FactoryUtil.setInstance(ActivationManager.class, null);
        SystemProperty.getProperties().remove(SystemProperty.MAGNOLIA_APP_ROOTDIR);
        SystemProperty.getProperties().remove(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR);
    }

    public void testDeactivateWithNoSubscriber() throws Exception {
        runDeactivateTest(new Runnable() {
            public void run() {
                try {
                    // TODO: shouldn't repo be set even tho there is no  subscriber???
                    expect(ctx.getHierarchyManager(null,null)).andReturn(hm);
                    expect(hm.getContentByUUID("some-real-uuid")).andReturn(cnt);
                    expect(cnt.getMetaData()).andReturn(meta);
                    //meta.setUnActivated();
                    expect(user.getName()).andReturn("Dummy");
                    //meta.setActivatorId("Dummy");
                    //meta.setLastActivationActionDate();
                    expect(cnt.getChildren((ContentFilter) anyObject())).andReturn(CollectionUtils.EMPTY_COLLECTION);
                    cnt.save();
                    expect(cnt.getItemType()).andReturn(ItemType.CONTENT);
                    expect(cnt.getHandle()).andReturn("/path");
                    expect(ctx.getUser()).andReturn(user).times(2);
                    expect(user.getName()).andReturn("Dummy");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }});

    }

    public void testDeactivateWithNoActiveSubscriber() throws Exception {
        runDeactivateTest(new Runnable() {
            public void run() {
                try {
                    Subscriber subscriber = createStrictMock(Subscriber.class);
                    subscribers.add(subscriber);
                    // TODO: shouldn't repo be set even tho there is no  subscriber???
                    expect(ctx.getHierarchyManager(null,null)).andReturn(hm);
                    expect(hm.getContentByUUID("some-real-uuid")).andReturn(cnt);
                    expect(subscriber.isActive()).andReturn(false);
                    expect(cnt.getMetaData()).andReturn(meta);
                    //meta.setUnActivated();
                    expect(user.getName()).andReturn("Dummy");
                    //meta.setActivatorId("Dummy");
                    //meta.setLastActivationActionDate();
                    expect(cnt.getChildren((ContentFilter) anyObject())).andReturn(CollectionUtils.EMPTY_COLLECTION);
                    cnt.save();
                    expect(cnt.getItemType()).andReturn(ItemType.CONTENT);
                    expect(cnt.getHandle()).andReturn("/path");
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
        expect(cnt.getHandle()).andReturn(path);
        expect(actMan.getSubscribers()).andReturn(subscribers);

        Subscriber subscriber = createStrictMock(Subscriber.class);
        Subscription subscription = createStrictMock(Subscription.class);
        all.add(subscription);
        subscribers.add(subscriber);
        // TODO: shouldn't repo be set even tho there is no  subscriber???
        expect(ctx.getHierarchyManager(null,null)).andReturn(hm);
        expect(subscriber.isActive()).andReturn(true);
        expect(subscriber.getMatchedSubscription(path, null)).andReturn(subscription);

        expect(cnt.getUUID()).andReturn("some-real-uuid");
        Workspace wks = createStrictMock(Workspace.class);
        expect(cnt.getWorkspace()).andReturn(wks);
        Session session = createStrictMock(Session.class);
        expect(wks.getSession()).andReturn(session);
        expect(cnt.getUUID()).andReturn("some-real-uuid");
        expect(cnt.getWorkspace()).andReturn(wks);
        expect(wks.getName()).andReturn("dummy-wks");
        boolean isFile = false;
        expect(cnt.isNodeType("nt:file")).andReturn(isFile);
        Node jcr = createStrictMock(Node.class);
        expect(cnt.getJCRNode()).andReturn(jcr);
        expect(jcr.getPath()).andReturn(path);
        session.exportSystemView(eq(path), isA(GZIPOutputStream.class), eq(false), eq(!isFile));
        expect(cnt.getName()).andReturn(path.substring(1));
        expect(cnt.getUUID()).andReturn("some-real-uuid");

        expect(cnt.getChildren((ContentFilter) anyObject())).andReturn(CollectionUtils.EMPTY_COLLECTION);

        //expect(hm.getContentByUUID("some-real-uuid")).andReturn(cnt);

        expect(hm.getContent(path)).andReturn(cnt);
        expect(cnt.getMetaData()).andReturn(meta);
        meta.setActivated();
        expect(user.getName()).andReturn("Dummy");
        meta.setActivatorId("Dummy");
        meta.setLastActivationActionDate();
        expect(cnt.getChildren((ContentFilter) anyObject())).andReturn(CollectionUtils.EMPTY_COLLECTION);
        cnt.save();
        expect(cnt.getHandle()).andReturn(path);
        expect(cnt.getItemType()).andReturn(ItemType.CONTENT);
        expect(ctx.getUser()).andReturn(user).times(2);
        expect(user.getName()).andReturn("Dummy");

        all.addAll(subscribers);
        all.addAll(Arrays.asList(new Object[] {cnt, actMan, ctx, hm, user, wks, session, jcr}));
        Object[] objs =  all.toArray();
        replay(objs);
        ss.activate("/", cnt);
        verify(objs);
    }

    public void runDeactivateTest(Runnable run) throws Exception {
        expect(cnt.getUUID()).andReturn("some-real-uuid");
        expect(cnt.getHandle()).andReturn("/path");
        expect(actMan.getSubscribers()).andReturn(subscribers);
        run.run();
        all.addAll(subscribers);
        all.addAll(Arrays.asList(new Object[] {cnt, actMan, ctx, hm, user}));
        Object[] objs =  all.toArray();
        replay(objs);
        ss.deactivate(cnt);
        verify(objs);
    }

}
