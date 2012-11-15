/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
public class FilterDecorator extends AbstractMgnlFilter {

    /**
     * Decorated Filter.
     */
    private Filter decoratedFilter;

    /**
     * Parameters passed in the config.
     */
    private Map config;

    private String filterName;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(FilterDecorator.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (this.decoratedFilter == null) {
            filterName = filterConfig.getFilterName();
            log.warn("{} is not correctly configured or can't be instantiated.", filterName);
            return;
        }
        this.decoratedFilter.init(new CustomFilterConfig(filterConfig, config));
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{
        if (this.decoratedFilter == null) {
            log.warn("{} is not correctly configured or can't be instantiated.", filterName);
            return;
        }
        this.decoratedFilter.doFilter(request, response, chain);
    }

    @Override
    public void destroy() {
        if (this.decoratedFilter == null) {
            log.warn("{} is not correctly configured or can't be instantiated.", filterName);
            return;
        }
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

    /**
     * Exposes the parameters configured using content2bean as {@link FilterConfig} to the servlet API.
     */
    public static class CustomFilterConfig implements FilterConfig {

        private Map parameters;

        private final FilterConfig parent;

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

        @Override
        public String getFilterName() {
            return parent.getFilterName();
        }

        @Override
        public String getInitParameter(String name) {
            return (String) parameters.get(name);
        }

        @Override
        public Enumeration getInitParameterNames() {
            return new Hashtable(parameters).keys();
        }

        @Override
        public ServletContext getServletContext() {
            return parent.getServletContext();
        }

        /**
         * This is a custom method to get recursive configuration parameters like maps or list.
         */
        public Object getInitParameterObject(String name) {
            return parameters.get(name);
        }
    }
}
