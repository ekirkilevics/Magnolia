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

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
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

    // FIXME: When you click in one group to navigate somewhere, only that group gets updated:
    // 1. Select an item in one NavigationGroup.
    // 2. Select one in another NavigationGroup.
    // 3. Now you have a selected item in each group.
    // FIXME: Move UI logic to NavigationWorkArea/Melodion. Let NavigationGroup be just a data
    // structure.
    private Melodion melodion = new Melodion();

    private Collection<NavigationItemDefinition> navigationItemDefs;

    private NavigationPermissionSchema permissions;

    private Presenter presenter;

    private Map<Component, NavigationItemDefinition> navigationItems = new HashMap<Component, NavigationItemDefinition>();

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

        melodion.setSizeUndefined();
        melodion.setWidth(100, Sizeable.UNITS_PERCENTAGE);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
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
        Tab tab = melodion.addTab(label);
        navigationItems.put(tab, item);

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
                navigationItems.put(button, subItem);
            }
        }

        // TODO: add notification badges
    }

    // TODO: Would it make sense to move the i18n logic to a more generic place?
    // For example, in the getters of MenuItemDefinition so that client classes need not worry about
    // i18n and this kind of utility code is not spread all over the place?
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
            Resource icon = NavigationGroup.this.getIcon(item);
            if (icon != null) {
                setIcon(icon);
            }
            setCaption(getLabel(item));

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

    @Override
    public Component asVaadinComponent() {
        return melodion;
    }

    @Override
    public void update(Place place) {
        for (Entry<Component, NavigationItemDefinition> entry : navigationItems.entrySet()) {
            if (!(entry.getValue().getActionDefinition() instanceof PlaceChangeActionDefinition)) {
                continue;
            }
            final PlaceChangeActionDefinition definition = (PlaceChangeActionDefinition) entry.getValue().getActionDefinition();
            if (definition.getPlace().equals(place)) {
                Component c = entry.getKey();
                melodion.setSelected(c);
                if (c instanceof Melodion.Tab) {
                    ((Melodion.Tab) c).open();
                }

                navigationWorkarea.setVisible(true);

                log.debug("selected tab {}", entry.getValue().getName());

                break;
            }
        }
    }
}
