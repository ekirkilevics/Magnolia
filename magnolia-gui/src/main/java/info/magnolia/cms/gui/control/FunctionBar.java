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

import info.magnolia.freemarker.FreemarkerUtil;

import java.util.HashMap;
import java.util.Map;


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
        return FreemarkerUtil.process("info/magnolia/cms/gui/control/FunctionBar.ftl", params);
    }

    /**
     * Render the javascript code to initialize it
     */
    public String getJavascript() {
        Map params = new HashMap();
        params.put("functionBar", this);
        return FreemarkerUtil.process("info/magnolia/cms/gui/control/FunctionBarJavascript.ftl", params);
    }

    /**
     * @return true if the functionBar has one or more functionBarItems
     */
    public boolean hasMenuItems() {
        return !this.getMenuItems().isEmpty();
    }

    public void addMenuItem(ContextMenuItem item) {
        if(item != null && !(item instanceof FunctionBarItem)){
            item = new FunctionBarItem(item);
        }
        super.addMenuItem(item);
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
     * @param onSearch The onSearch function to set.
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
