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
package info.magnolia.module.admincentral.views;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Form;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalSplitPanel;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.tree.action.Command;

/**
 * Displays commands and details about the currently selected item.
 *
 * @author fgrilli
 * @author tmattsson
 */
public class DetailView extends VerticalSplitPanel {

    /**
     * Presenter that is called when the user selects a command.
     */
    public interface Presenter {
        void onCommandSelected(String commandName);
    }

    private static final Logger log = LoggerFactory.getLogger(DetailView.class);
    private CommandList commandList;
    private Presenter presenter;

    public DetailView(Presenter presenter) {
        this.presenter = presenter;
        commandList = new CommandList();
        setFirstComponent(commandList);
        setSecondComponent(new DetailForm());
    }

    public void showCommands(List<Command> commands) {
        commandList.showCommands(commands);
    }

    /**
     * TODO.
     *
     * @author fgrilli
     */
    public class CommandList extends Table {

        public CommandList() {
            setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
            addContainerProperty("Command", String.class, "");
            setSizeFull();
            setSelectable(true);
            addListener(new ItemClickEvent.ItemClickListener() {
                public void itemClick(ItemClickEvent event) {
                    if (event.isDoubleClick() && presenter != null) {
                        presenter.onCommandSelected((String) event.getItemId());
                    }
                }
            });
        }

        public void showCommands(List<Command> commands) {
            clearCommands();
            for (Command command : commands) {
                addCommand(command);
            }
        }

        public void clearCommands() {
            commandList.removeAllItems();
        }

        public void addCommand(Command command) {
            Object itemId = command.getName();
            commandList.addItem(itemId);
            Item commandItem = commandList.getItem(itemId);
            commandItem.getItemProperty("Command").setValue(command.getLabel());
            commandList.setItemIcon(itemId, new ExternalResource(MgnlContext.getContextPath() + command.getIcon()));
            log.info("Added command {} to detail view", command);
        }
    }

    /**
     * TODO.
     *
     * @author fgrilli
     */
    public class DetailForm extends Form {
        public DetailForm() {
            addField("Some prop", new TextField("Some value"));
            addField("Another prop", new TextField("Another value"));
        }
    }
}
