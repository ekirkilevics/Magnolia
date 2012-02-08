/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.cms.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InterceptFilterTest {

    private static final String MGNL_PREVIEW = "mgnlPreview";
    private MockWebContext ctx;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private AggregationState state;

    @Before
    public void setUp() throws Exception {
        ctx = new MockWebContext();
        MockSession session = new MockSession("website");
        session.getRootNode().addNode("1");
        session.getRootNode().addNode("2");

        ctx.addSession("website", session);
        ctx.setUser(mock(User.class));

        MgnlContext.setInstance(ctx);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        state = new AggregationState();
        state.setHandle("/");
        ctx.setAggregationState(state);

    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testPreviewIsSetInMgnlContextSession() throws Exception {
        //GIVEN
        when(request.getParameter(InterceptFilter.INTERCEPT)).thenReturn("PREVIEW");
        when(request.getParameter(MGNL_PREVIEW)).thenReturn("true");
        InterceptFilter filter = new InterceptFilter();

        //WHEN
        filter.intercept(request, response);

        //THEN
        assertNotNull(MgnlContext.getAttribute(MGNL_PREVIEW, Context.SESSION_SCOPE));
    }

    @Test
    public void testPreviewIsRemovedFromMgnlContextSession() throws Exception {
        //GIVEN
        when(request.getParameter(InterceptFilter.INTERCEPT)).thenReturn("PREVIEW");
        when(request.getParameter(MGNL_PREVIEW)).thenReturn("false");
        ctx.setAttribute(MGNL_PREVIEW, "true", Context.SESSION_SCOPE);
        InterceptFilter filter = new InterceptFilter();

        //WHEN
        filter.intercept(request, response);

        //THEN
        assertNull(MgnlContext.getAttribute(MGNL_PREVIEW, Context.SESSION_SCOPE));

        //GIVEN
        ctx.setAttribute(MGNL_PREVIEW, "true", Context.SESSION_SCOPE);
        when(request.getParameter(MGNL_PREVIEW)).thenReturn(null);

        //WHEN
        filter.intercept(request, response);

        //THEN
        assertNull(MgnlContext.getAttribute(MGNL_PREVIEW, Context.SESSION_SCOPE));
    }

    @Test(expected=PathNotFoundException.class)
    public void testDeleteNodeAction() throws Exception {
        //GIVEN
        when(request.getParameter(InterceptFilter.INTERCEPT)).thenReturn("NODE_DELETE");
        when(request.getParameter("mgnlPath")).thenReturn("/1");

        InterceptFilter filter = new InterceptFilter();
        //WHEN
        filter.intercept(request, response);

        //THEN
        ctx.getJCRSession("website").getNode("/1");
    }

    @Test
    public void testSortNodeAction() throws Exception {
        //GIVEN
        when(request.getParameter(InterceptFilter.INTERCEPT)).thenReturn("NODE_SORT");
        when(request.getParameter("mgnlPathSelected")).thenReturn("/2");
        when(request.getParameter("mgnlPathSortAbove")).thenReturn("/1");
        when(request.getParameter("mgnlPath")).thenReturn("/");

        NodeIterator nodes = ctx.getJCRSession("website").getRootNode().getNodes();
        assertEquals("1", ((Node)nodes.next()).getName());
        assertEquals("2", ((Node)nodes.next()).getName());

        InterceptFilter filter = new InterceptFilter();
        //WHEN
        filter.intercept(request, response);

        //THEN
        nodes = ctx.getJCRSession("website").getRootNode().getNodes();
        assertEquals("2", ((Node)nodes.next()).getName());
        assertEquals("1", ((Node)nodes.next()).getName());
    }
}
