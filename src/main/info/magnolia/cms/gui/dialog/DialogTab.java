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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.gui.control.Button;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogTab extends DialogSuper {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogTab.class);

    public void drawHtmlPreSubs(JspWriter out) throws IOException {
        String parentId = this.getParent().getId();
        String id = this.getId();
        // add tab button to tab set
        if (!this.getLabel().equals("")) {
            Button control = new Button();
            control.setLabel(this.getLabel());
            control.setOnclick("mgnlDialogShiftTab('" + parentId + "','" + id + "');");
            this.getParent().addOption(control);
        }
        // add tab to js object
        out.println("<script type=\"text/javascript\">");
        out.println("mgnlControlSets['"
            + parentId
            + "'].items[mgnlControlSets['"
            + parentId
            + "'].items.length]='"
            + id
            + "';");
        out.println("</script>");
        // tab page
        out.println("<div id=\"" + id + "_div\" class=\"" + CSSCLASS_TAB + "\">");
        out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
        out.println("<tr><td class=\"" + CSSCLASS_TAB + "\">");
        out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
        out.println("<tr>");
        out.println("<td width=\"1%\"><img src=\""
            + this.getRequest().getContextPath()
            + "/admindocroot/0.gif\" height=\"1\" width=\"200\"></td>");
        out.println("<td width=\"100%\"><img src=\""
            + this.getRequest().getContextPath()
            + "/admindocroot/0.gif\" height=\"1\" width=\"200\"></td>");
        out.println("</tr>");

    }

    public void drawHtmlPostSubs(JspWriter out) throws IOException {
        out.println("</td></tr></table>");
        out.println("</table></div>");
    }
}
