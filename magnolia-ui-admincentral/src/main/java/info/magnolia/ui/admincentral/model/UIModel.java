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
package info.magnolia.ui.admincentral.model;

import info.magnolia.ui.admincentral.AdminCentralModule;
import info.magnolia.ui.admincentral.dialog.definition.DialogDefinition;
import info.magnolia.ui.admincentral.dialog.registry.DialogRegistry;
import info.magnolia.ui.admincentral.jcr.JCRUtil;
import info.magnolia.ui.admincentral.navigation.NavigationItemConfiguration;
import info.magnolia.ui.admincentral.tree.MenuItem;
import info.magnolia.ui.admincentral.tree.TreeDefinition;
import info.magnolia.ui.admincentral.tree.TreeRegistry;
import info.magnolia.ui.admincentral.tree.action.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * The UI model provides all the definition for the trees, dialogs, commands and so on.
 */
public class UIModel {

    private DialogRegistry dialogRegistry;
    private AdminCentralModule adminCentralModule;
    private TreeRegistry treeRegistry;

    public UIModel(DialogRegistry dialogRegistry, AdminCentralModule adminCentralModule, TreeRegistry treeRegistry) {
        this.dialogRegistry = dialogRegistry;
        this.adminCentralModule = adminCentralModule;
        this.treeRegistry = treeRegistry;
    }

    public void executeCommand(String commandName, String treeName, String path) throws RepositoryException {

        TreeDefinition treeDefinition = getTreeDefinition(treeName);

        Item item = getItem(treeDefinition, path);

        Command action = getCommand(treeDefinition, commandName);
        if (action.isAvailable(item))
            action.execute(null, item);
    }

    public Command getCommand(TreeDefinition treeDefinition, String commandName) {
        for (MenuItem mi : treeDefinition.getContextMenuItems()) {
            Command command = mi.getCommand();
            if (mi.getName().equals(commandName)) {
                return command;
            }
        }
        return null;
    }

    public List<Command> getCommandsForItem(String treeName, String path) throws RepositoryException {
        TreeDefinition treeDefinition = getTreeDefinition(treeName);
        return getCommandsForItem(treeName, getItem(treeDefinition, path));
    }

    public List<Command> getCommandsForItem(String treeName, Item item) {
        TreeDefinition treeDefinition = getTreeDefinition(treeName);
        return getCommandsForItem(treeDefinition, item);
    }

    private List<Command> getCommandsForItem(TreeDefinition treeDefinition, Item item) {

        // For now the commands are configured directly on the context menu configuration with the tree. Should be
        // configured in a more global scope and the context menu should only refer to them by name.

        List<Command> commands = new ArrayList<Command>();
        for (MenuItem mi : treeDefinition.getContextMenuItems()) {
            Command command = mi.getCommand();
            if (command.isAvailable(item))
                commands.add(command);
        }
        return commands;
    }

    public TreeDefinition getTreeDefinition(String treeName) {
        try {
            return treeRegistry.getTree(treeName);
        } catch (RepositoryException e) {
            throw new IllegalStateException(e);
        }
    }

    public Item getItem(String treeName, String path) throws RepositoryException {
        return getItem(getTreeDefinition(treeName), path);
    }

    public Item getItem(TreeDefinition treeDefinition, String path) throws RepositoryException {
        String base = treeDefinition.getPath();
        if (!base.equals("/"))
            path = base + path;
        return JCRUtil.getSession(treeDefinition.getRepository()).getItem(path);
    }

    public DialogDefinition getDialogDefinition(String dialogName) throws RepositoryException {
        return dialogRegistry.getDialog(dialogName);
    }

    public String getPathInTree(String treeName, Item item) throws RepositoryException {
        TreeDefinition treeDefinition = getTreeDefinition(treeName);
        String base = treeDefinition.getPath();
        if (base.equals("/"))
            return item.getPath();
        else
            return StringUtils.substringAfter(item.getPath(), base);
    }

    public Map<String, NavigationItemConfiguration> getMenuDefinition(){
        return adminCentralModule.getMenuItems();
    }

}
