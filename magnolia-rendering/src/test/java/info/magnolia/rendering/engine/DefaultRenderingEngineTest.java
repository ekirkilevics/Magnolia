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
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.renderer.registry.RendererProvider;
import info.magnolia.rendering.renderer.registry.RendererRegistry;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.util.RegistrationException;
import info.magnolia.test.mock.jcr.MockNode;

import java.io.IOException;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */

public class DefaultRenderingEngineTest {

    private static final String FREEMARKER_RENDERER_TYPE = "freemarker";
    private RendererRegistry rendererRegistry;
    private DefaultRenderingEngine renderingEngine;
    private TemplateDefinitionAssignment templateDefinitionAssignment;

    @Before
    public void setUp() {
        rendererRegistry = new RendererRegistry();
        templateDefinitionAssignment = mock(TemplateDefinitionAssignment.class);
        renderingEngine = new DefaultRenderingEngine(rendererRegistry, templateDefinitionAssignment);
    }

    @Test
    public void testGetAggregationStateSafely() {
        // given
        final WebContext context = mock(WebContext.class);
        final AggregationState aggregationState = new AggregationState();
        given(context.getAggregationState()).willReturn(aggregationState);
        MgnlContext.setInstance(context);

        // when
        AggregationState result = DefaultRenderingEngine.getAggregationStateSafely();

        // then
        assertEquals(aggregationState, result);
    }

    @Test
    public void testGetAggregationStateSafelyWithNonWebContext() {
        // given
        final Context context = mock(Context.class);
        MgnlContext.setInstance(context);

        // when
        AggregationState result = DefaultRenderingEngine.getAggregationStateSafely();

        // then
        assertNull(result);
    }

    @Test(expected = RenderException.class)
    public void testGetRendererForThrowsExceptionWhenNoneIsRegistered() throws RenderException {
        // given
        TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        given(templateDefinition.getRenderType()).willReturn(FREEMARKER_RENDERER_TYPE);

        // when
        renderingEngine.getRendererFor(templateDefinition);
    }

    @Test
    public void testGetRenderingContextWhenNotYetSet() {
        // given
        ComponentProvider componentProvider = mock(ComponentProvider.class);
        AggregationStateBasedRenderingContext renderingContext = mock(AggregationStateBasedRenderingContext.class);
        given(componentProvider.newInstance(AggregationStateBasedRenderingContext.class, (Object) null)).willReturn(
                renderingContext);
        Components.setProvider(componentProvider);

        // when
        RenderingContext result = renderingEngine.getRenderingContext();

        // then
        assertEquals(renderingContext, result);
    }

    @Test
    public void testRenderFromNodeAndAppendable() throws Exception {
        // given
        final Node content = new MockNode("parent");
        final Context context = mock(Context.class);
        final RenderingContext renderingContext = mock(RenderingContext.class);
        given(context.getAttribute(DefaultRenderingEngine.RENDERING_CONTEXT_ATTRIBUTE, Context.LOCAL_SCOPE))
                .willReturn(renderingContext);
        given(context.getAttribute(DefaultRenderingEngine.RENDERING_CONTEXT_ATTRIBUTE)).willReturn(renderingContext);

        MgnlContext.setInstance(context);

        TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        given(templateDefinitionAssignment.getAssignedTemplateDefinition(content)).willReturn(templateDefinition);

        Renderer freemarkerRenderer = mock(Renderer.class);
        RendererProvider freemarkerRendererProvider = mock(RendererProvider.class);
        given(freemarkerRendererProvider.getId()).willReturn(FREEMARKER_RENDERER_TYPE);
        given(freemarkerRendererProvider.getDefinition()).willReturn(freemarkerRenderer);
        rendererRegistry.register(freemarkerRendererProvider);

        StringBuilder builder = new StringBuilder();
        given(templateDefinition.getRenderType()).willReturn(FREEMARKER_RENDERER_TYPE);

        // when
        renderingEngine.render(content, builder);

        // then
        verify(freemarkerRenderer).render(content, templateDefinition, DefaultRenderingEngine.EMPTY_CONTEXT, builder);
    }

    @Test(expected = RenderException.class)
    public void testRenderThrowsRenderExceptionInCaseOfInternalIOException() throws Exception {
        // given
        final Node content = new MockNode("parent");
        final Context context = mock(Context.class);
        final RenderingContext renderingContext = mock(RenderingContext.class);
        given(context.getAttribute(DefaultRenderingEngine.RENDERING_CONTEXT_ATTRIBUTE, Context.LOCAL_SCOPE))
                .willReturn(renderingContext);
        given(context.getAttribute(DefaultRenderingEngine.RENDERING_CONTEXT_ATTRIBUTE)).willReturn(renderingContext);

        MgnlContext.setInstance(context);

        TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        given(templateDefinitionAssignment.getAssignedTemplateDefinition(content)).willReturn(templateDefinition);

        Renderer freemarkerRenderer = mock(Renderer.class);
        RendererProvider freemarkerRendererProvider = mock(RendererProvider.class);
        given(freemarkerRendererProvider.getId()).willReturn(FREEMARKER_RENDERER_TYPE);
        given(freemarkerRendererProvider.getDefinition()).willReturn(freemarkerRenderer);
        rendererRegistry.register(freemarkerRendererProvider);

        StringBuilder builder = new StringBuilder();
        given(templateDefinition.getRenderType()).willReturn(FREEMARKER_RENDERER_TYPE);
        willThrow(new IOException()).given(freemarkerRenderer).render(content, templateDefinition,
                DefaultRenderingEngine.EMPTY_CONTEXT, builder);

        // when
        renderingEngine.render(content, builder);

        // then - no code here as we expect an Exception
    }

    @Test(expected = RenderException.class)
    public void testRenderThrowsRenderExceptionInCaseOfInternalRegistrationException() throws Exception {
        // given
        final Node content = new MockNode("parent");
        final Context context = mock(Context.class);
        final RenderingContext renderingContext = mock(RenderingContext.class);
        given(context.getAttribute(DefaultRenderingEngine.RENDERING_CONTEXT_ATTRIBUTE, Context.LOCAL_SCOPE))
                .willReturn(renderingContext);
        given(context.getAttribute(DefaultRenderingEngine.RENDERING_CONTEXT_ATTRIBUTE)).willReturn(renderingContext);
        MgnlContext.setInstance(context);

        willThrow(new RegistrationException("test")).given(templateDefinitionAssignment).getAssignedTemplateDefinition(content);

        // when
        renderingEngine.render(content, null);

        // then - no code here as we expect an Exception
    }

}
