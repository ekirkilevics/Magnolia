/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface.trees;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.util.NodeNameComparator;
import info.magnolia.module.admininterface.AbstractTreeConfiguration;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class UsersTreeConfiguration extends AbstractTreeConfiguration {

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareTree(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request) {
        tree.setSortComparator(new NodeNameComparator());

        tree.setDrawShifter(true); // users can be in folders

        // context path is already added by Tree
        tree.setIcon(ItemType.USER.getSystemName(), Tree.ICONDOCROOT + "pawn_glass_yellow.gif"); //$NON-NLS-1$
        tree.setIconOndblclick("mgnlTreeMenuOpenDialog(" + tree.getJavascriptTree() //$NON-NLS-1$
            // + ",'.magnolia/adminCentral/users/dialog.html');");
            + ",'.magnolia/dialogs/useredit.html');"); //$NON-NLS-1$
        tree.addItemType(ItemType.USER);
        tree.addItemType(ItemType.NT_FOLDER, Tree.ICONDOCROOT + "folder.gif");

        final Messages msgs = getMessages();
        TreeColumn column0 = TreeColumn.createLabelColumn(tree, msgs.get("tree.users.name"), !browseMode);
        column0.setWidth(2);

        TreeColumn column1 = TreeColumn.createNodeDataColumn(tree, msgs.get("tree.users.fullname"), "title", !browseMode);
        column1.setWidth(2);

        TreeColumn columnIcons = TreeColumn.createActivationColumn(tree, msgs.get("tree.users.status"));

        TreeColumn column2 = TreeColumn.createMetaDataColumn(tree, msgs.get("tree.users.date"), MetaData.LAST_MODIFIED, "yyyy-MM-dd, HH:mm");
        column2.setWidth(2);

        tree.addColumn(column0);
        if (!browseMode) {
            tree.addColumn(column1);
            if (isAdminInstance() || hasAnyActiveSubscriber()) {
                tree.addColumn(columnIcons);
            }
            tree.addColumn(column2);
        }
    }

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareContextMenu(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareContextMenu(Tree tree, boolean browseMode, HttpServletRequest request) {
        final Messages msgs = getMessages();

        // might be usefull ;-)
        /*
        ContextMenuItem menuNewFolder = new ContextMenuItem("newFolder");
        menuNewFolder.setLabel(msgs.get("tree.users.menu.newFolder")); //$NON-NLS-1$
        menuNewFolder.setIcon(request.getContextPath() + "/.resources/icons/16/folder_add.gif"); //$NON-NLS-1$
        menuNewFolder.setOnclick(tree.getJavascriptTree() + ".createNode('"
            + ItemType.NT_FOLDER + "');"); //$NON-NLS-1$
        */

        ContextMenuItem menuOpen = new ContextMenuItem("edit");
        menuOpen.setLabel(msgs.get("tree.users.menu.edit")); //$NON-NLS-1$
        menuOpen.setIcon(request.getContextPath() + "/.resources/icons/16/pawn_glass_yellow.gif"); //$NON-NLS-1$
        menuOpen.setOnclick("mgnlTreeMenuOpenDialog(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ",'.magnolia/dialogs/useredit.html');"); //$NON-NLS-1$
        menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuNew = new ContextMenuItem("new");
        menuNew.setLabel(msgs.get("tree.users.menu.new")); //$NON-NLS-1$
        menuNew.setIcon(request.getContextPath() + "/.resources/icons/16/pawn_glass_yellow_add.gif"); //$NON-NLS-1$
        menuNew.setOnclick(tree.getJavascriptTree() + ".createNode('" //$NON-NLS-1$
            + ItemType.USER.getSystemName()
            + "');"); //$NON-NLS-1$
        menuNew.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotItemType(" //$NON-NLS-1$
                + tree.getJavascriptTree() + ","
                + "'" + ItemType.USER.getSystemName() + "'" 
                + ")"); //$NON-NLS-1$

        ContextMenuItem menuDelete = new ContextMenuItem("delete");
        menuDelete.setLabel(msgs.get("tree.users.menu.delete")); //$NON-NLS-1$
        menuDelete.setIcon(request.getContextPath() + "/.resources/icons/16/delete2.gif"); //$NON-NLS-1$
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();"); //$NON-NLS-1$
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuMove = new ContextMenuItem("move");
        menuMove.setLabel(msgs.get("tree.users.menu.move")); //$NON-NLS-1$
        menuMove.setIcon(request.getContextPath() + "/.resources/icons/16/up_down.gif"); //$NON-NLS-1$
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();"); //$NON-NLS-1$
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuCopy = new ContextMenuItem("copy");
        menuCopy.setLabel(msgs.get("tree.users.menu.copy")); //$NON-NLS-1$
        menuCopy.setIcon(request.getContextPath() + "/.resources/icons/16/copy.gif"); //$NON-NLS-1$
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();"); //$NON-NLS-1$
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuActivateExcl = new ContextMenuItem("activate");
        menuActivateExcl.setLabel(msgs.get("tree.users.menu.activate")); //$NON-NLS-1$
        menuActivateExcl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green.gif"); //$NON-NLS-1$
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuDeactivate = new ContextMenuItem("deactivate");
        menuDeactivate.setLabel(msgs.get("tree.users.menu.deactivate")); //$NON-NLS-1$
        menuDeactivate.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_left_red.gif"); //$NON-NLS-1$
        menuDeactivate.setOnclick(tree.getJavascriptTree() + ".deactivateNode(" + Tree.ACTION_DEACTIVATE + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuDeactivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        if (!hasAnyActiveSubscriber()) {
            menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuDeactivate.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        if (!browseMode) {
            // tree.addMenuItem(menuNewFolder);
            tree.addMenuItem(menuOpen);
            tree.addMenuItem(menuNew);
            tree.addMenuItem(null); // line
            tree.addMenuItem(menuDelete);

            tree.addMenuItem(null); // line
            tree.addMenuItem(menuActivateExcl);
            tree.addMenuItem(menuDeactivate);
        }
        else {
            tree.addMenuItem(ContextMenuItem.getRefreshMenuItem(tree, msgs, request));
        }
    }

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareFunctionBar(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareFunctionBar(Tree tree, boolean browseMode, HttpServletRequest request) {
        tree.addFunctionBarItemFromContextMenu("edit");
        tree.addFunctionBarItemFromContextMenu("new");
        tree.addFunctionBarItem(null);
        tree.addFunctionBarItemFromContextMenu("activate");
        tree.addFunctionBarItemFromContextMenu("deactivate");
        tree.addFunctionBarItem(null);

        tree.addFunctionBarItem(FunctionBarItem.getRefreshFunctionBarItem(tree, getMessages(), request));
    }

}
