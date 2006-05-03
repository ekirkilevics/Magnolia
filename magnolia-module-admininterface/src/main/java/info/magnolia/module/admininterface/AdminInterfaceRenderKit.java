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
package info.magnolia.module.admininterface;

import info.magnolia.cms.gui.controlx.impl.RenderKitImpl;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListColumnRenderer;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.module.admininterface.lists.AdminListControlRenderer;


/**
 * The default render kit for the admin interface
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class AdminInterfaceRenderKit extends RenderKitImpl {
    /**
     * Register the default renderers used in the admin interface 
     */
    public AdminInterfaceRenderKit() {
        this.register(ListColumn.RENDER_TYPE, new ListColumnRenderer());
        this.register(ListControl.RENDER_TYPE, new AdminListControlRenderer());
    }
}
