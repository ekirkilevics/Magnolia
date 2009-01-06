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

import info.magnolia.cms.i18n.Messages;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;


/**
 * Represents an item in the function bar.
 * @author higi
 */
public class FunctionBarItem extends ContextMenuItem {

    public static FunctionBarItem getRefreshFunctionBarItem(Tree tree, Messages msgs, HttpServletRequest request) {
        return new FunctionBarItem(ContextMenuItem.getRefreshMenuItem(tree, msgs, request));
    }

    private boolean active = false;

    public FunctionBarItem(String name) {
        super(name);
        this.active = false;
    }

    /**
     * Copy constructor. Used because the most buttons are redundant to it's context menu item
     * @param item
     */
    public FunctionBarItem(ContextMenuItem item) {
        this.setName(item.getName());
        this.setLabel(item.getLabel());
        if (item.getIcon() != null) {
            this.setIcon(item.getIcon().replaceFirst("/16/", "/24/"));
        }
        this.setJavascriptMenuName(item.getJavascriptMenuName());
        this.setOnclick(item.getOnclick());
        java.util.List jsConds = item.getJavascriptConditions();
        for (Iterator iter = jsConds.iterator(); iter.hasNext();) {
            String condition = (String) iter.next();
            this.addJavascriptCondition(condition);
            // I.E. : new mgnlTreeMenuItemConditionSelectedNotRoot(mgnlTreeControl)
        }
        this.active = false;
    }

    /**
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Add the _inactive suffix.
     * @return the proper name
     */
    public String getInactiveIcon() {
        if (super.getIcon() != null) {
            return super.getIcon().replaceFirst(".gif", "_inactive.gif");
        }
        return null;
    }
}
