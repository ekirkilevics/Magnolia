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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
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
import info.magnolia.test.mock.MockContent;
import junit.framework.TestCase;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class JspParagraphRendererTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();

        MgnlContext.setInstance(null);
        SystemProperty.setMagnoliaConfigurationProperties(new TestMagnoliaConfigurationProperties());

        // shunt log4j
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        super.tearDown();
    }

    public void testExposesNodesAsMaps() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final WebContext magnoliaCtx = createStrictMock(WebContext.class);
        MgnlContext.setInstance(magnoliaCtx);
        ComponentsTestUtil.setImplementation(RenderingEngine.class, DefaultRenderingEngine.class);

        final Content page = createStrictMock(Content.class);
        // we have 2 different nodes when rendering a paragraph, but getHandle() is only called on the page node, when using NodeMapWrapper
        expect(page.getHandle()).andReturn("/myPage").times(2);
        final Content paragraph = createStrictMock(Content.class);

        final AggregationState aggState = new AggregationState();
        aggState.setMainContent(page);
        expect(magnoliaCtx.getAggregationState()).andStubReturn(aggState);

        replay(magnoliaCtx, page, paragraph);
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

        verify(magnoliaCtx, page, paragraph);
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

    public void testCantRenderWithoutParagraphPathCorrectlySet() throws Exception {
        final WebContext webContext = createNiceMock(WebContext.class);
        MgnlContext.setInstance(webContext);
        final AggregationState aggState = new AggregationState();
        expect(webContext.getAggregationState()).andReturn(aggState);
        replay(webContext);
        final Content c = new MockContent("pouet");
        final Paragraph paragraph = new Paragraph();
        paragraph.setName("plop");
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        try {
            renderer.render(c, paragraph, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Unable to render info.magnolia.module.templating.Paragraph plop in page /pouet: templatePath not set.", e.getMessage());
        }
        verify(webContext);
    }


    public void testSkipRendering() throws Exception {
        final WebContext webContext = createNiceMock(WebContext.class);
        MgnlContext.setInstance(webContext);
        final AggregationState aggState = new AggregationState();
        expect(webContext.getAggregationState()).andReturn(aggState);
        replay(webContext);
        final Content c = new MockContent("pouet");
        final Paragraph par = new Paragraph();
        par.setName("plop");
        par.setTemplatePath("do_not_render_me.jsp");
        par.setModelClass(SkippableTestState.class);
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        final StringWriter out = new StringWriter();
        renderer.render(c, par, out);
        assertTrue(out.getBuffer().length() == 0);
        verify(webContext);
    }


    public void testShouldFailIfNoContextIsSet() throws Exception {
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

    public void testShouldFailIfContextIsNotWebContext() throws Exception {
        Content content = createStrictMock(Content.class);
        expect(content.getUUID()).andReturn("content-uuid");

        Context context = createStrictMock(Context.class);
        expect(context.getAttribute("info.magnolia.module.templating.RenderingModel")).andReturn(null);
        expect(context.getAttribute(ModelExecutionFilter.MODEL_ATTRIBUTE_PREFIX + "content-uuid")).andReturn(null);
        replay(content, context);

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

    public static final class SkippableTestState extends RenderingModelImpl {

        public SkippableTestState(Content content, RenderableDefinition definition, RenderingModel parent) {
            super(content, definition, parent);
        }
        @Override
        public String execute() {
            return RenderingModel.SKIP_RENDERING;
        }
    }
}
