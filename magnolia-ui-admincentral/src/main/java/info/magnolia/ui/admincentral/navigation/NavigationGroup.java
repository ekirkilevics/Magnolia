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
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.model.action.PlaceChangeActionDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationItemDefinition;
import info.magnolia.ui.model.navigation.registry.NavigationPermissionSchema;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;


/**
 * A group of navigation items defined by a collection of NavigationItemDefinitions (e.g. website,
 * data). It's part of a {@link NavigationWorkArea}.
 *
 * @author fgrilli
 */
public class NavigationGroup implements NavigationView, IsVaadinComponent {

    private static final Logger log = LoggerFactory.getLogger(NavigationGroup.class);

    private Map<Component, NavigationItemDefinition> navigationItems = new HashMap<Component, NavigationItemDefinition>();

    private Accordion accordion = new Accordion();

    private Collection<NavigationItemDefinition> navigationItemDefs;

    private NavigationPermissionSchema permissions;

    private Presenter presenter;

    private NavigationWorkArea navigationWorkarea;

    public NavigationGroup(Collection<NavigationItemDefinition> navigationItemDefs, NavigationPermissionSchema permissions) {
        this.navigationItemDefs = navigationItemDefs;
        this.permissions = permissions;

        for (NavigationItemDefinition navigationItemDef : this.navigationItemDefs) {
            if (this.permissions.hasPermission(navigationItemDef)) {
                // register new top level menu
                addTab(navigationItemDef, this.permissions);
            }
        }

        // register trigger for menu actions ... sucks but TabSheet doesn't support actions for tabs
        // only for sub menu items
        accordion.addListener(new SelectedNavigationItemTabChangeListener());
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    /**
     * The only way to add tabs to Magnolia menu - ensure he have references to all items.
     * @param navigationItemDef menu item configuration entry
     * @param permissions
     */
    public void addTab(NavigationItemDefinition navigationItemDef, NavigationPermissionSchema permissions) {
        // layout for sub menu entries
        Component subNavigation = addSubNavigationItems(navigationItemDef, permissions);
        subNavigation = subNavigation == null ? new Label() : subNavigation;
        Tab tab = accordion.addTab(subNavigation, getLabel(navigationItemDef), getIcon(navigationItemDef));
        tab.setDescription(navigationItemDef.getDescription());

        // TODO: add notification badges

        // store tab reference
        navigationItems.put(tab.getComponent(), navigationItemDef);
    }

    /**
     * Iterates over sub menu entries and adds them to the layout.
     * @param permissions
     * @return View with all relevant sub menu entries or null when none exists.
     */
    private Component addSubNavigationItems(NavigationItemDefinition navigationItemDef, NavigationPermissionSchema permissions) {
        if (navigationItemDef.getItems().isEmpty()) {
            return null;
        }
        final Layout layout = new VerticalLayout();
        // layout.setSpacing(true);
        layout.setMargin(true);
        layout.addStyleName(navigationItemDef.getName());

        // sub menu items (2 levels only)
        for (NavigationItemDefinition sub : navigationItemDef.getItems()) {
            if (permissions.hasPermission(sub)) {
                NavigationItem submenuItem = new NavigationItem(sub);
                layout.addComponent(submenuItem);
                // store submenu reference
                navigationItems.put(submenuItem, sub);
            }
        }

        return layout;
    }

    /**
     * Converts label key into i18n-ized string.
     */
    protected String getLabel(NavigationItemDefinition menuItem) {
        return MessagesUtil.getWithDefault(menuItem.getLabel(), menuItem.getLabel(), menuItem.getI18nBasename());
    }

    /**
     * Converts description key into i18n-ized string.
     */
    protected String getDescription(NavigationItemDefinition menuItem) {
        return MessagesUtil.getWithDefault(menuItem.getDescription(), menuItem.getDescription(), menuItem.getI18nBasename());
    }

    protected Resource getIcon(NavigationItemDefinition menuItem) {
        if (menuItem.getIcon() == null) {
            return null;
        }
        return new ExternalResource(MgnlContext.getContextPath() + menuItem.getIcon());
    }

    public void setNavigationWorkarea(NavigationWorkArea navigationWorkarea) {
        this.navigationWorkarea = navigationWorkarea;
    }

    public NavigationWorkArea getNavigationWorkarea() {
        return navigationWorkarea;
    }

    /**
     * Menu item button implementation.
     *
     * @author fgrilli
     */
    public class NavigationItem extends Button {

        private NavigationItemDefinition item;

        public NavigationItem(final NavigationItemDefinition item) {
            this.item = item;
        }

        /**
         * See {@link com.vaadin.ui.AbstractComponent#getApplication()} javadoc as to why we need to
         * do most of the initialization here and not in the constructor.
         */
        @Override
        public void attach() {
            super.attach();
            Resource icon = NavigationGroup.this.getIcon(item);
            if (icon != null) {
                setIcon(icon);
            }
            setCaption(getLabel(item));
            setStyleName(BaseTheme.BUTTON_LINK);
            setHeight(20f, Button.UNITS_PIXELS);

            this.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (presenter != null) {
                        NavigationItemDefinition menuConfig = navigationItems.get(event.getComponent());
                        presenter.onMenuSelection(menuConfig);
                    }
                }
            });
        }
    }

    /**
     * Trigger for all menu actions.
     *
     * @author fgrilli
     */
    public class SelectedNavigationItemTabChangeListener implements SelectedTabChangeListener {

        @Override
        public void selectedTabChange(SelectedTabChangeEvent event) {
            TabSheet tabsheet = event.getTabSheet();
            Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());

            if (tab != null) {
                NavigationItemDefinition menuConfig = navigationItems.get(tab.getComponent());
                presenter.onMenuSelection(menuConfig);
            }
        }
    }

    @Override
    public Component asVaadinComponent() {
        return accordion;
    }

    @Override
    public void update(Place place) {
        for (Entry<Component, NavigationItemDefinition> entry : navigationItems.entrySet()) {
            if (!(entry.getValue().getActionDefinition() instanceof PlaceChangeActionDefinition)) {
                continue;
            }
            final PlaceChangeActionDefinition definition = (PlaceChangeActionDefinition) entry.getValue().getActionDefinition();
            if (definition.getPlace().equals(place)) {
                accordion.setSelectedTab(entry.getKey());
                navigationWorkarea.setVisible(true);
                log.debug("selected tab {}", entry.getValue().getName());
                break;
            }
        }
    }
}
