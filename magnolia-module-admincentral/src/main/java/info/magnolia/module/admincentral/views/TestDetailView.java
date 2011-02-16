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
import com.vaadin.ui.Form;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalSplitPanel;
import info.magnolia.module.admincentral.tree.TreeSelectionChangedEvent;
import info.magnolia.module.admincentral.tree.TreeSelectionChangedEventListener;
import info.magnolia.module.admincentral.tree.TreeView;
import info.magnolia.module.admincentral.tree.action.TreeAction;
import info.magnolia.module.vaadin.event.EventBus;

/**
 * XXX remove just for testing purposes.
 *
 * @author fgrilli
 */
public class TestDetailView extends VerticalSplitPanel implements TreeSelectionChangedEventListener {

    private static final Logger log = LoggerFactory.getLogger(TestDetailView.class);
    private TreeView.Presenter presenter;

    public TestDetailView(EventBus eventBus) {
        setFirstComponent(new CommandList());
        setSecondComponent(new DetailForm());
        eventBus.addListener(this);
    }

    public CommandList getCommandList() {
        return (CommandList) super.getFirstComponent();
    }

    public void onTreeSelectionChanged(TreeSelectionChangedEvent event) {
        CommandList commandList = getCommandList();
        commandList.removeAllItems();
        List<TreeAction> actions = event.getTreeViewPresenter().getActionsForItem(event.getItem());

        presenter = event.getTreeViewPresenter();

        for (TreeAction action : actions) {
            Object itemId = action.getName();
            commandList.addItem(itemId);
            Item item = commandList.getItem(itemId);
            item.getItemProperty("Command").setValue(action.getCaption());
            commandList.setItemIcon(itemId, action.getIcon());
        }
    }

    /**
     * TODO.
     *
     * @author fgrilli
     */
    public class CommandList extends Table {
        public CommandList() {
            setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
            // create some dummy data
            addContainerProperty("Command", String.class, "");
            setSizeFull();
            setSelectable(true);
            addListener(new ItemClickEvent.ItemClickListener() {
                public void itemClick(ItemClickEvent event) {
                    if (event.isDoubleClick()) {
                        presenter.executeActionForSelectedItem((String) event.getItemId());
                    }
                }
            });
        }

        public void addCommand(Object command) {
            log.info("adding command {} to detail view", command);
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
