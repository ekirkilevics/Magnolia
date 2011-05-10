/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class SelectOption extends ControlImpl {

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

    @Override
    public void setLabel(String s) {
        this.label = s;
    }

    @Override
    public String getLabel() {
        if (this.label != null) {
            return this.label;
        }

        return this.getValue();
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<option value=\"" + this.getValue() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(this.getHtmlCssClass());
        html.append(this.getHtmlCssStyles());
        if (this.getSelected()) {
            html.append(" selected=\"selected\""); //$NON-NLS-1$
        }
        html.append(this.getHtmlId()); // id e.g. needed in rich editor
        html.append(">"); //$NON-NLS-1$
        // html.append("["+this.getLabel()+"]["+this.getValue()+"]");
        html.append(this.getLabel());
        html.append("</option>"); //$NON-NLS-1$
        return html.toString();
    }
}
