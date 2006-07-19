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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.admininterface.DialogHandlerManager;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;
import info.magnolia.module.data.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * 
 *
 * @author Christoph Hoffmann (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 * 
 */
public class GenericDataAdminTree extends AdminTreeMVCHandler {

	private String dialogName;
	private String nodeType;
	
	public GenericDataAdminTree(String name, HttpServletRequest request, HttpServletResponse response) {
		super(name, request, response);
	}
	
	protected void initialize() {
		super.initialize();
		ConfiguredDialog dialog = (ConfiguredDialog)DialogHandlerManager.getInstance().getDialogHandler(getDialogName(), request, response);
		try {
			nodeType = dialog.getConfigNode().getParent().getParent().getName();
			setCatalogueName(dialog.getName());
		} catch (Exception e) {
			throw new RuntimeException("cannot get the type of the data items to show.", e);
		}
		setConfiguration(new GenericDataAdminTreeConfig(dialog));
		
	}
	
	
	public Syndicator getActivationSyndicator(String path) {
        /*
         * Here rule defines which content types to collect, its a resposibility of the caller ro set this, it will be
         * different in every hierarchy, for instance - in website tree recursive activation : rule will allow
         * mgnl:contentNode, mgnl:content and nt:file - in website tree non-recursive activation : rule will allow
         * mgnl:contentNode and nt:file only
         */
		return getActivationSyndicator(this.getRepository(), MgnlContext.getUser());
	}

	public static Syndicator getActivationSyndicator(String repository, User user ){
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENT.getSystemName());
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_METADATA);
        rule.addAllowType(ItemType.NT_RESOURCE);

        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(user, repository, ContentRepository.getDefaultWorkspace(repository), rule);

        return syndicator;
	}
	
	public String getCommand() {
        if (StringUtils.isNotEmpty(this.getRequest().getParameter(Constants.DATA_COMMAND_PARAM))) { 
        	String action = this.getRequest().getParameter(Constants.DATA_COMMAND_PARAM);
        	if(action.equals(Constants.DATA_COMMAND_DELETE_ALL) || action.equals(Constants.DATA_COMMAND_ACTIVATE_ALL) || action.equals(Constants.DATA_COMMAND_DEACTIVATE_ALL) ){
        		return action;
        	}
        }
		return super.getCommand();
	}
	
	protected Context getCommandContext(String commandName) {
		Context ctx = super.getCommandContext(commandName);
		ctx.put(Constants.DATA_COMMAND_CONTEXT_ITEM_TYPE, nodeType);
		ctx.put(Constants.DATA_COMMAND_CONTEXT_ACTIVATION_SYNDICATOR, getActivationSyndicator("/"));
		return ctx;
	}
	
	public String getDialogName() {
		return dialogName;
	}

	public void setDialogName(String dialogName) {
		this.dialogName = dialogName;
	}

}
