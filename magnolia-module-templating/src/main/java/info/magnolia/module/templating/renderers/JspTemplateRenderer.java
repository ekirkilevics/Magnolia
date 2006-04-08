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

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class JspTemplateRenderer implements TemplateRenderer {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(JspTemplateRenderer.class);

    /**
     * @throws IOException
     * @throws ServletException
     * @see info.magnolia.cms.beans.runtime.TemplateRenderer#renderTemplate(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public void renderTemplate(Template template, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        String requestReceiver = template.getPath();

        if (requestReceiver == null) {
            log.error("requestReceiver is missing, returning a 404 error"); //$NON-NLS-1$
            response.sendError(404);
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug(MessageFormat.format("Dispatching request for [{0}] - forward to [{1}]", //$NON-NLS-1$
                new Object[]{request.getRequestURL(), requestReceiver}));
        }

        if (response.isCommitted()) {
            log.error(MessageFormat.format("Can''t forward to [{0}] for request [{1}]. Response is already committed.", //$NON-NLS-1$
                new Object[]{requestReceiver, request.getRequestURL()}));
            return;
        }

        RequestDispatcher rd = request.getRequestDispatcher(requestReceiver);
        rd.forward(request, response);
        rd = null;
    }

}
