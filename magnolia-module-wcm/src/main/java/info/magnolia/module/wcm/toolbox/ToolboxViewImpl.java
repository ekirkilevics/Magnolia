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
package info.magnolia.module.wcm.toolbox;

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

/**
 * View for the page editor toolbox.
 */
public class ToolboxViewImpl implements ToolboxView, IsVaadinComponent {

    private Table table;
    private Presenter presenter;
    private List<MenuItemDefinition> actions;

    public ToolboxViewImpl() {
        this.table = new Table();
        this.table.setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
        this.table.addContainerProperty("Command", String.class, "");
        this.table.setSizeFull();
        this.table.setSelectable(true);
        table.addListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                if (!event.isDoubleClick()) {
                    String name = (String) event.getItemId();
                    MenuItemDefinition menuItem = findMenuItemByName(name);
                    presenter.onMenuItemSelected(menuItem);
                }
            }
        });
    }

    private MenuItemDefinition findMenuItemByName(String name) {
        for (MenuItemDefinition action : actions) {
            if (action.getName().equals(name))
                return action;
        }
        return null;
    }

    @Override
    public void showRack(List<MenuItemDefinition> actions) {
        table.removeAllItems();
        this.actions = actions;
        for (MenuItemDefinition menuItem : actions) {
            Object itemId = menuItem.getName();
            table.addItem(itemId);
            Item commandItem = table.getItem(itemId);
            commandItem.getItemProperty("Command").setValue(menuItem.getLabel());
            table.setItemIcon(itemId, new ExternalResource(MgnlContext.getContextPath() + menuItem.getIcon()));
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Component asVaadinComponent() {
        return table;
    }
}
