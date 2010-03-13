/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.inline.BarMain;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.RenderableDefinition;
import info.magnolia.module.templating.RenderException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PlainTextTemplateRenderer extends AbstractTemplateRenderer {

    public void renderTemplate(Content content, Template template, Writer out) throws IOException {
        final HttpServletResponse response = MgnlContext.getWebContext().getResponse();

        final boolean isAdmin = ServerConfiguration.getInstance().isAdmin();
        final boolean isPreview = MgnlContext.getAggregationState().isPreviewMode();

        final String text = content.getNodeData("text").getString();
        final String contentType = content.getNodeData("contentType").getString();

        if (!isAdmin || isPreview) {
            response.setContentType(contentType);
            out.write(text);
        } else {
            final String dialogName = template.getParameter("dialog");
            response.setContentType("text/html");
            // delegate to jsp ?
            out.write("<html>\n");
            out.write("<body>\n");

            if (content.isGranted(Permission.SET)) {
                BarMain bar = new BarMain();
                bar.setPath(content.getHandle());
                bar.setDialog(dialogName);
                bar.setAdminButtonVisible(true);
                bar.setDefaultButtons();
                bar.placeDefaultButtons();
                bar.drawHtml(out);
            }

            out.write("<h2 style=\"padding-top: 30px;\">");
            out.write(content.getHandle());
            out.write(" : ");
            out.write(contentType);
            out.write("</h2>");
            out.write("<pre>\n");
            out.write(text);
            out.write("</pre>\n");
            out.write("</body>\n");
            out.write("</html>\n");
        }
    }

    protected Map newContext() {
        throw new IllegalStateException();
    }

    protected void onRender(Content content, RenderableDefinition definition, Writer out, Map ctx, String templatePath) throws RenderException {
        throw new IllegalStateException();
    }
}
