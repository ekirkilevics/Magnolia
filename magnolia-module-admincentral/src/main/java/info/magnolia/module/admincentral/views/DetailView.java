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

import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.model.UIModel;
import info.magnolia.module.admincentral.tree.action.TreeAction;

import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalSplitPanel;

/**
 * XXX remove just for testing purposes.
 *
 * @author fgrilli
 */
public class DetailView extends VerticalSplitPanel {

    private static final Logger log = LoggerFactory.getLogger(DetailView.class);
    private CommandList commandList;
    private UIModel uiModel;
    private String workspace;

    public DetailView(String workspace, UIModel uiModel) {
        this.uiModel = uiModel;
        this.workspace = workspace;
        commandList = new CommandList();
        setFirstComponent(commandList);
        setSecondComponent(new DetailForm());
    }

    public void showItem(String path) {
        // FIXME a very ugly hack
        try {
            Session session = MgnlContext.getHierarchyManager(workspace).getWorkspace().getSession();
            javax.jcr.Item item = session.getItem(path);
            commandList.showCommandsFor(item);
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
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
                        System.out.println("should tell the presenter which will get that action and execute it");
                    }
                }
            });
        }

        public void showCommandsFor(javax.jcr.Item item) {
            commandList.removeAllItems();
            List<TreeAction> actions = uiModel.getActionsForItem(workspace, item);

            for (TreeAction action : actions) {
                Object itemId = action.getName();
                commandList.addItem(itemId);
                Item commandItem = commandList.getItem(itemId);
                commandItem.getItemProperty("Command").setValue(action.getCaption());
                commandList.setItemIcon(itemId, action.getIcon());
            }

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
