/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.security;

import info.magnolia.cms.filters.MgnlFilterChain;
import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.UserContext;
import info.magnolia.logging.AuditLoggingUtil;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Sameer Charles
 * @author Fabrizio Giustina $Id$
 */
public class LogoutFilter extends OncePerRequestAbstractMgnlFilter {
    private static final Logger log = LoggerFactory.getLogger(LogoutFilter.class);

    public static final String PARAMETER_LOGOUT = "mgnlLogout";

    private ServletContext servletContext;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.servletContext = filterConfig.getServletContext();
    }

    /**
     * Check if a request parameter PARAMETER_LOGOUT is set. If so logout user,
     * unset the context and restart the filter chain.
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (null != request.getParameter(PARAMETER_LOGOUT)) {
            Context ctx = MgnlContext.getInstance();
            if (ctx instanceof UserContext) {
                ((UserContext) ctx).logout();
                AuditLoggingUtil.log((UserContext)ctx);
            }
            //MgnlContext.initAsAnonymousContext(request, response, servletContext);

            if (chain instanceof MgnlFilterChain) {
                ((MgnlFilterChain) chain).reset();
            }
        }

        chain.doFilter(request, response);
    }


}
