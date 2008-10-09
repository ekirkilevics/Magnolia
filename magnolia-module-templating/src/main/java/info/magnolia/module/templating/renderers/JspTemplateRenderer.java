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

import info.magnolia.cms.beans.config.Renderable;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.voting.voters.DontDispatchOnForwardAttributeVoter;
import info.magnolia.module.templating.RenderException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.Writer;
import java.util.Map;

/**
 * Simple jsp template renderer, mapped to template type <code>jsp</code>. The only valid attribute jsp templates is
 * <code>path</code>, which specify the jsp/servlet path to forward to.
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class JspTemplateRenderer extends AbstractTemplateRenderer {

    protected void callTemplate(String templatePath, Renderable renderable, Map ctx, Writer out) throws RenderException {

        // FIXME temp fix for MAGNOLIA-2387

        HttpServletRequest request = (HttpServletRequest) MgnlContext.getAttribute("request");
        HttpServletResponse response = (HttpServletResponse) MgnlContext.getAttribute("response");
        RequestDispatcher rd = request.getRequestDispatcher(templatePath);

        // set this attribute to avoid a second dispatching of the filters
        request.setAttribute(DontDispatchOnForwardAttributeVoter.DONT_DISPATCH_ON_FORWARD_ATTRIBUTE, Boolean.TRUE);
        // we can't do an include() because the called template might want to set cookies or call response.sendRedirect()
        try {
            rd.forward(request, response);
        }
        catch (Exception e) {
            throw new RenderException("Can't render template " + templatePath, e);
        }
    }

    protected Map newContext() {
        final Context ctx = MgnlContext.getInstance();
        if (!(ctx instanceof WebContext)) {
            throw new IllegalStateException("This template renderer can only be used with a WebContext");
        }
        return ctx;
    }

}
