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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import info.magnolia.cms.i18n.Messages;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class ContextMenuItem extends ControlImpl {

    public static ContextMenuItem getRefreshMenuItem(Tree tree, Messages msgs, HttpServletRequest request) {
        ContextMenuItem menuRefresh = new ContextMenuItem("refresh");
        menuRefresh.setLabel(msgs.get("tree.menu.refresh")); //$NON-NLS-1$
        menuRefresh.setIcon(request.getContextPath() + "/.resources/icons/16/refresh.gif"); //$NON-NLS-1$
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$
        return menuRefresh;
    }

    private String icon;

    private String onclick;

    private String javascriptMenuName;

    private List javascriptConditions = new ArrayList();

    public ContextMenuItem() {
    }

    public ContextMenuItem(String name) {
        this.setName(name);
    }

    public void setOnclick(String s) {
        this.onclick = s;
    }

    public String getOnclick() {
        return this.onclick;
    }

    /**
     * Must be a object with a method test(): addJavascriptCondition("new MyCondition()").
     */
    public void addJavascriptCondition(String methodName) {
        this.javascriptConditions.add(methodName);
    }

    public List getJavascriptConditions() {
        return this.javascriptConditions;
    }

    public String getJavascriptCondition(int index) {
        return (String) this.javascriptConditions.get(index);
    }

    // todo: icons
    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<div class=\"mgnlTreeMenuItem\" id=\"" //$NON-NLS-1$
            + this.getId()
            + "\" onclick=\"" //$NON-NLS-1$
            + this.getJavascriptMenuName()
            + ".hide();"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(this.onclick)) {
            html.append(this.onclick);
        }

        String label = this.getLabel();
        if (StringUtils.isNotEmpty(this.getIcon())) {
            label = "<img src=\"" //$NON-NLS-1$
                + this.getIcon()
                + "\" alt=\"\" /> <span style=\"position:relative;top:-3px\">" //$NON-NLS-1$
                + label
                + "</span>"; //$NON-NLS-1$
        }

        html.append("\" onmouseover=\"" //$NON-NLS-1$
            + this.getJavascriptMenuName()
            + ".menuItemHighlight(this);\"  onmouseout=\"" //$NON-NLS-1$
            + this.getJavascriptMenuName()
            + ".menuItemReset(this);\">" //$NON-NLS-1$
            + label
            + "</div>"); //$NON-NLS-1$
        return html.toString();
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getJavascriptMenuName() {
        return this.javascriptMenuName;
    }

    public void setJavascriptMenuName(String javascriptMenuName) {
        this.javascriptMenuName = javascriptMenuName;
    }
}
