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

import info.magnolia.cms.core.Content;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Edit extends ControlSuper {

    String rows = "1";

    public Edit() {
    }

    public Edit(String name, String value) {
        super(name, value);
    }

    public Edit(String name, Content websiteNode) {
        super(name, websiteNode);
    }

    public void setRows(String s) {
        this.rows = s;
    }

    public String getRows() {
        return this.rows;
    }

    public String getHtml() {
        StringBuffer html = new StringBuffer();
        String id = this.getId();
        if (id == null) {
            id = this.getName();
        }
        if (this.getRows().equals("1")) {
            html.append("<input type=\"text\"");
            html.append(" name=\"" + this.getName() + "\"");
            html.append(" id=\"" + id + "\"");
            html.append(" value=\"" + this.getValue() + "\"");
            html.append(getHtmlEvents());
            html.append(this.getHtmlCssClass());
            html.append(this.getHtmlCssStyles());
            html.append(" />");
        }
        else {
            html.append("<textarea");
            html.append(" name=\"" + this.getName() + "\"");
            html.append(" id=\"" + id + "\"");
            html.append(" rows=\"" + this.getRows() + "\"");
            html.append(this.getHtmlCssClass());
            html.append(this.getHtmlCssStyles());
            html.append(getHtmlEvents());
            html.append(">");
            html.append(this.getValue());
            html.append("</textarea>");
        }
        if (this.getSaveInfo()) {
            html.append(this.getHtmlSaveInfo());
        }
        return html.toString();
    }
}
