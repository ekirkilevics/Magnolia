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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ExternalResource;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.module.admincentral.dialog.I18nAwareComponent;
import info.magnolia.module.admincentral.views.IFrameView;

/**
 * Bean representing stored configuration of the menu item.
 * @author had
 * @version $Id: $
 */
public class MenuItemConfiguration extends I18nAwareComponent implements Serializable {

    private static final long serialVersionUID = -1471102661627728596L;

    private Logger log = LoggerFactory.getLogger(MenuItemConfiguration.class);

    private String icon;
    private String label;
    private AdminCentralAction action;
    private Map<String, MenuItemConfiguration> subMenuItems = new LinkedHashMap<String, MenuItemConfiguration>();
    private MenuItemConfiguration parent;
    private String location;
    /**
     * @deprecated use viewTarget instead
     */
    private String onclick;
    private String actionClass;
    /**
     * The fully qualified classname for a custom component (e.g. the ConfigurationTreeTableView) providing management for app history and bookmarking.
     */
    // TODO: aren't we introducing strong dep on vaadin here?
    private String view = IFrameView.class.getName();

    /**
     * A html file in classpath which will be embedded in an iframe.
     */
    private String viewTarget;

    private String name;


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
    public AdminCentralAction getAction() {
        if (this.action == null) {
            try {
                Class<? extends AdminCentralAction> clazz = this.actionClass == null ? DefaultMenuAction.class : (Class<? extends AdminCentralAction>) Class.forName(this.actionClass);
                // TODO: sucks action needs to have label set at the creation time :(
                // TODO: refactor and make c2b friendly
                this.action = clazz.getConstructor(String.class).newInstance(getMessages().getWithDefault(getLabel(), getLabel()));
                // this.action.setCaption("X" + );
                if (this.getIcon() != null) {
                    // TODO: might be too slow or chatty and we might want to swap it with ApplicationResource instead\
                    String iconPath = getIcon();
                    if (iconPath.startsWith("/")) {
                        iconPath = iconPath.substring(1);
                    }
                    this.action.setIcon(new ExternalResource(ServerConfiguration.getInstance().getDefaultBaseUrl() + iconPath));
                }

                // TODO: transfer i18n as well ... or set this as a parent for i18n
            } catch (Exception e) {
                log.error("Failed to instantiate action " + actionClass, e);
            }
        }
        return action;
    }
    public void setAction(AdminCentralAction action) {
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

    /**
     * @deprecated use viewTarget instead
     */
    public String getOnclick() {
        return this.onclick;
    }

    /**
     * @deprecated use viewTarget instead
     */
    public void setOnclick(String action) {
        this.onclick = action;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getViewTarget() {
        return viewTarget;
    }

    public void setViewTarget(String viewTarget) {
        this.viewTarget = viewTarget;
    }

}
