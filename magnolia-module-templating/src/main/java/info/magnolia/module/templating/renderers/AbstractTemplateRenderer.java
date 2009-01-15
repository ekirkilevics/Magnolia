/**
 * This file Copyright (c) 2008-2009 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.templating.AbstractRenderer;
import info.magnolia.module.templating.RenderException;
import info.magnolia.module.templating.TemplateRenderer;
import info.magnolia.module.templating.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * @author pbracher
 * @version $Id$
 *
 */
public abstract class AbstractTemplateRenderer extends AbstractRenderer implements TemplateRenderer {
    private static final Logger log = LoggerFactory.getLogger(AbstractTemplateRenderer.class);

    /**
     * @deprecated since 4.0
     */
    public void renderTemplate(Template template, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        renderTemplate(template, response);
    }

    public void renderTemplate(Template template, HttpServletResponse response) throws IOException, ServletException {
        if (response.isCommitted()) {
            // TODO : this was using request.getRequestURL - the whole block should pbly move to RenderingFilter
            log.warn("Attempting to include template {}, but response is already committed.", template.getName());
        }

        final Content content = MgnlContext.getAggregationState().getMainContent();
        final PrintWriter out = response.getWriter();

        try {
            render(content, template, out);
        }
        catch (RenderException e) {
            throw new ServletException(e);
        }
        finally{
            out.flush();
        }
    }
}
