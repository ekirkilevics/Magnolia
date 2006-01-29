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

import info.magnolia.cms.core.Content;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class SelectOption extends ControlSuper {

    private String label;

    private boolean selected;

    public SelectOption() {
    }

    public SelectOption(String label, String value) {
        this.setLabel(label);
        this.setValue(value);
    }

    public SelectOption(String label, Content websiteNode) {
        this.setLabel(label);
        this.setWebsiteNode(websiteNode);
    }

    public void setSelected(boolean b) {
        this.selected = b;
    }

    public boolean getSelected() {
        return this.selected;
    }

    public void setLabel(String s) {
        this.label = s;
    }

    public String getLabel() {
        if (this.label != null) {
            return this.label;
        }

        return this.getValue();
    }

    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<option value=\"" + this.getValue() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(this.getHtmlCssClass());
        html.append(this.getHtmlCssStyles());
        if (this.getSelected()) {
            html.append(" selected"); //$NON-NLS-1$
        }
        html.append(this.getHtmlId()); // id e.g. needed in rich editor
        html.append(">"); //$NON-NLS-1$
        // html.append("["+this.getLabel()+"]["+this.getValue()+"]");
        html.append(this.getLabel());
        html.append("</option>"); //$NON-NLS-1$
        return html.toString();
    }
}
