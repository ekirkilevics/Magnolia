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

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import info.magnolia.context.WebContext;
import info.magnolia.context.WebContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * This class initializes the current context and configures MDC logging.
 *
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ContextFilter extends AbstractMgnlFilter {

    public static Logger log = LoggerFactory.getLogger(ContextFilter.class);

    private final WebContextFactory webContextFactory;
    private ServletContext servletContext;

    public ContextFilter(WebContextFactory webContextFactory) {
        this.webContextFactory = webContextFactory;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.servletContext = filterConfig.getServletContext();
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        // This filter can be invoked multiple times. The first time it's called it initializes the MgnlContext. On
        // subsequent invocations it only pushes the request and response objects on the stack in WebContext. The
        // multiple invocations result from either dispatching a forward, an include or an error request, and also
        // when the filter chain is reset (which happens when logging out).

        final Context originalContext = MgnlContext.hasInstance() ? MgnlContext.getInstance() : null;
        boolean initializedContext = false;
        if (!MgnlContext.hasInstance() || MgnlContext.isSystemInstance()) {
            final WebContext webContext = webContextFactory.createWebContext(request, response, servletContext);
            MgnlContext.setInstance(webContext);
            initializedContext = true;
            try {
                String uri = request.getRequestURI();
                if (uri != null) {
                    MDC.put("requesturi", uri);
                }

                String userName = MgnlContext.getUser().getName();
                if (userName != null) {
                    // FIXME: Performance: following line of code forces creation of "users", "userroles" and "usergroups" JCR workspace sessions on _every_ request no matter if the user is logged in or not!
                    MDC.put("userid", userName);
                }

                String referer = request.getHeader("Referer");
                if (referer != null) {
                    MDC.put("Referer", referer);
                }

                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null) {
                    MDC.put("User-Agent", userAgent);
                }

                String remoteHost = request.getRemoteHost();
                if (remoteHost != null) {
                    MDC.put("Remote-Host", remoteHost);
                }

                HttpSession session = request.getSession(false);
                if (session != null) {
                    MDC.put("SessionId", session.getId());
                }
            }
            catch (Throwable e) {
                // if for any reason the MDC couldn't be set, just ignore it.
                log.debug(e.getMessage(), e);
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            if (initializedContext) {
                MgnlContext.release();
                MgnlContext.setInstance(originalContext);

                // cleanup
                MDC.clear();
            }
        }
    }
}
