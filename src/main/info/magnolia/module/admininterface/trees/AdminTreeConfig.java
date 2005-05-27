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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.misc.Icon;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Handles the tree rendering for the "config" repository.
 * @author Fabrizio Giustina
 * @version $Id: AdminTreeConfig.java 685 2005-05-04 19:23:59Z philipp $
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

        tree.setIconPage(Tree.ICONDOCROOT + "folder_cubes.gif");

        tree.addItemType(ItemType.CONTENT.getSystemName());
        tree.addItemType(ItemType.CONTENTNODE.getSystemName());
        tree.addItemType(ItemType.NT_NODEDATA);
        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setWidth(1);
        column0.setHtmlEdit();
        column0.setIsLabel(true);
        column0.setWidth(3);
        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName("");
        column1.setTitle(msgs.get("tree.config.value"));
        column1.setIsNodeDataValue(true);
        column1.setWidth(2);
        column1.setHtmlEdit();
        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName("");
        column2.setTitle(msgs.get("tree.config.type"));
        column2.setIsNodeDataType(true);
        column2.setWidth(2);
        Select typeSelect = new Select();
        typeSelect.setName(tree.getJavascriptTree() + TreeColumn.EDIT_NAMEADDITION);
        typeSelect.setSaveInfo(false);
        typeSelect.setCssClass(TreeColumn.EDIT_CSSCLASS_SELECT);
        typeSelect.setEvent("onblur", tree.getJavascriptTree() + TreeColumn.EDIT_JSSAVE);
        typeSelect.setOptions(PropertyType.TYPENAME_STRING, Integer.toString(PropertyType.STRING));
        typeSelect.setOptions(PropertyType.TYPENAME_BOOLEAN, Integer.toString(PropertyType.BOOLEAN));
        typeSelect.setOptions(PropertyType.TYPENAME_LONG, Integer.toString(PropertyType.LONG));
        typeSelect.setOptions(PropertyType.TYPENAME_DOUBLE, Integer.toString(PropertyType.DOUBLE));
        // todo: typeSelect.setOptions(PropertyType.TYPENAME_DATE,Integer.toString(PropertyType.DATE));
        column2.setHtmlEdit(typeSelect.getHtml());
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass("");
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        columnIcons.setIconsPermission(true);
        TreeColumn column4 = new TreeColumn(tree.getJavascriptTree(), request);
        column4.setName(MetaData.LAST_MODIFIED);
        column4.setIsMeta(true);
        column4.setDateFormat("yy-MM-dd, HH:mm");
        column4.setWidth(2);
        column4.setTitle(msgs.get("tree.config.date"));
        tree.addColumn(column0);
        tree.addColumn(column1);
        tree.addColumn(column2);
        if (Server.isAdmin()) {
            tree.addColumn(columnIcons);
        }
        tree.addColumn(column4);
    }

    /**
     * @param tree
     * @param request
     * @param msgs
     */
    protected void prepareContextMenu(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages(request);
        
        ContextMenuItem menuNewPage = new ContextMenuItem();
        menuNewPage.setLabel("<img src=\""
            + request.getContextPath()
            + new Icon().getSrc(Icon.PAGE, Icon.SIZE_SMALL)
            + "\"> <span style=\"position:relative;top:-3px;\">"
            + msgs.get("tree.config.menu.newFolder")
            + "</span>");
        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.CONTENT.getSystemName() + "');");
        menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("+tree.getJavascriptTree()+")");
        menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContentNode("+tree.getJavascriptTree()+")");

        ContextMenuItem menuNewContentNode = new ContextMenuItem();
        menuNewContentNode.setLabel("<img src=\""
            + request.getContextPath()
            + new Icon().getSrc(Icon.CONTENTNODE, Icon.SIZE_SMALL)
            + "\"> <span style=\"position:relative;top:-3px\">"
            + msgs.get("tree.config.menu.newNode")
            + "</span>");
        menuNewContentNode.setOnclick(tree.getJavascriptTree()
            + ".createNode('"
            + ItemType.CONTENTNODE.getSystemName()
            + "');");
        menuNewContentNode.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("+tree.getJavascriptTree()+")");

        ContextMenuItem menuNewNodeData = new ContextMenuItem();
        menuNewNodeData.setLabel("<img src=\""
            + request.getContextPath()
            + new Icon().getSrc(Icon.NODEDATA, Icon.SIZE_SMALL)
            + "\"> <span style=\"position:relative;top:-3px;\">"
            + msgs.get("tree.config.menu.newNodeData")
            + "</span>");
        menuNewNodeData.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.NT_NODEDATA + "');");
        menuNewNodeData.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("+tree.getJavascriptTree()+")");

        ContextMenuItem menuDelete = new ContextMenuItem();
        menuDelete.setLabel(msgs.get("tree.config.menu.delete"));
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();");
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("+tree.getJavascriptTree()+")");

        ContextMenuItem menuCopy = new ContextMenuItem();
        menuCopy.setLabel(msgs.get("tree.config.menu.copy"));
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("+tree.getJavascriptTree()+")");
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("+tree.getJavascriptTree()+")");
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();");

        ContextMenuItem menuCut = new ContextMenuItem();
        menuCut.setLabel(msgs.get("tree.config.menu.move"));
        menuCut.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("+tree.getJavascriptTree()+")");
        menuCut.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("+tree.getJavascriptTree()+")");
        menuCut.setOnclick(tree.getJavascriptTree() + ".cutNode();");

        ContextMenuItem menuActivateExcl = new ContextMenuItem();
        menuActivateExcl.setLabel(msgs.get("tree.config.menu.activate"));
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);");
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("+tree.getJavascriptTree()+")");
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("+tree.getJavascriptTree()+")");

        ContextMenuItem menuActivateIncl = new ContextMenuItem();
        menuActivateIncl.setLabel(msgs.get("tree.config.menu.activateInclSubs"));
        menuActivateIncl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",true);");
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("+tree.getJavascriptTree()+")");
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("+tree.getJavascriptTree()+")");

        ContextMenuItem menuDeActivate = new ContextMenuItem();
        menuDeActivate.setLabel(msgs.get("tree.config.menu.deactivate"));
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");");
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("+tree.getJavascriptTree()+")");
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("+tree.getJavascriptTree()+")");

        ContextMenuItem menuRefresh = new ContextMenuItem();
        menuRefresh.setLabel(msgs.get("tree.menu.refresh"));
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();");

        ContextMenuItem menuExport = new ContextMenuItem();
        menuExport.setLabel(msgs.get("tree.menu.export"));
        menuExport.setOnclick(tree.getJavascriptTree() + ".exportNode();");
        menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("+tree.getJavascriptTree()+")");
        menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("+tree.getJavascriptTree()+")");

        tree.addMenuItem(menuNewPage);
        tree.addMenuItem(menuNewContentNode);
        tree.addMenuItem(menuNewNodeData);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuDelete);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuCopy);
        tree.addMenuItem(menuCut);

        if (Server.isAdmin()) {
            tree.addMenuItem(null); // line
            tree.addMenuItem(menuActivateExcl);
            tree.addMenuItem(menuActivateIncl);
            tree.addMenuItem(menuDeActivate);
            tree.addMenuItem(null); // line
            tree.addMenuItem(menuExport);
        }
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuRefresh);
    }

}