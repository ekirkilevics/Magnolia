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

import org.apache.commons.collections.CollectionUtils;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.Content.ContentFilter;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.FactoryUtil;
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
    }
    
    public void tearDown() {
        MgnlContext.setInstance(null);
        FactoryUtil.setInstance(ActivationManager.class, null);
    }
    
    public void testDeactivateWithNoSubscriber() throws Exception {
        runTest(new Runnable() {
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
                    expect(cnt.getHandle()).andReturn("/path");
                    expect(ctx.getUser()).andReturn(user).times(2);
                    expect(user.getName()).andReturn("Dummy");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }});
        
    }
    
    public void testDeactivateWithNoActiveSubscriber() throws Exception {
        runTest(new Runnable() {
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
                    expect(cnt.getHandle()).andReturn("/path");
                    expect(ctx.getUser()).andReturn(user).times(2);
                    expect(user.getName()).andReturn("Dummy");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }});
        
    }
        
    public void runTest(Runnable run) throws Exception {
        expect(cnt.getUUID()).andReturn("some-real-uuid");
        expect(cnt.getHandle()).andReturn("/path");
        expect(actMan.getSubscribers()).andReturn(subscribers);
        run.run();
        List all = new ArrayList();
        all.addAll(subscribers);
        all.addAll(Arrays.asList(new Object[] {cnt, actMan, ctx, hm, user}));
        Object[] objs =  all.toArray();
        replay(objs);
        ss.deactivate(cnt);
        verify(objs);
    }

}
