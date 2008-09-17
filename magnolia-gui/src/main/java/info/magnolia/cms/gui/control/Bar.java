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
package info.magnolia.cms.gui.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Bar extends ControlImpl {

    private List buttonsLeft = new ArrayList();

    private List buttonsRight = new ArrayList();

    private boolean small = true;

    public void setButtonsLeft(List buttons) {
        this.buttonsLeft = buttons;
    }

    public void setButtonsLeft(Button button) {
        this.getButtonsLeft().add(button);
    }

    public List getButtonsLeft() {
        return this.buttonsLeft;
    }

    public void setButtonsRight(List buttons) {
        this.buttonsRight = buttons;
    }

    public void setButtonsRight(Button button) {
        this.getButtonsRight().add(button);
    }

    public List getButtonsRight() {
        return this.buttonsRight;
    }

    public void setSmall(boolean b) {
        this.small = b;
    }

    public boolean getSmall() {
        return this.small;
    }

    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (StringUtils.isEmpty(this.getCssClass())) {
            if (this.getSmall()) {
                this.setCssClass(CSSCLASS_CONTROLBARSMALL);
            }
            else {
                this.setCssClass(CSSCLASS_CONTROLBAR);
            }
        }
        html.append("<table"); //$NON-NLS-1$
        html.append(this.getHtmlEvents());
        html.append(this.getHtmlCssClass());
        if (this.getId() != null) {
            html.append(" id=\"" + this.getId() + "\" cellspacing=\"0\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        html.append(">"); //$NON-NLS-1$
        html.append("<tr>"); //$NON-NLS-1$

        // left
        List btnLeft = this.getButtonsLeft();
        if (!btnLeft.isEmpty()) {
            html.append("<td class=\"mgnlBtnsLeft\">"); //$NON-NLS-1$
            Iterator itLeft = btnLeft.iterator();
            while (itLeft.hasNext()) {
                Button b = (Button) itLeft.next();
                if (this.getSmall()) {
                    b.setSmall(true);
                }
                b.setCssStyles("background", "transparent"); //$NON-NLS-1$ //$NON-NLS-2$
                b.setSaveInfo(false);
                html.append(b.getHtml());
            }
            html.append("</td>"); //$NON-NLS-1$
        }

        // title in the middle
        final String label = getLabel();
        if (StringUtils.isNotEmpty(label)) {
            html.append("<td class=\"smothBarLabelContainer\">");
            html.append("<table class=\"smothBarLabel\"><tr><td class=\"smothBarLabel\">");
            html.append(label);
            html.append("</td></tr></table>");
            html.append("</td>");
        }

        // right
        List btnRight = this.getButtonsRight();
        if (!btnRight.isEmpty()) {
            html.append("<td class=\"mgnlBtnsRight\">"); //$NON-NLS-1$

            Iterator itRight = this.getButtonsRight().iterator();
            while (itRight.hasNext()) {
                Button b = (Button) itRight.next();
                if (this.getSmall()) {
                    b.setSmall(true);
                }
                b.setCssStyles("background", "transparent"); //$NON-NLS-1$ //$NON-NLS-2$
                b.setSaveInfo(false);
                html.append(b.getHtml());
            }
            html.append("</td>"); //$NON-NLS-1$
        }

        html.append("</tr>"); //$NON-NLS-1$
        html.append("</table>"); //$NON-NLS-1$
        return html.toString();
    }

}
