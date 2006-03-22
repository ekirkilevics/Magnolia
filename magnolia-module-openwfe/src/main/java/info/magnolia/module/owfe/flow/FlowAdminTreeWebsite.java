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
package info.magnolia.module.owfe.flow;

import info.magnolia.cms.beans.config.*;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.TemplateMessagesUtil;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.module.admininterface.trees.TemplateTreeColumnHtmlRenderer;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;


/**
 * Handles the tree rendering for the "website" repository.
 *
 * @author Fabrizio Giustina
 * @version $Id: AdminTreeWebsite.java 1610 2006-01-30 23:54:23Z svk $
 */
public class FlowAdminTreeWebsite extends FlowAdminTreeMVCHandler {

    /**
     * @param name
     * @param request
     * @param response
     */
    public FlowAdminTreeWebsite(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * @see info.magnolia.module.admininterface.AdminTree#prepareTree()
     */
    protected void prepareTree(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages();

        tree.setIconOndblclick("mgnlTreeMenuItemOpen(" + tree.getJavascriptTree() + ");"); //$NON-NLS-1$ //$NON-NLS-2$

        tree.addItemType(ItemType.CONTENT);

        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setIsLabel(true);
        column0.setWidth(3);
        if (Server.isAdmin()) {
            column0.setHtmlEdit();
        }
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass(StringUtils.EMPTY);
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        columnIcons.setIconsPermission(true);
        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName("title"); //$NON-NLS-1$
        column1.setTitle(msgs.get("tree.web.title")); //$NON-NLS-1$
        column1.setWidth(2);
        if (Server.isAdmin()) {
            column1.setHtmlEdit();
        }
        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName(MetaData.TEMPLATE);
        column2.setIsMeta(true);
        column2.setWidth(2);
        column2.setTitle(msgs.get("tree.web.template")); //$NON-NLS-1$
        // must render this column specially
        column2.setHtmlRenderer(new TemplateTreeColumnHtmlRenderer());

        Select templateSelect = new Select();
        templateSelect.setName(tree.getJavascriptTree() + TreeColumn.EDIT_NAMEADDITION);
        templateSelect.setSaveInfo(false);
        templateSelect.setCssClass(TreeColumn.EDIT_CSSCLASS_SELECT);

        // we must pass the displayValue to this function
        // templateSelect.setEvent("onblur", tree.getJavascriptTree() + TreeColumn.EDIT_JSSAVE);
        // templateSelect.setEvent("onchange", tree.getJavascriptTree() + TreeColumn.EDIT_JSSAVE);
        templateSelect.setEvent("onblur", tree.getJavascriptTree() //$NON-NLS-1$
                + ".saveNodeData(this.value,this.options[this.selectedIndex].text)"); //$NON-NLS-1$
        templateSelect.setEvent("onchange", tree.getJavascriptTree() //$NON-NLS-1$
                + ".saveNodeData(this.value,this.options[this.selectedIndex].text)"); //$NON-NLS-1$

        Iterator templates = TemplateManager.getInstance().getAvailableTemplates(SessionAccessControl.getAccessManager(
                request,
                ContentRepository.CONFIG));
        while (templates.hasNext()) {
            Template template = (Template) templates.next();
            String title = template.getTitle();
            // TODO enable an induvidual message bundel for the template title
            title = TemplateMessagesUtil.get(request, title);
            title = Messages.javaScriptString(title);
            templateSelect.setOptions(title, template.getName());
        }
        if (Server.isAdmin()) {
            column2.setHtmlEdit(templateSelect.getHtml());
        }
        // todo: key/value -> column2.addKeyValue("sampleBasic","Samples: Basic Template");
        // todo: preselection (set on createPage)
        TreeColumn column3 = new TreeColumn(tree.getJavascriptTree(), request);
        column3.setName(MetaData.LAST_MODIFIED);
        // column3.setName(MetaData.SEQUENCE_POS);
        column3.setIsMeta(true);
        column3.setDateFormat("yy-MM-dd, HH:mm"); //$NON-NLS-1$
        column3.setWidth(2);
        column3.setTitle(msgs.get("tree.web.date")); //$NON-NLS-1$

        tree.addColumn(column0);

        if (!this.isBrowseMode()) {
            tree.addColumn(column1);
            if (Server.isAdmin() || Subscriber.isSubscribersEnabled()) {
                tree.addColumn(columnIcons);
            }
            tree.addColumn(column2);
            tree.addColumn(column3);
        }
    }

    /**
     * @see info.magnolia.module.admininterface.AdminTreeMVCHandler#prepareContextMenu(info.magnolia.cms.gui.control.Tree,
     *      javax.servlet.http.HttpServletRequest)
     */
    protected void prepareContextMenu(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages();
        ContextMenuItem menuOpen = new ContextMenuItem();
        menuOpen.setLabel(msgs.get("tree.web.menu.open")); //$NON-NLS-1$
        menuOpen.setIcon(request.getContextPath() + "/.resources/icons/16/document_plain_earth.gif"); //$NON-NLS-1$
        menuOpen.setOnclick("mgnlTreeMenuItemOpen(" + tree.getJavascriptTree() + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$

        ContextMenuItem menuNewPage = new ContextMenuItem();
        menuNewPage.setLabel(msgs.get("tree.web.menu.new")); //$NON-NLS-1$
        menuNewPage.setIcon(request.getContextPath() + "/.resources/icons/16/document_plain_earth_add.gif"); //$NON-NLS-1$

        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.CONTENT.getSystemName() + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$

        ContextMenuItem menuDelete = new ContextMenuItem();
        menuDelete.setLabel(msgs.get("tree.web.menu.delete")); //$NON-NLS-1$
        menuDelete.setIcon(request.getContextPath() + "/.resources/icons/16/delete2.gif"); //$NON-NLS-1$
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();"); //$NON-NLS-1$
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$

        ContextMenuItem menuMove = new ContextMenuItem();
        menuMove.setLabel(msgs.get("tree.web.menu.move")); //$NON-NLS-1$
        menuMove.setIcon(request.getContextPath() + "/.resources/icons/16/up_down.gif"); //$NON-NLS-1$
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();"); //$NON-NLS-1$
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$

        ContextMenuItem menuCopy = new ContextMenuItem();
        menuCopy.setLabel(msgs.get("tree.web.menu.copy")); //$NON-NLS-1$
        menuCopy.setIcon(request.getContextPath() + "/.resources/icons/16/copy.gif"); //$NON-NLS-1$
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();"); //$NON-NLS-1$
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$

        ContextMenuItem menuActivateExcl = new ContextMenuItem();
        menuActivateExcl.setLabel(msgs.get("tree.web.menu.activate")); //$NON-NLS-1$
        menuActivateExcl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green.gif"); //$NON-NLS-1$

        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$

        ContextMenuItem menuActivateIncl = new ContextMenuItem();
        menuActivateIncl.setLabel(msgs.get("tree.web.menu.activateInclSubs")); //$NON-NLS-1$
        menuActivateIncl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green.gif"); //$NON-NLS-1$
        menuActivateIncl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",true);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$

        ContextMenuItem menuDeActivate = new ContextMenuItem();
        menuDeActivate.setLabel(msgs.get("tree.web.menu.deactivate")); //$NON-NLS-1$
        menuDeActivate.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_left_red.gif"); //$NON-NLS-1$
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$

        ContextMenuItem menuRefresh = new ContextMenuItem();
        menuRefresh.setLabel(msgs.get("tree.menu.refresh")); //$NON-NLS-1$
        menuRefresh.setIcon(request.getContextPath() + "/.resources/icons/16/refresh.gif"); //$NON-NLS-1$
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$

        ContextMenuItem menuExport = new ContextMenuItem();
        menuExport.setLabel(msgs.get("tree.menu.export")); //$NON-NLS-1$
        menuExport.setIcon(request.getContextPath() + "/.resources/icons/16/export.gif"); //$NON-NLS-1$
        menuExport.setOnclick(tree.getJavascriptTree() + ".exportNode();"); //$NON-NLS-1$

        ContextMenuItem menuImport = new ContextMenuItem();
        menuImport.setLabel(msgs.get("tree.menu.import")); //$NON-NLS-1$
        menuImport.setIcon(request.getContextPath() + "/.resources/icons/16/import1.gif"); //$NON-NLS-1$
        menuImport.setOnclick(tree.getJavascriptTree() + ".importNode(this);"); //$NON-NLS-1$

        // those menuitems are not active on public side
        if (!Server.isAdmin()) {
            menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuImport.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        // is there a subscriber?
        if (!Subscriber.isSubscribersEnabled()) {
            menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        menuImport.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$

        menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
                + tree.getJavascriptTree()
                + ")"); //$NON-NLS-1$

        if (!this.isBrowseMode()) {
            tree.addMenuItem(menuOpen);
            tree.addMenuItem(menuNewPage);

            tree.addSeparator();
            tree.addMenuItem(menuDelete);

            tree.addSeparator();
            tree.addMenuItem(menuMove);
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

    protected void prepareFunctionBar(Tree tree, HttpServletRequest request) {
        // TODO Auto-generated method stub
        
    }
}
