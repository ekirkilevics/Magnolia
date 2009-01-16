/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.jsp.TaglibFactory;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.ServletContextHashModel;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.freemarker.models.MagnoliaObjectWrapper;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * A generic helper to render Content instances with Freemarker templates.
 * Is used to render both paragraphs and templates.
 *
 * TODO : expose Configuration#clearTemplateCache()
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerHelper {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FreemarkerHelper.class);

    public static FreemarkerHelper getInstance() {
        return (FreemarkerHelper) FactoryUtil.getSingleton(FreemarkerHelper.class);
    }

    private final Configuration cfg;

    // taglib support stuff
    private TaglibFactory taglibFactory;
    private ServletContextHashModel servletContextHashModel;

    public FreemarkerHelper() {
        cfg = new Configuration();
        resetObjectWrapper();

        // template loaders will be set later on - to make sure changes to the configuration are picked up immediately
        // default template loader until FreemarkerConfig is ready:
        cfg.setTemplateLoader(new ClassTemplateLoader(FreemarkerUtil.class, "/"));

        cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        cfg.setDefaultEncoding("UTF8");
        // TODO : configure this (maybe based on the dev-mode system property)
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        //cfg.setTemplateUpdateDelay(10);
    }

    public void resetObjectWrapper() {
        cfg.setObjectWrapper(newObjectWrapper());
    }

    protected ObjectWrapper newObjectWrapper() {
        return (ObjectWrapper) FactoryUtil.newInstance(MagnoliaObjectWrapper.class);
    }

    /**
     * @see #render(String, Locale, String, Object, java.io.Writer)
     */
    public void render(String templatePath, Object root, Writer out) throws TemplateException, IOException {
        render(templatePath, null, null, root, out);
    }

    /**
     * Renders the given template, using the given root object (can be a map, or any other type of object
     * handled by MagnoliaContentWrapper) to the given Writer.
     * If the root is an instance of a Map, the following elements are added to it:
     * - ctx, the current Context instance retrieved from MgnlContext
     * - contextPath, if we have an available WebContext (@deprecated)
     * - defaultBaseUrl, as per Server.getDefaultBaseUrl()
     *
     * @see ServerConfiguration#getDefaultBaseUrl()
     */
    public void render(String templatePath, Locale locale, String i18nBasename, Object root, Writer out) throws TemplateException, IOException {
        final Locale localeToUse = checkLocale(locale);
        prepareRendering(localeToUse, i18nBasename, root);

        final Template template = cfg.getTemplate(templatePath, localeToUse);
        template.process(root, out);
    }

    /**
     * Renders the template read by the given Reader instance. It should be noted that this method completely bypasses
     * Freemarker's caching mechanism. The template will be parsed everytime, which might have a performance impact.
     *
     * @see #render(Reader, Locale, String, Object, Writer)
     */
    public void render(Reader template, Object root, Writer out) throws TemplateException, IOException {
        render(template, null, null, root, out);
    }

    protected void render(Reader template, Locale locale, String i18nBasename, Object root, Writer out) throws TemplateException, IOException {
        final Locale localeToUse = checkLocale(locale);
        prepareRendering(localeToUse, i18nBasename, root);

        final Template t = new Template("inlinetemplate", template, cfg);
        t.setLocale(localeToUse);
        t.process(root, out);
    }

    /**
     * Returns the passed Locale if non-null, otherwise attempts to get the Locale from the current context.
     */
    protected Locale checkLocale(Locale locale) {
        if (locale != null) {
            return locale;
        } else if (MgnlContext.hasInstance()) {
            return MgnlContext.getLocale();
        } else {
            return Locale.getDefault();
        }
    }

    /**
     * Call checkLocale() before calling this method, to ensure it is not null.
     */
    protected void prepareRendering(Locale checkedLocale, String i18nBasename, Object root) {
        if (root instanceof Map) {
            final Map data = (Map) root;
            addDefaultData(data, checkedLocale, i18nBasename);
        }

        // TODO - we could have a LazyMultiTemplateLoader, much like our other Lazy*TemplateImplementations
        // set all currently known loaders
        final FreemarkerConfig loaderManager = FreemarkerConfig.getInstance();
        if (loaderManager != null) {
            final TemplateLoader tl = loaderManager.getMultiTemplateLoader();
            if (tl != cfg.getTemplateLoader()) {
                // update only if loader instance changed in between
                cfg.setTemplateLoader(tl);
            }
        } else {
            // TODO - this should not be necessary - see MAGNOLIA-2533
            log.debug("FreemarkerConfig is not ready yet.");
        }
    }

    protected void addDefaultData(Map data, Locale locale, String i18nBasename) {
        if (MgnlContext.hasInstance()) {
            data.put("ctx", MgnlContext.getInstance());
        }
        if (MgnlContext.isWebContext()) {
            final WebContext webCtx = MgnlContext.getWebContext();
            // @deprecated (-> update all templates) - TODO see MAGNOLIA-1789
            data.put("contextPath", webCtx.getContextPath());
            data.put("aggregationState", webCtx.getAggregationState());

            addTaglibSupportData(data, webCtx);
        }

        data.put("defaultBaseUrl", ServerConfiguration.getInstance().getDefaultBaseUrl());

        if (i18nBasename != null) {
            data.put("i18n", new MessagesWrapper(i18nBasename, locale));
        }

        // TODO : this is currently still in FreemarkerUtil. If we add it here,
        // the attribute "message" we put in the Freemarker context should have a less generic name
        // (-> update all templates)
//            if (AlertUtil.isMessageSet(mgnlCtx)) {
//                data.put("message", AlertUtil.getMessage(mgnlCtx));
//            }
    }

    protected void addTaglibSupportData(Map data, WebContext webCtx) {
        final ServletContext servletContext = webCtx.getServletContext();
        try {
            data.put(FreemarkerServlet.KEY_JSP_TAGLIBS, checkTaglibFactory(servletContext));
            data.put(FreemarkerServlet.KEY_APPLICATION_PRIVATE, checkServletContextModel(servletContext));
            data.put(FreemarkerServlet.KEY_REQUEST_PRIVATE, new HttpRequestHashModel(webCtx.getRequest(), cfg.getObjectWrapper()));
        } catch (ServletException e) {
            // this should be an IllegalStateException (i.e there's no reason we should end up here) but this constructor isn't available in 1.4
            throw new RuntimeException("Can't initalize taglib support for Freemarker: ", e);
        }
    }

    protected TaglibFactory checkTaglibFactory(ServletContext servletContext) {
        if (taglibFactory == null) {
            taglibFactory = new TaglibFactory(servletContext);
        }
        return taglibFactory;
    }

    protected ServletContextHashModel checkServletContextModel(ServletContext servletContext) throws ServletException {
        if (servletContextHashModel == null) {
            // Freemarker needs an instance of a GenericServlet, but it doesn't have to do anything other than provide references to the ServletContext
            final GenericServlet fs = new DoNothingServlet(servletContext);
            servletContextHashModel = new ServletContextHashModel(fs, cfg.getObjectWrapper());
        }
        return servletContextHashModel;
    }

    protected Configuration getConfiguration() {
        return cfg;
    }
}
