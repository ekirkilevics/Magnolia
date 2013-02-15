/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.templating.jsp.taglib;

import static org.mockito.Mockito.*;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.jsp.PageContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockPageContext;
import com.mockrunner.mock.web.MockServletConfig;

/**
 * SimpleNavigationTagTest.
 */
public class SimpleNavigationTagTest {

    @Before
    public void setUp() throws Exception {
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());

    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testChildren() throws Exception {
        SimpleNavigationTag tag = new SimpleNavigationTag();
        Content current = mock(Content.class);
        Content parent = mock(Content.class);
        Content dummy = mock(Content.class);
        Content hideInNav = mock(Content.class);
        WebContext ctx = mock(WebContext.class);
        NodeData bool = mock(NodeData.class);
        NodeData string = mock(NodeData.class);
        AggregationState aggState = new AggregationState();
        PageContext pageContext = new MockPageContext(new MockServletConfig(), new MockHttpServletRequest(), new MockHttpServletResponse());
        MgnlContext.setInstance(ctx);
        when(ctx.getAggregationState()).thenReturn(aggState);
        aggState.setCurrentContent(current);
        when(current.getNodeTypeName()).thenReturn(ItemType.CONTENT.getSystemName());
        tag.setPageContext(pageContext);
        // start level is 0 by default, so returning 0 here is enough
        when(current.getLevel()).thenReturn(0);
        when(current.getAncestor(0)).thenReturn(parent);

        // drawChildren()
        when(parent.getChildren(ItemType.CONTENT)).thenReturn(Arrays.asList(new Content[] { dummy, hideInNav, current }));
        when(parent.getLevel()).thenReturn(0);

        // loop children
        when(dummy.getNodeData("hideInNav")).thenReturn(bool);
        when(bool.getBoolean()).thenReturn(new Boolean(false));

        when(hideInNav.getNodeData("hideInNav")).thenReturn(bool);
        when(bool.getBoolean()).thenReturn(new Boolean(true));

        when(current.getNodeData("hideInNav")).thenReturn(bool);
        when(bool.getBoolean()).thenReturn(new Boolean(false));

        // draw visible children
        // dummy
        when(dummy.getNodeData("navTitle")).thenReturn(string);
        when(string.getString("")).thenReturn("dummyTitle");

        // test if child is current page
        when(current.getHandle()).thenReturn("/");
        when(dummy.getHandle()).thenReturn("/dummy");

        // test if children should be rendered too
        // the real level would be higher, but so would be bigger the list of ancestors
        when(dummy.getLevel()).thenReturn(1);
        when(current.getAncestors()).thenReturn(Arrays.asList(new Content[] {}));
        when(dummy.getNodeData("openMenu")).thenReturn(bool);
        when(bool.getBoolean()).thenReturn(false);
        when(dummy.getChildren()).thenReturn(new ArrayList<Content>());
        when(dummy.getLevel()).thenReturn(1);
        when(current.getLevel()).thenReturn(0);
        when(dummy.getNodeData("accessKey")).thenReturn(string);
        when(string.getString("")).thenReturn("");
        when(dummy.getHandle()).thenReturn("/dummy");

        // hideInNav
        // - nothing

        // current
        when(current.getNodeData("navTitle")).thenReturn(string);
        when(string.getString("")).thenReturn("currentTitle");

        // test if child is current page
        when(current.getHandle()).thenReturn("/");
        when(current.getHandle()).thenReturn("/current");

        // test if children should be rendered too
        // the real level would be higher, but so would be bigger the list of ancestors
        when(current.getLevel()).thenReturn(1);
        when(current.getAncestors()).thenReturn(Arrays.asList(new Content[] {}));
        when(current.getNodeData("openMenu")).thenReturn(bool);
        when(bool.getBoolean()).thenReturn(false);
        when(current.getChildren()).thenReturn(new ArrayList<Content>());
        when(current.getLevel()).thenReturn(1);
        when(current.getLevel()).thenReturn(0);
        when(current.getNodeData("accessKey")).thenReturn(string);
        when(string.getString("")).thenReturn("");
        when(current.getHandle()).thenReturn("/current");

        tag.doEndTag();
    }

}
