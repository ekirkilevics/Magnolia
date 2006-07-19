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
package info.magnolia.module.data.trees;

import info.magnolia.cms.beans.config.ContentRepository;
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
import info.magnolia.module.data.Constants;
import info.magnolia.module.data.DataModule;
import info.magnolia.module.data.tools.DataImportManager;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * Configures the Data trees menu
 * @author Christoph Hoffmann (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 */
public class TypeAdminTreeConfig implements AdminTreeConfiguration {
    
    protected Messages msgs = MessagesUtil.chainWithDefault("info.magnolia.module.data.messages");
    
    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareTree(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request) {

    	// starting point 
    	tree.setPath(Constants.TYPES_NODE_PATH);
        tree.setIndentionWidth(0);
        tree.setDrawShifter(false);
        
        tree.addItemType(ItemType.CONTENT.getSystemName());
        tree.setIconPage(Tree.ICONDOCROOT + "pawn_glass_yellow.gif"); //$NON-NLS-1$
        tree.setIconOndblclick("mgnl.data.TypeTree.edit(" + tree.getJavascriptTree() + ")");


        TreeColumn colName = new TreeColumn(tree.getJavascriptTree(), request);
        colName.setWidth(1);
        colName.setName(Constants.TYPE_NAME);
        colName.setIsLabel(true);
        colName.setTitle(msgs.get("module.data.tree.type.column.name.label"));

        TreeColumn colTitle = new TreeColumn(tree.getJavascriptTree(), request);
        colTitle.setWidth(1);
        colTitle.setName(Constants.TYPE_TITLE);
        colTitle.setTitle(msgs.get("module.data.tree.type.column.title.label"));

        TreeColumn colDate = new TreeColumn(tree.getJavascriptTree(), request);
        colDate.setName(MetaData.LAST_MODIFIED);
        colDate.setIsMeta(true);
        colDate.setWidth(1);
        colDate.setTitle(msgs.get("module.data.tree.type.column.date.label"));

        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass(StringUtils.EMPTY);
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        columnIcons.setIconsPermission(true);

        tree.addColumn(colName);
        tree.addColumn(colTitle);
        tree.addColumn(colDate);
        tree.addColumn(columnIcons);
        
    }
    
    /**
     * @see info.magnolia.module.admininterface.AdminTreeConfiguration#prepareContextMenu(info.magnolia.cms.gui.control.Tree,
     * boolean, javax.servlet.http.HttpServletRequest)
     */
    public void prepareContextMenu(Tree tree, boolean browseMode, HttpServletRequest request) {

        ContextMenuItem menuNew = new ContextMenuItem("new");
        menuNew.setLabel(msgs.get("module.data.tree.type.menu.new"));
        menuNew.setIcon(request.getContextPath() + "/.resources/icons/16/document_add.gif");
        menuNew.setOnclick("mgnl.data.TypeTree.create(" + tree.getJavascriptTree() + ".selectedNode.id);");
        menuNew.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContent(" //$NON-NLS-1$
                + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuEdit = new ContextMenuItem("edit");
        menuEdit.setLabel(msgs.get("module.data.tree.type.menu.edit"));
        menuEdit.setIcon(request.getContextPath() + "/.resources/icons/16/document_edit.gif");
        menuEdit.setOnclick("mgnl.data.TypeTree.edit("
            + tree.getJavascriptTree() + ")");
        menuEdit.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        
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

        tree.addMenuItem(menuNew);
        tree.addMenuItem(menuEdit);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuDelete);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuCut);
        tree.addMenuItem(menuCopy);
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuActivateExcl);
        tree.addMenuItem(menuDeActivate);
        tree.addMenuItem(null); // line
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
        bar.setSearchable(false);
        
        ContextMenuItem menuImport = new FunctionBarItem("importData");
        menuImport.setLabel(msgs.get("module.data.tree.type.menu.importData"));
        menuImport.setIcon(request.getContextPath() + "/.resources/icons/16/import2.gif");
        menuImport.setOnclick("mgnl.data.TypeTree.importData(" + tree.getJavascriptTree() + ");");
        if(DataImportManager.getInstance().getDefault(DataModule.getRepository()) == null){
        	menuImport.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("new")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("edit")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("delete")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("activate")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("deactivate")));
        bar.addMenuItem(null);
        bar.addMenuItem(menuImport);
    }

}
