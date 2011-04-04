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
package info.magnolia.module.templatingcomponents.components;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import junit.framework.TestCase;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EditBarTest extends TestCase {
    // TODO: to be uncommented and fixed as soon as there's a suitable implementation of AccessProvider
    public void testPathNodeCollectionNameEtc() throws Exception {
//        final MockHierarchyManager hm = MockUtil.createHierarchyManager("/foo/bar/baz/paragraphs/01.text=dummy");
//        AccessManager accessManager = createMock(AccessManager.class);
//        // for finer-but-not-too-verbose checks, use the contains() constraint
//        expect(accessManager.isGranted(isA(String.class), anyLong())).andReturn(true).anyTimes();
//        hm.setAccessManager(accessManager);
//
//        final AggregationState aggregationState = new AggregationState();
//        aggregationState.setMainContent(hm.getContent("/foo/bar/baz"));
//        aggregationState.setCurrentContent(hm.getContent("/foo/bar/baz/paragraphs/01"));
//        final WebContext ctx = createMock(WebContext.class);
//        expect(ctx.getAggregationState()).andReturn(aggregationState).anyTimes();
//        expect(ctx.getLocale()).andReturn(Locale.US).anyTimes();
//        expect(ctx.getAttribute(SingletonParagraphBar.class.getName(), Context.LOCAL_SCOPE)).andReturn(null).anyTimes();
//        MgnlContext.setInstance(ctx);
//        replay(accessManager, ctx);
//
//        final ServerConfiguration serverCfg = new ServerConfiguration();
//        serverCfg.setAdmin(true);
//        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);
//        // register some default components used internally
//        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
//        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
//        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());
//
//        final EditBar bar = EditBar.make(serverCfg, aggregationState, null, null, null, true, true);
//        final StringWriter out = new StringWriter();
//        bar.doRender(out);
//
//        // TODO assertTrue(out.contains(....))
    }

    @Override
    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.clear();
        super.tearDown();
    }
}
