/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.navigation;

import java.util.List;

import info.magnolia.module.admincentral.dialog.I18nAwareComponent;

/**
 * Bean representing stored configuration of the menu item.
 * @author had
 * @version $Id: $
 */
public class MenuItemConfiguration extends I18nAwareComponent {

    private String icon;
    private String label;
    private MenuAction action;
    private List<MenuItemConfiguration> subMenuItems;
    private MenuItemConfiguration parent;

    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public MenuAction getAction() {
        return action;
    }
    public void setAction(MenuAction action) {
        this.action = action;
    }

    public List<MenuItemConfiguration> getSubMenuItems() {
        return subMenuItems;
    }
    public void setSubMenuItems(List<MenuItemConfiguration> subMenuItems) {
        this.subMenuItems = subMenuItems;
    }

    public void addSubMenuItem(MenuItemConfiguration subMenuItem) {
        subMenuItem.setParent(this);
        this.subMenuItems.add(subMenuItem);
    }

    private void setParent(MenuItemConfiguration parent) {
        this.parent = parent;
    }

    @Override
    public I18nAwareComponent getI18nAwareParent() {
        // no parent
        return this.parent;
    }
}
