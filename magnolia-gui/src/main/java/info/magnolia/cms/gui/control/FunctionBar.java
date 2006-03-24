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

import java.util.HashMap;
import java.util.Map;

import info.magnolia.cms.util.FreeMarkerUtil;


/**
 * The bar containing function buttons and the search field.
 * @author higi
 */
public class FunctionBar extends ContextMenu {

    /**
     * Name to reference this object in js
     */
    private String javascriptName;

    /**
     * @param menuName the name of this menu (used in js)
     */
    public FunctionBar(String menuName) {
        super(menuName);
        // TODO Auto-generated constructor stub
        this.javascriptName = "mgnlFunctionBar";
    }

    /**
     * Render the html code using the freemarker template
     */
    public String getHtml() {
        Map params = new HashMap();
        params.put("functionBar", this);
        return FreeMarkerUtil.process("info/magnolia/cms/gui/control/FunctionBar.ftl", params);
    }

    /**
     * @return true if the functionBar has one or more functionBarItems
     */
    public boolean hasMenuItems() {
        return !this.getMenuItems().isEmpty();
    }

    /**
     * @return Returns the javascriptName.
     */
    public String getJavascriptName() {
        return javascriptName;
    }

    /**
     * @param javascriptName The javascriptName to set.
     */
    public void setJavascriptName(String javascriptName) {
        this.javascriptName = javascriptName;
    }
}
