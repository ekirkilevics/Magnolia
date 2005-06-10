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
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Handles the tree rendering for the "users" repository.
 * @author Fabrizio Giustina
 * @version $Id: AdminTreeUsers.java 685 2005-05-04 19:23:59Z philipp $
 */
public class AdminTreeUsers extends AdminTreeMVCHandler {

    /**
     * @param name
     * @param request
     * @param response
     */
    public AdminTreeUsers(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.AdminTree#prepareTree(info.magnolia.cms.gui.control.Tree,
     * javax.servlet.http.HttpServletRequest)
     */
    protected void prepareTree(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages(request);

        tree.setDrawShifter(false);
        // context path is already added by Tree
        tree.setIconPage(Tree.ICONDOCROOT + "pawn_glass_yellow.gif");
        if (Server.isAdmin()) {
            tree.setIconOndblclick("mgnlTreeMenuOpenDialog(" + tree.getJavascriptTree()
            // + ",'.magnolia/adminCentral/users/dialog.html');");
                + ",'.magnolia/dialogs/useredit.html');");
        }
        tree.addItemType(ItemType.CONTENT);

        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setIsLabel(true);
        if (Server.isAdmin()) {
            column0.setHtmlEdit();
        }
        column0.setTitle(msgs.get("tree.users.name"));
        column0.setWidth(2);
        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName("title");
        if (Server.isAdmin()) {
            column1.setHtmlEdit();
        }
        column1.setTitle(msgs.get("tree.users.fullname"));
        column1.setWidth(2);
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass("");
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName(MetaData.LAST_MODIFIED);
        column2.setIsMeta(true);
        column2.setDateFormat("yyyy-MM-dd, HH:mm");
        column2.setTitle(msgs.get("tree.users.date"));
        column2.setWidth(2);
        tree.addColumn(column0);
        tree.addColumn(column1);
        if (Server.isAdmin() || Subscriber.isSubscribersEnabled()) {
            tree.addColumn(columnIcons);
        }
        tree.addColumn(column2);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.AdminTreeMVCHandler#prepareContextMenu(info.magnolia.cms.gui.control.Tree,
     * javax.servlet.http.HttpServletRequest)
     */
    protected void prepareContextMenu(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages(request);
        ContextMenuItem menuOpen = new ContextMenuItem();
        menuOpen.setLabel(msgs.get("tree.users.menu.edit"));
        menuOpen.setIcon(request.getContextPath() + "/admindocroot/icons/16/pawn_glass_yellow.gif");
        menuOpen.setOnclick("mgnlTreeMenuOpenDialog("
            + tree.getJavascriptTree()
            + ",'.magnolia/dialogs/useredit.html');");
        menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        ContextMenuItem menuNewPage = new ContextMenuItem();
        menuNewPage.setLabel(msgs.get("tree.users.menu.new"));
        menuNewPage.setIcon(request.getContextPath() + "/admindocroot/icons/16/pawn_glass_yellow_add.gif");
        menuNewPage.setOnclick(tree.getJavascriptTree()
            + ".createRootNode('"
            + ItemType.CONTENT.getSystemName()
            + "');");
        ContextMenuItem menuDelete = new ContextMenuItem();
        menuDelete.setLabel(msgs.get("tree.users.menu.delete"));
        menuDelete.setIcon(request.getContextPath() + "/admindocroot/icons/16/delete2.gif");
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();");
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        ContextMenuItem menuMove = new ContextMenuItem();
        menuMove.setLabel(msgs.get("tree.users.menu.move"));
        menuMove.setIcon(request.getContextPath() + "/admindocroot/icons/16/up_down.gif");
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();");
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        ContextMenuItem menuCopy = new ContextMenuItem();
        menuCopy.setLabel(msgs.get("tree.users.menu.copy"));
        menuCopy.setIcon(request.getContextPath() + "/admindocroot/icons/16/copy.gif");
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();");
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        ContextMenuItem menuActivateExcl = new ContextMenuItem();
        menuActivateExcl.setLabel(msgs.get("tree.users.menu.activate"));
        menuActivateExcl.setIcon(request.getContextPath() + "/admindocroot/icons/16/arrow_right_green.gif");
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);");
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        ContextMenuItem menuDeActivate = new ContextMenuItem();
        menuDeActivate.setLabel(msgs.get("tree.users.menu.deactivate"));
        menuDeActivate.setIcon(request.getContextPath() + "/admindocroot/icons/16/arrow_left_red.gif");
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");");
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        ContextMenuItem menuRefresh = new ContextMenuItem();
        menuRefresh.setLabel(msgs.get("tree.menu.refresh"));
        menuRefresh.setIcon(request.getContextPath() + "/admindocroot/icons/16/refresh.gif");
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();");

        if (!Server.isAdmin()) {
            menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
            menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
        }
        
        if (!Subscriber.isSubscribersEnabled()) {
            menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
            menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
        }
        
        tree.addMenuItem(menuOpen);
        tree.addMenuItem(menuNewPage);

        tree.addMenuItem(null); // line
        tree.addMenuItem(menuDelete);

        tree.addMenuItem(null); // line
        tree.addMenuItem(menuActivateExcl);
        tree.addMenuItem(menuDeActivate);
        
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuRefresh);
    }
}
