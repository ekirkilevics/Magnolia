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
package info.magnolia.module.dms;

import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.module.admininterface.AdminTreeConfiguration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * Configures the DMS trees menu
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DMSAdminTreeConfig implements AdminTreeConfiguration {
    
    protected Messages msgs = MessagesUtil.chainWithDefault("info.magnolia.module.dms.messages");
    
    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareTree(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request) {
        tree.setIconPage("/.resources/icons/16/folder.gif");
        tree.setIconContentNode("/.resources/fileIcons/general.gif");

        tree.addItemType(ItemType.CONTENT.getSystemName());
        tree.addItemType(ItemType.CONTENTNODE.getSystemName());

        tree.setIconOndblclick("mgnl.dms.DMS.showDialogInTree(" + tree.getJavascriptTree() + ")");

        TreeColumn colTitle = new TreeColumn(tree.getJavascriptTree(), request);
        colTitle.setWidth(1);
        colTitle.setHtmlEdit();
        colTitle.setName("title");
        // column0.setIsLabel(true);
        colTitle.setWidth(3);
        colTitle.setTitle(msgs.get("dms.list.title"));

        TreeColumn colURL = new TreeColumn(tree.getJavascriptTree(), request);
        colURL.setIsLabel(true);
        colURL.setHtmlEdit();
        colURL.setWidth(3);
        colURL.setTitle(msgs.get("dms.list.url"));

        TreeColumn colType = new TreeColumn(tree.getJavascriptTree(), request);
        colType.setName("type");
        colType.setHtmlEdit("");
        colType.setWidth(1);
        colType.setTitle(msgs.get("dms.list.type"));

        TreeColumn colDate = new TreeColumn(tree.getJavascriptTree(), request);
        colDate.setName(MetaData.LAST_MODIFIED);
        colDate.setIsMeta(true);
        colDate.setDateFormat(DMSConfig.getDateFormat());
        colDate.setWidth(1);
        colDate.setTitle(msgs.get("dms.list.date"));

        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass(StringUtils.EMPTY);
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        columnIcons.setIconsPermission(true);

        tree.addColumn(colTitle);
        tree.addColumn(colURL);
        tree.addColumn(colType);
        tree.addColumn(colDate);
        tree.addColumn(columnIcons);
    }

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareContextMenu(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareContextMenu(Tree tree, boolean browseMode, HttpServletRequest request) {
        ContextMenuItem menuNewFolder = new ContextMenuItem("newFolder");

        menuNewFolder.setLabel(msgs.get("dms.menu.newFolder"));
        menuNewFolder.setIcon(request.getContextPath() + "/.resources/icons/16/folder_add.gif");
        menuNewFolder.setOnclick(tree.getJavascriptTree() + ".createNode('" + ItemType.CONTENT.getSystemName() + "');");
        menuNewFolder.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("
            + tree.getJavascriptTree()
            + ")");
        menuNewFolder.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContentNode("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuNewDocument = new ContextMenuItem("newDocument");
        menuNewDocument.setLabel(msgs.get("dms.menu.newDocument"));
        menuNewDocument.setIcon(request.getContextPath() + "/.resources/icons/16/document_add.gif");
        menuNewDocument.setOnclick("mgnl.dms.DMS.createNew(" + tree.getJavascriptTree() + ".selectedNode.id);");
        menuNewDocument.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContentNode("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuEditDocument = new ContextMenuItem("edit");
        menuEditDocument.setLabel(msgs.get("dms.menu.edit"));
        menuEditDocument.setIcon(request.getContextPath() + "/.resources/icons/16/document_edit.gif");
        menuEditDocument.setOnclick("mgnlTreeMenuOpenDialog("
            + tree.getJavascriptTree()
            + ",'.magnolia/dialogs/dmsEdit.html');");
        menuEditDocument.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContent("
            + tree.getJavascriptTree()
            + ")");
        menuEditDocument.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuDownloadDocument = new ContextMenuItem("download");
        menuDownloadDocument.setLabel(msgs.get("dms.menu.download"));
        menuDownloadDocument.setIcon(request.getContextPath() + "/.resources/icons/16/document_download.gif");
        menuDownloadDocument.setOnclick("mgnl.dms.DMS.downloadFile(" + tree.getJavascriptTree() + ".selectedNode.id);");
        menuDownloadDocument.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContent("
            + tree.getJavascriptTree()
            + ")");
        menuDownloadDocument.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuUploadZip = new ContextMenuItem("zipUplaod");
        menuUploadZip.setLabel(msgs.get("dms.menu.uploadZip"));
        menuUploadZip.setIcon(request.getContextPath() + "/.resources/icons/16/package.gif");
        menuUploadZip.setOnclick("mgnl.dms.DMS.uploadZip(" + tree.getJavascriptTree() + ".selectedNode.id);");
        menuUploadZip.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContentNode("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuVersions = new ContextMenuItem("versions");
        menuVersions.setLabel(msgs.get("dms.menu.versions"));
        menuVersions.setIcon(request.getContextPath() + "/.resources/icons/16/elements1.gif");
        menuVersions.setOnclick("mgnl.dms.DMS.showVersions(" + tree.getJavascriptTree() + ".selectedNode.id);");
        menuVersions.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContent("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuDelete = new ContextMenuItem("delete");
        menuDelete.setLabel(msgs.get("tree.config.menu.delete"));
        menuDelete.setIcon(request.getContextPath() + "/.resources/icons/16/delete2.gif");
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();");
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");

        ContextMenuItem menuCopy = new ContextMenuItem("copy");
        menuCopy.setLabel(msgs.get("tree.config.menu.copy"));
        menuCopy.setIcon(request.getContextPath() + "/.resources/icons/16/copy.gif");
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot("
            + tree.getJavascriptTree()
            + ")");
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("
            + tree.getJavascriptTree()
            + ")");
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();");

        ContextMenuItem menuCut = new ContextMenuItem("move");
        menuCut.setLabel(msgs.get("tree.config.menu.move"));
        menuCut.setIcon(request.getContextPath() + "/.resources/icons/16/up_down.gif");
        menuCut
            .addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" + tree.getJavascriptTree() + ")");
        menuCut.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData("
            + tree.getJavascriptTree()
            + ")");
        menuCut.setOnclick(tree.getJavascriptTree() + ".cutNode();");

        ContextMenuItem menuActivateExcl = new ContextMenuItem("activate");
        menuActivateExcl.setLabel(msgs.get("tree.config.menu.activate")); //$NON-NLS-1$
        menuActivateExcl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green.gif"); //$NON-NLS-1$
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuActivateIncl = new ContextMenuItem("activateIncl");
        menuActivateIncl.setLabel(msgs.get("tree.config.menu.activateInclSubs")); //$NON-NLS-1$
        menuActivateIncl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green_double.gif"); //$NON-NLS-1$
        menuActivateIncl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",true);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContentNode(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuDeActivate = new ContextMenuItem("deactivate");
        menuDeActivate.setLabel(msgs.get("tree.config.menu.deactivate")); //$NON-NLS-1$
        menuDeActivate.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_left_red.gif"); //$NON-NLS-1$
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeData(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        if (!Subscriber.isSubscribersEnabled()) {
            menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuActivateIncl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        ContextMenuItem menuExport = new ContextMenuItem();
        menuExport.setLabel(msgs.get("tree.menu.export")); //$NON-NLS-1$
        menuExport.setIcon(request.getContextPath() + "/.resources/icons/16/export1.gif"); //$NON-NLS-1$
        // keep versions
        menuExport.setOnclick(tree.getJavascriptTree() + ".exportNode(true);"); //$NON-NLS-1$

        ContextMenuItem menuImport = new ContextMenuItem();
        menuImport.setLabel(msgs.get("tree.menu.import")); //$NON-NLS-1$
        menuImport.setIcon(request.getContextPath() + "/.resources/icons/16/import2.gif"); //$NON-NLS-1$
        menuImport.setOnclick(tree.getJavascriptTree() + ".importNode(this);"); //$NON-NLS-1$

        ContextMenuItem menuRefresh = new ContextMenuItem();
        menuRefresh.setLabel(msgs.get("tree.menu.refresh"));
        menuRefresh.setIcon(request.getContextPath() + "/.resources/icons/16/refresh.gif");
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();");

        tree.addMenuItem(menuNewFolder);
        tree.addMenuItem(menuNewDocument);
        tree.addMenuItem(menuEditDocument);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuDownloadDocument);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuDelete);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuCut);
        tree.addMenuItem(menuCopy);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuVersions);
        tree.addSeparator();
        tree.addMenuItem(menuActivateExcl);
        tree.addMenuItem(menuActivateIncl);
        tree.addMenuItem(menuDeActivate);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuUploadZip);
        tree.addMenuItem(menuImport);
        tree.addMenuItem(menuExport);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuRefresh);
    }

    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareFunctionBar(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareFunctionBar(Tree tree, boolean browseMode, HttpServletRequest request) {
        FunctionBar bar = tree.getFunctionBar();
        ContextMenu menu = tree.getMenu();
        bar.setSearchable(true);
        bar.setOnSearchFunction("mgnl.dms.DMS.simpleSearch");
        
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("newFolder")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("newDocument")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("edit")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("download")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("delete")));
        bar.addMenuItem(null);
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("activate")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("deactivate")));

    }

}
