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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.jouni.animator.Disclosure;

import com.vaadin.addon.chameleon.SidebarMenu;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeButton;


/**
 * A group of navigation items defined by a collection of NavigationItemDefinitions (e.g. website,
 * data). It's part of a {@link NavigationWorkArea}.
 *
 * @author fgrilli
 * @author mrichert
 */
public class NavigationGroup implements NavigationView, IsVaadinComponent {

    private static final Logger log = LoggerFactory.getLogger(NavigationGroup.class);

    private Map<Component, NavigationItemDefinition> navigationItems = new HashMap<Component, NavigationItemDefinition>();

    private SidebarMenu accordion = new SidebarMenu();

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

        accordion.setSizeUndefined();
        accordion.setWidth(100, Sizeable.UNITS_PERCENTAGE);
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
        Collection<NativeButton> subNavigation = addSubNavigationItems(navigationItemDef, permissions);

        // Label tab = new Label("<img src='"
        // + MgnlContext.getContextPath()
        // + navigationItemDef.getIcon()
        // + "'>"
        // + getLabel(navigationItemDef), Label.CONTENT_XHTML);

        NavDisclosure tab = new NavDisclosure(getLabel(navigationItemDef));
        tab.setDescription(getDescription(navigationItemDef));

        accordion.addComponent(tab);

        CssLayout layout = new CssLayout();
        for (NativeButton button : subNavigation) {
            layout.addComponent(button);
        }
        tab.setContent(layout);

        // TODO: add notification badges

        // store tab reference
        navigationItems.put(tab, navigationItemDef);
    }

    /**
     * Iterates over sub menu entries and adds them to the layout.
     * @param permissions
     * @return View with all relevant sub menu entries or null when none exists.
     */
    private Collection<NativeButton> addSubNavigationItems(NavigationItemDefinition navigationItemDef, NavigationPermissionSchema permissions) {
        List<NativeButton> list = new ArrayList<NativeButton>();

        // sub menu items (2 levels only)
        for (NavigationItemDefinition sub : navigationItemDef.getItems()) {
            if (permissions.hasPermission(sub)) {
                NavigationItem submenuItem = new NavigationItem(sub);
                list.add(submenuItem);

                // store submenu reference
                navigationItems.put(submenuItem, sub);
            }
        }

        return list;
    }

    /**
     * TODO: would it make sense to move the i18n logic to a more generic place, i.e. in the getters
     * of MenuItemDefinition so that client classes need not worry about i18n and this kind of
     * utility code is not spread all over the place? Converts label key into i18n-ized string.
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

    private class NavDisclosure extends Disclosure {

        public NavDisclosure(String caption) {
            super(caption);
            addListener(new LayoutClickListener() {

                @Override
                public void layoutClick(LayoutClickEvent event) {
                    Component clicked = event.getClickedComponent();

                    if (event.getChildComponent() != null) {
                        System.out.println("event.getChildComponent(): "
                            + event.getChildComponent().getClass()
                            + ", "
                            + event.getChildComponent().getCaption());
                    }
                    else {
                        System.out.println("event.getChildComponent(): null");
                    }
                    if (clicked != null) {
                        System.out.println("event.getClickedComponent(): " + clicked.getClass() + ", " + clicked.getCaption());
                    }
                    else {
                        System.out.println("event.getClickedComponent(): null");
                    }

                    if (clicked != null && (clicked instanceof NavDisclosure || clicked instanceof NavigationItem)) {
                        presenter.onMenuSelection(navigationItems.get(clicked));
                    }
                }
            });

        }

        public Component getCaptionComponent() {
            return caption;
        }

        @Override
        public Disclosure open() {
            super.open();
            presenter.onMenuSelection(navigationItems.get(this));
            return this;
        }

        @Override
        public Disclosure close() {
            super.close();
            presenter.onMenuSelection(navigationItems.get(this));
            return this;
        }
    }

    /**
     * Menu item button implementation.
     *
     * @author fgrilli
     * @author mrichert
     */
    public class NavigationItem extends NativeButton {

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

                // accordion.setSelectedTab(entry.getKey());
                Component key = entry.getKey();
                if (key instanceof NativeButton) {
                    accordion.setSelected((NativeButton) key);
                }

                navigationWorkarea.setVisible(true);

                log.debug("selected tab {}", entry.getValue().getName());

                break;
            }
        }
    }
}
