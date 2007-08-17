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
package info.magnolia.module.admininterface.setup;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 * TODO : possibility to just order the node, like it was done for 3.0 ? 
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AddMainMenuItemTask extends AbstractTask {
    private final String menuName;
    private final String label;
    private final String i18nBasename;
    private final String onClick;
    private final String icon;
    private final String orderBefore;

    /**
     * @param orderBefore the menu name before which this new menu should be positioned 
     */
    public AddMainMenuItemTask(String menuName, String label, String i18nBasename, String onClick, String icon, String orderBefore) {
        super("Menu", "Adds an item in the admin interface menu for " + menuName);
        this.menuName = menuName;
        this.label = label;
        this.i18nBasename = i18nBasename;
        this.onClick = onClick;
        this.icon = icon;
        this.orderBefore = orderBefore;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        try {
            final Content mainMenu = getMainMenuNode(ctx);
            final Content menu = mainMenu.createContent(menuName, ItemType.CONTENTNODE);
            menu.createNodeData("icon", icon);
            menu.createNodeData("onclick", onClick);
            menu.createNodeData("label", label);
            menu.createNodeData("i18nBasename", i18nBasename);

            mainMenu.orderBefore(menuName, orderBefore);
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Could not create or place " + menuName + " menu item.", e);
        }
    }

    protected Content getMainMenuNode(InstallContext ctx) throws RepositoryException {
        return ctx.getConfigHierarchyManager().getContent("/modules/adminInterface/config/menu");
    }
}
