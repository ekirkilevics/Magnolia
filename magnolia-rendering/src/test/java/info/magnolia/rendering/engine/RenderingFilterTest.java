/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.rendering.engine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.SessionTestUtil;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionProvider;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.jcr.Node;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.mockrunner.mock.web.MockServletOutputStream;

/**
 * @version $Id$
 */
public class RenderingFilterTest {

    private static final String TEMPLATE_NAME = "testTemplateName";
    private RenderingEngine renderingEngine;
    private TemplateDefinitionRegistry templateDefinitionRegistry;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @Before
    public void setUp() {
        renderingEngine = mock(RenderingEngine.class);
        templateDefinitionRegistry = new TemplateDefinitionRegistry();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @Test
    public void testDoFilterWithNullTemplateName() throws Exception {
        MgnlContext.setInstance(new MockWebContext());

        // GIVEN
        RenderingFilter filter = new RenderingFilter(renderingEngine, templateDefinitionRegistry);

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN - doFilter could not properly be executed so when ended up with an Error.
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoFilter() throws Exception {
        // GIVEN
        MockWebContext context = new MockWebContext();
        AggregationState aggState = new AggregationState();
        aggState.setTemplateName(TEMPLATE_NAME);
        context.setAggregationState(aggState);
        MgnlContext.setInstance(context);

        TemplateDefinitionProvider provider = mock(TemplateDefinitionProvider.class);
        when(provider.getId()).thenReturn(TEMPLATE_NAME);
        templateDefinitionRegistry.register(provider);

        RenderingFilter filter = new RenderingFilter(renderingEngine, templateDefinitionRegistry);
        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        verify(response).flushBuffer();
        verify(renderingEngine).render((Node) anyObject(), (RenderableDefinition) anyObject(), (Map) anyObject(),
                (Appendable) anyObject());
    }

    @Test
    public void testDoFilterContinuesOnIOExceptionFromFlushBuffer() throws Exception {
        // GIVEN
        MockWebContext context = new MockWebContext();
        AggregationState aggState = new AggregationState();
        aggState.setTemplateName(TEMPLATE_NAME);
        context.setAggregationState(aggState);
        MgnlContext.setInstance(context);

        TemplateDefinitionProvider provider = mock(TemplateDefinitionProvider.class);
        when(provider.getId()).thenReturn(TEMPLATE_NAME);
        templateDefinitionRegistry.register(provider);

        doThrow(new IOException()).when(response).flushBuffer();

        RenderingFilter filter = new RenderingFilter(renderingEngine, templateDefinitionRegistry);
        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        verify(response).flushBuffer();
        verify(renderingEngine).render((Node) anyObject(), (RenderableDefinition) anyObject(), (Map) anyObject(),
                (Appendable) anyObject());
    }

    @Test(expected = RuntimeException.class)
    public void testDoFilterFailesOnNonRendererExceptions() throws Exception {
        // GIVEN
        MockWebContext context = new MockWebContext();
        AggregationState aggState = new AggregationState();
        aggState.setTemplateName(TEMPLATE_NAME);
        context.setAggregationState(aggState);
        MgnlContext.setInstance(context);

        TemplateDefinitionProvider provider = mock(TemplateDefinitionProvider.class);
        when(provider.getId()).thenReturn(TEMPLATE_NAME);
        templateDefinitionRegistry.register(provider);

        doThrow(new IllegalArgumentException()).when(response).flushBuffer();

        RenderingFilter filter = new RenderingFilter(renderingEngine, templateDefinitionRegistry);
        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        verify(response).flushBuffer();
        verify(renderingEngine).render((Node) anyObject(), (RenderableDefinition) anyObject(), (Map) anyObject(),
                (Appendable) anyObject());
    }

    @Test(expected = ServletException.class)
    public void testDoFilterThrowsServletExceptionOnMissingTemplateDefinitionProvider() throws Exception {
        // GIVEN
        AggregationState aggState = new AggregationState();
        aggState.setTemplateName(TEMPLATE_NAME);
        MockWebContext context = new MockWebContext();
        context.setAggregationState(aggState);
        MgnlContext.setInstance(context);

        RenderingFilter filter = new RenderingFilter(renderingEngine, templateDefinitionRegistry);
        // WHEN
        filter.doFilter(request, response, chain);

        // THEN - expected Exception
    }

    @Test
    public void testDoFilterWhenTemplateIsNotRegistered() throws Exception {
        // GIVEN
        RenderingFilter filter = new RenderingFilter(renderingEngine, templateDefinitionRegistry);
        MockWebContext context = new MockWebContext();
        AggregationState aggState = new AggregationState();
        context.setAggregationState(aggState);
        MgnlContext.setInstance(context);

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN - expected Exception
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testHandleResourceRequest() throws Exception {
        // GIVEN
        String contentProperties = StringUtils.join(Arrays.asList(
                "/first/attachment2.@type=mgnl:resource",
                "/first/attachment2.fileName=test",
                "/first/attachment2.extension=jpeg",
                "/first/attachment2.jcr\\:data=binary:X",
                "/first/attachment2.jcr\\:mimeType=image/jpeg",
                "/first/attachment2.size=1"
        ), "\n");

        final String first = "first";

        MockSession session = SessionTestUtil.createSession(contentProperties);

        final String binaryNodeName = "attachment2";

        AggregationState aggState = new AggregationState();
        aggState.setHandle("/" + first + "/" + binaryNodeName);
        final String repository = "testRepository";
        aggState.setRepository(repository);
        WebContext context = mock(WebContext.class);
        MgnlContext.setInstance(context);
        when(context.getJCRSession(repository)).thenReturn(session);
        MockServletOutputStream outputStream = new MockServletOutputStream();

        when(response.getOutputStream()).thenReturn(outputStream);

        RenderingFilter filter = new RenderingFilter(renderingEngine, templateDefinitionRegistry);

        // WHEN
        filter.handleResourceRequest(aggState, request, response);

        // THEN
        verify(response).setContentLength(1);
        assertEquals("X", outputStream.getContent());
    }
}
