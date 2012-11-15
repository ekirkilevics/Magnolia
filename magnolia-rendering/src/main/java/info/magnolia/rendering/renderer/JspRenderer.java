/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.rendering.renderer;

import info.magnolia.cms.util.ServletUtils;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.RenderableDefinition;

import java.util.Map;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Renders templates by dispatching to the servlet container.
 *
 * @version $Id$
 */
public class JspRenderer extends AbstractRenderer {

    @Override
    protected void onRender(Node content, RenderableDefinition definition, RenderingContext renderingCtx, Map<String, Object> ctx, String templateScript) throws RenderException {
        HttpServletRequest request = ((WebContext) MgnlContext.getInstance()).getRequest();
        HttpServletResponse response = ((WebContext) MgnlContext.getInstance()).getResponse();
        try {
            if (response.isCommitted() || (ServletUtils.isForward(request) || ServletUtils.isInclude(request)) || !NodeUtil.isSame(content, renderingCtx.getMainContent()) ) {
                ((WebContext) ctx).include(templateScript, renderingCtx.getAppendable());
            } else {
                // we can't do an include() because the called template might want to set cookies or call response.sendRedirect()
                request.getRequestDispatcher(templateScript).forward(request, response);
            }
        } catch (Exception e) {
            throw new RenderException("Can't render template " + templateScript, e);
        }
    }

    // TODO this is copied from the old rendering, need to go or be adapted
    /**
     * We expose nodes as Map instances in JSPs.
     *
    @Override
    protected Node wrapNodeForTemplate(Node currentContent, Node mainContent) {
        final Node wrapped = super.wrapNodeForTemplate(currentContent, mainContent);
        return new NodeMapWrapper(wrapped, mainContent.getHandle());
    }
     */
    @Override
    protected Map newContext() {
        return MgnlContext.getWebContext("JspRenderer can only be used with a WebContext");
    }

}
