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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.beans.config.Template;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A Template Rendered implementation is responsible for generating the actual response from request data and a
 * template. A typical jsp implementation will simply forward the request to the jsp through request dispatcher, but
 * anybody is free to bind a specific implementation to a template type.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public interface TemplateRenderer {

    /**
     * Generates the actual output using the selected template
     * @param template template to be rendered
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException exception occurred while writing to the output stream
     * @throws ServletException generic servlet exception
     */
    void renderTemplate(Template template, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException;
}
