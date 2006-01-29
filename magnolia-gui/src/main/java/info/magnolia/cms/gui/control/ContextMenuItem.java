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
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class ContextMenuItem extends ControlSuper {

    private String icon;

    private String onclick;

    private String javascriptMenuName;

    private List javascriptConditions = new ArrayList();

    public ContextMenuItem() {
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
                + "\"> <span style=\"position:relative;top:-3px\">" //$NON-NLS-1$
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
