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
import freemarker.template.TemplateExceptionHandler;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

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
        // TODO : configure this (maybe based on the dev-mode system property)
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    }

    /**
     * Renders the given template, using the given root object (can be a map, or any other type of object
     * handled by MagnoliaContentWrapper) to the given Writer.
     * If the root is a map, the following elements are added to it:
     * - contextPath, if we have an available WebContext
     */
    public void render(String templatePath, Object root, Writer out) throws TemplateException, IOException {
        final Locale locale = determineLocale();
        if (root instanceof Map) {
            final Map data = (Map) root;
            final Context mgnlCtx = MgnlContext.getInstance();
            if (mgnlCtx instanceof WebContext) {
                data.put("contextPath", ((WebContext) mgnlCtx).getContextPath());
            }
            // TODO : this is currently still in FreemarkerUtil. If we add it here,
            // the attribute "message" we put in the freemarker context should have a less generic name
            // (-> update all templates)
//            if (AlertUtil.isMessageSet(mgnlCtx)) {
//                data.put("message", AlertUtil.getMessage(mgnlCtx));
//            }
        }
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
