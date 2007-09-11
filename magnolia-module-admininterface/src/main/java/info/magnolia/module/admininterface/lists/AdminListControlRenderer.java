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

    public String getJavaScriptClass() {
        return this.javaScriptClass;
    }

    public void setJavaScriptClass(String javaScriptClass) {
        this.javaScriptClass = javaScriptClass;
    }
}
