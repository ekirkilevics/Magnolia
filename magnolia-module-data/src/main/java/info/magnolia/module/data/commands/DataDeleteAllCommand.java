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
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.module.data.Constants;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 *
 * @author Enrico Kufahl (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 * 
 */
public class DataDeleteAllCommand extends MgnlCommand{

	private static final Logger log = LoggerFactory.getLogger(DataDeleteAllCommand.class);

	
    public boolean execute(Context ctx) {
        String repository = (String) ctx.get(Context.ATTRIBUTE_REPOSITORY);
        String itemType = (String) ctx.get(Constants.DATA_COMMAND_CONTEXT_ITEM_TYPE);
    	try {
			Content root = ContentRepository.getHierarchyManager(repository).getRoot();
			Collection nodes = root.getChildren(itemType);
			deleteAll(nodes, ctx.getUser(), repository);
			root.save();
		} catch (Exception e) {
			AlertUtil.setMessage(ctx.getMessages("info.magnolia.module.data.messages").get("module.data.command.data.deleteAll.error", new Object[]{itemType}) + " " + AlertUtil.getExceptionMessage(e));
        	log.error("cannot delete all data of type: " + itemType, e);
            return true;
		}
        return false;
    }
    
    public void deleteAll(Collection content, User user, String repository) throws ExchangeException, RepositoryException {
    	boolean subscribed = Subscriber.isSubscribersEnabled();
    	for(Iterator nodes = content.iterator(); nodes.hasNext();){
			Content node = (Content)nodes.next();
			if(subscribed && node.getMetaData().getIsActivated()){
				DataDeactivateAllCommand.doDeactivate(user, node.getHandle(), repository);
			}
			node.delete();
		}
    }
}
