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
package info.magnolia.freemarker;

import freemarker.cache.TemplateLoader;
import freemarker.ext.jsp.TaglibFactory;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.ServletContextHashModel;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.objectfactory.Components;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    /**
     * @deprecated since 5.0, use IoC !
     */
    public static FreemarkerHelper getInstance() {
        return Components.getSingleton(FreemarkerHelper.class);
    }

    private final Configuration cfg;

    // taglib support stuff
    private TaglibFactory taglibFactory;
    private ServletContextHashModel servletContextHashModel;

    /**
     * @deprecated since 5.0, use IoC, i.e use {@link #FreemarkerHelper(FreemarkerConfig)}
     */
    public FreemarkerHelper() {
        this(Components.getSingleton(FreemarkerConfig.class));
    }

    public FreemarkerHelper(final FreemarkerConfig freemarkerConfig) {
        // we subclass freemarker.Configuration to override some methods which must delegate to our observed FreemarkerConfig
        this.cfg = new Configuration() {
            @Override
            public Set getSharedVariableNames() {
                final Set names = super.getSharedVariableNames();
                names.addAll(freemarkerConfig.getSharedVariables().keySet());
                return names;
            }

            @Override
            public TemplateModel getSharedVariable(String name) {
                final TemplateModel value = super.getSharedVariable(name);
                if (value==null) {
                    return freemarkerConfig.getSharedVariables().get(name);
                }
                return value;
            }
        };
        cfg.setTemplateExceptionHandler(freemarkerConfig.getTemplateExceptionHandler());

        // ... and here we essentially do the same by instantiate delegator implementations of FreeMarker components, which delegate to our observed FreemarkerConfig
        // these setters do more than their equivalent getters, so we can't just override the getter instead.
        // ultimately, we could probably have our own clean subclass of freemarker.Configuration to hide all these details off FreemarkerHelper
        cfg.setTemplateLoader(new ConfigDelegatingTemplateLoader(freemarkerConfig));
        cfg.setObjectWrapper(new ConfigDelegatingObjectWrapper(freemarkerConfig));

        cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        cfg.setDefaultEncoding("UTF8");
        //cfg.setTemplateUpdateDelay(10);
    }

    /**
     * @deprecated not needed anymore since 4.3
     */
    public void resetObjectWrapper() {
        // getConfiguration().setObjectWrapper(newObjectWrapper());
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
            final Map<String, Object> data = (Map<String, Object>) root;
            addDefaultData(data, checkedLocale, i18nBasename);
        }
    }

    protected void addDefaultData(Map<String, Object> data, Locale locale, String i18nBasename) {
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

    protected void addTaglibSupportData(Map<String, Object> data, WebContext webCtx) {
        final ServletContext servletContext = webCtx.getServletContext();
        try {
            data.put(FreemarkerServlet.KEY_JSP_TAGLIBS, checkTaglibFactory(servletContext));
            data.put(FreemarkerServlet.KEY_APPLICATION_PRIVATE, checkServletContextModel(servletContext));
            data.put(FreemarkerServlet.KEY_REQUEST_PRIVATE, new HttpRequestHashModel(webCtx.getRequest(), webCtx.getResponse(), cfg.getObjectWrapper()));
        } catch (ServletException e) {
            // this should be an IllegalStateException (i.e there's no reason we should end up here) but this constructor isn't available in 1.4
            throw new RuntimeException("Can't initialize taglib support for FreeMarker: ", e);
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
            // FreeMarker needs an instance of a GenericServlet, but it doesn't have to do anything other than provide references to the ServletContext
            final GenericServlet fs = new DoNothingServlet(servletContext);
            servletContextHashModel = new ServletContextHashModel(fs, cfg.getObjectWrapper());
        }
        return servletContextHashModel;
    }

    protected Configuration getConfiguration() {
        return cfg;
    }

    private class ConfigDelegatingTemplateLoader implements TemplateLoader {
        private final FreemarkerConfig freemarkerConfig;

        public ConfigDelegatingTemplateLoader(FreemarkerConfig freemarkerConfig) {
            this.freemarkerConfig = freemarkerConfig;
        }

        public Object findTemplateSource(String name) throws IOException {
            return freemarkerConfig.getTemplateLoader().findTemplateSource(name);
        }

        public long getLastModified(Object templateSource) {
            return freemarkerConfig.getTemplateLoader().getLastModified(templateSource);
        }

        public Reader getReader(Object templateSource, String encoding) throws IOException {
            return freemarkerConfig.getTemplateLoader().getReader(templateSource, encoding);
        }

        public void closeTemplateSource(Object templateSource) throws IOException {
            freemarkerConfig.getTemplateLoader().closeTemplateSource(templateSource);
        }
    }

    private class ConfigDelegatingObjectWrapper implements ObjectWrapper {
        private final FreemarkerConfig freemarkerConfig;

        public ConfigDelegatingObjectWrapper(FreemarkerConfig freemarkerConfig) {
            this.freemarkerConfig = freemarkerConfig;
        }

        public TemplateModel wrap(Object obj) throws TemplateModelException {
            return freemarkerConfig.getObjectWrapper().wrap(obj);
        }
    }
}
