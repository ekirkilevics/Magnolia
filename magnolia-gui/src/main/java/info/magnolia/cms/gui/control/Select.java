/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Select extends ControlImpl {

    private List options = new ArrayList();

    public Select() {
    }

    public Select(String name, String value) {
        super(name, value);
    }

    public Select(String name, Content websiteNode) {
        super(name, websiteNode);
    }

    public void setOptions(List l) {
        this.options = l;
    }

    public void setOptions(SelectOption option) {
        this.getOptions().add(option);
    }

    public void setOptions(String label, String value) {
        this.getOptions().add(new SelectOption(label, value));
    }

    public List getOptions() {
        return this.options;
    }

    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<select"); //$NON-NLS-1$
        html.append(" name=\"" + this.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(" id=\"" + this.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(this.getHtmlCssClass());
        html.append(this.getHtmlCssStyles());
        html.append(this.getHtmlEvents());
        html.append(">"); //$NON-NLS-1$
        Iterator it = this.getOptions().iterator();
        while (it.hasNext()) {
            SelectOption o = (SelectOption) it.next();
            if (StringUtils.isNotEmpty(this.getValue())) {
                if (this.getValue().equals(o.getValue())) {
                    o.setSelected(true);
                }
                else {
                    o.setSelected(false);
                }
            }
            html.append(o.getHtml());
        }
        html.append("</select>"); //$NON-NLS-1$
        if (this.getSaveInfo()) {
            html.append(this.getHtmlSaveInfo());
        }
        return html.toString();
    }
}
