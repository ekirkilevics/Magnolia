/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.templating.renderers;

import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.runtime.TemplateRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.NodeMapWrapper;
import info.magnolia.context.MgnlContext;
import info.magnolia.voting.voters.DontDispatchOnForwardAttributeVoter;

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
            log.error("JSP path is missing for {}, returning a 404 error", request.getRequestURL()); //$NON-NLS-1$
            response.sendError(404);
            return;
        }

        log.debug("Dispatching request for [{}] - forward to [{1}]", request.getRequestURL(), requestReceiver);

        if (response.isCommitted()) {
            log.warn("Including {} for request {}, but response is already committed.", requestReceiver, request.getRequestURL());
        }

        Content page = MgnlContext.getAggregationState().getMainContent();
        request.setAttribute("content", new NodeMapWrapper(page, page.getHandle()));
        request.setAttribute("actpage", new NodeMapWrapper(page, page.getHandle()));
        request.setAttribute("templateConfig", template);
        request.setAttribute("aggregationState", MgnlContext.getAggregationState());
        request.setAttribute("ctx", MgnlContext.getInstance());
        RequestDispatcher rd = request.getRequestDispatcher(requestReceiver);
        // set this attribute to avoid a second dispatching of the filters
        request.setAttribute(DontDispatchOnForwardAttributeVoter.DONT_DISPATCH_ON_FORWARD_ATTRIBUTE, Boolean.TRUE);
        // we can't do an include() because the called template might want to set cookies or call response.sendRedirect()
        rd.forward(request, response);
        // TODO: should we remove extra attributes again?
    }

}
