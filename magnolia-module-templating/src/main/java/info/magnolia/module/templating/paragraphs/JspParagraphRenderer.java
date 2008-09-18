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
package info.magnolia.module.templating.paragraphs;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.NodeMapWrapper;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;

/**
 * A simple paragraph renderer which delegates to a jsp.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class JspParagraphRenderer extends ActionBasedParagraphRenderer {

    /**
     * The given content node is ignored here (except for exception messages),
     * since it is supposed to be stored in the context by the include tag,
     * and that's also how the included jsp will render it.
     * (this is subject to change in the future)
     */
    protected void render(String template, Content content, Paragraph paragraph, ActionResult actionResult, Writer out) throws IOException{

        try {
            final Context ctx = MgnlContext.getInstance();
            if (!(ctx instanceof WebContext)) {
                throw new IllegalStateException("This paragraph renderer can only be used with a WebContext");
            }
            Object attContent = ctx.getAttribute("content");
            ctx.setAttribute("content", new NodeMapWrapper(content, MgnlContext.getAggregationState().getMainContent().getHandle()), Context.LOCAL_SCOPE);
            ctx.setAttribute("paragraphConfig", paragraph, Context.LOCAL_SCOPE);
            if (actionResult != null) {
                ctx.setAttribute("result", actionResult.getResult(), Context.LOCAL_SCOPE);
                ctx.setAttribute("action", actionResult.getActionBean(), Context.LOCAL_SCOPE);
            }
            ((WebContext) ctx).include(template, out);
            // restore back original value of the parameter
            ctx.setAttribute("content", attContent, Context.LOCAL_SCOPE);
            ctx.setAttribute("paragraphConfig", null, Context.LOCAL_SCOPE);
        } catch (ServletException e) {
            throw new RuntimeException(e); // TODO
        }

    }

}
