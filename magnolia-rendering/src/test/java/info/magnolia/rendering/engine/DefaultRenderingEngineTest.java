/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.rendering.engine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.renderer.registry.RendererProvider;
import info.magnolia.rendering.renderer.registry.RendererRegistry;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.util.AppendableWriter;
import info.magnolia.test.mock.jcr.MockNode;

import javax.inject.Provider;
import javax.jcr.Node;

import org.junit.Test;

/**
 * @version $Id$
 */

public class DefaultRenderingEngineTest {

    private static final String FREEMARKER_RENDERER_TYPE = "freemarker";

    @Test(expected = RenderException.class)
    public void testGetRendererForThrowsExceptionWhenNoneIsRegistered() throws RenderException {
        // GIVEN
        DefaultRenderingEngine renderingEngine = createDefaultRenderingEngine();
        TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        when(templateDefinition.getRenderType()).thenReturn(FREEMARKER_RENDERER_TYPE);

        // WHEN
        renderingEngine.getRendererFor(templateDefinition);
    }

    @Test
    public void testGetRenderingContextWhenNotYetSet() {
        // GIVEN
        final AggregationStateBasedRenderingContext renderingContext = mock(AggregationStateBasedRenderingContext.class);
        DefaultRenderingEngine renderingEngine = createDefaultRenderingEngine(renderingContext);

        // WHEN
        RenderingContext result = renderingEngine.getRenderingContext();

        // THEN
        assertEquals(renderingContext, result);
    }

    @Test
    public void testRenderFromNodeAndAppendable() throws Exception {
        // GIVEN
        final Node content = new MockNode("parent");

        RendererRegistry rendererRegistry = new RendererRegistry();
        TemplateDefinitionAssignment templateDefinitionAssignment = mock(TemplateDefinitionAssignment.class);
        RenderingContext renderingCtx = mock(RenderingContext.class);
        DefaultRenderingEngine renderingEngine = createDefaultRenderingEngine(rendererRegistry, templateDefinitionAssignment, renderingCtx);

        TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        when(templateDefinitionAssignment.getAssignedTemplateDefinition(content)).thenReturn(templateDefinition);

        Renderer freemarkerRenderer = mock(Renderer.class);
        RendererProvider freemarkerRendererProvider = mock(RendererProvider.class);
        when(freemarkerRendererProvider.getId()).thenReturn(FREEMARKER_RENDERER_TYPE);
        when(freemarkerRendererProvider.getDefinition()).thenReturn(freemarkerRenderer);
        rendererRegistry.register(freemarkerRendererProvider);

        final StringBuilder builder = new StringBuilder();
        OutputProvider builderWrapper = new AppendableOnlyOutputProvider(builder);
        final AppendableWriter writer = new AppendableWriter(builder);
        when(renderingCtx.getAppendable()).thenReturn(writer);
        when(templateDefinition.getRenderType()).thenReturn(FREEMARKER_RENDERER_TYPE);

        // WHEN
        renderingEngine.render(content, builderWrapper);

        // THEN
        verify(freemarkerRenderer).render(renderingCtx, DefaultRenderingEngine.EMPTY_CONTEXT);
    }

    @Test(expected = RenderException.class)
    public void testRenderThrowsRenderExceptionInCaseOfInternalIOException() throws Exception {
        // GIVEN
        final Node content = new MockNode("parent");

        RendererRegistry rendererRegistry = new RendererRegistry();
        TemplateDefinitionAssignment templateDefinitionAssignment = mock(TemplateDefinitionAssignment.class);
        RenderingContext renderingCtx = mock(RenderingContext.class);
        DefaultRenderingEngine renderingEngine = createDefaultRenderingEngine(rendererRegistry, templateDefinitionAssignment, renderingCtx);

        TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        when(templateDefinitionAssignment.getAssignedTemplateDefinition(content)).thenReturn(templateDefinition);

        Renderer freemarkerRenderer = mock(Renderer.class);
        RendererProvider freemarkerRendererProvider = mock(RendererProvider.class);
        when(freemarkerRendererProvider.getId()).thenReturn(FREEMARKER_RENDERER_TYPE);
        when(freemarkerRendererProvider.getDefinition()).thenReturn(freemarkerRenderer);
        rendererRegistry.register(freemarkerRendererProvider);

        final StringBuilder builder = new StringBuilder();
        OutputProvider builderWrapper = new AppendableOnlyOutputProvider(builder);
        final AppendableWriter writer = new AppendableWriter(builder);
        when(renderingCtx.getAppendable()).thenReturn(writer);
        when(templateDefinition.getRenderType()).thenReturn(FREEMARKER_RENDERER_TYPE);
        doThrow(new RenderException("")).when(freemarkerRenderer).render(renderingCtx, DefaultRenderingEngine.EMPTY_CONTEXT);

        // WHEN
        renderingEngine.render(content, builderWrapper);

        // THEN - no code here as we expect an Exception
    }

    @Test(expected = RenderException.class)
    public void testRenderThrowsRenderExceptionInCaseOfInternalRegistrationException() throws Exception {
        // GIVEN
        final Node content = new MockNode("parent");
        TemplateDefinitionAssignment templateDefinitionAssignment = mock(TemplateDefinitionAssignment.class);
        DefaultRenderingEngine renderingEngine = createDefaultRenderingEngine(templateDefinitionAssignment, mock(RenderingContext.class));

        doThrow(new RegistrationException("test")).when(templateDefinitionAssignment).getAssignedTemplateDefinition(content);

        // WHEN
        renderingEngine.render(content, null);

        // THEN - no code here as we expect an Exception
    }

    private DefaultRenderingEngine createDefaultRenderingEngine() {
        return createDefaultRenderingEngine(null);
    }

    private DefaultRenderingEngine createDefaultRenderingEngine(AggregationStateBasedRenderingContext renderingContext) {
        return createDefaultRenderingEngine(mock(TemplateDefinitionAssignment.class), renderingContext);
    }

    private DefaultRenderingEngine createDefaultRenderingEngine(TemplateDefinitionAssignment templateDefinitionAssignment, RenderingContext renderingContext) {
        return createDefaultRenderingEngine(new RendererRegistry(), templateDefinitionAssignment, renderingContext);
    }

    private DefaultRenderingEngine createDefaultRenderingEngine(RendererRegistry rendererRegistry, TemplateDefinitionAssignment templateDefinitionAssignment, final RenderingContext renderingContext) {
        Provider<RenderingContext> renderingContextProvider = null;
        if (renderingContext!= null) {
            renderingContextProvider = new Provider<RenderingContext>() {
                @Override
                public RenderingContext get() {
                    return renderingContext;
                }
            };
        }
        return new DefaultRenderingEngine(rendererRegistry, templateDefinitionAssignment, renderingContextProvider);
    }
}
