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

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class JspParagraphRendererTest extends TestCase {

    protected void setUp() throws Exception {
        MgnlContext.setInstance(null);
    }

    public void testCantRenderWithoutParagraphPathCorrectlySet() throws IOException {
        final Paragraph paragraph = new DummyParagraph("plop", null);
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        try {
            renderer.render(paragraph, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Unable to render paragraph plop: templatePath not set.", e.getMessage());
        }
    }

    public void testIncludesPathWhenProvided() throws IOException, ServletException {
        final Paragraph paragraph = new DummyParagraph("plop", "/foo/bar.jsp");
        final WebContext ctx = createStrictMock(WebContext.class);
        MgnlContext.setInstance(ctx);

        final StringWriter w = new StringWriter();
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        ctx.include("/foo/bar.jsp", w);
        replay(ctx);

        renderer.render(paragraph, w);

        //verify(ctx);
    }

    public void testShouldFailIfContextIsNotWebContext() throws IOException {
        final JspParagraphRenderer renderer = new JspParagraphRenderer();
        try {
            renderer.render(new DummyParagraph("plop", "/foo/bar.jsp"), new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("MgnlContext is not set for this thread", e.getMessage());
        }

        MgnlContext.setInstance(createStrictMock(Context.class));
        try {
            renderer.render(new DummyParagraph("plop", "/foo/bar.jsp"), new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("This paragraph renderer can only be used with a WebContext", e.getMessage());
        }
    }

    private static class DummyParagraph extends Paragraph {
        private final String name;
        private final String tplPath;

        public DummyParagraph(String name, String tplPath) {
            this.name = name;
            this.tplPath = tplPath;
        }

        public String getName() {
            return name;
        }

        public String getTemplatePath() {
            return tplPath;
        }
    }
}
