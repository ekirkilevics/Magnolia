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

import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.RenderableDefinition;
import info.magnolia.module.templating.RenderException;
import info.magnolia.voting.voters.DontDispatchOnForwardAttributeVoter;
import org.apache.commons.lang.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;


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
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class ServletTemplateRenderer extends AbstractTemplateRenderer {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServletTemplateRenderer.class);

    public void renderTemplate(Content content, Template template, Writer out) throws IOException, RenderException {
        final HttpServletRequest request = MgnlContext.getWebContext("ServletTemplateRenderer can only be used with a WebContext").getRequest();
        final HttpServletResponse response = MgnlContext.getWebContext().getResponse();
        final String className = template.getParameter("className");

        if (StringUtils.isEmpty(className)) {
            // className not set, simply use path and forward this request
            final String path = template.getTemplatePath();
            if (StringUtils.isEmpty(path)) {
                throw new IllegalStateException("path or className is missing for servlet template " + template.getName() + ", returning a 404 error");
            }

            if (response.isCommitted()) {
                log.warn("Forwarding to {} for request {}, but response is already committed.", path, request.getRequestURL());
            }

            RequestDispatcher rd = request.getRequestDispatcher(path);
            // set this attribute to avoid a second dispatching of the filters
            request.setAttribute(DontDispatchOnForwardAttributeVoter.DONT_DISPATCH_ON_FORWARD_ATTRIBUTE, Boolean.TRUE);
            // we can't do an include() because the called template might want to set cookies or call response.sendRedirect()
            try {
                rd.forward(request, response);
            } catch (ServletException e) {
                throw new RenderException(e);
            }
        } else {
            // use className
            try {
                final HttpServlet servlet = (HttpServlet) ClassUtil.newInstance(className);
                servlet.service(request, response);
            } catch (ServletException e) {
                throw new RenderException(e);
            } catch (IllegalAccessException e) {
                throw new RenderException(e);
            } catch (ClassNotFoundException e) {
                throw new RenderException(e);
            } catch (InstantiationException e) {
                throw new RenderException(e);
            }
        }
    }

    protected Map newContext() {
        throw new IllegalStateException();
    }

    protected void onRender(Content content, RenderableDefinition definition, Writer out, Map ctx, String templatePath) throws RenderException {
        throw new IllegalStateException();
    }

}
