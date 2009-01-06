/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
