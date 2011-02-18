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
import info.magnolia.module.admincentral.jcr.JCRUtil;
import info.magnolia.module.admincentral.model.UIModel;
import info.magnolia.module.admincentral.tree.TreeDefinition;
import info.magnolia.module.admincentral.tree.action.TreeAction;

/**
 * XXX remove just for testing purposes.
 *
 * @author fgrilli
 */
public class DetailView extends VerticalSplitPanel {

    /**
     * Listener that is called when the user selects a command.
     */
    public interface CommandSelectedListener {
        void onCommandSelected(String commandName, String path);
    }

    private static final Logger log = LoggerFactory.getLogger(DetailView.class);
    private CommandList commandList;
    private UIModel uiModel;
    private String workspace;
    private String path;
    private CommandSelectedListener commandSelectedListener;

    public DetailView(String workspace, UIModel uiModel) {
        this.uiModel = uiModel;
        this.workspace = workspace;
        commandList = new CommandList();
        setFirstComponent(commandList);
        setSecondComponent(new DetailForm());
    }

    public void setCommandSelectedListener(CommandSelectedListener commandSelectedListener) {
        this.commandSelectedListener = commandSelectedListener;
    }

    public void showItem(String path) {
        // FIXME a very ugly hack
        try {
            if (!path.equals("/")) {

                this.path = path;

                // In reality the workspace passed to this class is the tree name
                TreeDefinition treeDefinition = uiModel.getTreeDefinition(workspace);

                Session session = JCRUtil.getSession(treeDefinition.getRepository());
                javax.jcr.Item item = session.getItem(path);
                List<TreeAction> actions = uiModel.getCommandsForItem(workspace, item);
                commandList.showCommands(actions);
            }
        } catch (RepositoryException e) {
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
            addContainerProperty("Command", String.class, "");
            setSizeFull();
            setSelectable(true);
            addListener(new ItemClickEvent.ItemClickListener() {
                public void itemClick(ItemClickEvent event) {
                    if (event.isDoubleClick() && commandSelectedListener != null) {
                        commandSelectedListener.onCommandSelected((String) event.getItemId(), path);
                    }
                }
            });
        }

        public void showCommands(List<TreeAction> actions) {
            clearCommands();
            for (TreeAction action : actions) {
                addCommand(action);
            }
        }

        public void clearCommands() {
            commandList.removeAllItems();
        }

        public void addCommand(TreeAction command) {
            Object itemId = command.getName();
            commandList.addItem(itemId);
            Item commandItem = commandList.getItem(itemId);
            commandItem.getItemProperty("Command").setValue(command.getLabel());
            commandList.setItemIcon(itemId, command.getIcon());
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
