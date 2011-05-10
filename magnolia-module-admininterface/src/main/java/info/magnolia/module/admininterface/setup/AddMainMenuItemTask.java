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
package info.magnolia.module.admininterface.setup;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
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
     * @param orderBefore the menu name before which this new menu should be positioned. ignored if null.
     * @param i18nBasename ignored if null.
     */
    public AddMainMenuItemTask(String menuName, String label, String i18nBasename, String onClick, String icon, String orderBefore) {
        super("Menu", "Adds or updates an item in the admin interface menu for " + menuName);
        this.menuName = menuName;
        this.label = label;
        this.i18nBasename = i18nBasename;
        this.onClick = onClick;
        this.icon = icon;
        this.orderBefore = orderBefore;
    }

    @Override
    public void execute(InstallContext ctx) throws TaskExecutionException {
        try {
            final Content parent = getParentNode(ctx);
            final Content menu = ContentUtil.getOrCreateContent(parent, menuName, ItemType.CONTENTNODE);
            NodeDataUtil.getOrCreateAndSet(menu, "icon", icon);
            NodeDataUtil.getOrCreateAndSet(menu, "onclick", onClick);
            NodeDataUtil.getOrCreateAndSet(menu, "label", label);
            if (i18nBasename != null) {
                NodeDataUtil.getOrCreateAndSet(menu, "i18nBasename", i18nBasename);
            }

            if (orderBefore != null){
                parent.orderBefore(menuName, orderBefore);
            }
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Could not create or place " + menuName + " menu item.", e);
        }
    }

    protected Content getParentNode(InstallContext ctx) throws RepositoryException {
        return ctx.getConfigHierarchyManager().getContent("/modules/adminInterface/config/menu");
    }
}
