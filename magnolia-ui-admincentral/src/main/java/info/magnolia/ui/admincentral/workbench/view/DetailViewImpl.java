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
package info.magnolia.ui.admincentral.workbench.view;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalSplitPanel;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

/**
 * Displays commands and details about the currently selected item.
 *
 * @author fgrilli
 * @author tmattsson
 */
// FIXME don't extend CustomComponent, make it composite.
public class DetailViewImpl extends VerticalSplitPanel implements IsVaadinComponent, DetailView {

    private static final Logger log = LoggerFactory.getLogger(DetailViewImpl.class);
    private ActionList actionList;
    private Presenter presenter;

    public DetailViewImpl(Presenter presenter) {
        this.presenter = presenter;
        actionList = new ActionList();
        setSizeFull();

        setFirstComponent(actionList);
        setSecondComponent(new DetailForm());
    }

    public void showActions(List<MenuItemDefinition> contextMenuItems) {
        actionList.showActions(contextMenuItems);
    }

    /**
     * UI component that displays a list of actions available for the selected tree item.
     *
     * @author fgrilli
     */
    public class ActionList extends Table {

        public ActionList() {
            setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
            addContainerProperty("Command", String.class, "");
            setSizeFull();
            setSelectable(true);
            addListener(new ItemClickEvent.ItemClickListener() {

                public void itemClick(ItemClickEvent event) {
                    if (presenter != null) {
                        presenter.onCommandSelected((String) event.getItemId());
                    }
                }
            });
        }

        public void showActions(List<MenuItemDefinition> contextMenuItems) {
            clearCommands();
            for (MenuItemDefinition menuItem : contextMenuItems) {
                addAction(menuItem);
            }
        }

        public void clearCommands() {
            actionList.removeAllItems();
        }

        public void addAction(MenuItemDefinition menuItem) {
            Object itemId = menuItem.getName();
            actionList.addItem(itemId);
            Item commandItem = actionList.getItem(itemId);
            commandItem.getItemProperty("Command").setValue(menuItem.getLabel());
            actionList.setItemIcon(itemId, new ExternalResource(MgnlContext.getContextPath() + menuItem.getIcon()));
            log.debug("Added command {} to detail view", menuItem);
        }
    }

    /**
     * UI component that displays details about the selected tree item.
     *
     * @author fgrilli
     */
    public class DetailForm extends Form {

        public DetailForm() {
            addField("Some prop", new TextField("Some value"));
            addField("Another prop", new TextField("Another value"));
        }
    }

    public Component asVaadinComponent() {
        return this;
    }
}
