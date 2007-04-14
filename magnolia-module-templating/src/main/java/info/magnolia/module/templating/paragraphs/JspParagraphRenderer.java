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
import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;

/**
 * A simple paragraph renderer which delegates to a jsp.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class JspParagraphRenderer implements ParagraphRenderer {

    /**
     * The given content node is ignored here (except for exception messages),
     * since it is supposed to be stored in the context by the include tag,
     * and that's also how the included jsp will render it.
     * (this is subject to change in the future)
     */
    public void render(Content content, Paragraph paragraph, Writer out) throws IOException {
        final String jspPath = paragraph.getTemplatePath();

        if (jspPath == null) {
            // TODO:  in page {}
            throw new IllegalStateException("Unable to render paragraph " + paragraph.getName() + " in page " + content.getHandle() + ": templatePath not set.");
        }

        try {
            final Context ctx = MgnlContext.getInstance();
            if (!(ctx instanceof WebContext)) {
                throw new IllegalStateException("This paragraph renderer can only be used with a WebContext");
            }
            ((WebContext) ctx).include(jspPath, out);
        } catch (ServletException e) {
            throw new RuntimeException(e); // TODO
        }

    }
}
