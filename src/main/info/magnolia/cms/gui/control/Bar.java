/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
        String cssClass;
        if (this.getSmall()) {
            cssClass = CSSCLASS_CONTROLBARSMALL;
        }
        else {
            cssClass = CSSCLASS_CONTROLBAR;
        }
        html.append("<table");
        html.append(" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"");
        html.append(this.getHtmlEvents());
        html.append(" class=\"" + cssClass + "\"");
        if (this.getId() != null) {
            html.append(" id=\"" + this.getId() + "\"");
        }
        html.append(">");
        html.append("<tr><td class=\"" + cssClass + "\">");
        Iterator itLeft = this.getButtonsLeft().iterator();
        while (itLeft.hasNext()) {
            Button b = (Button) itLeft.next();
            if (this.getSmall()) {
                b.setSmall(true);
            }
            b.setCssStyles("background", "transparent");
            b.setSaveInfo(false);
            html.append(b.getHtml() + "\n");
        }
        html.append("</td><td class=\"" + cssClass + "\" align=\"right\">");
        Iterator itRight = this.getButtonsRight().iterator();
        while (itRight.hasNext()) {
            Button b = (Button) itRight.next();
            if (this.getSmall()) {
                b.setSmall(true);
            }
            b.setCssStyles("background", "transparent");
            b.setSaveInfo(false);
            html.append(b.getHtml() + "\n");
        }
        html.append("</td></tr></table>");
        return html.toString();
    }
}
