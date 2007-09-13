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
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.cms.i18n.TemplateMessagesUtil;
import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AbstractTreeConfiguration;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class WebsiteTreeConfiguration extends AbstractTreeConfiguration {

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareTree(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request) {
        final Messages msgs = getMessages();

        tree.setIconOndblclick("mgnlTreeMenuItemOpen(" + tree.getJavascriptTree() + ");"); //$NON-NLS-1$ //$NON-NLS-2$

        tree.addItemType(ItemType.CONTENT);

        TreeColumn column0 = TreeColumn.createLabelColumn(tree, msgs.get("tree.web.page"), true);
        column0.setWidth(3);

        TreeColumn columnIcons = TreeColumn.createActivationColumn(tree);
        columnIcons.setIconsPermission(true);

        TreeColumn column1 = TreeColumn.createNodeDataColumn(tree, msgs.get("tree.web.title"), "title", true);
        column1.setWidth(2);

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

        Iterator templates = TemplateManager.getInstance().getAvailableTemplates(
            MgnlContext.getAccessManager(ContentRepository.CONFIG));
        Messages templateMsgs = TemplateMessagesUtil.getMessages();
        while (templates.hasNext()) {
            Template template = (Template) templates.next();
            String title = template.getTitle();
            title = templateMsgs.getWithDefault(title, title);
            title = MessagesUtil.javaScriptString(title);
            templateSelect.setOptions(title, template.getName());
        }
        column2.setHtmlEdit(templateSelect.getHtml());

        // todo: key/value -> column2.addKeyValue("sampleBasic","Samples: Basic Template");
        // todo: preselection (set on createPage)

        TreeColumn column3 = TreeColumn.createMetaDataColumn(tree, msgs.get("tree.web.date"), MetaData.LAST_MODIFIED, "yy-MM-dd, HH:mm");
        column3.setWidth(2);

        tree.addColumn(column0);

        if (!browseMode) {
            tree.addColumn(column1);
            if (Server.isAdmin() || ActivationManagerFactory.getActivationManager().hasAnyActiveSubscriber()) {
                tree.addColumn(columnIcons);
            }
            tree.addColumn(column2);
            tree.addColumn(column3);
        }

    }

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareContextMenu(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareContextMenu(Tree tree, boolean browseMode, HttpServletRequest request) {
        final Messages msgs = getMessages();
        ContextMenuItem menuOpen = new ContextMenuItem("open");
        menuOpen.setLabel(msgs.get("tree.web.menu.open")); //$NON-NLS-1$
        menuOpen.setIcon(request.getContextPath() + "/.resources/icons/16/document_plain_earth.gif"); //$NON-NLS-1$
        menuOpen.setOnclick("mgnlTreeMenuItemOpen(" + tree.getJavascriptTree() + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuNewPage = new ContextMenuItem("new");
        menuNewPage.setLabel(msgs.get("tree.web.menu.new")); //$NON-NLS-1$
        menuNewPage.setIcon(request.getContextPath() + "/.resources/icons/16/document_plain_earth_add.gif"); //$NON-NLS-1$

        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.CONTENT.getSystemName() + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuDelete = new ContextMenuItem("delete");
        menuDelete.setLabel(msgs.get("tree.web.menu.delete")); //$NON-NLS-1$
        menuDelete.setIcon(request.getContextPath() + "/.resources/icons/16/delete2.gif"); //$NON-NLS-1$
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();"); //$NON-NLS-1$
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuMove = new ContextMenuItem("move");
        menuMove.setLabel(msgs.get("tree.web.menu.move")); //$NON-NLS-1$
        menuMove.setIcon(request.getContextPath() + "/.resources/icons/16/up_down.gif"); //$NON-NLS-1$
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();"); //$NON-NLS-1$
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuCopy = new ContextMenuItem("copy");
        menuCopy.setLabel(msgs.get("tree.web.menu.copy")); //$NON-NLS-1$
        menuCopy.setIcon(request.getContextPath() + "/.resources/icons/16/copy.gif"); //$NON-NLS-1$
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();"); //$NON-NLS-1$
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuVersions = new ContextMenuItem("versions");
        menuVersions.setLabel(msgs.get("versions")); //$NON-NLS-1$
        menuVersions.setIcon(request.getContextPath() + "/.resources/icons/16/elements1.gif"); //$NON-NLS-1$
        menuVersions.setOnclick("mgnl.admininterface.WebsiteTree.showVersions(" + tree.getJavascriptTree() + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuVersions.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuActivateExcl = new ContextMenuItem("activate");
        menuActivateExcl.setLabel(msgs.get("tree.web.menu.activate")); //$NON-NLS-1$
        menuActivateExcl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green.gif"); //$NON-NLS-1$

        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuActivateIncl = new ContextMenuItem("activateInclSubs");
        menuActivateIncl.setLabel(msgs.get("tree.web.menu.activateInclSubs")); //$NON-NLS-1$
        menuActivateIncl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green_double.gif"); //$NON-NLS-1$
        menuActivateIncl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",true);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuDeactivate = new ContextMenuItem("deactivate");
        menuDeactivate.setLabel(msgs.get("tree.web.menu.deactivate")); //$NON-NLS-1$
        menuDeactivate.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_left_red.gif"); //$NON-NLS-1$
        menuDeactivate.setOnclick(tree.getJavascriptTree() + ".deactivateNode(" + Tree.ACTION_DEACTIVATE + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuDeactivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$
        menuDeactivate.addJavascriptCondition("new mgnlTreeMenuItemConditionPermissionWrite(" //$NON-NLS-1$
            + tree.getJavascriptTree()
            + ")"); //$NON-NLS-1$

        ContextMenuItem menuExport = new ContextMenuItem("export");
        menuExport.setLabel(msgs.get("tree.menu.export")); //$NON-NLS-1$
        menuExport.setIcon(request.getContextPath() + "/.resources/icons/16/export1.gif"); //$NON-NLS-1$
        menuExport.setOnclick(tree.getJavascriptTree() + ".exportNode();"); //$NON-NLS-1$

        ContextMenuItem menuImport = new ContextMenuItem("import");
        menuImport.setLabel(msgs.get("tree.menu.import")); //$NON-NLS-1$
        menuImport.setIcon(request.getContextPath() + "/.resources/icons/16/import2.gif"); //$NON-NLS-1$
        menuImport.setOnclick(tree.getJavascriptTree() + ".importNode(this);"); //$NON-NLS-1$

        // is there a subscriber?
        if (!ActivationManagerFactory.getActivationManager().hasAnyActiveSubscriber()) {
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
            tree.addMenuItem(menuOpen);
            tree.addMenuItem(menuNewPage);

            tree.addSeparator();
            tree.addMenuItem(menuDelete);

            tree.addSeparator();
            tree.addMenuItem(menuMove);
            tree.addMenuItem(menuCopy);

            tree.addSeparator();
            tree.addMenuItem(menuVersions);

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
        tree.addFunctionBarItemFromContextMenu("open");
        tree.addFunctionBarItemFromContextMenu("new");
        tree.addFunctionBarItem(null);
        tree.addFunctionBarItemFromContextMenu("activate");
        tree.addFunctionBarItemFromContextMenu("deactivate");
        tree.addFunctionBarItem(null);

        tree.addFunctionBarItem(FunctionBarItem.getRefreshFunctionBarItem(tree, getMessages(), request));

        tree.getFunctionBar().setSearchable(true);
        tree.getFunctionBar().setOnSearchFunction("mgnl.admininterface.WebsiteTree.search");
    }

}
