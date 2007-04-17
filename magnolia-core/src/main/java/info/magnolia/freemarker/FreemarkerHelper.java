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
import info.magnolia.cms.util.FactoryUtil;
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
public class FreemarkerHelper {
    public static FreemarkerHelper getInstance() {
        return (FreemarkerHelper) FactoryUtil.getSingleton(FreemarkerHelper.class);
    }

    private final Configuration cfg;

    public FreemarkerHelper() {
        cfg = new Configuration();
        cfg.setObjectWrapper(new MagnoliaContentWrapper());
        cfg.setClassForTemplateLoading(FreemarkerUtil.class, "/");
        cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        cfg.setDefaultEncoding("UTF8");
    }

    /**
     * Renders the given template, using the given root object (can be a map, or any other type of object
     * handled by MagnoliaContentWrapper) to the given Writer.
     */
    public void render(String templatePath, Object root, Writer out) throws TemplateException, IOException {
        final Locale locale = determineLocale();
        cfg.getTemplate(templatePath, locale).process(root, out);
    }

    protected Locale determineLocale() {
        if (MgnlContext.hasInstance()) {
            return MgnlContext.getLocale();
        } else {
            return Locale.getDefault();
        }
    }

    protected Configuration getConfiguration() {
        return cfg;
    }
}
