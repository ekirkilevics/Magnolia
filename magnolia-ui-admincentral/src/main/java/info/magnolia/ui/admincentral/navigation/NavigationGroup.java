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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;


/**
 * A group of navigation items defined by a collection of NavigationItemDefinitions (e.g. website,
 * data). It's part of a {@link NavigationWorkArea}.
 *
 * @author fgrilli
 * @author mrichert
 */
public class NavigationGroup implements NavigationView, IsVaadinComponent {

    private static final Logger log = LoggerFactory.getLogger(NavigationGroup.class);

    // private Map<Component, NavigationItemDefinition> navigationItems = new HashMap<Component,
    // NavigationItemDefinition>();

    private Tree accordion = new Tree();

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

        accordion.addListener(new ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                NavigationItemDefinition navItem = (NavigationItemDefinition) event.getItemId();
                if (navItem != null) {
                    expand(navItem);
                    select(navItem);
                    presenter.onMenuSelection(navItem);
                }
            }
        });

        accordion.addStyleName("sidebar-menu");
        accordion.setMultiSelect(false);
        accordion.setNullSelectionAllowed(false);
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
        accordion.addItem(navigationItemDef);
        accordion.setItemCaption(navigationItemDef, getLabel(navigationItemDef));
        accordion.setItemIcon(navigationItemDef, getIcon(navigationItemDef));

        for (NavigationItemDefinition item : navigationItemDef.getItems()) {
            if (permissions.hasPermission(item)) {
                accordion.addItem(item);
                accordion.setItemCaption(item, getLabel(item));
                accordion.setItemIcon(item, getIcon(item));
                accordion.setParent(item, navigationItemDef);
                accordion.setChildrenAllowed(item, false);
            }
        }

        // TODO: add notification badges
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

    @Override
    public Component asVaadinComponent() {
        return accordion;
    }

    @Override
    public void update(Place place) {
        NavigationItemDefinition selection = null;
        for (Object itemId : accordion.getItemIds()) {
            NavigationItemDefinition navItem = (NavigationItemDefinition) itemId;
            if (!(navItem.getActionDefinition() instanceof PlaceChangeActionDefinition)) {
                continue;
            }
            final PlaceChangeActionDefinition definition = (PlaceChangeActionDefinition) navItem.getActionDefinition();
            if (definition.getPlace().equals(place)) {

                selection = navItem;

                navigationWorkarea.setVisible(true);

                log.debug("selected tab {}", navItem.getName());

                if (!accordion.hasChildren(navItem)) {
                    break;
                }
            }
        }
        if (selection != null) {
            select(selection);
            expand(selection);
        }
    }

    private void select(NavigationItemDefinition navItem) {
        if (!accordion.isSelected(navItem)) {
            accordion.select(navItem);
        }
    }

    private void expand(NavigationItemDefinition navItem) {
        for (Object itemId : accordion.getItemIds()) {
            if (accordion.isRoot(itemId)) {
                if (itemId.equals(navItem) || itemId.equals(accordion.getParent(navItem))) {
                    accordion.expandItem(itemId);
                }
                else {
                    accordion.collapseItem(itemId);
                }
            }
        }
    }
}
