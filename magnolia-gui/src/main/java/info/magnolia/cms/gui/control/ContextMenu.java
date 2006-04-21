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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class encapsulates the context menu, used in the tree.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ContextMenu extends ControlSuper {

    /**
     * All the menu items showed by the context menu
     */
    private List menuItems = new ArrayList();

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
    public List getMenuItems() {
        return this.menuItems;
    }

    /**
     * Populate it with a list
     * @param menuItems the list
     */
    public void setMenuItems(List menuItems) {
        this.menuItems = menuItems;
    }

    /**
     * @param col index
     * @return the item
     */
    public ContextMenuItem getMenuItem(int col) {
        return (ContextMenuItem) this.getMenuItems().get(col);
    }

    /**
     * @param col index
     * @return the item
     */
    public ContextMenuItem getMenuItemByName(String name) {
        java.util.List menuItems = this.getMenuItems ();
        for (Iterator iter = menuItems.iterator(); iter.hasNext();) {
            ContextMenuItem menuItem = (ContextMenuItem) iter.next();
            if (menuItem != null && menuItem.getName() != null && menuItem.getName() == name){
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