/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.templating.renderers;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Access;
import info.magnolia.cms.gui.inline.BarMain;
import info.magnolia.context.MgnlContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.renderer.AbstractRenderer;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.util.AppendableWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * Template renderer for plain text.
 * @version $Revision: $ ($Author: $)
 */
public class PlainTextTemplateRenderer extends AbstractRenderer {

    @Override
    protected String determineTemplatePath(Node content, RenderableDefinition definition, RenderingModel<?> model, String actionResult) {
        return StringUtils.EMPTY;
    }

    @Override
    protected void onRender(Node content, info.magnolia.rendering.template.RenderableDefinition definition, RenderingContext renderingCtx, Map<String, Object> ctx, String templateScript) throws info.magnolia.rendering.engine.RenderException {
        final HttpServletResponse response = MgnlContext.getWebContext().getResponse();

        final boolean isAdmin = ServerConfiguration.getInstance().isAdmin();
        final boolean isPreview = MgnlContext.getAggregationState().isPreviewMode();
        try {
            // TODO: use components instead of rendering bars yourself
            final String text = content.getProperty("text").getString();
            final String contentType = content.getProperty("contentType").getString();

            AppendableWriter out = renderingCtx.getAppendable();
            if (!isAdmin || isPreview) {
                response.setContentType(contentType);
                out.write(text);
            } else {
                final String dialogName = (String) definition.getParameters().get("dialog");
                response.setContentType("text/html");
                // delegate to jsp ?
                out.write("<html>\n");
                out.write("<body>\n");

                if (Access.isGranted(content.getSession(), content.getPath(), Session.ACTION_SET_PROPERTY)) {
                    BarMain bar = new BarMain();
                    bar.setPath(content.getPath());
                    bar.setDialog(dialogName);
                    bar.setAdminButtonVisible(true);
                    bar.setDefaultButtons();
                    bar.placeDefaultButtons();
                    bar.drawHtml(out);
                }

                out.write("<h2 style=\"padding-top: 30px;\">");
                out.write(content.getPath());
                out.write(" : ");
                out.write(contentType);
                out.write("</h2>");
                out.write("<pre>\n");
                out.write(text);
                out.write("</pre>\n");
                out.write("</body>\n");
                out.write("</html>\n");
            }
        } catch (RepositoryException e) {
            throw new info.magnolia.rendering.engine.RenderException(e);
        } catch (IOException e) {
            throw new info.magnolia.rendering.engine.RenderException(e);
        }
    }

    @Override
    protected Map newContext() {
        return new HashMap<String, Object>();
    }

}
