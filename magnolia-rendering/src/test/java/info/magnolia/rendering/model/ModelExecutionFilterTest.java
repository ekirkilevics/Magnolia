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
package info.magnolia.rendering.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.filters.WebContainerResources;
import info.magnolia.cms.filters.WebContainerResourcesImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.registry.SessionProviderRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.renderer.RenderingModelBasedRenderer;
import info.magnolia.rendering.renderer.registry.RendererProvider;
import info.magnolia.rendering.renderer.registry.RendererRegistry;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.DefaultTemplateAvailability;
import info.magnolia.rendering.template.registry.TemplateAvailability;
import info.magnolia.rendering.template.registry.TemplateDefinitionProvider;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.test.ComponentProviderBasedMagnoliaTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;

import javax.jcr.Node;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;

/**
 * @version $Id$
 */
public class ModelExecutionFilterTest extends ComponentProviderBasedMagnoliaTestCase {

    @Override
    protected void customizeComponents(ComponentProviderConfiguration components) throws Exception {
        super.customizeComponents(components);
        components.registerImplementation(RendererRegistry.class);
        components.registerImplementation(TemplateDefinitionRegistry.class);
        components.registerImplementation(ModelExecutionFilter.class);
        components.registerImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);
        components.registerInstance(SessionProviderRegistry.class, new SessionProviderRegistry());
        components.registerImplementation(TemplateAvailability.class, DefaultTemplateAvailability.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockContext ctx = (MockContext) MgnlContext.getInstance();
        ctx.addHierarchyManager(ContentRepository.WEBSITE, MockUtil.createAndSetHierarchyManager(ContentRepository.WEBSITE,
                StringUtils.join(new String[] { "/foo.@uuid=12345",
                        "/foo/MetaData.mgnl\\:template=some-template" }, "\n")
        ));

        TemplateDefinitionRegistry templateDefinitionRegistry = Components.getComponent(TemplateDefinitionRegistry.class);
        templateDefinitionRegistry.register(new TemplateDefinitionProvider() {
            @Override
            public String getId() {
                return "some-template";
            }

            @Override
            public TemplateDefinition getDefinition() throws RegistrationException {
                ConfiguredTemplateDefinition definition = new ConfiguredTemplateDefinition();
                definition.setRenderType("test-renderer");
                return definition;
            }
        });
    }

    @Test
    public void testWithoutParameter() throws IOException, ServletException {

        // GIVEN
        FilterChain chain = mock(FilterChain.class);
        ModelExecutionFilter filter = Components.getComponent(ModelExecutionFilter.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        MgnlContext.push(request, response);

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        verify(chain).doFilter(request, response);
    }

    @Test
    public void testExecutesRenderingModel() throws Exception {

        // GIVEN
        FilterChain chain = mock(FilterChain.class);
        ModelExecutionFilter filter = Components.getComponent(ModelExecutionFilter.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        setupRequestAndAggregationState(request, response, "12345");

        RenderingModel renderingModel = mock(RenderingModel.class);
        setupRendererThatReturnsMockRenderingModel(renderingModel);

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        verify(renderingModel).execute();
        verify(chain).doFilter(request, response);
    }

    @Test
    public void testExecutesEarlyExecutionAwareRenderingModel() throws Exception {

        // GIVEN
        FilterChain chain = mock(FilterChain.class);
        ModelExecutionFilter filter = Components.getComponent(ModelExecutionFilter.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        setupRequestAndAggregationState(request, response, "12345");

        EarlyExecutionAware renderingModel = mock(EarlyExecutionAware.class);
        setupRendererThatReturnsMockRenderingModel(renderingModel);

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        verify(renderingModel).executeEarly();
        verify(chain).doFilter(request, response);
    }

    @Test
    public void testThrowsServletExceptionWhenParameterPointsToNonExistingContent() throws Exception {

        // GIVEN
        FilterChain chain = mock(FilterChain.class);
        ModelExecutionFilter filter = Components.getComponent(ModelExecutionFilter.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        setupRequestAndAggregationState(request, response, "10002");

        EarlyExecutionAware renderingModel = mock(EarlyExecutionAware.class);
        setupRendererThatReturnsMockRenderingModel(renderingModel);

        // THEN
        try {
            filter.doFilter(request, response, chain);
            fail();
        } catch (ServletException e) {
            assertEquals("Can't read content for early execution, node: 10002", e.getMessage());
        }
    }

    @Test
    public void testSkipsRenderingWhenRenderingModelWantsItTo() throws Exception {

        // GIVEN
        FilterChain chain = mock(FilterChain.class);
        ModelExecutionFilter filter = Components.getComponent(ModelExecutionFilter.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        setupRequestAndAggregationState(request, response, "12345");

        EarlyExecutionAware renderingModel = mock(EarlyExecutionAware.class);
        when(renderingModel.executeEarly()).thenReturn(RenderingModel.SKIP_RENDERING);
        setupRendererThatReturnsMockRenderingModel(renderingModel);

        // WHEN
        filter.doFilter(request, response, chain);

        // THEN
        verify(renderingModel).executeEarly();
        verify(chain, never()).doFilter(request, response);
    }

    private void setupRequestAndAggregationState(MockHttpServletRequest request, MockHttpServletResponse response, String nodeIdentifier) {
        MgnlContext.push(request, response);
        MgnlContext.setAttribute(ModelExecutionFilter.DEFAULT_MODEL_EXECUTION_ATTRIBUTE_NAME, nodeIdentifier);
        MgnlContext.getAggregationState().setRepository(ContentRepository.WEBSITE);
    }

    private void setupRendererThatReturnsMockRenderingModel(RenderingModel renderingModel) throws RenderException {
        final RenderingModelBasedRenderer renderer = mock(RenderingModelBasedRenderer.class);
        when(renderer.newModel(any(Node.class), any(RenderableDefinition.class), any(RenderingModel.class))).thenReturn(renderingModel);

        RendererRegistry rendererRegistry = Components.getComponent(RendererRegistry.class);
        rendererRegistry.register(new RendererProvider() {
            @Override
            public String getId() {
                return "test-renderer";
            }

            @Override
            public Renderer getDefinition() throws RegistrationException {
                return renderer;
            }
        });
    }
}
