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
package info.magnolia.module.admincentral.navigation;

import info.magnolia.module.admincentral.dialog.I18nAwareComponent;
import info.magnolia.ui.action.Action;
import info.magnolia.ui.action.ActionDefinition;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bean representing stored configuration of the menu item.
 * @author had
 * @version $Id: $
 */
public class MenuItemConfiguration extends I18nAwareComponent implements Serializable {

    private static final long serialVersionUID = -1471102661627728596L;

    private String name;
    private String icon;
    private String label;
    private String location;
    private MenuItemConfiguration parent;
    private ActionDefinition<? extends Action> actionDefinition;
    private Map<String, MenuItemConfiguration> subMenuItems = new LinkedHashMap<String, MenuItemConfiguration>();

    @Override
    public I18nAwareComponent getI18nAwareParent() {
        return this.parent;
    }

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

    public Map<String, MenuItemConfiguration> getMenuItems() {
        return subMenuItems;
    }
    public void setMenuItems(Map<String, MenuItemConfiguration> subMenuItems) {
        this.subMenuItems = subMenuItems;
    }

    public void addMenuItem(String name, MenuItemConfiguration subMenuItem) {
        subMenuItem.setParent(this);
        subMenuItem.setName(name);
        this.subMenuItems.put(name, subMenuItem);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setParent(MenuItemConfiguration parent) {
        this.parent = parent;
    }

    public String getLocation() {
        if (this.location != null) {
            return this.location;
        }
        String location = "/" + name;
        if (this.parent != null) {
            location = this.parent.getLocation() + location;
        }
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

   public ActionDefinition<? extends Action> getActionDefinition() {
       return actionDefinition;
   }

   public void setActionDefinition(ActionDefinition<? extends Action> actionDefinition) {
       this.actionDefinition = actionDefinition;
   }
}
