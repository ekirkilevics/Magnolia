/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.templating.paragraphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.ContentWrapper;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.templating.AbstractRenderer;
import info.magnolia.module.templating.ModelExecutionFilter;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.RenderableDefinition;
import info.magnolia.module.templating.RenderingModel;
import info.magnolia.module.templating.RenderingModelImpl;
import info.magnolia.module.templating.engine.DefaultRenderingEngine;
import info.magnolia.module.templating.engine.RenderingEngine;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestMagnoliaConfigurationProperties;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class JspParagraphRendererTest {

    private WebContext magnoliaCtx;
    private Content page;

    @Before
    public void setUp() throws Exception {

        MgnlContext.setInstance(null);
        SystemProperty.setMagnoliaConfigurationProperties(new TestMagnoliaConfigurationProperties());

        magnoliaCtx = mock(WebContext.class);
        MgnlContext.setInstance(magnoliaCtx);
        ComponentsTestUtil.setImplementation(RenderingEngine.class, DefaultRenderingEngine.class);
        // the page node is exposed twice, once as "actpage", once as "content"
        page = mock(Content.class);
        when(page.getHandle()).thenReturn("/myPage");

        final AggregationState aggState = new AggregationState();
        aggState.setLocale(Locale.ENGLISH);
        when(magnoliaCtx.getLocale()).thenReturn(Locale.ENGLISH);
        final Node jcrPage = mock(Node.class);
        when(page.getJCRNode()).thenReturn(jcrPage);
        final Session session = mock(Session.class);
        when(jcrPage.getSession()).thenReturn(session);
        when(jcrPage.getPath()).thenReturn("/myPage");
        final Workspace workspace = mock(Workspace.class);
        when(session.getWorkspace()).thenReturn(workspace);
        when(workspace.getName()).thenReturn("test");

        aggState.setMainContent(page.getJCRNode());
        when(magnoliaCtx.getAggregationState()).thenReturn(aggState);
        final HierarchyManager hm = mock(HierarchyManager.class);
        when(magnoliaCtx.getHierarchyManager("test")).thenReturn(hm);
        when(hm.getWorkspace()).thenReturn(workspace);
        when(hm.getContent("/myPage")).thenReturn(page);
        when(workspace.getSession()).thenReturn(session);


        // shunt log4j
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.clear();
    }

    @Test
    public void testExposesNodesAsMaps() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ComponentsTestUtil.setImplementation(RenderingEngine.class, DefaultRenderingEngine.class);

        // we have 2 different nodes when rendering a paragraph, but getHandle() is only called on the page node, when using NodeMapWrapper
        final Content paragraph = mock(Content.class);

        final Map templateCtx = new HashMap();

        final JspParagraphRenderer renderer = new JspParagraphRenderer();

        // ugly hack to exexute renderer.setupContext()
        Method setupContextMethod = AbstractRenderer.class.getDeclaredMethod("setupContext", new Class[]{Map.class, Content.class, RenderableDefinition.class, RenderingModel.class, Object.class});
        setupContextMethod.setAccessible(true);
        setupContextMethod.invoke(renderer, new Object[]{templateCtx, paragraph, null, null, null});

        // other tests should verify the other objects !
        assertEquals("Unexpected amount of objects in context", 7, templateCtx.size());
        assertTrue(templateCtx.get("actpage") instanceof Map);
        assertEquals(page, unwrap((Content) templateCtx.get("actpage")));
        assertTrue(templateCtx.get("content") instanceof Map);
        assertEquals(paragraph, unwrap((Content) templateCtx.get("content")));
    }

    /*
    public void testIncludesPathWhenProvided() throws IOException, ServletException {
        final Paragraph paragraph = new Paragraph();
        paragraph.setName("plop");
        paragraph.setTemplatePath("/foo/bar.jsp");
        final WebContext ctx = createStrictMock(WebContext.class);
        MgnlContext.setInstance(ctx);

        final StringWriter w = new StringWriter();
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        MockAggregationState mas = new MockAggregationState();
        mas.setMainContent(new MockContent("bla"));
        expect(ctx.get("content")).andReturn(null);
        expect(ctx.get("result")).andReturn(null);
        expect(ctx.get("action")).andReturn(null);
        expect(ctx.get("paragraphDef")).andReturn(null);

        expect(ctx.put(eq("content"), isA(NodeMapWrapper.class))).andReturn(null);
        expect(ctx.getAggregationState()).andReturn(mas);
        ctx.setAttribute(eq("content"), isA(NodeMapWrapper.class), eq(1));
        ctx.setAttribute(eq("paragraphDef"), isA(Paragraph.class), eq(1));
        ctx.include("/foo/bar.jsp", w);
        expect(ctx.put("content", null)).andReturn(null);
        expect(ctx.put("paragraphDef", null)).andReturn(null);
        replay(ctx);

        renderer.render(null, paragraph, w);

        verify(ctx);
    }
    */

    @Test
    public void testCantRenderWithoutParagraphPathCorrectlySet() throws Exception {
        final Paragraph paragraph = new Paragraph();
        paragraph.setName("plop");
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        try {
            renderer.render(page, paragraph, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Unable to render info.magnolia.module.templating.Paragraph plop in page /pouet: templatePath not set.", e.getMessage());
        }
    }


    @Test
    public void testSkipRendering() throws Exception {
        final Paragraph par = new Paragraph();
        par.setName("plop");
        par.setTemplatePath("do_not_render_me.jsp");
        par.setModelClass(SkippableTestState.class);
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        final StringWriter out = new StringWriter();
        renderer.render(page, par, out);
        assertTrue(out.getBuffer().length() == 0);
    }


    @Test
    public void testShouldFailIfNoContextIsSet() throws Exception {
        MgnlContext.setInstance(null);
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        try {
            final Paragraph p = new Paragraph();
            p.setName("plop");
            p.setTemplatePath("/foo/bar.jsp");
            renderer.render(null, p, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("MgnlContext is not set for this thread", e.getMessage());
        }
    }

    @Test
    public void testShouldFailIfContextIsNotWebContext() throws Exception {
        Content content = mock(Content.class);
        when(content.getUUID()).thenReturn("content-uuid");

        Context context = mock(Context.class);
        when(context.getAttribute("info.magnolia.module.templating.RenderingModel")).thenReturn(null);
        when(context.getAttribute(ModelExecutionFilter.MODEL_ATTRIBUTE_PREFIX + "content-uuid")).thenReturn(null);

        MgnlContext.setInstance(context);
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        try {
            final Paragraph p = new Paragraph();
            p.setName("plop");
            p.setTemplatePath("/foo/bar.jsp");
            renderer.render(content, p, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("JspParagraphRenderer can only be used with a WebContext", e.getMessage());
        }
    }

    private Content unwrap(Content c) {
        if (c instanceof ContentWrapper) {
            return unwrap(((ContentWrapper) c).getWrappedContent());
        }
        return c;
    }

    public static final class SkippableTestState extends RenderingModelImpl<RenderableDefinition> {

        public SkippableTestState(Content content, RenderableDefinition definition, RenderingModel parent) {
            super(content, definition, parent);
        }
        @Override
        public String execute() {
            return RenderingModel.SKIP_RENDERING;
        }
    }
}
