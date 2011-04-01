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
import info.magnolia.ui.admincentral.workbench.place.WorkbenchPlace;
import info.magnolia.ui.framework.place.Place;
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
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.themes.BaseTheme;


/**
 * A group of navigation items defined by a collection of NavigationItemDefinitions (e.g. website, data). It's part of a {@link NavigationWorkArea}.
 *
 * @author fgrilli
 */
public class NavigationGroup extends CustomComponent implements NavigationView, IsVaadinComponent{


    private static final Logger log = LoggerFactory.getLogger(NavigationGroup.class);

    private final Map<Tab, NavigationItemDefinition> navigationItems = new HashMap<Tab, NavigationItemDefinition>();
    private Accordion accordion = new Accordion();
    private Collection<NavigationItemDefinition> navigationItemDefs;
    private NavigationPermissionSchema permissions;
    private Presenter presenter;
    private NavigationWorkArea navigationWorkarea;

    public NavigationGroup(Collection<NavigationItemDefinition> navigationItemDefs, NavigationPermissionSchema permissions) {
        setCompositionRoot(accordion);
        setSizeFull();
        this.navigationItemDefs = navigationItemDefs;
        this.permissions = permissions;

        for(NavigationItemDefinition navigationItemDef:  this.navigationItemDefs) {
            if(this.permissions.hasPermission(navigationItemDef)){
                // register new top level menu
                addTab(navigationItemDef, this.permissions);
            }
        }

        // register trigger for menu actions ... sucks but TabSheet doesn't support actions for tabs only for sub menu items
        accordion.addListener(new SelectedNavigationItemTabChangeListener());
    }

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
        Tab tab = accordion.addTab(subNavigation == null ? new Label() : subNavigation, getLabel(navigationItemDef), getIcon(navigationItemDef));
        // store tab reference
        this.navigationItems.put(tab, navigationItemDef);
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
        final GridLayout layout = new GridLayout(1,1);
        layout.setSpacing(true);
        layout.setMargin(true);
        // sub menu items (2 levels only)
        for (NavigationItemDefinition sub :  navigationItemDef.getItems()) {
            if (permissions.hasPermission(sub)) {
                layout.addComponent(new NavigationItem(sub));
            }
        }

        return layout;
    }

    /**
     * Converts label key into i18n-ized string.
     */
    protected String getLabel(NavigationItemDefinition menuItem) {
        return  MessagesUtil.getWithDefault(menuItem.getLabel(), menuItem.getLabel(), menuItem.getI18nBasename());
    }

    protected Resource getIcon(NavigationItemDefinition menuItem){
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
         * See {@link com.vaadin.ui.AbstractComponent#getApplication()} javadoc as to why we need to do most of the initialization here and not in the constructor.
         */
        @Override
        public void attach() {
            super.attach();
            Resource icon = NavigationGroup.this.getIcon(item);
            if (icon != null) {
                setIcon(icon);
            }
            setCaption(NavigationGroup.this.getLabel(item));
            setStyleName(BaseTheme.BUTTON_LINK);
            setHeight(20f, Button.UNITS_PIXELS);

            this.addListener(new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    presenter.onMenuSelection(item);
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

        public void selectedTabChange(SelectedTabChangeEvent event) {
            TabSheet tabsheet = event.getTabSheet();
            Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());

            if (tab != null) {
                NavigationItemDefinition menuConfig = navigationItems.get(tab);
                presenter.onMenuSelection(menuConfig);
            }
        }
    }

    public Component asVaadinComponent() {
        return this;
    }

    public void update(Place place) {
        if(!(place instanceof WorkbenchPlace)){
            return;
        }
        WorkbenchPlace currentPlace = (WorkbenchPlace) place;
        String workbenchName = currentPlace.getWorkbenchName();
        for(Entry<Tab, NavigationItemDefinition> entry:navigationItems.entrySet()){
            if(entry.getValue().getName().equals(workbenchName)){
                accordion.setSelectedTab(entry.getKey().getComponent());
                navigationWorkarea.setVisible(true);
                log.debug("selected tab {}", entry.getValue().getName());
                break;
            }
        }
    }
}
