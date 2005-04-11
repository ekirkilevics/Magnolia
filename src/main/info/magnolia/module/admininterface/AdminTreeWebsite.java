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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.gui.control.TreeMenuItem;
import info.magnolia.cms.i18n.ContextMessages;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.TemplateMessagesUtil;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;


/**
 * Handles the tree rendering for the "website" repository.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class AdminTreeWebsite implements AdminTree {

    /**
     * @see AdminTree#configureTree(Tree, HttpServletRequest, String, String, String, boolean, String)
     */
    public void configureTree(Tree tree, HttpServletRequest request, String path, String pathOpen, String pathSelected,
        boolean create, String createItemType) {
        Messages msgs = MessagesManager.getMessages(request);
        
        tree.setIconOndblclick("mgnlTreeMenuItemOpen(" + tree.getJavascriptTree() + ");");
        tree.setPath(path);
        if (create) {
            tree.createNode(createItemType);
        }
        else {
            tree.setPathOpen(pathOpen);
            tree.setPathSelected(pathSelected);
        }
        tree.addItemType(ItemType.NT_CONTENT);

        // to view all nodes uncomment this lines
        //tree.addItemType(ItemType.NT_CONTENTNODE);
        //tree.addItemType(ItemType.NT_NODEDATA);
        
        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setIsLabel(true);
        column0.setWidth(3);
        if (Server.isAdmin())
            column0.setHtmlEdit();
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
        if (Server.isAdmin())
            column1.setHtmlEdit();
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
        templateSelect.setEvent("onblur", tree.getJavascriptTree() + ".saveNodeData(this.value,this.options[this.selectedIndex].text)");
        templateSelect.setEvent("onchange", tree.getJavascriptTree() + ".saveNodeData(this.value,this.options[this.selectedIndex].text)");
        
        Iterator templates = Template.getAvailableTemplates();
        while (templates.hasNext()) {
            Template template = (Template) templates.next();
            String title = template.getTitle();
            title = TemplateMessagesUtil.get(request, title);
            title = Messages.javaScriptString(title);
            templateSelect.setOptions(title, template.getName());
        }
        if (Server.isAdmin())
            column2.setHtmlEdit(templateSelect.getHtml());
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
        if (Server.isAdmin()) {
            tree.addColumn(columnIcons);
        }
        tree.addColumn(column2);
        tree.addColumn(column3);

        TreeMenuItem menuOpen = new TreeMenuItem();
        menuOpen.setLabel(msgs.get("tree.web.menu.open"));
        menuOpen.setOnclick("mgnlTreeMenuItemOpen(" + tree.getJavascriptTree() + ");");
        menuOpen.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

        TreeMenuItem menuNewPage = new TreeMenuItem();
        menuNewPage.setLabel(msgs.get("tree.web.menu.new"));
        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.NT_CONTENT + "');");
        menuNewPage.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

        TreeMenuItem menuDelete = new TreeMenuItem();
        menuDelete.setLabel(msgs.get("tree.web.menu.delete"));
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();");
        menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

        TreeMenuItem menuMove = new TreeMenuItem();
        menuMove.setLabel(msgs.get("tree.web.menu.move"));
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();");
        menuMove.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuMove.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

        TreeMenuItem menuCopy = new TreeMenuItem();
        menuCopy.setLabel(msgs.get("tree.web.menu.copy"));
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();");
        menuCopy.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");

        TreeMenuItem menuActivateExcl = new TreeMenuItem();
        menuActivateExcl.setLabel(msgs.get("tree.web.menu.activate"));
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);");
        menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

        TreeMenuItem menuActivateIncl = new TreeMenuItem();
        menuActivateIncl.setLabel(msgs.get("tree.web.menu.activateInclSubs"));
        menuActivateIncl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",true);");
        menuActivateIncl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuActivateIncl.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

        TreeMenuItem menuDeActivate = new TreeMenuItem();
        menuDeActivate.setLabel(msgs.get("tree.web.menu.deactivate"));
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");");
        menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionPermissionWrite");

        TreeMenuItem menuRefresh = new TreeMenuItem();
        menuRefresh.setLabel(msgs.get("tree.menu.refresh"));
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();");

        TreeMenuItem menuExport = new TreeMenuItem();
        menuExport.setLabel(msgs.get("tree.menu.export"));
        menuExport.setOnclick(tree.getJavascriptTree() + ".exportNode();");
        menuExport.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        menuExport.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotNodeData");

        tree.addMenuItem(menuOpen);
        tree.addMenuItem(null); // line
        if (Server.isAdmin()) {
            tree.addMenuItem(menuNewPage);
        }
        tree.addMenuItem(menuDelete);
        if (Server.isAdmin()) {
            tree.addMenuItem(null); // line
            tree.addMenuItem(menuCopy);
            tree.addMenuItem(menuMove);
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
