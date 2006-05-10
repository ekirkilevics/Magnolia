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
     * True if the search field is showed
     */
    private boolean searchable = false;
    
    /**
     * Javascript called if Enter is pressed in the field
     */
    private String onSearchFunction = "function(val){alert(val)}";
    
    /**
     * String already present in the search field
     */
    private String searchStr = "";

    /**
     * @param menuName the name of this menu (used in js)
     */
    public FunctionBar(String menuName) {
        super(menuName);
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
     * Render the javascript code to initialize it
     */
    public String getJavascript() {
        Map params = new HashMap();
        params.put("functionBar", this);
        return FreeMarkerUtil.process("info/magnolia/cms/gui/control/FunctionBarJavascript.ftl", params);
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

    
    /**
     * @return Returns the searchable.
     */
    public boolean isSearchable() {
        return this.searchable;
    }

    
    /**
     * @param searchable The searchable to set.
     */
    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    
    /**
     * @return Returns the onSearchFunction.
     */
    public String getOnSearchFunction() {
        return this.onSearchFunction;
    }

    
    /**
     * @param onSearchFunction The onSearchFunction to set.
     */
    public void setOnSearchFunction(String onSearch) {
        this.onSearchFunction = onSearch;
    }

    
    /**
     * @return Returns the searchStr.
     */
    public String getSearchStr() {
        return this.searchStr;
    }

    
    /**
     * @param searchStr The searchStr to set.
     */
    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }
}
