/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.navigation;

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.navigation.Melodion.Tab;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationGroupDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationItemDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationWorkareaDefinition;
import info.magnolia.ui.model.navigation.registry.NavigationPermissionSchema;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;


/**
 * Represents a collection of menus (that is {@link NavigationGroup} objects). The type and number
 * of work areas will depend on user's privileges.
 *
 * @author fgrilli
 * @author mrichert
 */
public class NavigationWorkArea implements NavigationView, IsVaadinComponent {

    private static final Logger log = LoggerFactory.getLogger(NavigationWorkArea.class);

    private Melodion melodion = new Melodion();

    private Map<MenuItemDefinition, Component> navigationItems = new HashMap<MenuItemDefinition, Component>();

    private Presenter presenter;

    public NavigationWorkArea(NavigationWorkareaDefinition definition, NavigationPermissionSchema permissions) {

        melodion.addStyleName(definition.getName());
        melodion.setSizeUndefined();
        melodion.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        Component lastSpacer = null;
        for (NavigationGroupDefinition group : definition.getGroups()) {

            log.debug("creating navigation group {}", group.getName());

            for (NavigationItemDefinition navigationItemDef : group.getItems()) {
                if (permissions.hasPermission(navigationItemDef)) {
                    // register new top level menu
                    addTab(navigationItemDef, permissions);
                }
            }
            lastSpacer = melodion.addSpacer();
        }
        melodion.removeComponent(lastSpacer);
    }

    /**
     * The only way to add tabs to Magnolia menu - ensure he have references to all items.
     * @param item menu item configuration entry
     * @param permissions
     */
    public void addTab(final NavigationItemDefinition item, NavigationPermissionSchema permissions) {
        final Label label = new Label("<img src='"
            + MgnlContext.getContextPath()
            + item.getIcon()
            + "'>"
            + getLabel(item), Label.CONTENT_XHTML);
        label.setDescription(getDescription(item));

        // FIXME: Show real notifications instead of random ones.
        // Both setCaption and setIcon can be used.
        int notif = new Random().nextInt(10);
        if (notif > 0) {
            label.setCaption(notif + " new");
        }

        Tab tab = melodion.addTab(label);

        navigationItems.put(item, tab);

        tab.addListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {
                if (event.getChildComponent() == label) {
                    if (presenter != null) {
                        presenter.onMenuSelection(item);
                    }
                }
            }
        });

        for (NavigationItemDefinition subItem : item.getItems()) {
            if (permissions.hasPermission(subItem)) {
                NavButton button = new NavButton(subItem);
                tab.addButton(button);
                navigationItems.put(subItem, button);
            }
        }
    }

    // TODO: Would it make sense to move the i18n logic to a more generic place?
    // For example, in the getters of MenuItemDefinition so that client classes need not worry
    // about
    // i18n and this kind of utility code is not spread all over the place?
    /**
     * Converts label key into i18n-ized string.
     */
    private static String getLabel(NavigationItemDefinition menuItem) {
        return MessagesUtil.getWithDefault(menuItem.getLabel(), menuItem.getLabel(), menuItem.getI18nBasename());
    }

    /**
     * Converts description key into i18n-ized string.
     */
    private static String getDescription(NavigationItemDefinition menuItem) {
        return MessagesUtil.getWithDefault(menuItem.getDescription(), menuItem.getDescription(), menuItem.getI18nBasename());
    }

    @Override
    public Component asVaadinComponent() {
        return melodion;
    }

    public void setVisible(boolean visible) {
        melodion.setVisible(visible);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void select(MenuItemDefinition menuItemDefinition) {
        Component c = navigationItems.get(menuItemDefinition);
        if (c == null) {
            return;
        }
        melodion.setSelected(c);
        if (c instanceof Melodion.Tab) {
            ((Melodion.Tab) c).expand();
        }
    }

    private static Resource getIcon(NavigationItemDefinition menuItem) {
        if (menuItem.getIcon() == null) {
            return null;
        }
        return new ExternalResource(MgnlContext.getContextPath() + menuItem.getIcon());
    }

    private class NavButton extends NativeButton {

        private NavigationItemDefinition item;

        public NavButton(NavigationItemDefinition item) {
            this.item = item;
        }

        /**
         * See {@link com.vaadin.ui.AbstractComponent#getApplication()} javadoc as to why we need to
         * do most of the initialization here and not in the constructor.
         */
        @Override
        public void attach() {
            super.attach();
            Resource icon = NavigationWorkArea.getIcon(item);
            if (icon != null) {
                setIcon(icon);
            }
            setCaption(NavigationWorkArea.getLabel(item));
            setDescription(NavigationWorkArea.getDescription(item));

            this.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (presenter != null) {
                        presenter.onMenuSelection(item);
                    }
                }
            });
        }
    }
}
