/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
package info.magnolia.module.admininterface.commands;


import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;


/**
 * @author had
 *
 */
public class ActivationCommandTest extends TestCase {
    private static final String PARENT_PATH = "/foo/bar";

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testActivateNonRecursiveNoSiblings() throws Exception {
        final ActivationCommand command = new ActivationCommand();
        command.setRepository("some-repo");
        command.setPath(PARENT_PATH);
        final SystemContext sysCtx = createStrictMock(SystemContext.class);
        final WebContext ctx = createStrictMock(WebContext.class);
        final HierarchyManager hm = createMock(HierarchyManager.class);
        final Content state = createStrictMock(Content.class);
        final Content parent = createStrictMock(Content.class);
        final Node stateJCRNode = createStrictMock(Node.class);
        final Node parentJCRNode = createStrictMock(Node.class);
        final NodeIterator siblings = createNiceMock(NodeIterator.class);
        final Syndicator syndicator = createStrictMock(Syndicator.class);
        command.setSyndicator(syndicator);
        final Messages messages = createNiceMock(Messages.class);

        FactoryUtil.setInstance(SystemContext.class, sysCtx);
        MgnlContext.setInstance(ctx);

        expect(ctx.getHierarchyManager("some-repo")).andReturn(hm);
        expect(hm.getContent(PARENT_PATH)).andReturn(state);
        expect(state.getHandle()).andReturn(PARENT_PATH);
        expect(state.getName()).andReturn("foo");
        expect(state.getJCRNode()).andReturn(stateJCRNode);
        expect(state.getNodeTypeName()).andReturn("mgnl:contentNode");
        expect(state.getUUID()).andReturn("123-uuid-blablabla-123");
        expect(stateJCRNode.getParent()).andReturn(parentJCRNode);
        expect(parentJCRNode.getNodes()).andReturn(siblings);
        expect(siblings.hasNext()).andReturn(true);
        expect(siblings.nextNode()).andReturn(stateJCRNode);
        expect(stateJCRNode.isNodeType("mgnl:contentNode")).andReturn(true);
        expect(stateJCRNode.getUUID()).andReturn("123-uuid-blablabla-123");
        expect(siblings.hasNext()).andReturn(false);
        syndicator.activate("/foo", state, new ArrayList());
        // called only on errors:
        //expect(ctx.getMessages()).andReturn(messages);

        replay(ctx, hm, state, parent, stateJCRNode, parentJCRNode, siblings, messages, syndicator);
        command.execute(ctx);
        verify(ctx, hm, state, parent, stateJCRNode, parentJCRNode, siblings, messages, syndicator);
    }

    //todo: refactor and test other scenarios
    // - recursive
    // - with siblings before and after in the list


}
