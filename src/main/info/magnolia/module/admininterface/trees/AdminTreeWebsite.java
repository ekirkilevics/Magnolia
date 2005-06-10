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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.TemplateMessagesUtil;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Handles the tree rendering for the "website" repository.
 * @author Fabrizio Giustina
 * @version $Id: AdminTreeWebsite.java 685 2005-05-04 19:23:59Z philipp $
 */
public class AdminTreeWebsite extends AdminTreeMVCHandler {

    /**
     * @param name
     * @param request
     * @param response
     */
    public AdminTreeWebsite(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.AdminTree#prepareTree()
     */
    protected void prepareTree(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages(request);

        tree.setIconOndblclick("mgnlTreeMenuItemOpen(" + tree.getJavascriptTree() + ");");

        tree.addItemType(ItemType.CONTENT);

        // to view all nodes uncomment this lines
        // tree.addItemType(ItemType.NT_CONTENTNODE);
        // tree.addItemType(ItemType.NT_NODEDATA);

        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setIsLabel(true);
        column0.setWidth(3);
        if (Server.isAdmin()) {
            column0.setHtmlEdit();
        }
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass("");
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        columnIcons.setIconsPermission(true);
        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName("title");
        column1.setTitle(msgs.get("tree.web.title"));
        column1.setWidth(2);
        if (Server.isAdmin()) {
            column1.setHtmlEdit();
        }
        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName(MetaData.TEMPLATE);
        column2.setIsMeta(true);
        column2.setWidth(2);
        column2.setTitle(msgs.get("tree.web.template"));
        // must render this column specially
        column2.setHtmlRenderer(new TemplateTreeColumnHtmlRenderer());

        Select templateSelect = new Select();
        templateSelect.setName(tree.getJavascriptTree() + TreeColumn.EDIT_NAMEADDITION);
        templateSelect.setSaveInfo(false);
        templateSelect.setCssClass(TreeColumn.EDIT_CSSCLASS_SELECT);

        // we must pass the displayValue to this function
        // templateSelect.setEvent("onblur", tree.getJavascriptTree() + TreeColumn.EDIT_JSSAVE);
        // templateSelect.setEvent("onchange", tree.getJavascriptTree() + TreeColumn.EDIT_JSSAVE);
        templateSelect.setEvent("onblur", tree.getJavascriptTree()
            + ".saveNodeData(this.value,this.options[this.selectedIndex].text)");
        templateSelect.setEvent("onchange", tree.getJavascriptTree()
            + ".saveNodeData(this.value,this.options[this.selectedIndex].text)");

        Iterator templates = Template.getAvailableTemplates(SessionAccessControl.getAccessManager(
            request,
            ContentRepository.CONFIG));
        while (templates.hasNext()) {
            Template template = (Template) templates.next();
            String title = template.getTitle();
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
        column3.setDateFormat("yy-MM-dd, HH:mm");
        column3.setWidth(2);
        column3.setTitle(msgs.get("tree.web.date"));
        tree.addColumn(column0);
        tree.addColumn(column1);
        if (Server.isAdmin() || Subscriber.isSubscribersEnabled()) {
            tree.addColumn(columnIcons);
        }
        tree.addColumn(column2);
        tree.addColumn(column3);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.AdminTreeMVCHandler#prepareContextMenu(info.magnolia.cms.gui.control.Tree,
     * javax.servlet.http.HttpServletRequest)
     */
    protected void prepareContextMenu(Tree tree, HttpServletRequest request) {
        Messages msgs = MessagesManager.getMessages(request);
        ContextMenuItem menuOpen = new ContextMenuItem();
        menuOpen.setLabel(msgs.get("tree.web.menu.open"));
        menuOpen.setOnclick("mgnlTreeMenuItemOpen(" + tree.getJavascriptTree() + ");");
        menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuNewPage = new ContextMenuItem();
        menuNewPage.setLabel(msgs.get("tree.web.menu.new"));
        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.CONTENT.getSystemName() + "');");
        menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuDelete = new ContextMenuItem();
        menuDelete.setLabel(msgs.get("tree.web.menu.delete"));
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();");
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuMove = new ContextMenuItem();
        menuMove.setLabel(msgs.get("tree.web.menu.move"));
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();");
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuCopy = new ContextMenuItem();
        menuCopy.setLabel(msgs.get("tree.web.menu.copy"));
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();");
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuActivateExcl = new ContextMenuItem();
        menuActivateExcl.setLabel(msgs.get("tree.web.menu.activate"));
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);");
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuActivateIncl = new ContextMenuItem();
        menuActivateIncl.setLabel(msgs.get("tree.web.menu.activateInclSubs"));
        menuActivateIncl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",true);");
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuDeActivate = new ContextMenuItem();
        menuDeActivate.setLabel(msgs.get("tree.web.menu.deactivate"));
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");");
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuRefresh = new ContextMenuItem();
        menuRefresh.setLabel(msgs.get("tree.menu.refresh"));
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();");

        ContextMenuItem menuExport = new ContextMenuItem();
        menuExport.setLabel(msgs.get("tree.menu.export"));
        menuExport.setOnclick(tree.getJavascriptTree() + ".exportNode();");
        menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("
            + tree.getJavascriptTree()
            + ")");

        tree.addMenuItem(menuOpen);
        tree.addMenuItem(null); // line

        // those menuitems are not active on public side
        if (!Server.isAdmin()) {
            menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
            menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
            menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
            menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
        }

        // is there a subscriber?
        if (!Subscriber.isSubscribersEnabled()) {
            menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
            menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
            menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
        }

        // only superuser can export data
        if (!Authenticator.getUser(request).isInRole(Role.ROLE_SUPERUSER)) {
            menuExport.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)");
        }

        tree.addMenuItem(menuNewPage);
        tree.addMenuItem(menuDelete);

        tree.addMenuItem(null); // line
        tree.addMenuItem(menuCopy);
        tree.addMenuItem(menuMove);

        tree.addMenuItem(null); // line
        tree.addMenuItem(menuActivateExcl);
        tree.addMenuItem(menuActivateIncl);
        tree.addMenuItem(menuDeActivate);

        tree.addMenuItem(null); // line
        tree.addMenuItem(menuExport);

        tree.addMenuItem(null); // line
        tree.addMenuItem(menuRefresh);
    }

}