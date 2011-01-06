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


/**
 * This class encapsulates the context menu, used in the tree.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ContextMenu extends ControlImpl {

    /**
     * All the menu items showed by the context menu.
     */
    private List<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();

    /**
     * Create a context menu and provide the name (used in javascript)
     * @param menuName the name used for the menu
     */
    public ContextMenu(String menuName) {
        super();
        this.setName(menuName);
    }

    /**
     * @return all menu items
     */
    public List<ContextMenuItem> getMenuItems() {
        return this.menuItems;
    }

    /**
     * Populate it with a list
     * @param menuItems the list
     */
    public void setMenuItems(List<ContextMenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    /**
     * @param col index
     * @return the item
     */
    public ContextMenuItem getMenuItem(int col) {
        return this.getMenuItems().get(col);
    }

    public ContextMenuItem getMenuItemByName(String name) {
        List<ContextMenuItem> menuItems = this.getMenuItems();
        for (ContextMenuItem menuItem : menuItems) {
            if (menuItem != null && menuItem.getName() != null && menuItem.getName().equals(name)) {
                return menuItem;
            }
        }
        return null;
    }

    /**
     * Add a item
     * @param item the item object
     */
    public void addMenuItem(ContextMenuItem item) {
        this.getMenuItems().add(item);
    }

    /**
     * Renders the HTML Code. Creates a div with all the containing menuitems and adds the initialization in javascript
     * @return html code
     */
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"" + getName() + "_DivMenu\" class=\"mgnlTreeMenu\" onmouseover=\"" + getName() + ".keepShowing();\" onmouseout=\"" + getName() + ".hide();\" >"); //$NON-NLS-1$ //$NON-NLS-2$
        int counter = 0;

        for (int i = 0; i < this.getMenuItems().size(); i++) {
            ContextMenuItem item = this.getMenuItem(i);
            if (item == null) {
                html.append("<div class=\"mgnlTreeMenuLine\"><!-- ie --></div>"); //$NON-NLS-1$
            }
            else {
                item.setJavascriptMenuName(getName());
                String id = getName() + "_MenuItem" + i; //$NON-NLS-1$
                item.setId(id);
                html.append(item.getHtml());
                counter++;
            }
            html.append("\n");
        }

        html.append("</div>"); //$NON-NLS-1$
        return html.toString();
    }

    public String getJavascript() {
        StringBuffer menuJavascript = new StringBuffer();
        menuJavascript.append("var " + getName() + "= new mgnlContextMenu('" + getName() + "');");
        int counter = 0;
        for (int i = 0; i < this.getMenuItems().size(); i++) {
            ContextMenuItem item = this.getMenuItem(i);
            if (item != null) {
                item.setJavascriptMenuName(getName());
                String id = getName() + "_MenuItem" + i; //$NON-NLS-1$
                item.setId(id);
                menuJavascript.append(getName() + ".menuItems[" //$NON-NLS-1$
                    + counter
                    + "]=new mgnlContextMenuItem('" //$NON-NLS-1$
                    + id
                    + "');\n"); //$NON-NLS-1$
                menuJavascript.append(getName() + ".menuItems[" + counter + "].conditions=new Object();"); //$NON-NLS-1$ //$NON-NLS-2$
                for (int cond = 0; cond < item.getJavascriptConditions().size(); cond++) {
                    menuJavascript.append(getName() + ".menuItems[" //$NON-NLS-1$
                        + counter
                        + "].conditions[" //$NON-NLS-1$
                        + cond
                        + "]=" //$NON-NLS-1$
                        + item.getJavascriptCondition(cond)
                        + ";"); //$NON-NLS-1$
                }
                counter++;
            }
        }

        return menuJavascript.toString();
    }
}
