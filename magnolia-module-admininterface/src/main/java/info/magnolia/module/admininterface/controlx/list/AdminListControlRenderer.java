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
package info.magnolia.module.admininterface.controlx.list;

import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListControlRenderer;
import info.magnolia.module.admininterface.AdminInterfaceRenderKit;


/**
 * Show the context menu if existing.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class AdminListControlRenderer extends ListControlRenderer {

    /**
     * Set the admin interface list template
     */
    public AdminListControlRenderer() {
        super();
        setTemplateName(AdminInterfaceRenderKit.DEFAULT_TEMPLATE_PATH + "/list/ListControl.html");
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
}
