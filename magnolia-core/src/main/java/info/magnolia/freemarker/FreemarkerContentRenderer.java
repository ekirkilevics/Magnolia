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
package info.magnolia.freemarker;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * A generic helper to render Content instances with freemarker templates.
 * Is (potentially) used to render both paragraphs and templates.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerContentRenderer {
    private final Configuration cfg;

    public FreemarkerContentRenderer(Configuration cfg) {
        this.cfg = cfg;
    }

    public void render(String templatePath, Content content, Writer out) throws TemplateException, IOException {
        final Locale locale = MgnlContext.getLocale();
        cfg.getTemplate(templatePath, locale).process(content, out, new MagnoliaContentWrapper());
    }
}
