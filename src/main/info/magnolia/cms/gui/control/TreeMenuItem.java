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


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class TreeMenuItem extends ControlSuper {

    private String onclick = "";

    private String javascriptTree = "";

    private ArrayList javascriptConditions = new ArrayList();

    public TreeMenuItem() {
    }

    public void setOnclick(String s) {
        this.onclick = s;
    }

    public String getOnclick() {
        return this.onclick;
    }

    /**
     * Set the name of the javascript tree object.
     * @param variableName
     */
    public void setJavascriptTree(String variableName) {
        this.javascriptTree = variableName;
    }

    public String getJavascriptTree() {
        return this.javascriptTree;
    }

    /**
     * To enable/disable menu items; the tree object will be passed to the method.
     * @param methodName (without brackets! e.g "checkIfWriteAccess" not "checkIfWriteAccess();"
     */
    public void addJavascriptCondition(String methodName) {
        this.javascriptConditions.add(methodName);
    }

    public ArrayList getJavascriptConditions() {
        return this.javascriptConditions;
    }

    public String getJavascriptCondition(int index) {
        return (String) this.javascriptConditions.get(index);
    }

    // todo: icons
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<div class=\"mgnlTreeMenuItem\" id=\""
            + this.getId()
            + "\" onclick=\""
            + this.getJavascriptTree()
            + ".menuHide();"
            + this.getOnclick()
            + "\" onmouseover=\""
            + this.getJavascriptTree()
            + ".menuItemHighlight(this);\"  onmouseout=\""
            + this.getJavascriptTree()
            + ".menuItemReset(this);\">"
            + this.getLabel()
            + "</div>");
        return html.toString();
    }
}
