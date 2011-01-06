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
package info.magnolia.module.admininterface.lists;

import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListControlRenderer;


import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Show the context menu if existing.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class AdminListControlRenderer extends ListControlRenderer {

    public static Logger log = LoggerFactory.getLogger(AdminListControlRenderer.class);

    private boolean border = false;

    private String javaScriptClass = "mgnl.controls.List";

    /**
     * Set the admin interface list template
     */
    public AdminListControlRenderer() {
        super();
        setTemplateName("info/magnolia/module/admininterface/lists/ListControl.html");
    }

    /**
     * Sets the selected id in the js object
     */
    public String onSelect(ListControl list, Integer index) {
        return list.getName() + ".select(" + index + ");";
    }

    /**
     * Show the context menu
     */
    public String onRightClick(ListControl list, Integer index) {
        if (list.getContextMenu() != null) {
            return list.getContextMenu().getName() + ".show(event);";
        }
        return "";
    }

    public String onDblClick(ListControl list, Integer index) {
        return list.getName() + ".show();";
    }

    public String getJavaScriptObject(ListControl list, Integer index){
        return "{" + buildJavaScriptObject(list, list.getIteratorValueObject()) + "}";
    }

    protected String buildJavaScriptObject(ListControl list, Object value) {
        return "id: '" + toViewId(list.getIteratorId()) + "'";
    }

    public String toViewId(String id) {
        return StringEscapeUtils.escapeJavaScript(id);
    }

    public boolean isBorder() {
        return this.border;
    }

    public void setBorder(boolean border) {
        this.border = border;
    }

    /**
     * Defines the class/constructor function used to instantiate the javascript list object.
     */
    public String getJavaScriptClass() {
        return this.javaScriptClass;
    }

    public void setJavaScriptClass(String javaScriptClass) {
        this.javaScriptClass = javaScriptClass;
    }

    /**
     * The arguments passed to the constructor.
     */
    public String getConstructorArguments(ListControl list){
        return "'" + list.getName() + "', document.mgnlForm";
    }
}
