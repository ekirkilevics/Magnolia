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
package info.magnolia.ui.admincentral.sidebar.view;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.sidebar.view.SidebarView.Presenter;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

/**
 * Implementation for {@link ActionListView}.
 *
 * @author fgrilli
 */
public class ActionListViewImpl implements ActionListView {

    private Presenter presenter;
    private Table table = new Table();

    public ActionListViewImpl() {
        table.setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
        table.addContainerProperty("Command", String.class, "");
        table.setSizeFull();
        table.setSelectable(true);
        table.addListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                if (!event.isDoubleClick()) {
                    presenter.onMenuItemSelected((String) event.getItemId());
                }
            }
        });
    }

    @Override
    public void show(List<MenuItemDefinition> contextMenuItems) {
        clear();
        for (MenuItemDefinition menuItem : contextMenuItems) {
            addAction(menuItem);
        }
    }

    protected void clear() {
        // table.removeAllItems();
    }

    protected void addAction(MenuItemDefinition menuItem) {
        Object itemId = menuItem.getName();
        table.addItem(itemId);
        Item commandItem = table.getItem(itemId);
        commandItem.getItemProperty("Command").setValue(menuItem.getLabel());
        table.setItemIcon(itemId, new ExternalResource(MgnlContext.getContextPath() + menuItem.getIcon()));
    }

    @Override
    public void setPresenter(Presenter presenter){
        this.presenter = presenter;
    }

    @Override
    public Component asVaadinComponent() {
        return table;
    }
}