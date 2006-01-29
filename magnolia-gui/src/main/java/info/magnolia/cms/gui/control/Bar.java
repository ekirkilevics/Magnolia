/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
public class Bar extends ControlSuper {

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
