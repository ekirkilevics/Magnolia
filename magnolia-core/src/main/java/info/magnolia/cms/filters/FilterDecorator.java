/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.filters;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Used to decorate a normal (not magnolia) filter.
 * @author philipp
 * @version $Id$
 */
public class FilterDecorator extends AbstractMagnoliaFilter {

    /**
     * Decorated Filter
     */
    private Filter decoratedFilter;

    /**
     * Parameters passed in the config
     */
    private Map config;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(FilterDecorator.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        this.decoratedFilter.init(new CustomFilterConfig(filterConfig, config));
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{
        this.decoratedFilter.doFilter(request, response, chain);
    }

    public void destroy() {
        this.decoratedFilter.destroy();
    }

    public Map getConfig() {
        return this.config;
    }

    public void setConfig(Map config) {
        this.config = config;
    }

    public Filter getDecoratedFilter() {
        return this.decoratedFilter;
    }

    public void setDecoratedFilter(Filter decoratedFilter) {
        this.decoratedFilter = decoratedFilter;
    }

    public static class CustomFilterConfig implements FilterConfig {

        private Map parameters;

        private FilterConfig parent;

        /**
         * @param parameters
         */
        public CustomFilterConfig(FilterConfig parent, Map parameters) {
            super();
            this.parent = parent;
            if (parameters != null) {
                this.parameters = parameters;
            }
            else {
                this.parameters = new HashMap();
            }
        }

        /**
         * @see javax.servlet.FilterConfig#getFilterName()
         */
        public String getFilterName() {
            return parent.getFilterName();
        }

        /**
         * @see javax.servlet.FilterConfig#getInitParameter(java.lang.String)
         */
        public String getInitParameter(String name) {
            return (String) parameters.get(name);
        }

        /**
         * @see javax.servlet.FilterConfig#getInitParameterNames()
         */
        public Enumeration getInitParameterNames() {
            return new Hashtable(parameters).keys();
        }

        /**
         * @see javax.servlet.FilterConfig#getServletContext()
         */
        public ServletContext getServletContext() {
            return parent.getServletContext();
        }

        /**
         * This is a custom method to get recursive configuration parameters like maps, list
         */
        public Object getInitParameterObject(String name) {
            return parameters.get(name);
        }
    }
}
