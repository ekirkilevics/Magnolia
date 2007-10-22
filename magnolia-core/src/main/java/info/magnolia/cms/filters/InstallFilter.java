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

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContextImpl;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleManagerUI;
import info.magnolia.module.ModuleManagerWebUI;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Filter responsible for executing the update/install mechanism.
 *
 * @author philipp
 * @version $Id$
 */
public class InstallFilter extends AbstractMgnlFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InstallFilter.class);
    private final ModuleManager moduleManager;
    private ServletContext servletContext;

    public InstallFilter(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        servletContext = filterConfig.getServletContext();
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // this isn't the cleanest thing, but we're basically tricking FreemarkerHelper into using a Context, while avoiding using WebContextImpl and its depedencies on the repository
        final InstallWebContext ctx = new InstallWebContext();
        ctx.init(request, response, servletContext);
        MgnlContext.setInstance(ctx);

        try {
            final String contextPath = request.getContextPath();
            // TODO : this will be invalid the day we allow other resources (css, images) to be served through the installer
            response.setContentType("text/html");
            final Writer out = response.getWriter();
            final String uri = request.getRequestURI();
            final Map parameterMap = request.getParameterMap();
            final ModuleManagerUI ui = moduleManager.getUI();

            if (uri.startsWith(contextPath + ModuleManagerWebUI.INSTALLER_PATH)) {
                final boolean shouldContinue = ui.execute(out, parameterMap);
                if (!shouldContinue) {
                    return;
                } else {
                    MgnlMainFilter.getInstance().reset();
                    // redirect to root
                    response.sendRedirect(contextPath);
                }
            } else {
                ui.renderTempPage(out);
                return;
            }
        } catch (ModuleManagementException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e); // TODO
        } finally {
            MgnlContext.setInstance(null);
        }
    }

    private final static class InstallWebContext extends WebContextImpl {
        public User getUser() {
            return null;
        }
    }
}
