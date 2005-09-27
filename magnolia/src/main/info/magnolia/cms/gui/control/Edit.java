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

    String rows = "1"; //$NON-NLS-1$

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
        if (this.getRows().equals("1")) { //$NON-NLS-1$
            html.append("<input type=\"text\""); //$NON-NLS-1$
            html.append(" name=\"" + this.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            html.append(" id=\"" + id + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            html.append(" value=\"" + ControlSuper.escapeHTML(this.getValue()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            html.append(getHtmlEvents());
            html.append(this.getHtmlCssClass());
            html.append(this.getHtmlCssStyles());
            html.append(" />"); //$NON-NLS-1$
        }
        else {
            html.append("<textarea"); //$NON-NLS-1$
            html.append(" name=\"" + this.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            html.append(" id=\"" + id + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            html.append(" rows=\"" + this.getRows() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            html.append(this.getHtmlCssClass());
            html.append(this.getHtmlCssStyles());
            html.append(getHtmlEvents());
            html.append(">"); //$NON-NLS-1$
            html.append(this.getValue());
            html.append("</textarea>"); //$NON-NLS-1$
        }
        if (this.getSaveInfo()) {
            html.append(this.getHtmlSaveInfo());
        }
        return html.toString();
    }
}
