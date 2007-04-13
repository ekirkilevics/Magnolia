/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.templating.renderers;

import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.runtime.TemplateRenderer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Simple jsp template renderer, mapped to template type <code>jsp</code>. The only valid attribute jsp templates is
 * <code>path</code>, which specify the jsp/servlet path to forward to.
 * </p>
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class JspTemplateRenderer implements TemplateRenderer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JspTemplateRenderer.class);

    public void renderTemplate(Template template, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final String requestReceiver = template.getPath();

        if (requestReceiver == null) {
            log.error("requestReceiver is missing, returning a 404 error"); //$NON-NLS-1$
            response.sendError(404);
            return;
        }

        log.debug("Dispatching request for [{}] - forward to [{1}]", request.getRequestURL(), requestReceiver);

        if (response.isCommitted()) {
            log.error("Can''t forward to [{}] for request [{}]. Response is already committed.", requestReceiver, request.getRequestURL());
            return;
        }

        RequestDispatcher rd = request.getRequestDispatcher(requestReceiver);
        rd.forward(request, response);
    }

}
