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

import info.magnolia.cms.gui.control.Edit;

import java.io.IOException;

import javax.jcr.PropertyType;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogEdit extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogEdit.class);

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(JspWriter)
     */
    public void drawHtml(JspWriter out) throws IOException {
        Edit control = new Edit(this.getName(), this.getValue());
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING));
        if (this.getConfigValue("saveInfo").equals("false")) {
            control.setSaveInfo(false);
        }
        control.setCssClass(CSSCLASS_EDIT);
        control.setRows(this.getConfigValue("rows", "1"));
        control.setCssStyles("width", this.getConfigValue("width", "100%"));
        if (this.getConfigValue("onchange", null) != null) {
            control.setEvent("onchange", this.getConfigValue("onchange"));
        }
        this.drawHtmlPre(out);
        out.println(control.getHtml());
        this.drawHtmlPost(out);
    }
}
