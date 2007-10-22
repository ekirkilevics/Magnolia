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

import freemarker.template.TemplateException;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleManagerUI;

import javax.jcr.RepositoryException;
import javax.servlet.FilterChain;
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

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        ModuleManager moduleManager = ModuleManager.Factory.getInstance();

        final String contextPath = request.getContextPath();
        final ModuleManagerUI ui = new ModuleManagerUI(contextPath);
        // TODO : this will be invalid the day we allow other resources (css, images) to be served through the installer
        response.setContentType("text/html");
        final Writer out = response.getWriter();
        final String uri = request.getRequestURI();
        try {
            if (uri.startsWith(contextPath + ModuleManagerUI.INSTALLER_PATH)) {
                final Map parameterMap = request.getParameterMap();
                final boolean shouldContinue = ui.execute(moduleManager, out, parameterMap);
                if (!shouldContinue) {
                    return;
                } else {
                    MgnlMainFilter.getInstance().reset();
                    // redirect to root
                    response.sendRedirect(contextPath);
                }
            } else {
                ui.renderTempPage(moduleManager, out);
                return;
            }
        } catch (TemplateException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e); // TODO
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e); // TODO
        }
    }
}
