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

import javax.inject.Singleton;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;


/**
 * Implementation for {@link ActionListView}.
 * 
 * @author fgrilli
 * @author mrichert
 */
@Singleton
public class ActionListViewImpl implements ActionListView {

    private Presenter presenter;

    private CssLayout menu = new CssLayout();

    @Override
    public void show(List<MenuItemDefinition> contextMenuItems) {
        clear();
        for (MenuItemDefinition menuItem : contextMenuItems) {
            addAction(menuItem);
        }
    }

    protected void clear() {
        menu.removeAllComponents();
    }

    protected void addAction(MenuItemDefinition menuItem) {
        final String itemId = menuItem.getName();
        Button button = new Button(menuItem.getLabel());
        button.setIcon(new ExternalResource(MgnlContext.getContextPath() + menuItem.getIcon()));
        button.setStyleName("action");
        button.addStyleName("borderless");

        button.addListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                presenter.onMenuItemSelected(itemId);
            }
        });

        menu.addComponent(button);
    }

    @Override
    public void setPresenter(Presenter presenter){
        this.presenter = presenter;
    }

    @Override
    public Component asVaadinComponent() {
        return menu;
    }
}