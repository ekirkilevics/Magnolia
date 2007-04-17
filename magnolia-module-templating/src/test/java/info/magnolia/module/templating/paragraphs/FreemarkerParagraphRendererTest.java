/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.templating.paragraphs;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import info.magnolia.cms.beans.config.ActionBasedParagraph;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.core.Content;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerContentRenderer;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerParagraphRendererTest extends TestCase {
    private StringTemplateLoader tplLoader;
    private FreemarkerParagraphRenderer renderer;
    private Configuration cfg;

    protected void setUp() throws Exception {
        super.setUp();
        tplLoader = new StringTemplateLoader();
        cfg = new Configuration();
        cfg.setTemplateLoader(tplLoader);
        renderer = new FreemarkerParagraphRenderer(new FreemarkerContentRenderer(cfg));
        final Context context = createNiceMock(Context.class);
        expect(context.getLocale()).andReturn(Locale.US);
        MgnlContext.setInstance(context);
        replay(context);
    }

    public void testWorksWithNonActionParagraphAndContentIsExposedToFreemarker() throws TemplateException, IOException {
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

    public void testCanRenderWithActionParagraphIfActionClassNotSet() throws IOException {
        final ActionBasedParagraph par = new ActionBasedParagraph();
        par.setName("test-with-action");
        par.setTemplatePath("test_action.ftl");
        final MockContent c = new MockContent("plop");
        try {
            renderer.render(c, par, new StringWriter());
        } catch (IllegalStateException e) {
            assertEquals("Can't render paragraph test-with-action in page /plop: actionClass not set.", e.getMessage());
        }
    }

    public void testActionClassGetsExecutedAndIsPutOnContextAlongWithResultAndContent() throws IOException {
        tplLoader.putTemplate("test_action.ftl", "${content.boo} : ${action.pouet} : ${result}");
        final ActionBasedParagraph par = new ActionBasedParagraph();
        par.setName("test-with-action");
        par.setTemplatePath("test_action.ftl");
        par.setActionClass(SimpleTestAction.class);
        final MockContent c = new MockContent("plop");
        c.addNodeData(new MockNodeData("boo", "yay"));
        final StringWriter out = new StringWriter();
        renderer.render(c, par, out);
        assertEquals("yay : it works : success", out.toString());
    }

    public void testCantRenderWithoutParagraphPathCorrectlySet() throws IOException {
        tplLoader.putTemplate("foo", "");
        final Content c = new MockContent("pouet");
        final Paragraph paragraph = new Paragraph();
        paragraph.setName("plop");
        try {
            renderer.render(c, paragraph, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Unable to render paragraph plop in page /pouet: templatePath not set.", e.getMessage());
        }
    }

    public static final class SimpleTestAction {
        public String execute() {
            return "success";
        }

        public String getPouet() {
            return "it works";
        }
    }
}
