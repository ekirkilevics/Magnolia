/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
import static org.mockito.Mockito.*;
import info.magnolia.channel.ChannelConfiguration;
import info.magnolia.channel.ChannelManager;
import info.magnolia.channel.ChannelManagerImpl;
import info.magnolia.channel.ChannelResolver;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockWebContext;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultiChannelFilterTest {

    public class TestChannelResolver implements ChannelResolver {

        private String channel;

        public TestChannelResolver(String channel) {
            this.channel = channel;
        }

        @Override
        public String resolveChannel(HttpServletRequest request) {
            return channel;
        }
    }

    private MockWebContext ctx;
    private MultiChannelFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private AggregationState state;
    private ChannelManagerImpl channelManager;

    @Before
    public void setUp() throws Exception {
        ctx = new MockWebContext();
        ComponentsTestUtil.setInstance(WebContext.class, ctx);
        MgnlContext.setInstance(ctx);

        ComponentsTestUtil.setImplementation(SystemContext.class, MockContext.class);
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);

        channelManager = new ChannelManagerImpl();
        ComponentsTestUtil.setInstance(ChannelManager.class, channelManager);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        filter = new MultiChannelFilter(channelManager);

        state = new AggregationState();
        ctx.setAggregationState(state);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testALLGetsSetWhenThereIsNoResolver() throws Exception {
        // GIVEN
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://bla/context/root.selector.html"));

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        assertEquals(ChannelResolver.ALL, MgnlContext.getAggregationState().getChannel().getName());
    }

    @Test
    public void testChannelFromResolverGetsSet() throws Exception {
        // GIVEN
        final ChannelConfiguration unresolvedChannel = new ChannelConfiguration();
        unresolvedChannel.setResolver(new TestChannelResolver(ChannelResolver.UNRESOLVED));
        channelManager.addChannel(ChannelResolver.UNRESOLVED, unresolvedChannel);

        final ChannelConfiguration resolvedChannel = new ChannelConfiguration();

        final String testChannel = "test";
        resolvedChannel.setResolver(new TestChannelResolver(testChannel));
        channelManager.addChannel(testChannel, resolvedChannel);

        when(request.getRequestURL()).thenReturn(new StringBuffer("http://bla/context/root.selector.html"));

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        assertEquals(testChannel, MgnlContext.getAggregationState().getChannel().getName());
    }

    @Test
    public void testChannelParameterValueGetsSet() throws Exception {
        // GIVEN
        final ChannelConfiguration resolvedChannel = new ChannelConfiguration();
        final String testChannel = "channelFromResolver";
        resolvedChannel.setResolver(new TestChannelResolver(testChannel));
        channelManager.addChannel(testChannel, resolvedChannel);

        final String channelParamValue = "channelFromParam";
        MgnlContext.setAttribute(MultiChannelFilter.ENFORCE_CHANNEL_PARAMETER, channelParamValue);

        when(request.getRequestURL()).thenReturn(new StringBuffer("http://bla/context/root.selector.html"));

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        assertEquals(channelParamValue, MgnlContext.getAggregationState().getChannel().getName());
    }

}
