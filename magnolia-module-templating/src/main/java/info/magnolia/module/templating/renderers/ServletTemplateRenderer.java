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
import info.magnolia.cms.util.ClassUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Servlet template renderer, mapped to template type <code>servlet</code>. Servlet templates can have a
 * <code>path</code> parameter like the jsp ones (request is forwarded to the specified path), or a
 * <code>className</code> parameter.
 * </p>
 * <p>
 * If <code>className</code> is set the servlet class will be instantiated and service() will be called (note the
 * servlet will not properly be loaded by the container in this case, so don't rely on other methods except for
 * doGet()/doPost() being called).
 * </p>
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class ServletTemplateRenderer extends JspTemplateRenderer {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ServletTemplateRenderer.class);

    /**
     * @see JspTemplateRenderer#renderTemplate(Template, HttpServletRequest, HttpServletResponse)
     */
    public void renderTemplate(Template template, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        String className = template.getParameter("className");

        if (className == null) {
            // className not set, simply use path and forward this request
            super.renderTemplate(template, request, response);
            return;
        }

        // use className
        HttpServlet servlet;
        try {
            servlet = (HttpServlet) ClassUtil.newInstance(className);
        }
        catch (Exception e) {
            // simply retrow to the client for now...
            throw new NestableRuntimeException(e);
        }

        servlet.service(request, response);

    }

}
