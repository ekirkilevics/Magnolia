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

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ExternalResource;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.dialog.I18nAwareComponent;

/**
 * Bean representing stored configuration of the menu item.
 * @author had
 * @version $Id: $
 */
public class MenuItemConfiguration extends I18nAwareComponent {

    private Logger log = LoggerFactory.getLogger(MenuItemConfiguration.class);

    private String icon;
    private String label;
    private MenuAction action;
    private Map<String, MenuItemConfiguration> subMenuItems = new LinkedHashMap<String, MenuItemConfiguration>();
    private MenuItemConfiguration parent;
    private String location;
    private String onClick;
    private String actionClass;


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
    public MenuAction getAction() {
        if (this.action == null) {
            try {
                Class<? extends MenuAction> clazz = this.actionClass == null ? DefaultMenuAction.class : (Class<? extends MenuAction>) Class.forName(this.actionClass);

                this.action = clazz.getConstructor(String.class).newInstance(getMessages().getWithDefault(getLabel(), getLabel()));
                // this.action.setCaption("X" + );
                if (this.getIcon() != null) {
                    // TODO: might be too slow or chatty and we might want to swap it with ApplicationResource instead
                    this.action.setIcon(new ExternalResource(ServerConfiguration.getInstance().getDefaultBaseUrl() + getIcon()));
                }

                if (this.getOnClick() != null) {
                    ((DefaultMenuAction) this.action).setOnClick(this.getOnClick());
                }
                // TODO: transfer i18n as well ... or set this as a parent for i18n
            } catch (Exception e) {
                log.error("Failed to instantiate action " + actionClass, e);
            }
        }
        return action;
    }
    public void setAction(MenuAction action) {
        this.action = action;
    }

    public void setActionClass(String className) {
        this.action = null;
        this.actionClass = className;
    }

    public String getActionClass() {
        return this.actionClass;
    }

    public Map<String, MenuItemConfiguration> getMenuItems() {
        return subMenuItems;
    }
    public void setMenuItems(Map<String, MenuItemConfiguration> subMenuItems) {
        this.subMenuItems = subMenuItems;
    }

    public void addMenuItem(String name, MenuItemConfiguration subMenuItem) {
        subMenuItem.setParent(this);
        subMenuItem.setLocation((this.getLocation() == null ? "" : this.getLocation())  +"/" + name);
        this.subMenuItems.put(name, subMenuItem);
    }

    private void setParent(MenuItemConfiguration parent) {
        this.parent = parent;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOnClick() {
        return this.onClick;
    }

    public void setOnClick(String action) {
        this.onClick = action;
    }
}
