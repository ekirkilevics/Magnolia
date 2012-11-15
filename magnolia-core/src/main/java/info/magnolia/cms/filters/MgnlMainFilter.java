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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;


/**
 * Entry point for Magnolia filter dispatching. Intercepts all requests and passes them on to the Magnolia filter chain
 * that will either process the request or pass it on to the next filter configured in web.xml.
 *
 * @version $Id$
 * @see FilterManager
 * @see MgnlFilterDispatcher
 */
public class MgnlMainFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(MgnlMainFilter.class);

    /**
     * @deprecated since 4.5, use {@link FilterManager#SERVER_FILTERS}.
     */
    public static final String SERVER_FILTERS = FilterManager.SERVER_FILTERS;

    /**
     * @deprecated since 4.5, use IoC to access FilterManager.
     */
    private static MgnlMainFilter instance;

    private FilterManager filterManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        instance = this;
        filterManager = getFilterManager(filterConfig.getServletContext());
        filterManager.init(filterConfig);
    }

    @Override
    public void destroy() {
        if (filterManager != null) {
            filterManager.destroy();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("Handling URI: {} - Path info: {}", request.getRequestURI(), request.getPathInfo());

        // global fix for MAGNOLIA-3338 to make this independent of other dispatching rules
        boolean contextUpdated = false;
        if (MgnlContext.hasInstance()) {
            MgnlContext.push(request, response);
            contextUpdated = true;
        }

        try {
            filterManager.getFilterDispatcher().doDispatch(request, response, chain);
        } finally {
            if (contextUpdated && MgnlContext.hasInstance()) {
                MgnlContext.pop();
            }
        }
    }

    /**
     * Returns the root filter, note that the filter is destroyed if the filter chain is reloaded.
     */
    public MgnlFilter getRootFilter() {
        return filterManager.getFilterDispatcher().getTargetFilter();
    }

    protected FilterManager getFilterManager(ServletContext servletContext) {
        return Components.getComponent(FilterManager.class);
    }

    /**
     * @deprecated since 4.5, use IoC to access FilterManager.
     */
    public static MgnlMainFilter getInstance() {
        return instance;
    }

}
