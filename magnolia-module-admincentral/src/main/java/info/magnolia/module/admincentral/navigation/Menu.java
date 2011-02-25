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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.admincentral.AdminCentralModule;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.RepositoryException;

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
 * The Application accordion Menu.
 * TODO Add simple animation to make it look nicer.
 * @author fgrilli
 *
 */
public class Menu extends CustomComponent {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(Menu.class);
    private final Map<Tab, MenuItemConfiguration> menuItems = new HashMap<Tab, MenuItemConfiguration>();
    private final Map<Tab, String> menuItemKeys = new HashMap<Tab, String>();

    private Accordion accordion = new Accordion();

    /**
     * Presenter we have to inform about navigation events.
     */
    public static interface Presenter{
        void onMenuSelection(MenuItemConfiguration menuConf);
    }

    private Presenter presenter;

    public Menu(Presenter presenter) throws RepositoryException {
        this.presenter = presenter;
        setCompositionRoot(accordion);
        setSizeFull();

        final Map<String, MenuItemConfiguration> menuConfig = ((AdminCentralModule) ModuleRegistry.Factory.getInstance().getModuleInstance("admin-central")).getMenuItems();

        for (Entry<String, MenuItemConfiguration> menuItemEntry : menuConfig.entrySet()) {
            MenuItemConfiguration menuItem = menuItemEntry.getValue();
            // check permission
            if (!isMenuItemRenderable(menuItem)) {
                continue;
            }

            // register new top level menu
            addTab(menuItemEntry.getKey(), menuItem);
        }

        // register trigger for menu actions ... sucks but TabSheet doesn't support actions for tabs only for sub menu items
        accordion.addListener(new SelectedMenuItemTabChangeListener());
    }


    /**
     * The only way to add tabs to Magnolia menu - ensure he have references to all items.
     * @param menuItemKey unique menu item key. The key is used for bookmarking and IS visible to the end users ... take care
     * @param menuItem menu item configuration entry
     */
    public void addTab(String menuItemKey, MenuItemConfiguration menuItem) {
        // layout for sub menu entries
        Component subMenu = addSubMenuItemsIntoLayout(menuItem);
        Tab tab = accordion.addTab(subMenu == null ? new Label() : subMenu, getLabel(menuItem), getIcon(menuItem));
        // store tab reference
        this.menuItems.put(tab, menuItem);
        this.menuItemKeys.put(tab, menuItemKey);
    }

    /**
     * Iterates over sub menu entries and adds them to the layout.
     * @return Component with all relevant sub menu entries or null when none exists.
     */
    private Component addSubMenuItemsIntoLayout(MenuItemConfiguration menuItem) {
        if (menuItem.getMenuItems().isEmpty()) {
            return null;
        }
        final GridLayout layout = new GridLayout(1,1);
        layout.setSpacing(true);
        layout.setMargin(true);
        // sub menu items (2 levels only)
        for (MenuItemConfiguration sub :  menuItem.getMenuItems().values()) {
            if (isMenuItemRenderable(sub)) {
                layout.addComponent(new MenuItem(sub));
            }
        }

        return layout;
    }

    /**
     * Converts label key into i18n-ized string.
     */
    protected String getLabel(MenuItemConfiguration menuItem) {
        return menuItem.getMessages().getWithDefault(menuItem.getLabel(), menuItem.getLabel());
    }

    protected Resource getIcon(MenuItemConfiguration menuItem){
        if (menuItem.getIcon() == null) {
            return null;
        }
        return new ExternalResource(MgnlContext.getContextPath() + menuItem.getIcon());
    }

    /**
     * @param menuItem
     * @return <code>true</code> if the the current user is granted access to this menu item, <code>false</code> otherwise
     */
    protected boolean isMenuItemRenderable(MenuItemConfiguration menuItem) {
        return MgnlContext.getAccessManager(ContentRepository.CONFIG).isGranted(menuItem.getLocation(), Permission.READ);
    }

    /**
     * Menu item button implementation.
     * @author fgrilli
     *
     */
    public class MenuItem extends Button {

        private static final long serialVersionUID = 1L;

        private MenuItemConfiguration item;

        public MenuItem(final MenuItemConfiguration item) {
            this.item = item;
        }

        /**
         * See {@link com.vaadin.ui.AbstractComponent#getApplication()} javadoc as to why we need to do most of the initialization here and not in the constructor.
         */
        @Override
        public void attach() {
            super.attach();
            Resource icon = Menu.this.getIcon(item);
            if (icon != null) {
                setIcon(icon);
            }
            setCaption(Menu.this.getLabel(item));

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
     * @author fgrilli
     *
     */
    public class SelectedMenuItemTabChangeListener implements SelectedTabChangeListener {

        private static final long serialVersionUID = 1L;

        public void selectedTabChange(SelectedTabChangeEvent event) {
            TabSheet tabsheet = event.getTabSheet();

            Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
            if (tab != null) {
                MenuItemConfiguration menuConfig = menuItems.get(tab);
                presenter.onMenuSelection(menuConfig);
            }
        }
    }
}
