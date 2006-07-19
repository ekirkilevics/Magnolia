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
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.module.data.Constants;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *
 * @author Enrico Kufahl (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 * 
 */
public class DataActivateAllCommand extends MgnlCommand{

    private static Logger log = LoggerFactory.getLogger(DataActivateAllCommand.class);

    public boolean execute(Context ctx) {
    	String repository = (String) ctx.get(Context.ATTRIBUTE_REPOSITORY);
	    String itemType = (String) ctx.get(Constants.DATA_COMMAND_CONTEXT_ITEM_TYPE);
        Syndicator syndicator = (Syndicator) ctx.get(Constants.DATA_COMMAND_CONTEXT_ACTIVATION_SYNDICATOR);
		try {
			Content root = ContentRepository.getHierarchyManager(repository).getRoot();
			Collection nodes = root.getChildren(itemType);
			activateAll(nodes, syndicator);
			root.save();
		} catch (Exception e) {
			AlertUtil.setMessage(ctx.getMessages("info.magnolia.module.data.messages").get("module.data.command.data.activateAll.error", new Object[]{itemType}) + " " + AlertUtil.getExceptionMessage(e));
	    	log.error("cannot activate all data of type: " + itemType, e);
	        return true;
		}
	    return false;
    }

    public void activateAll(Collection content, Syndicator syndicator) throws ExchangeException, RepositoryException {
    	for(Iterator nodes = content.iterator(); nodes.hasNext();){
			Content node = (Content)nodes.next();
		 	doActivate(syndicator, node.getHandle());
    	}
    }
    
    /**
     * do real activation
     * @param path node path
     * @throws RepositoryException 
     * @throws ExchangeException 
     */
    private void doActivate(Syndicator syndicator, String path) throws ExchangeException, RepositoryException {
        String parentPath = StringUtils.substringBeforeLast(path, "/");
        if (StringUtils.isEmpty(parentPath)) {
            parentPath = "/";
        }
        syndicator.activate(parentPath, path);

    }
}
