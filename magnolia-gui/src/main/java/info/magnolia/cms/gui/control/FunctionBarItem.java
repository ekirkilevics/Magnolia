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

import java.util.Iterator;


/**
 * Represents an item in the function bar.
 * @author higi
 */
public class FunctionBarItem extends ContextMenuItem {

    private boolean active = false;

    public FunctionBarItem() {
        this.active = false;
    }

    /**
     * Copy constructor. Used because the most buttons are redundant to it's context menu item
     * @param item
     */
    public FunctionBarItem(ContextMenuItem item) {
        this.setName(item.getName());
        this.setLabel(item.getLabel());
        if (item.getIcon() != null){
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
        if (super.getIcon() != null)
            return super.getIcon().replaceFirst(".gif", "_inactive.gif");
        return null;
    }
}
