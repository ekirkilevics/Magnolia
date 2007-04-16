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
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.core.Content;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.templating.freemarker.FreemarkerContentRenderer;
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

    protected void setUp() throws Exception {
        super.setUp();
        tplLoader = new StringTemplateLoader();
        final Configuration cfg = new Configuration();
        cfg.setTemplateLoader(tplLoader);
        renderer = new FreemarkerParagraphRenderer(new FreemarkerContentRenderer(cfg));
    }

    private void assertRendereredContent(String expectedOutput, MockContent c, String templateName) throws TemplateException, IOException {
        final Context context = createStrictMock(Context.class);
        expect(context.getLocale()).andReturn(Locale.US);
        replay(context);
        MgnlContext.setInstance(context);

        final StringWriter out = new StringWriter();
        final DummyParagraph p = new DummyParagraph("test-para", templateName, null);
        renderer.render(c, p, out);

        assertEquals(expectedOutput, out.toString());
        verify(context);
    }

    public void testActionClassIsNotMandatory() throws TemplateException, IOException {
        tplLoader.putTemplate("test_noclass.ftl", "This is a test template, rendering the content node under ${@handle} with UUID ${@uuid}.\n" +
                "The value of the foo property is ${foo}.");

        final MockContent c = new MockContent("plop");
        c.setUUID("123");
        c.addNodeData(new MockNodeData("foo", "bar"));
        assertRendereredContent("This is a test template, rendering the content node under /plop with UUID 123.\n" +
                "The value of the foo property is bar.", c, "test_noclass.ftl");
    }

    public void testCantRenderWithoutParagraphPathCorrectlySet() throws IOException {
        tplLoader.putTemplate("foo", "");
        final Content c = new MockContent("pouet");
        final Paragraph paragraph = new DummyParagraph("plop", null, null);
        try {
            renderer.render(c, paragraph, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Unable to render paragraph plop in page /pouet: templatePath not set.", e.getMessage());
        }
    }

    private static final class SimpleTestAction {

    }

    private static class DummyParagraph extends Paragraph {
        private final String name;
        private final String tplPath;
        private final String actionClass;

        public DummyParagraph(String name, String tplPath, String actionClass) {
            this.name = name;
            this.tplPath = tplPath;
            this.actionClass = actionClass;
        }

        public String getName() {
            return name;
        }

        public String getTemplatePath() {
            return tplPath;
        }

        public String getActionClass() {
            return actionClass;
        }
    }
}
