/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.admininterface.trees;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.module.admininterface.AbstractTreeConfiguration;

import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ConfigTreeConfiguration extends AbstractTreeConfiguration {

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareTree(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request) {
        final Messages msgs = getMessages();

        tree.setIconPage(Tree.ICONDOCROOT + "folder_cubes.gif"); //$NON-NLS-1$

        tree.addItemType(ItemType.CONTENT.getSystemName());
        tree.addItemType(ItemType.CONTENTNODE.getSystemName());
        tree.addItemType(Tree.ITEM_TYPE_NODEDATA);
        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setHtmlEdit();
        column0.setIsLabel(true);
        column0.setWidth(3);

        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName(StringUtils.EMPTY);
        column1.setTitle(msgs.get("tree.config.value")); //$NON-NLS-1$
        column1.setIsNodeDataValue(true);
        column1.setWidth(3);
        column1.setHtmlEdit();

        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName(StringUtils.EMPTY);
        column2.setTitle(msgs.get("tree.config.type")); //$NON-NLS-1$
        column2.setIsNodeDataType(true);
        column2.setWidth(1);
        Select typeSelect = new Select();
        typeSelect.setName(tree.getJavascriptTree() + TreeColumn.EDIT_NAMEADDITION);
        typeSelect.setSaveInfo(false);
        typeSelect.setCssClass(TreeColumn.EDIT_CSSCLASS_SELECT);
        typeSelect.setEvent(
            "onblur", tree.getJavascriptTree() + ".saveNodeData(this.value,this.options[this.selectedIndex].text)"); //$NON-NLS-1$
        typeSelect.setOptions(PropertyType.TYPENAME_STRING, Integer.toString(PropertyType.STRING));
        typeSelect.setOptions(PropertyType.TYPENAME_BOOLEAN, Integer.toString(PropertyType.BOOLEAN));
        typeSelect.setOptions(PropertyType.TYPENAME_LONG, Integer.toString(PropertyType.LONG));
        typeSelect.setOptions(PropertyType.TYPENAME_DOUBLE, Integer.toString(PropertyType.DOUBLE));
        // todo: typeSelect.setOptions(PropertyType.TYPENAME_DATE,Integer.toString(PropertyType.DATE));
        column2.setHtmlEdit(typeSelect.getHtml());

        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass(StringUtils.EMPTY);
        columnIcons.setTitle(msgs.get("tree.config.status")); //$NON-NLS-1$
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        columnIcons.setIconsPermission(true);

        TreeColumn column4 = new TreeColumn(tree.getJavascriptTree(), request);
        column4.setName(MetaData.LAST_MODIFIED);
        column4.setIsMeta(true);
        column4.setDateFormat("yy-MM-dd, HH:mm"); //$NON-NLS-1$
        column4.setWidth(2);
        column4.setTitle(msgs.get("tree.config.date")); //$NON-NLS-1$

        tree.addColumn(column0);

        if (!browseMode) {
            tree.addColumn(column1);
            tree.addColumn(column2);

            if (isAdminInstance() || hasAnyActiveSubscriber()) {
                tree.addColumn(columnIcons);
            }
            tree.addColumn(column4);
        }
    }

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareContextMenu(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareContextMenu(Tree tree, boolean browseMode, HttpServletRequest request) {
        final Messages msgs = getMessages();

        ContextMenuItem menuNewPage = new ContextMenuItem("newFolder");
        menuNewPage.setLabel(msgs.get("tree.config.menu.newFolder")); //$NON-NLS-1$
        menuNewPage.setIcon(request.getContextPath() + "/.resources/icons/16/folder_cubes.gif"); //$NON-NLS-1$
        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.CONTENT.getSystemName() + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContentNode(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuNewContentNode = new ContextMenuItem("newNode");
        menuNewContentNode.setLabel(msgs.get("tree.config.menu.newNode")); //$NON-NLS-1$
        menuNewContentNode.setIcon(request.getContextPath() + "/.resources/icons/16/cubes.gif"); //$NON-NLS-1$
        menuNewContentNode.setOnclick(tree.getJavascriptTree() + ".createNode('" //$NON-NLS-1$
            + ItemType.CONTENTNODE.getSystemName()
            + "');"); //$NON-NLS-1$
        menuNewContentNode.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuNewNodeData = new ContextMenuItem("newData");
        menuNewNodeData.setLabel(msgs.get("tree.config.menu.newNodeData")); //$NON-NLS-1$
        menuNewNodeData.setIcon(request.getContextPath() + "/.resources/icons/16/cube_green.gif"); //$NON-NLS-1$
        menuNewNodeData.setOnclick(tree.getJavascriptTree() + ".createNode('" + Tree.ITEM_TYPE_NODEDATA + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        menuNewNodeData.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuDelete = new ContextMenuItem("delete");
        menuDelete.setLabel(msgs.get("tree.config.menu.delete")); //$NON-NLS-1$
        menuDelete.setIcon(request.getContextPath() + "/.resources/icons/16/delete2.gif"); //$NON-NLS-1$
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();"); //$NON-NLS-1$
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuCopy = new ContextMenuItem("copy");
        menuCopy.setLabel(msgs.get("tree.config.menu.copy")); //$NON-NLS-1$
        menuCopy.setIcon(request.getContextPath() + "/.resources/icons/16/copy.gif"); //$NON-NLS-1$
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();"); //$NON-NLS-1$

        ContextMenuItem menuCut = new ContextMenuItem("move");
        menuCut.setLabel(msgs.get("tree.config.menu.move")); //$NON-NLS-1$
        menuCut.setIcon(request.getContextPath() + "/.resources/icons/16/up_down.gif"); //$NON-NLS-1$
        menuCut
            .addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" + tree.getJavascriptTree() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        menuCut.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuCut.setOnclick(tree.getJavascriptTree() + ".cutNode();"); //$NON-NLS-1$

        ContextMenuItem menuActivateExcl = new ContextMenuItem("activate");
        menuActivateExcl.setLabel(msgs.get("tree.config.menu.activate")); //$NON-NLS-1$
        menuActivateExcl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green.gif"); //$NON-NLS-1$
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuActivateIncl = new ContextMenuItem("activateInclSubs");
        menuActivateIncl.setLabel(msgs.get("tree.config.menu.activateInclSubs")); //$NON-NLS-1$
        menuActivateIncl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green_double.gif"); //$NON-NLS-1$
        menuActivateIncl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",true);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuDeactivate = new ContextMenuItem("deactivate");
        menuDeactivate.setLabel(msgs.get("tree.config.menu.deactivate")); //$NON-NLS-1$
        menuDeactivate.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_left_red.gif"); //$NON-NLS-1$
        menuDeactivate.setOnclick(tree.getJavascriptTree() + ".deactivateNode(" + Tree.ACTION_DEACTIVATE + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuDeactivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuDeactivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuExport = new ContextMenuItem("export");
        menuExport.setLabel(msgs.get("tree.menu.export")); //$NON-NLS-1$
        menuExport.setIcon(request.getContextPath() + "/.resources/icons/16/export1.gif"); //$NON-NLS-1$
        menuExport.setOnclick(tree.getJavascriptTree() + ".exportNode();"); //$NON-NLS-1$
        menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuImport = new ContextMenuItem("import");
        menuImport.setLabel(msgs.get("tree.menu.import")); //$NON-NLS-1$
        menuImport.setIcon(request.getContextPath() + "/.resources/icons/16/import2.gif"); //$NON-NLS-1$
        menuImport.setOnclick(tree.getJavascriptTree() + ".importNode(this);"); //$NON-NLS-1$
        menuImport.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        // is it possible to activate?
        if (!hasAnyActiveSubscriber()) {
            menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuDeactivate.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        menuImport.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        if (!browseMode) {
            tree.addMenuItem(menuNewPage);
            tree.addMenuItem(menuNewContentNode);
            tree.addMenuItem(menuNewNodeData);

            tree.addSeparator();
            tree.addMenuItem(menuDelete);

            tree.addSeparator();
            tree.addMenuItem(menuCut);
            tree.addMenuItem(menuCopy);

            tree.addSeparator();
            tree.addMenuItem(menuActivateExcl);
            tree.addMenuItem(menuActivateIncl);
            tree.addMenuItem(menuDeactivate);

            tree.addSeparator();
            tree.addMenuItem(menuExport);
            tree.addMenuItem(menuImport);
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
        final Messages msgs = getMessages();
        tree.addFunctionBarItemFromContextMenu("newFolder");
        tree.addFunctionBarItemFromContextMenu("newNode");
        tree.addFunctionBarItemFromContextMenu("newData");
        // null is separator :)
        tree.addFunctionBarItem(null);
        tree.addFunctionBarItemFromContextMenu("activate");
        tree.addFunctionBarItemFromContextMenu("deactivate");
        // null is separator :)
        tree.addFunctionBarItem(null);

        tree.addFunctionBarItem(FunctionBarItem.getRefreshFunctionBarItem(tree, msgs, request));
    }

}
