/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.commands.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockContext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

public class MarkNodeAsDeletedCommandTest extends RepositoryTestCase {

    private Node node;
    private Node childNode;
    private MarkNodeAsDeletedCommand cmd;
    private Context ctx;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ctx = mock(Context.class);
        when(ctx.get("deleteNode")).thenReturn("home-test");
        when(ctx.get("comment")).thenReturn("comment");

        ActivationManager activationManager = mock(ActivationManager.class);
        Subscriber subscriber = mock(Subscriber.class);
        Collection<Subscriber> subscribers = new ArrayList<Subscriber>();
        subscribers.add(subscriber);
        ComponentsTestUtil.setInstance(ActivationManager.class, activationManager);
        when(activationManager.getSubscribers()).thenReturn(subscribers);
        when(subscriber.isActive()).thenReturn(true);

        node = MgnlContext.getJCRSession("website").getRootNode().addNode("home-test", NodeTypes.Page.NAME);
        childNode = node.addNode("child-test", NodeTypes.Page.NAME);

        cmd = new MarkNodeAsDeletedCommand();
        cmd.setPath("/");
        cmd.setRepository("website");
    }

    @Test
    public void testUpdateAuthorIdAndModificationDateWhenMarkNodeAsDelete() throws  Exception{
        // GIVEN
        Calendar timeBeforeDelete = new GregorianCalendar(TimeZone.getDefault());
        NodeTypes.LastModified.update(node, "user-before-delete", timeBeforeDelete);
        NodeTypes.LastModified.update(childNode, "user-before-delete", timeBeforeDelete);


        node.getSession().save();

        MockContext context = (MockContext) MgnlContext.getInstance();
        context.setUser(new MgnlUser("user-after-delete","admin",Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_MAP, null, null));

        // WHEN
        cmd.execute(ctx);

        // THEN
        assertEquals("user-after-delete", NodeTypes.Deleted.getDeletedBy(node));
        assertTrue(timeBeforeDelete.getTimeInMillis() < NodeTypes.Deleted.getDeleted(node).getTimeInMillis());

        assertEquals("user-after-delete", NodeTypes.Deleted.getDeletedBy(childNode));
        assertTrue(timeBeforeDelete.getTimeInMillis() < NodeTypes.Deleted.getDeleted(childNode).getTimeInMillis());
    }

}
