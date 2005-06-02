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

import java.util.ArrayList;
import java.util.List;


/**
 * @author philipp
 */
public class ContextMenu extends ControlSuper {

    private List menuItems = new ArrayList();

    public ContextMenu(String menuName) {
        super();
        this.setName(menuName);
    }

    public List getMenuItems() {
        return this.menuItems;
    }

    public void setMenuItems(List menuItems) {
        this.menuItems = menuItems;
    }

    public ContextMenuItem getMenuItem(int col) {
        return (ContextMenuItem) this.getMenuItems().get(col);
    }

    public void addMenuItem(ContextMenuItem tmi) {
        this.getMenuItems().add(tmi);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.gui.control.ControlSuper#getHtml()
     */
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        StringBuffer menuJavascript = new StringBuffer();
        html.append("<div id=\"" + getName() + "_DivMenu\" class=\"mgnlTreeMenu\" >");
        int counter = 0;
        for (int i = 0; i < this.getMenuItems().size(); i++) {
            ContextMenuItem item = this.getMenuItem(i);
            if (item == null) {
                html.append("<div class=\"mgnlTreeMenuLine\"><!-- ie --></div>");
            }
            else {
                item.setJavascriptMenuName(getName());
                String id = getName() + "_MenuItem" + i;
                item.setId(id);
                menuJavascript.append(getName()
                    + ".menuItems["
                    + counter
                    + "]=new mgnlContextMenuItem('"
                    + id
                    + "');\n");
                menuJavascript.append(getName() + ".menuItems[" + counter + "].conditions=new Object();");
                for (int cond = 0; cond < item.getJavascriptConditions().size(); cond++) {
                    menuJavascript.append(getName()
                        + ".menuItems["
                        + counter
                        + "].conditions["
                        + cond
                        + "]="
                        + item.getJavascriptCondition(cond)
                        + ";");
                }
                html.append(item.getHtml());
                counter++;
            }
        }
        html.append("</div>");

        // html.append(this.getJavascriptTree()+".columns=new Array();"); //->in tree.js
        // add menu to tree object

        html.append("<script type=\"text/javascript\">");
        html.append("var " + getName() + "= new mgnlContextMenu('" + getName() + "');");
        html.append(menuJavascript);
        html.append("</script>");
        return html.toString();
    }
}
