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
package info.magnolia.templating.elements;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.DefaultRenderingEngine;
import info.magnolia.rendering.engine.OutputProvider;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.renderer.registry.RendererRegistry;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.variation.NoopVariationResolver;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ParagraphMarker.
 *
 * @version $Id$
 */
public class ComponentElementTest {

    private StringWriter out;
    private ComponentElement marker;
    private ConfiguredTemplateDefinition templateDefinition;

    @Before
    public void setUp() throws Exception {
        final MockHierarchyManager session = MockUtil.createHierarchyManager(
            "/foo/bar/baz/paragraphs/01@type=mgnl:component\n" +
            "/foo/bar/baz/paragraphs/01.text=dummy" +
            "/foo/bar/baz/paragraphs/01/MetaData.mgnl\\:template=testParagraph0");

        final AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(session.getContent("/foo/bar/baz"));
        Content currentContent = session.getContent("/foo/bar/baz/paragraphs/01");
        aggregationState.setCurrentContent(currentContent);
        final WebContext ctx = mock(WebContext.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(ctx.getResponse()).thenReturn(response);
        MgnlContext.setInstance(ctx);
        when(ctx.getHierarchyManager("TestMockHierarchyManager")).thenReturn(session);
        when(ctx.getAggregationState()).thenReturn(new AggregationState());

        final ServerConfiguration serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);
        // register some default components used internally
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());
        RenderingEngine renderingEngine = mock(RenderingEngine.class);
        ComponentsTestUtil.setInstance(RenderingEngine.class, renderingEngine);

        final TemplateDefinitionAssignment templateDefinitionAssignment = mock(TemplateDefinitionAssignment.class);
        templateDefinition = new ConfiguredTemplateDefinition();
        templateDefinition.setRenderType("blah");
        when(templateDefinitionAssignment.getAssignedTemplateDefinition(currentContent.getJCRNode())).thenReturn(templateDefinition);

        RendererRegistry registry = mock(RendererRegistry.class);
        Renderer renderer = mock(Renderer.class);
        when(registry.getRenderer("blah")).thenReturn(renderer);
        final AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        out = new StringWriter();
        context.push(aggregationState.getCurrentContent().getJCRNode(), templateDefinition, new OutputProvider() {

            @Override
            public OutputStream getOutputStream() throws RenderException, IOException {
                return null;
            }

            @Override
            public Appendable getAppendable() throws RenderException, IOException {
                return out;
            }
        });


        DefaultRenderingEngine engine = new DefaultRenderingEngine(registry, templateDefinitionAssignment, new NoopVariationResolver(), new Provider<RenderingContext>() {
            @Override
            public RenderingContext get() {
                return context;
            }
        });

        marker = new ComponentElement(serverCfg, context, engine, templateDefinitionAssignment);
    }



    @Test
    public void testRenderBeginOnlyContent() throws Exception {
        // GIVEN

        // WHEN
        marker.begin(out);

        // THEN
        assertEquals("<!-- cms:component content=\"testSession:/foo/bar/baz/paragraphs/01\" -->\n",out.toString());
    }

    @Test
    public void testRenderBeginAll() throws Exception {
        // GIVEN
        templateDefinition.setDialog("dialog");

        // WHEN
        marker.begin(out);

        // THEN
        assertEquals("<!-- cms:component content=\"testSession:/foo/bar/baz/paragraphs/01\" dialog=\"dialog\" -->\n", out.toString());
    }

    @Test
    public void testPostRender() throws Exception {
        marker.end(out);
        assertEquals("<!-- /cms:component -->\n", out.toString());
    }



    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.clear();
    }
}
