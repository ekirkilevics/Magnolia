/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.misc.CssConstants;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Vinzenz Wyser
 * @version $Revision$ ($Author$)
 */
public class DialogEditWithButton extends DialogBox {

    private List buttons = new ArrayList();

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        buttons.add(new Button());
    }

    public Button getButton() {
        return this.getButton(0);
    }

    public Button getButton(int index) {
        return (Button) this.getButtons().get(index);
    }

    public void setButtons(List l) {
        this.buttons = l;
    }

    public List getButtons() {
        return this.buttons;
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        doBeforeDrawHtml();

        Edit control = new Edit(this.getName(), this.getValue());
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING)); //$NON-NLS-1$
        if (this.getConfigValue("saveInfo").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
            control.setSaveInfo(false);
        }
        control.setCssClass(CssConstants.CSSCLASS_EDIT);
        control.setRows(this.getConfigValue("rows", "1")); //$NON-NLS-1$ //$NON-NLS-2$
        control.setCssStyles("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
        if (this.getConfigValue("onchange", null) != null) { //$NON-NLS-1$
            control.setEvent("onchange", this.getConfigValue("onchange")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.drawHtmlPre(out);
        String width = this.getConfigValue("width", "95%"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"" + width + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write("<tr><td style=\"width:100%\"  class=\"" + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        out.write(control.getHtml());
        if (this.getConfigValue("buttonLabel", null) != null) { //$NON-NLS-1$
            String label = this.getConfigValue("buttonLabel"); //$NON-NLS-1$
            label = this.getMessage(label);
            this.getButton().setLabel(label);
        }
        for (int i = 0; i < this.getButtons().size(); i++) {
            out.write("</td><td></td><td class=\"" + CssConstants.CSSCLASS_EDITWITHBUTTON + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            out.write(this.getButton(i).getHtml());
        }
        out.write("</td></tr></table>"); //$NON-NLS-1$

        this.drawHtmlPost(out);
    }

    /**
     * A hook method for lazy configuration of the widget.
     */
    protected void doBeforeDrawHtml() {
    }
}
