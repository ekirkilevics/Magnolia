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
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.i18n.TemplateMessages;

import java.io.IOException;
import java.io.Writer;

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

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogTab() {
    }

    public void drawHtmlPreSubs(Writer out) throws IOException {
        String parentId = this.getParent().getId();
        String id = this.getId();
        // add tab button to tab set
        if (!this.getLabel().equals("")) {
            Button control = new Button();
            control.setLabel(TemplateMessages.get(this,this.getLabel()));
            control.setOnclick("mgnlDialogShiftTab('" + parentId + "','" + id + "');");
            this.getParent().addOption(control);
        }
        // add tab to js object
        out.write("<script type=\"text/javascript\">");
        out.write("mgnlControlSets['"
            + parentId
            + "'].items[mgnlControlSets['"
            + parentId
            + "'].items.length]='"
            + id
            + "';");
        out.write("</script>");
        // tab page
        out.write("<div id=\"" + id + "_div\" class=\"" + CssConstants.CSSCLASS_TAB + "\">");
        out.write("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
        out.write("<tr><td class=\"" + CssConstants.CSSCLASS_TAB + "\">");
        out
            .write("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout:fixed\">");
        out.write("<col width=\"200\" /><col />");
    }

    public void drawHtmlPostSubs(Writer out) throws IOException {
        out.write("</table>");
        out.write("</td></tr></table></div>");
    }
}
