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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Edit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogEditWithButton extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogEditWithButton.class);

    private List buttons = new ArrayList();

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#init(Content, Content, PageContext)
     */
    public void init(Content configNode, Content websiteNode, PageContext pageContext) throws RepositoryException {
        super.init(configNode, websiteNode, pageContext);
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
        control.setCssStyles("width", "100%");
        if (this.getConfigValue("onchange", null) != null) {
            control.setEvent("onchange", this.getConfigValue("onchange"));
        }
        this.drawHtmlPre(out);
        String width = this.getConfigValue("width", "100%");
        out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"" + width + "\">");
        out.println("<tr><td width=\"100%\"  class=\"" + CSSCLASS_EDITWITHBUTTON + "\">");
        out.println(control.getHtml());
        if (this.getConfigValue("buttonLabel", null) != null) {
            this.getButton().setLabel(this.getConfigValue("buttonLabel"));
        }
        for (int i = 0; i < this.getButtons().size(); i++) {
            out.println("</td><td>&nbsp;</td><td class=\"" + CSSCLASS_EDITWITHBUTTON + "\">");
            out.println(this.getButton(i).getHtml());
        }
        out.println("</td></tr></table>");

        this.drawHtmlPost(out);
    }
}