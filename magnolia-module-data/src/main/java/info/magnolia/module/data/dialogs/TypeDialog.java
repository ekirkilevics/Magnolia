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
package info.magnolia.module.data.dialogs;
	    
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.module.admininterface.SaveHandler;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;
import info.magnolia.module.data.Constants;
import info.magnolia.module.data.DataModule;
import info.magnolia.module.data.commands.DataActivateAllCommand;
import info.magnolia.module.data.commands.DataDeactivateAllCommand;
import info.magnolia.module.data.commands.DataDeleteAllCommand;
import info.magnolia.module.data.save.UUIDConversionSaveHandler;
import info.magnolia.module.data.trees.GenericDataAdminTree;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.StringValue;

/**
 * Handles the document upload and adds some special properties (for searching)
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class TypeDialog extends ConfiguredDialog {


    private boolean create;

    private String version;

    /**
     * @param name
     * @param request
     * @param response
     * @param configNode
     */
    public TypeDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
        this.version = request.getParameter("mgnlVersion");
    }

    /**
     * Overriden to force creation if the node does not exist
     */
    protected boolean onPreSave(SaveHandler handler) {
        // check if this is a creation
        this.create = handler.getPath().endsWith("/mgnlNew");

        if(this.create){
            handler.setCreate(true);

            String path = StringUtils.substringBeforeLast(handler.getPath(), "/");

            String name = form.getParameter("title");
            name = Path.getValidatedLabel(name);
            if (name.matches("^-*$")) {
                name = "data";
            }

            name = Path.getUniqueLabel(hm, path, name);
            this.path = path + "/" + name;
            handler.setPath(this.path);
            handler.setCreationItemType(ItemType.CONTENT);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#onPostSave(info.magnolia.cms.gui.control.Save)
     */
    protected boolean onPostSave(SaveHandler handler) {
        super.onPostSave(handler);
        Content type = this.getStorageNode();
        try {
        	final String dialogName = configureDialog(type);
            configureTree(type, dialogName);

			Content dataMenu = hm.getContent("/modules/adminInterface/config/menu/data");
			Content subMenu = dataMenu.getChildByName(type.getName());
			
			if(subMenu == null){
				subMenu = dataMenu.createContent(type.getName(), ItemType.CONTENTNODE);
				subMenu.createNodeData("label", new StringValue(type.getTitle()));
				subMenu.createNodeData("onclick", new StringValue("MgnlAdminCentral.showTree('" + Constants.DATA_TREE_PRE +  type.getName() + "')"));
				subMenu.createNodeData("icon", new StringValue("/.resources/icons/16/dot.gif"));
			}else{
				subMenu.setNodeData("label", new StringValue(type.getTitle()));
			}
			dataMenu.save();

            
            
            
		} catch (AccessDeniedException e) {
			throw new RuntimeException(e);
		} catch (PathNotFoundException e) {
			throw new RuntimeException(e);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
        return true;
    }

	protected String configureDialog(Content type) throws RepositoryException {
		final String dialogName = Constants.DATA_DIALOG_PRE + type.getName();
		if(!type.hasContent(Constants.TYPE_DIALOGS_NODE)) {
			final Content dialog = type.createContent(Constants.TYPE_DIALOGS_NODE).createContent(dialogName);
			//here add the dialog fields
			dialog.createNodeData(Constants.TYPE_DIALOG_CLASS).setValue(DataDialog.class.getName());
			dialog.createNodeData(Constants.TYPE_DIALOG_SAVE_HANDLER).setValue(UUIDConversionSaveHandler.class.getName());
			dialog.createNodeData(Constants.TYPE_DIALOG_NAME).setValue(dialogName);
			dialog.createNodeData(Constants.TYPE_DIALOG_I18N_BASE).setValue("info.magnolia.module.data.messages");
//			dialog.createNodeData(Constants.TYPE_DIALOG_TYPE).setValue(type.getName());
			type.save();
		} else {
			// update dialog fields
		}
		return dialogName;
	}

	protected void configureTree(Content type, String dialogName) throws AccessDeniedException, PathNotFoundException, RepositoryException {
		if(type.getChildByName(Constants.TYPE_TREES_NODE) == null){
			Content trees = type.createContent(Constants.TYPE_TREES_NODE, ItemType.CONTENT);
			String treeName = Constants.DATA_TREE_PRE +  type.getName();
			Content tree = trees.createContent(treeName, ItemType.CONTENTNODE);
			NodeData clazz = tree.createNodeData(Constants.TYPE_TREE_CLASS);
			clazz.setValue(GenericDataAdminTree.class.getName());
			NodeData name = tree.createNodeData(Constants.TYPE_TREE_NAME);
			name.setValue(treeName);
			NodeData repository = tree.createNodeData(Constants.TYPE_TREE_REPOSITORY);
			repository.setValue(DataModule.getRepository());
			tree.createNodeData(Constants.TYPE_TREE_DIALOG).setValue(dialogName);
			
			// creating commands
			Content commandsNode = tree.createContent("commands", ItemType.CONTENTNODE);
			addCommand(commandsNode, Constants.DATA_COMMAND_DELETE_ALL, DataDeleteAllCommand.class.getName());
			addCommand(commandsNode, Constants.DATA_COMMAND_ACTIVATE_ALL, DataActivateAllCommand.class.getName());
			addCommand(commandsNode, Constants.DATA_COMMAND_DEACTIVATE_ALL, DataDeactivateAllCommand.class.getName());
			
			type.save();
		}
	}
	
	protected void addCommand(Content commandsNode, String name, String clazz) throws AccessDeniedException, PathNotFoundException, RepositoryException{
		Content command = commandsNode.createContent(name, ItemType.CONTENTNODE);
		command.createNodeData("impl", new StringValue(clazz)); //$NON-NLS-1$
	}
}
