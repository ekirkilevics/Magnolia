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

import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.gui.control.TreeColumnHtmlRenderer;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeConfiguration;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;
import info.magnolia.module.data.Constants;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 *
 * @author Christoph Hoffmann (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 * 
 */
public class GenericDataAdminTreeConfig implements AdminTreeConfiguration {

    private static Logger log = Logger.getLogger(GenericDataAdminTreeConfig.class);
    protected Messages msgs = MessagesUtil.chainWithDefault("info.magnolia.module.data.messages");

	
	protected final ConfiguredDialog dialog;
	protected final String itemType;
	
	public GenericDataAdminTreeConfig(final ConfiguredDialog dialog){
		this.dialog = dialog;
        try {
    		this.itemType = dialog.getConfigNode().getParent().getParent().getName(); 
		} catch (Exception e) {
			throw new RuntimeException("cannot get the type of the generic data tree.", e);
		}
	}
	
	 
	public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request) {

		tree.addItemType(itemType);
				
        // starting point 
        tree.setIndentionWidth(0);
        tree.setDrawShifter(false);
        tree.setIconPage(Tree.ICONDOCROOT + "pawn_glass_yellow.gif"); //$NON-NLS-1$
        tree.setIconOndblclick("mgnl.data.DataTree.edit(" + tree.getJavascriptTree() + ")");

        
        TreeColumn colName = new TreeColumn(tree.getJavascriptTree(), request);
        colName.setWidth(1);
        colName.setName(Constants.TYPE_NAME);
        colName.setIsLabel(true);
        colName.setTitle(msgs.get("module.data.tree.type.column.name.label"));
        tree.addColumn(colName);
        
        Collection controls = ContentUtil.collectAllChildren(dialog.getConfigNode(), new Content.ContentFilter(){
        	public boolean accept(Content content) {
        		final NodeData control = content.getNodeData("controlType");
        		return control != null && !control.getString().equalsIgnoreCase("tab") && !StringUtils.isEmpty(control.getString());
        	}
        });
        
        for(Iterator it = controls.iterator(); it.hasNext();){
        	final Content field = (Content)it.next();
        	
        	try{
	        	if(field.hasNodeData(Constants.TYPE_DIALOG_FIELD_VISIBILITY) && !field.getNodeData(Constants.TYPE_DIALOG_FIELD_VISIBILITY).getBoolean()){
	        		continue;
	        	}
        	}catch(RepositoryException ex){
        		log.error("Error while validating visibility of datafield.", ex);
        	}
        	
            TreeColumn col = new TreeColumn(tree.getJavascriptTree(), request);
            col.setWidth(1);
            col.setName(field.getNodeData(Constants.TYPE_DIALOG_FIELD_NAME).getString());
            col.setTitle(field.getName());
        	
            
            if(field.getNodeData(Constants.TYPE_DIALOG_FIELD_TYPE).getString().equals(Constants.FIELD_TYPE_REFERENCE_MULTI_SELECT)) {
        		final String repository = tree.getRepository();
            	col.setHtmlRenderer(new TreeColumnHtmlRenderer() {
        			public String renderHtml(TreeColumn treeColumn, Content content) {
        				try {
            				Collection uuids = content.getContent(treeColumn.getName()).getNodeDataCollection();
            				final StringBuffer result = new StringBuffer();
            				final HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
            				for(Iterator it = uuids.iterator(); it.hasNext();) {
            					final NodeData uuid = (NodeData)it.next();
            					result.append(hm.getContentByUUID(uuid.getString()).getHandle()).append(", ");
            				}
            				if(result.length() > 1){
            					result.setLength(result.length()-2);
            				}
							return result.toString();
						} catch (AccessDeniedException e) {
							e.printStackTrace();
						} catch (PathNotFoundException e) {
							e.printStackTrace();
						} catch (RepositoryException e) {
							e.printStackTrace();
						}
						return "??";
        			}
        		});
        	}
            tree.addColumn(col);
        }
        
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass(StringUtils.EMPTY);
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        columnIcons.setIconsPermission(true);
        
        tree.addColumn(columnIcons);

	}

	public void prepareContextMenu(Tree tree, boolean browseMode, HttpServletRequest request) {
        ContextMenuItem menuNew = new ContextMenuItem("new");
        menuNew.setLabel(msgs.get("module.data.tree.data.menu.new"));
        menuNew.setIcon(request.getContextPath() + "/.resources/icons/16/document_add.gif");
        menuNew.setOnclick("mgnl.data.DataTree.create(" + tree.getJavascriptTree() + ".selectedNode.id, '"+ Constants.DATA_DIALOG_PRE + itemType + "');");
//        menuNew.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotNodeType("
//            + tree.getJavascriptTree()
//            + ",'" + itemType + "')");
        
        ContextMenuItem menuEdit = new ContextMenuItem("edit");
        menuEdit.setLabel(msgs.get("module.data.tree.data.menu.edit"));
        menuEdit.setIcon(request.getContextPath() + "/.resources/icons/16/document_edit.gif");
        menuEdit.setOnclick("mgnl.data.DataTree.edit(" 
        		+ tree.getJavascriptTree()
        		+ ",'" + Constants.DATA_DIALOG_PRE + itemType + "');");
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

	public void prepareFunctionBar(Tree tree, boolean browseMode, HttpServletRequest request) {
        FunctionBar bar = tree.getFunctionBar();
        ContextMenu menu = tree.getMenu();
        bar.setSearchable(false);
        
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("new")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("edit")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("delete")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("activate")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("deactivate")));
        bar.addMenuItem(null);
        
        ContextMenuItem menuDeleteAll = new FunctionBarItem("deleteAll");
        menuDeleteAll.setLabel(msgs.get("module.data.tree.data.menu.deleteAll"));
        menuDeleteAll.setIcon(request.getContextPath() + "/.resources/icons/16/delete2.gif");
        menuDeleteAll.setOnclick("mgnl.data.DataTree.deleteAll(" + tree.getJavascriptTree() + ");");

        ContextMenuItem menuActivateAll = new FunctionBarItem("activateAll");
        menuActivateAll.setLabel(msgs.get("module.data.tree.data.menu.activateAll"));
        menuActivateAll.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green.gif");
        menuActivateAll.setOnclick("mgnl.data.DataTree.activateAll(" + tree.getJavascriptTree() + ");");

        ContextMenuItem menuDeactivateAll = new FunctionBarItem("deactivateAll");
        menuDeactivateAll.setLabel(msgs.get("module.data.tree.data.menu.deactivateAll"));
        menuDeactivateAll.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_left_red.gif");
        menuDeactivateAll.setOnclick("mgnl.data.DataTree.deactivateAll(" + tree.getJavascriptTree() + ");");

        if (!Subscriber.isSubscribersEnabled()) {
        	menuActivateAll.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        	menuDeactivateAll.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }
        
        bar.addMenuItem(menuDeleteAll);
        bar.addMenuItem(menuActivateAll);
        bar.addMenuItem(menuDeactivateAll);
        
	}

}
