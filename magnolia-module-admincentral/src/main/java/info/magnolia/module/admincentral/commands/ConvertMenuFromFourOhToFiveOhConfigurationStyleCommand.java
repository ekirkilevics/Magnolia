/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.admincentral.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;

import java.util.Collection;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME: does not work anymore as menu structure changed.
 * Convert old style menus to the new ones.
 * @author had
 * @version $Id: $
 */
public class ConvertMenuFromFourOhToFiveOhConfigurationStyleCommand extends MgnlCommand {


    private static final Logger log = LoggerFactory.getLogger(ConvertMenuFromFourOhToFiveOhConfigurationStyleCommand.class);
    private static final String MENU_ITEMS = "menuItems";
    private static final String NEW_MENU_LOCATION = "/modules/admin-central/config/" + MENU_ITEMS;

    @Override
    public boolean execute(Context context) throws Exception {
        try {
            convertMenus(context);
            return true;
        } catch (RepositoryException e) {
            log.error("Failed to convert menus for vaadin.", e);
        }
        return false;
    }

    private void convertMenus(Context context) throws RepositoryException {
        HierarchyManager hm = context.getHierarchyManager(ContentRepository.CONFIG);
        if (!hm.isExist(NEW_MENU_LOCATION)) {
            ContentUtil.createPath(hm, NEW_MENU_LOCATION);
            // not saving here breaks copy ops later
            hm.save();
        }
        Collection<Content> menuItems = hm.getContent("/modules/adminInterface/config/menu").getChildren(ItemType.CONTENTNODE);

        for (Content menuItem : menuItems) {
            String menuItemHandle = NEW_MENU_LOCATION + "/" + menuItem.getName();
            if (menuItem.hasNodeData("importedTo50")) {
                continue;
            }
            if (!hm.isExist(menuItemHandle)) {
                hm.copyTo(menuItem.getHandle(), menuItemHandle);
                hm.save();
            }
            Content newMenuItem = hm.getContent(menuItemHandle);
            //TODO Remove me. This is just for having a working configuration to start with during development
            if("website".equals(newMenuItem.getName())){
                newMenuItem.setNodeData("workspace", "website");
            } else if("config".equals(newMenuItem.getName())){
                newMenuItem.setNodeData("workspace", "config");
            } else if("store-client".equals(newMenuItem.getName())){
                newMenuItem.setNodeData("viewTarget", "/.magnolia/pages/allModulesList.html");
            }
            transformSubmenus(hm, newMenuItem);
            //TODO: onClick => viewTarget
            menuItem.setNodeData("importedTo50", true);
        }

    }

    private void transformSubmenus(HierarchyManager hm, Content menuItem) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        Collection<Content> children = menuItem.getChildren(ItemType.CONTENTNODE);
        if (children.isEmpty()) {
            return;
        }

        Content subMenus;
        if (menuItem.hasContent(MENU_ITEMS)) {
            subMenus = menuItem.getContent(MENU_ITEMS);
        } else {
            subMenus = menuItem.createContent(MENU_ITEMS, ItemType.CONTENTNODE);
            // yeah again ... there's move down the road
            menuItem.save();
        }
        for (Content sub : children) {
            if (sub.getName().equals(MENU_ITEMS)) {
                // previous unsuccessful attempt?
                continue;
            }
            if (subMenus.hasContent(sub.getName())) {
                // skip, already done (or duplicate)
                continue;
            }
            // process submenus of the submenu (if any)
            transformSubmenus(hm, sub);
            hm.moveTo(sub.getHandle(), subMenus.getHandle() + "/" + sub.getName());

            if (sub.getName().equals("usersAdmin")) {
                sub.setNodeData("repo", "usersAdmin");
                sub.setNodeData("actionClass", "info.magnolia.module.admincentral.navigation.OpenMainViewMenuAction");
            } else if (sub.getName().equals("usersSystem")) {
                sub.setNodeData("repo", "usersSystem");
                sub.setNodeData("actionClass", "info.magnolia.module.admincentral.navigation.OpenMainViewMenuAction");
            }

            hm.save();
        }
    }
}
