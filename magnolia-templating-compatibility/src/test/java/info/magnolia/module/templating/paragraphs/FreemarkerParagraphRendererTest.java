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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.EmptyMessages;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.freemarker.FreemarkerConfig;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.RenderableDefinition;
import info.magnolia.module.templating.RenderingModel;
import info.magnolia.module.templating.RenderingModelImpl;
import info.magnolia.module.templating.engine.DefaultRenderingEngine;
import info.magnolia.module.templating.engine.RenderingEngine;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import junit.framework.TestCase;
import freemarker.cache.StringTemplateLoader;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerParagraphRendererTest extends TestCase {
    private StringTemplateLoader tplLoader;
    private FreemarkerParagraphRenderer renderer;
    private Content page;
    private WebContext magnoliaCtx;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        tplLoader = new StringTemplateLoader();
        final FreemarkerConfig freemarkerConfig = new FreemarkerConfig();
        freemarkerConfig.addTemplateLoader(tplLoader);
        final FreemarkerHelper freemarkerHelper = new FreemarkerHelper(freemarkerConfig);
        renderer = new FreemarkerParagraphRenderer(freemarkerHelper);

        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDefaultBaseUrl("http://myTests:1234/yay");
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);
        ComponentsTestUtil.setInstance(LinkTransformerManager.class, new LinkTransformerManager());
        ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);
        ComponentsTestUtil.setImplementation(RenderingEngine.class, DefaultRenderingEngine.class);

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
    }

    public void testWorksWithNonActionParagraphAndContentIsExposedToFreemarker() throws Exception {
        tplLoader.putTemplate("test_noclass.ftl", "This is a test template, rendering the content node under ${content.@handle} with UUID ${content.@uuid}.\n" +
                "The value of the foo property is ${content.foo}.");

        when(page.getUUID()).thenReturn("123");
        when(page.getNodeData("foo")).thenReturn(new MockNodeData("foo", "bar"));

        final StringWriter out = new StringWriter();
        final Paragraph p = new Paragraph();
        p.setName("test-para");
        p.setTemplatePath("test_noclass.ftl");
        renderer.render(page, p, out);

        assertEquals("This is a test template, rendering the content node under /plop with UUID 123.\n" +
                "The value of the foo property is bar.", out.toString());
    }

    public void testActionClassGetsExecutedAndIsPutOnContextAlongWithResultAndContent() throws Exception {
        tplLoader.putTemplate("test_action.ftl", "${content.boo} : ${model.pouet} : ${actionResult}");
        final Paragraph par = new Paragraph();
        par.setName("test-with-action");
        par.setTemplatePath("test_action.ftl");
        par.setModelClass(SimpleTestState.class);
        final MockContent c = new MockContent("plop");
        c.addNodeData(new MockNodeData("boo", "yay"));
        final StringWriter out = new StringWriter();
        renderer.render(c, par, out);
        assertEquals("yay : it works : success", out.toString());
    }

    public void testActionGetsPopulated() throws Exception {
        Map<String,String> params=new HashMap<String,String>();
        params.put("blah", "tralala");
        params.put("foo", "bar");
        when(magnoliaCtx.getParameters()).thenReturn(params);
        when(magnoliaCtx.getMessages("testmessages")).thenReturn(new EmptyMessages());
        when(magnoliaCtx.getMessages()).thenReturn(new EmptyMessages());
        when(magnoliaCtx.getLocale()).thenReturn(Locale.ENGLISH);
        when(magnoliaCtx.getContextPath()).thenReturn("/pouet");
        when(magnoliaCtx.getServletContext()).thenReturn(null);

        tplLoader.putTemplate("test_action.ftl", "${content.boo} : ${model.pouet} : ${model.blah} : ${actionResult}");
        final Paragraph par = new Paragraph();
        par.setName("test-with-action");
        par.setI18nBasename("testmessages");
        par.setTemplatePath("test_action.ftl");
        par.setModelClass(SimpleTestState.class);

        when(page.getNodeData("boo")).thenReturn(new MockNodeData("boo", "yay"));

        final StringWriter out = new StringWriter();
        renderer.render(page, par, out);
        assertEquals("yay : it works : tralala : success", out.toString());
    }

    public void testCantRenderWithoutParagraphPathCorrectlySet() throws Exception {
        tplLoader.putTemplate("foo", "");
        final Content c = new MockContent("pouet");
        final Paragraph paragraph = new Paragraph();
        paragraph.setName("plop");
        try {
            renderer.render(c, paragraph, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Unable to render info.magnolia.module.templating.Paragraph plop in page /pouet: templatePath not set.", e.getMessage());
        }
    }

    public void testSkipRendering() throws Exception {
        final Paragraph par = new Paragraph();
        par.setName("plop");
        par.setTemplatePath("do_not_render_me.ftl");
        par.setModelClass(SkippableTestState.class);
        final FreemarkerParagraphRenderer renderer = new FreemarkerParagraphRenderer(new FreemarkerHelper(new FreemarkerConfig()));
        final StringWriter out = new StringWriter();
        renderer.render(page, par, out);
        assertTrue(out.getBuffer().length() == 0);
    }

    public static final class SimpleTestState extends RenderingModelImpl<RenderableDefinition>{
        public SimpleTestState(Content content, RenderableDefinition definition, RenderingModel parent) {
            super(content, definition, parent);
        }

        private String pouet = "it works";
        private String blah;

        @Override
        public String execute() {
            return "success";
        }

        public String getPouet() {
            return pouet;
        }

        public void setPouet(String pouet) {
            this.pouet = pouet;
        }

        public String getBlah() {
            return blah;
        }

        public void setBlah(String blah) {
            this.blah = blah;
        }
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
