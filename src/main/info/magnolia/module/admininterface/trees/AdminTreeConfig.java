/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.trees;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.gui.misc.Icon;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * Handles the tree rendering for the "config" repository.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class AdminTreeConfig extends AdminTreeMVCHandler {

    /**
     * @param name
     * @param request
     * @param response
     */
    public AdminTreeConfig(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    protected void prepareTree(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages(request);

        tree.setIconPage(Tree.ICONDOCROOT + "folder_cubes.gif"); //$NON-NLS-1$

        tree.addItemType(ItemType.CONTENT.getSystemName());
        tree.addItemType(ItemType.CONTENTNODE.getSystemName());
        tree.addItemType(ItemType.NT_NODEDATA);
        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setWidth(1);
        column0.setHtmlEdit();
        column0.setIsLabel(true);
        column0.setWidth(3);
        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName(StringUtils.EMPTY);
        column1.setTitle(msgs.get("tree.config.value")); //$NON-NLS-1$
        column1.setIsNodeDataValue(true);
        column1.setWidth(2);
        column1.setHtmlEdit();
        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName(StringUtils.EMPTY);
        column2.setTitle(msgs.get("tree.config.type")); //$NON-NLS-1$
        column2.setIsNodeDataType(true);
        column2.setWidth(2);
        Select typeSelect = new Select();
        typeSelect.setName(tree.getJavascriptTree() + TreeColumn.EDIT_NAMEADDITION);
        typeSelect.setSaveInfo(false);
        typeSelect.setCssClass(TreeColumn.EDIT_CSSCLASS_SELECT);
        typeSelect.setEvent("onblur", tree.getJavascriptTree() + TreeColumn.EDIT_JSSAVE); //$NON-NLS-1$
        typeSelect.setOptions(PropertyType.TYPENAME_STRING, Integer.toString(PropertyType.STRING));
        typeSelect.setOptions(PropertyType.TYPENAME_BOOLEAN, Integer.toString(PropertyType.BOOLEAN));
        typeSelect.setOptions(PropertyType.TYPENAME_LONG, Integer.toString(PropertyType.LONG));
        typeSelect.setOptions(PropertyType.TYPENAME_DOUBLE, Integer.toString(PropertyType.DOUBLE));
        // todo: typeSelect.setOptions(PropertyType.TYPENAME_DATE,Integer.toString(PropertyType.DATE));
        column2.setHtmlEdit(typeSelect.getHtml());
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass(StringUtils.EMPTY);
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

        if (!this.isBrowseMode()) {
            tree.addColumn(column1);
            tree.addColumn(column2);
            if (Server.isAdmin() || Subscriber.isSubscribersEnabled()) {
                tree.addColumn(columnIcons);
            }
            tree.addColumn(column4);
        }
    }

    /**
     * @param tree
     * @param request
     */
    protected void prepareContextMenu(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages(request);

        ContextMenuItem menuNewPage = new ContextMenuItem();
        menuNewPage.setLabel("<img src=\"" //$NON-NLS-1$
            + request.getContextPath()
            + new Icon().getSrc(Icon.PAGE, Icon.SIZE_SMALL)
            + "\"> <span style=\"position:relative;top:-3px;\">" //$NON-NLS-1$
            + msgs.get("tree.config.menu.newFolder") //$NON-NLS-1$
            + "</span>"); //$NON-NLS-1$
        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.CONTENT.getSystemName() + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContentNode(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuNewContentNode = new ContextMenuItem();
        menuNewContentNode.setLabel("<img src=\"" //$NON-NLS-1$
            + request.getContextPath()
            + new Icon().getSrc(Icon.CONTENTNODE, Icon.SIZE_SMALL)
            + "\"> <span style=\"position:relative;top:-3px\">" //$NON-NLS-1$
            + msgs.get("tree.config.menu.newNode") //$NON-NLS-1$
            + "</span>"); //$NON-NLS-1$
        menuNewContentNode.setOnclick(tree.getJavascriptTree() + ".createNode('" //$NON-NLS-1$
            + ItemType.CONTENTNODE.getSystemName() + "');"); //$NON-NLS-1$
        menuNewContentNode.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuNewNodeData = new ContextMenuItem();
        menuNewNodeData.setLabel("<img src=\"" //$NON-NLS-1$
            + request.getContextPath()
            + new Icon().getSrc(Icon.NODEDATA, Icon.SIZE_SMALL)
            + "\"> <span style=\"position:relative;top:-3px;\">" //$NON-NLS-1$
            + msgs.get("tree.config.menu.newNodeData") //$NON-NLS-1$
            + "</span>"); //$NON-NLS-1$
        menuNewNodeData.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.NT_NODEDATA + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        menuNewNodeData.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuDelete = new ContextMenuItem();
        menuDelete.setLabel(msgs.get("tree.config.menu.delete")); //$NON-NLS-1$
        menuDelete.setIcon(request.getContextPath() + "/admindocroot/icons/16/delete2.gif"); //$NON-NLS-1$
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();"); //$NON-NLS-1$
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuCopy = new ContextMenuItem();
        menuCopy.setLabel(msgs.get("tree.config.menu.copy")); //$NON-NLS-1$
        menuCopy.setIcon(request.getContextPath() + "/admindocroot/icons/16/copy.gif"); //$NON-NLS-1$
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();"); //$NON-NLS-1$

        ContextMenuItem menuCut = new ContextMenuItem();
        menuCut.setLabel(msgs.get("tree.config.menu.move")); //$NON-NLS-1$
        menuCut.setIcon(request.getContextPath() + "/admindocroot/icons/16/up_down.gif"); //$NON-NLS-1$
        menuCut
            .addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" + tree.getJavascriptTree() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        menuCut.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuCut.setOnclick(tree.getJavascriptTree() + ".cutNode();"); //$NON-NLS-1$

        ContextMenuItem menuActivateExcl = new ContextMenuItem();
        menuActivateExcl.setLabel(msgs.get("tree.config.menu.activate")); //$NON-NLS-1$
        menuActivateExcl.setIcon(request.getContextPath() + "/admindocroot/icons/16/arrow_right_green.gif"); //$NON-NLS-1$
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuActivateIncl = new ContextMenuItem();
        menuActivateIncl.setLabel(msgs.get("tree.config.menu.activateInclSubs")); //$NON-NLS-1$
        menuActivateIncl.setIcon(request.getContextPath() + "/admindocroot/icons/16/arrow_right_green.gif"); //$NON-NLS-1$
        menuActivateIncl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",true);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuDeActivate = new ContextMenuItem();
        menuDeActivate.setLabel(msgs.get("tree.config.menu.deactivate")); //$NON-NLS-1$
        menuDeActivate.setIcon(request.getContextPath() + "/admindocroot/icons/16/arrow_left_red.gif"); //$NON-NLS-1$
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuRefresh = new ContextMenuItem();
        menuRefresh.setLabel(msgs.get("tree.menu.refresh")); //$NON-NLS-1$
        menuRefresh.setIcon(request.getContextPath() + "/admindocroot/icons/16/refresh.gif"); //$NON-NLS-1$
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$

        ContextMenuItem menuExport = new ContextMenuItem();
        menuExport.setLabel(msgs.get("tree.menu.export")); //$NON-NLS-1$
        menuExport.setIcon(request.getContextPath() + "/admindocroot/icons/16/export.gif"); //$NON-NLS-1$
        menuExport.setOnclick(tree.getJavascriptTree() + ".exportNode();"); //$NON-NLS-1$
        menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuImport = new ContextMenuItem();
        menuImport.setLabel(msgs.get("tree.menu.import")); //$NON-NLS-1$
        menuImport.setIcon(request.getContextPath() + "/admindocroot/icons/16/import1.gif"); //$NON-NLS-1$
        menuImport.setOnclick(tree.getJavascriptTree() + ".importNode(this);"); //$NON-NLS-1$
        menuImport.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        // is it possible to activate?
        if (!Subscriber.isSubscribersEnabled()) {
            menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        menuImport.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        if (!this.isBrowseMode()) {
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
            tree.addMenuItem(menuDeActivate);

            tree.addSeparator();
            tree.addMenuItem(menuExport);
            tree.addMenuItem(menuImport);

            tree.addSeparator();
        }
        tree.addMenuItem(menuRefresh);
    }

    /**
     * Do not active sub CONTENTNODES automatically
     */
    public String activate() {
        boolean recursive = (request.getParameter("recursive") != null); //$NON-NLS-1$
        // do not activate nodes of type CONTENTNODE if recursive is false
        this.getTree().activateNode(this.getPathSelected(), recursive, false);
        return VIEW_TREE;
    }

}