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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.SessionTestUtil;
import info.magnolia.templating.renderer.Renderer;
import info.magnolia.templating.renderer.registry.RendererRegistry;
import info.magnolia.templating.rendering.AggregationStateBasedRenderingContext;
import info.magnolia.templating.rendering.DefaultRenderingEngine;
import info.magnolia.templating.rendering.RenderingContext;
import info.magnolia.templating.rendering.RenderingEngine;
import info.magnolia.templating.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.templating.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockSession;

import java.io.StringWriter;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Test;

/**
 * Tests for ParagraphMarker.
 *
 * @version $Id$
 */
public class RenderComponentTest {

    @Test
    public void testDoRender() throws Exception {
        final MockSession session = SessionTestUtil.createSession("/foo/bar/baz/paragraphs/01.text=dummy");

        final AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(session.getNode("/foo/bar/baz"));
        Node currentContent = session.getNode("/foo/bar/baz/paragraphs/01");
        aggregationState.setCurrentContent(currentContent);
        final WebContext ctx = mock(WebContext.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(ctx.getResponse()).thenReturn(response);
        MgnlContext.setInstance(ctx);

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
        final ConfiguredTemplateDefinition templateDefinition = new ConfiguredTemplateDefinition();
        templateDefinition.setRenderType("blah");
        when(templateDefinitionAssignment.getAssignedTemplateDefinition(currentContent)).thenReturn(templateDefinition);

        RendererRegistry registry = mock(RendererRegistry.class);
        Renderer renderer = mock(Renderer.class);
        when(registry.getRenderer("blah")).thenReturn(renderer);
        DefaultRenderingEngine engine = new DefaultRenderingEngine(registry, templateDefinitionAssignment);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        when(ctx.getAttribute(RenderingContext.class.getName(), Context.LOCAL_SCOPE)).thenReturn(context);
        when(ctx.getAttribute(RenderingContext.class.getName())).thenReturn(context);
        final RenderComponent marker = new RenderComponent(serverCfg, context, engine);
        final StringWriter out = new StringWriter();
        marker.doRender(out);

        String outString = out.toString();

        // TODO - fit in proper asserts as implementation advances...
        assertEquals("", outString);
    }

    @Test
    public void testPostRender() throws Exception {
        final MockSession session = SessionTestUtil.createSession(
                "/foo/bar/baz/paragraphs/01.text=dummy\n" +
                "/foo/bar/baz/paragraphs/01.@uuid=100");

        final AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(session.getNode("/foo/bar/baz"));
        aggregationState.setCurrentContent(session.getNode("/foo/bar/baz/paragraphs/01"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        req.setAttribute(Sources.REQUEST_LINKS_DRAWN, Boolean.TRUE);

        HttpServletResponse res = mock(HttpServletResponse.class);
        when(res.getWriter()).thenReturn(null);

        final WebContext ctx = mock(WebContext.class);
        //when(ctx.getHierarchyManager(session.getName())).thenReturn(session);
        MgnlContext.setInstance(ctx);
        when(ctx.getResponse()).thenReturn(res);
        when(ctx.getRequest()).thenReturn(req);


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
        DefaultRenderingEngine engine = new DefaultRenderingEngine(new RendererRegistry(), templateDefinitionAssignment);
        final RenderComponent marker = new RenderComponent(serverCfg, new AggregationStateBasedRenderingContext(aggregationState), engine);

        final StringWriter out = new StringWriter();
        marker.postRender(out);

        String outString = out.toString();

        // TODO - fit in proper asserts as implementation advances...
        assertEquals("", outString);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.clear();
    }
}
