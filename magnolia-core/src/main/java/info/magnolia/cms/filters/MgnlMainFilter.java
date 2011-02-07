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
package info.magnolia.cms.filters;

import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A single filter which in turn executes a chain of other filters not configured in web.xml. This filters delegates to
 * one single filter which is either the filter chain configured in the config repository or the primitive system UI when
 * a system/module installation or update is needed.
 *
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class MgnlMainFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(MgnlMainFilter.class);

    /**
     * @deprecated since 5.0, use IoC!
     */
    private static MgnlMainFilter instance;

    private FilterManager filterManager;

    /**
     * @deprecated since 5.0, use {@link FilterManager#SERVER_FILTERS}.
     */
    public static final String SERVER_FILTERS = "/server/filters";

    public void init(FilterConfig filterConfig) throws ServletException {
        instance = this;
        filterManager = getFilterManager(filterConfig.getServletContext());
        filterManager.init(filterConfig);
    }

    public void destroy() {
        if (filterManager != null) {
            filterManager.destroyRootFilter();
        }
    }

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

        final MgnlFilter rootFilter = getRootFilter();
        try {
            if (rootFilter.matches(request)) {
                rootFilter.doFilter(request, response, chain);
            } else {
                // pass request to next filter in web.xml
                chain.doFilter(request, response);
            }
        } finally {
            if (contextUpdated && MgnlContext.hasInstance()) {
                MgnlContext.pop();
            }
        }
    }

    protected MgnlFilter getRootFilter() {
        return filterManager.getRootFilter();
    }

    protected FilterManager getFilterManager(ServletContext servletContext) {
        return Components.getComponent(FilterManager.class);
    }

    /**
     * @deprecated since 5.0, use IoC.
     */
    public static MgnlMainFilter getInstance() {
        return instance;
    }

}
