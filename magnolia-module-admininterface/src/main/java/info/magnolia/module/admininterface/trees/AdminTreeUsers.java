/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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

import org.apache.commons.lang.StringUtils;


/**
 * Handles the tree rendering for the "users" repository.
 * @author Fabrizio Giustina
 * @version $Id$
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
        Messages msgs = MessagesManager.getMessages();

        tree.setDrawShifter(false);
        // context path is already added by Tree
        tree.setIconPage(Tree.ICONDOCROOT + "pawn_glass_yellow.gif"); //$NON-NLS-1$
        if (Server.isAdmin()) {
            tree.setIconOndblclick("mgnlTreeMenuOpenDialog(" + tree.getJavascriptTree() //$NON-NLS-1$
                // + ",'.magnolia/adminCentral/users/dialog.html');");
                + ",'.magnolia/dialogs/useredit.html');"); //$NON-NLS-1$
        }
        tree.addItemType(ItemType.CONTENT);

        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setIsLabel(true);
        if (Server.isAdmin()) {
            column0.setHtmlEdit();
        }
        column0.setTitle(msgs.get("tree.users.name")); //$NON-NLS-1$
        column0.setWidth(2);
        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName("title"); //$NON-NLS-1$
        if (Server.isAdmin()) {
            column1.setHtmlEdit();
        }
        column1.setTitle(msgs.get("tree.users.fullname")); //$NON-NLS-1$
        column1.setWidth(2);
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass(StringUtils.EMPTY);
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName(MetaData.LAST_MODIFIED);
        column2.setIsMeta(true);
        column2.setDateFormat("yyyy-MM-dd, HH:mm"); //$NON-NLS-1$
        column2.setTitle(msgs.get("tree.users.date")); //$NON-NLS-1$
        column2.setWidth(2);

        tree.addColumn(column0);

        if (!this.isBrowseMode()) {
            tree.addColumn(column1);
            if (Server.isAdmin() || Subscriber.isSubscribersEnabled()) {
                tree.addColumn(columnIcons);
            }
            tree.addColumn(column2);
        }
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.AdminTreeMVCHandler#prepareContextMenu(info.magnolia.cms.gui.control.Tree,
     * javax.servlet.http.HttpServletRequest)
     */
    protected void prepareContextMenu(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages();
        ContextMenuItem menuOpen = new ContextMenuItem();
        menuOpen.setLabel(msgs.get("tree.users.menu.edit")); //$NON-NLS-1$
        menuOpen.setIcon(request.getContextPath() + "/.resources/icons/16/pawn_glass_yellow.gif"); //$NON-NLS-1$
        menuOpen.setOnclick("mgnlTreeMenuOpenDialog(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ",'.magnolia/dialogs/useredit.html');"); //$NON-NLS-1$
        menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        
        ContextMenuItem menuAddToGroup = new ContextMenuItem();
        menuAddToGroup.setLabel("Add user to groups"); //$NON-NLS-1$
        menuAddToGroup.setIcon(request.getContextPath() + "/.resources/icons/16/pawn_glass_yellow.gif"); //$NON-NLS-1$
        menuAddToGroup.setOnclick("mgnlTreeMenuOpenDialog(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ",'.magnolia/dialogs/useraddtogroups.html');"); //$NON-NLS-1$
        menuAddToGroup.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
 
        ContextMenuItem menuNewPage = new ContextMenuItem();
        menuNewPage.setLabel(msgs.get("tree.users.menu.new")); //$NON-NLS-1$
        menuNewPage.setIcon(request.getContextPath() + "/.resources/icons/16/pawn_glass_yellow_add.gif"); //$NON-NLS-1$
        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createRootNode('" //$NON-NLS-1$
            + ItemType.CONTENT.getSystemName()
            + "');"); //$NON-NLS-1$
        ContextMenuItem menuDelete = new ContextMenuItem();
        menuDelete.setLabel(msgs.get("tree.users.menu.delete")); //$NON-NLS-1$
        menuDelete.setIcon(request.getContextPath() + "/.resources/icons/16/delete2.gif"); //$NON-NLS-1$
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();"); //$NON-NLS-1$
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        ContextMenuItem menuMove = new ContextMenuItem();
        menuMove.setLabel(msgs.get("tree.users.menu.move")); //$NON-NLS-1$
        menuMove.setIcon(request.getContextPath() + "/.resources/icons/16/up_down.gif"); //$NON-NLS-1$
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();"); //$NON-NLS-1$
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        ContextMenuItem menuCopy = new ContextMenuItem();
        menuCopy.setLabel(msgs.get("tree.users.menu.copy")); //$NON-NLS-1$
        menuCopy.setIcon(request.getContextPath() + "/.resources/icons/16/copy.gif"); //$NON-NLS-1$
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();"); //$NON-NLS-1$
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        ContextMenuItem menuActivateExcl = new ContextMenuItem();
        menuActivateExcl.setLabel(msgs.get("tree.users.menu.activate")); //$NON-NLS-1$
        menuActivateExcl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green.gif"); //$NON-NLS-1$
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        ContextMenuItem menuDeActivate = new ContextMenuItem();
        menuDeActivate.setLabel(msgs.get("tree.users.menu.deactivate")); //$NON-NLS-1$
        menuDeActivate.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_left_red.gif"); //$NON-NLS-1$
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        ContextMenuItem menuRefresh = new ContextMenuItem();
        menuRefresh.setLabel(msgs.get("tree.menu.refresh")); //$NON-NLS-1$
        menuRefresh.setIcon(request.getContextPath() + "/.resources/icons/16/refresh.gif"); //$NON-NLS-1$
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$

        if (!Server.isAdmin()) {
            menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        if (!Subscriber.isSubscribersEnabled()) {
            menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        if (!this.isBrowseMode()) {
            tree.addMenuItem(menuOpen);
            tree.addMenuItem(menuAddToGroup);
            
            tree.addMenuItem(menuNewPage);

            tree.addMenuItem(null); // line
            tree.addMenuItem(menuDelete);

            tree.addMenuItem(null); // line
            tree.addMenuItem(menuActivateExcl);
            tree.addMenuItem(menuDeActivate);

            tree.addMenuItem(null); // line
        }
        tree.addMenuItem(menuRefresh);
    }
}