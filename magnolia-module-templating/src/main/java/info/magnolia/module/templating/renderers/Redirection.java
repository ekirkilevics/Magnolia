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

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.runtime.TemplateRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class Redirection implements TemplateRenderer {

    public void renderTemplate(Template template, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final Content page = Resource.getCurrentActivePage(request);
        final String location = page.getNodeData("location").getString();

        if (Server.isAdmin()) { //|| Resource.showPreview(request)) {
            // TODO : this doesnt print edit bars etc 
            request.getRequestDispatcher("/templates/templating/redirect-preview.jsp").include(request, response);
        } else {
            if (StringUtils.isEmpty(location)) {
                throw new IllegalStateException("Redirection page is missing the location property to determine where to redirect to");
            }

            // TODO : property to prefix location with context or not

            response.sendRedirect(location);
        }
    }
}
