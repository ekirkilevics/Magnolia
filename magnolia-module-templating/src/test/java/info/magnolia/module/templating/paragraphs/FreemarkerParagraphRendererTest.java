/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;
import freemarker.cache.StringTemplateLoader;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
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
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerParagraphRendererTest extends MgnlTestCase {
    private StringTemplateLoader tplLoader;
    private FreemarkerParagraphRenderer renderer;

    protected void setUp() throws Exception {
        super.setUp();

        tplLoader = new StringTemplateLoader();
        final FreemarkerConfig fmTemplateLoader = new FreemarkerConfig();
        fmTemplateLoader.addTemplateLoader(tplLoader);
        ComponentsTestUtil.setInstance(FreemarkerConfig.class, fmTemplateLoader);

        final FreemarkerHelper freemarkerHelper = new FreemarkerHelper();
        renderer = new FreemarkerParagraphRenderer(freemarkerHelper);

        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDefaultBaseUrl("http://myTests:1234/yay");
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);
        ComponentsTestUtil.setInstance(LinkTransformerManager.class, new LinkTransformerManager());
        ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);
        ComponentsTestUtil.setImplementation(RenderingEngine.class, DefaultRenderingEngine.class);

        final WebContext context = createNiceMock(WebContext.class);
        AggregationState state = new AggregationState();
        state.setLocale(Locale.ENGLISH);
        expect(context.getAggregationState()).andStubReturn(state);
        expect(context.getLocale()).andReturn(Locale.ENGLISH);

        MgnlContext.setInstance(context);
        replay(context);
    }

    public void testWorksWithNonActionParagraphAndContentIsExposedToFreemarker() throws Exception {
        tplLoader.putTemplate("test_noclass.ftl", "This is a test template, rendering the content node under ${content.@handle} with UUID ${content.@uuid}.\n" +
                "The value of the foo property is ${content.foo}.");

        final MockContent c = new MockContent("plop");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("foo", "bar"));

        final StringWriter out = new StringWriter();
        final Paragraph p = new Paragraph();
        p.setName("test-para");
        p.setTemplatePath("test_noclass.ftl");
        renderer.render(c, p, out);

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
        final WebContext context = createNiceMock(WebContext.class);
        Map<String,String> params=new HashMap<String,String>();
        params.put("blah", "tralala");
        params.put("foo", "bar");
        expect(context.getParameters()).andReturn(params);
        expect(context.getMessages("testmessages")).andReturn(new EmptyMessages());
        expect(context.getMessages()).andReturn(new EmptyMessages());
        expect(context.getLocale()).andStubReturn(Locale.ENGLISH);
        expect(context.getContextPath()).andReturn("/pouet");
        expect(context.getServletContext()).andReturn(null);
        expect(context.getAggregationState()).andStubReturn(new AggregationState());
        replay(context);
        MgnlContext.setInstance(context);

        tplLoader.putTemplate("test_action.ftl", "${content.boo} : ${model.pouet} : ${model.blah} : ${actionResult}");
        final Paragraph par = new Paragraph();
        par.setName("test-with-action");
        par.setI18nBasename("testmessages");
        par.setTemplatePath("test_action.ftl");
        par.setModelClass(SimpleTestState.class);
        final MockContent c = new MockContent("plop");
        c.addNodeData(new MockNodeData("boo", "yay"));
        final StringWriter out = new StringWriter();
        renderer.render(c, par, out);
        assertEquals("yay : it works : tralala : success", out.toString());
    }

    /*public void testCantRenderWithoutParagraphPathCorrectlySet() throws Exception {
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
    }*/

    public void testSkipRenderingIfParagraphPathIsNull() throws Exception {
        final WebContext webContext = createNiceMock(WebContext.class);
        MgnlContext.setInstance(webContext);
        final AggregationState aggState = new AggregationState();
        expect(webContext.getAggregationState()).andReturn(aggState);
        replay(webContext);
        final Content c = new MockContent("pouet");
        final Paragraph paragraph = new Paragraph();
        paragraph.setName("plop");
        final FreemarkerParagraphRenderer renderer = new FreemarkerParagraphRenderer();
        final StringWriter out = new StringWriter();
        renderer.render(c, paragraph, out);
        assertTrue(out.getBuffer().length() == 0);
        verify(webContext);
    }

    public static final class SimpleTestState extends RenderingModelImpl{
        public SimpleTestState(Content content, RenderableDefinition definition, RenderingModel parent) {
            super(content, definition, parent);
        }

        private String pouet = "it works";
        private String blah;

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
}
