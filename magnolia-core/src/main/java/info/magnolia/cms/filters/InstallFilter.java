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

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContextImpl;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ui.ModuleManagerUI;
import info.magnolia.module.ui.ModuleManagerWebUI;
import org.apache.commons.lang.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * Filter responsible for executing the update/install mechanism.
 *
 * @author philipp
 * @version $Id$
 */
public class InstallFilter extends AbstractMgnlFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InstallFilter.class);

    private final ModuleManager moduleManager;
    private final FilterManager filterManager;
    private ServletContext servletContext;

    public InstallFilter(ModuleManager moduleManager, FilterManager filterManager) {
        this.moduleManager = moduleManager;
        this.filterManager = filterManager;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        servletContext = filterConfig.getServletContext();
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // this isn't the cleanest thing, but we're basically tricking FreemarkerHelper into using a Context, while avoiding using WebContextImpl and its depedencies on the repository
        final Context originalContext = MgnlContext.hasInstance() ? MgnlContext.getInstance() : null;
        final InstallWebContext ctx = new InstallWebContext();
        ctx.init(request, response, servletContext);
        MgnlContext.setInstance(ctx);

        try {
            final String contextPath = request.getContextPath();
            // TODO : this will be invalid the day we allow other resources (css, images) to be served through the installer
            response.setContentType("text/html");
            final Writer out = response.getWriter();
            final String uri = request.getRequestURI();
            final ModuleManagerUI ui = moduleManager.getUI();

            final String prefix = contextPath + ModuleManagerWebUI.INSTALLER_PATH;
            if (uri.startsWith(prefix)) {
                final String command = StringUtils.defaultIfEmpty(StringUtils.substringAfter(uri, prefix + "/"), null);
                final boolean installDone = ui.execute(out, command);
                if (installDone) {
                    filterManager.resetRootFilter();
                    // invalidate session: MAGNOLIA-2611
                    request.getSession().invalidate();
                    // redirect to root
                    response.sendRedirect(contextPath + "/");
                }
            } else {
                // redirect to /.magnolia/installer - even if it has no concrete effect, the change in the browser's url bar makes this more explicit
                response.sendRedirect(prefix);
            }
        } catch (ModuleManagementException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e); // TODO
        } finally {
            MgnlContext.release();
            MgnlContext.setInstance(originalContext);
        }
    }

    private final static class InstallWebContext extends WebContextImpl {
        public User getUser() {
            return null;
        }

        public Locale getLocale() {
            return MgnlContext.getSystemContext().getLocale();
        }
    }
}
