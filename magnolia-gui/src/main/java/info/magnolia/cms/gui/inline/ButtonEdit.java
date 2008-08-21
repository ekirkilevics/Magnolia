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
package info.magnolia.cms.gui.inline;

import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class ButtonEdit extends Button {

    String label = "buttons.edit"; //$NON-NLS-1$

    public ButtonEdit() {
    }

    public ButtonEdit(HttpServletRequest request) {
        this.setRequest(request);
    }

    public ButtonEdit(
        HttpServletRequest request,
        String path,
        String nodeCollectionName,
        String nodeName,
        String paragraph) {
        this.setRequest(request);
        this.setPath(path);
        this.setNodeCollectionName(nodeCollectionName);
        this.setNodeName(nodeName);
        this.setParagraph(paragraph);
    }

    public void setDefaultOnclick(HttpServletRequest request) {
        String nodeCollectionName = this.getNodeCollectionName();
        if (nodeCollectionName == null) {
            nodeCollectionName = StringUtils.EMPTY;
        }
        String nodeName = this.getNodeName();
        if (nodeName == null) {
            nodeName = StringUtils.EMPTY;
        }

        String repository = MgnlContext.getAggregationState().getRepository();
        this.setOnclick("mgnlOpenDialog('" //$NON-NLS-1$
            + this.getPath()
            + "','" //$NON-NLS-1$
            + nodeCollectionName
            + "','" //$NON-NLS-1$
            + nodeName
            + "','" //$NON-NLS-1$
            + this.getParagraph()
            + "','" //$NON-NLS-1$
            + repository
            + "');"); //$NON-NLS-1$
    }

    public String getLabel() {
        return MessagesManager.getWithDefault(label, label);
    }

    public void setLabel(String s) {
        this.label = s;
    }

    /**
     * @deprecated use drawHtml(Writer out) instead.
     */
    public void drawHtml(JspWriter out) throws IOException {
        drawHtml((Writer)out);
    }

    /**
     * Draws the edit button. The request has to be set!
     */
    public void drawHtml(Writer out) throws IOException {
        if (this.getRequest() != null) {
            boolean isGranted = Resource.getActivePage().isGranted(Permission.SET);
            if (!Resource.showPreview() && isGranted) {
                println(out, this.getHtml());
            }
        }
    }
}
