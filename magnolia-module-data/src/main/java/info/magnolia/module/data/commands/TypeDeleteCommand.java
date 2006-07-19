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
package info.magnolia.module.data.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.data.DataModule;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 *
 * @author Enrico Kufahl (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 * 
 */
public class TypeDeleteCommand extends MgnlCommand{

	private static final Logger log = LoggerFactory.getLogger(TypeDeleteCommand.class);

	
    public boolean execute(Context ctx) {
    	
        String path = (String) ctx.get(Context.ATTRIBUTE_PATH);
        String node = (String) ctx.get("deleteNode"); //$NON-NLS-1$
        try {
	    	Collection items = ContentRepository.getHierarchyManager(DataModule.getRepository()).getRoot().getChildren(new ItemType(node));
	    	if(items.size() > 0){
	        	AlertUtil.setMessage(ctx.getMessages("info.magnolia.module.data.messages").get("module.data.command.type.delete.error", new Object[]{node}));
	        	return false;
	    	}
	        deleteNode(ctx, path, node);
	        deleteSubMenu(ctx, node);
	    }
        catch (Exception e) {
        	log.error("cannot do delete", e);
            return true;
        }
        return false;
    }

    private void deleteNode(Context context, String parentPath, String label) throws Exception{
        try {
	        Content parentNode = MgnlContext.getHierarchyManager(ContentRepository.CONFIG).getContent(parentPath);
	        Content node = parentNode.getChildByName(label);
	        if(node != null){
	            if(node.getMetaData().getIsActivated()){
	            	deactivateNode(context, node.getHandle());
	            }
	            node.delete();
	            parentNode.save();
	        }
        } catch (Exception e) {
            AlertUtil.setMessage(context.getMessages("info.magnolia.module.data.messages").get("module.data.command.type.delete.error..deleteNode") + " " + AlertUtil.getExceptionMessage(e));
            throw e;
        }
    }
    
    private void deleteSubMenu(Context context, String node) throws Exception{
        try {
	    	Content dataMenu = context.getHierarchyManager(ContentRepository.CONFIG).getContent("/modules/adminInterface/config/menu/data");
	    	Content subMenu = dataMenu.getChildByName(node);
	    	if(subMenu != null){
	    		if(subMenu.getMetaData().getIsActivated()){
	    			deactivateMenu(context, subMenu.getHandle());
	    		}
	        	subMenu.delete();
	        	dataMenu.save();
	    	}
        } catch (Exception e) {
            AlertUtil.setMessage(context.getMessages("info.magnolia.module.data.messages").get("module.data.command.type.delete.error.deleteMenu") + " " + AlertUtil.getExceptionMessage(e));
            throw e;
        }
    }
    
    private void deactivateNode(Context context, String path) throws Exception{
        try {
	        Rule rule = new Rule();
	        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
	        syndicator.init(context.getUser(), ContentRepository.CONFIG, ContentRepository
	            .getDefaultWorkspace(ContentRepository.CONFIG), rule);
	        syndicator.deActivate(path);
        } catch (Exception e) {
            AlertUtil.setMessage(context.getMessages("info.magnolia.module.data.messages").get("module.data.command.type.delete.error.deactivateNode") + " " + AlertUtil.getExceptionMessage(e));
            throw e;
        }
    }

    private void deactivateMenu(Context context, String path) throws Exception{
        try {
	        Rule rule = new Rule();
	        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
	        syndicator.init(context.getUser(), ContentRepository.CONFIG, ContentRepository
	            .getDefaultWorkspace(ContentRepository.CONFIG), rule);
	       	syndicator.deActivate(path);
        } catch (Exception e) {
            AlertUtil.setMessage(context.getMessages("info.magnolia.module.data.messages").get("module.data.command.type.delete.error.deactivateMenu") + " " + AlertUtil.getExceptionMessage(e));
            throw e;
        }
    }



}
