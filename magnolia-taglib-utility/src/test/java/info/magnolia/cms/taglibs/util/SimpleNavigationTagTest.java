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
package info.magnolia.cms.taglibs.util;

import java.util.ArrayList;
import java.util.Arrays;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import javax.jcr.query.InvalidQueryException;
import javax.servlet.jsp.PageContext;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockPageContext;
import com.mockrunner.mock.web.MockServletConfig;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;
/**
 * @author had
 */
public class SimpleNavigationTagTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FactoryUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());

    }

    @Override
    protected void tearDown() throws Exception {
        FactoryUtil.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }
    /**
     * Test rendering child entries
     */
    public void testChildren() throws Exception {
        SimpleNavigationTag tag = new SimpleNavigationTag();
        Content current = createStrictMock(Content.class);
        Content parent = createStrictMock(Content.class);
        Content dummy = createStrictMock(Content.class);
        Content hideInNav = createStrictMock(Content.class);
        WebContext ctx = createStrictMock(WebContext.class);
        NodeData bool = createStrictMock(NodeData.class);
        NodeData string = createStrictMock(NodeData.class);
        AggregationState aggState = new AggregationState();
        PageContext pageContext = new MockPageContext(new MockServletConfig(),new MockHttpServletRequest(), new MockHttpServletResponse());
        MgnlContext.setInstance(ctx);
        expect(ctx.getAggregationState()).andReturn(aggState);
        aggState.setCurrentContent(current);
        expect(current.getNodeTypeName()).andReturn(ItemType.CONTENT.getSystemName());
        tag.setPageContext(pageContext);
        // start level is 0 by default, so returning 0 here is enough
        expect(current.getLevel()).andReturn(0);
        expect(current.getAncestor(0)).andReturn(parent);

        //drawChildren()
        expect(parent.getChildren(ItemType.CONTENT)).andReturn(Arrays.asList(new Content[] {dummy, hideInNav, current}));
        expect(parent.getLevel()).andReturn(0);

        // loop children
        expect(dummy.getNodeData("hideInNav")).andReturn(bool);
        expect(bool.getBoolean()).andReturn(new Boolean(false));

        expect(hideInNav.getNodeData("hideInNav")).andReturn(bool);
        expect(bool.getBoolean()).andReturn(new Boolean(true));

        expect(current.getNodeData("hideInNav")).andReturn(bool);
        expect(bool.getBoolean()).andReturn(new Boolean(false));

        //draw visible children
        //dummy
        expect(dummy.getNodeData("navTitle")).andReturn(string);
        expect(string.getString("")).andReturn("dummyTitle");

        // test if child is current page
        expect(current.getHandle()).andReturn("/");
        expect(dummy.getHandle()).andReturn("/dummy");

        // test if children should be rendered too
        // the real level would be higher, but so would be bigger the list of ancestors
        expect(dummy.getLevel()).andReturn(1);
        expect(current.getAncestors()).andReturn(Arrays.asList(new Content[] {}));
        expect(dummy.getNodeData("openMenu")).andReturn(bool);
        expect(bool.getBoolean()).andReturn(false);
        expect(dummy.getChildren()).andReturn(new ArrayList<Content>());
        expect(dummy.getLevel()).andReturn(1);
        expect(current.getLevel()).andReturn(0);
        expect(dummy.getNodeData("accessKey")).andReturn(string);
        expect(string.getString("")).andReturn("");
        expect(dummy.getHandle()).andReturn("/dummy");

        //hideInNav
        // - nothing

        // current
        expect(current.getNodeData("navTitle")).andReturn(string);
        expect(string.getString("")).andReturn("currentTitle");

        // test if child is current page
        expect(current.getHandle()).andReturn("/");
        expect(current.getHandle()).andReturn("/current");

        // test if children should be rendered too
        // the real level would be higher, but so would be bigger the list of ancestors
        expect(current.getLevel()).andReturn(1);
        expect(current.getAncestors()).andReturn(Arrays.asList(new Content[] {}));
        expect(current.getNodeData("openMenu")).andReturn(bool);
        expect(bool.getBoolean()).andReturn(false);
        expect(current.getChildren()).andReturn(new ArrayList<Content>());
        expect(current.getLevel()).andReturn(1);
        expect(current.getLevel()).andReturn(0);
        expect(current.getNodeData("accessKey")).andReturn(string);
        expect(string.getString("")).andReturn("");
        expect(current.getHandle()).andReturn("/current");


        Object mocks[] = new Object[] {current, ctx, parent, dummy, hideInNav, bool, string};
        replay(mocks);
        tag.doEndTag();
        verify(mocks);
    }
}
