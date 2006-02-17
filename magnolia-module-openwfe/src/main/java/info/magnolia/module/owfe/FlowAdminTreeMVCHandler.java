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

package info.magnolia.module.owfe;

import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.MailHandler;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import openwfe.org.embed.impl.engine.PersistedEngine;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;

import org.apache.log4j.Logger;

/**
 * This is a subclass of the regular MVCHandler to plug in flow events.
 * 
 * In this case, only the activate method is part of a flow. We should find a way to plug in flow on the different methods.
 * 
 * @author jackie
 * @authro Niko
 */

public abstract class FlowAdminTreeMVCHandler extends AdminTreeMVCHandler {

	private static Logger log = Logger.getLogger(FlowAdminTreeMVCHandler.class);

	public FlowAdminTreeMVCHandler(String name, HttpServletRequest request,
			HttpServletResponse response) {
		super(name, request, response);
	}
	
	public String activate() {
		boolean recursive = (request.getParameter("recursive") != null); //$NON-NLS-1$
		try {
			log.debug("Launch Activate Flow");
			//Get a reference to the workflow engine
			PersistedEngine engine = OWFEEngine.getEngine();
			
			// Create a new LaunchItem
			LaunchItem li = new LaunchItem();
			li.setWorkflowDefinitionUrl("field:__definition__");
			li.addAttribute("recursive", new StringAttribute(recursive ? "true"
					: "false"));
			li.addAttribute("pathSelected", new StringAttribute(pathSelected));
			li.addAttribute("OK", new StringAttribute("false"));
			li.addAttribute("Action",new StringAttribute("Activate"));

			// Retrieve and add the flow definition to the LaunchItem
			String flowDef = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<process-definition "
					+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
					+ "xsi:noNamespaceSchemaLocation=\"http://www.openwfe.org/flowdef_r1.5.0.xsd\" "
					+ "name=\"docflow\" "
					+ "revision=\"1.0\">"
					+ "<description language=\"default\"> "
					+ "This just the complete flow definition of docflow process. "
					+ "</description>" + "<sequence>"
					+ "<participant ref=\"user-superuser\"/>" + "</sequence>"
					+ "</process-definition>";
			li.getAttributes().puts("__definition__", flowDef);
			
			// Start the engine
			engine.launch(li, true);
			
			// Notify by email ? Should be in the flow itself, not here.
			new Thread(new MailSender(pathSelected)).start();
			
			// Alert the user using a javascript notification
			String message = "Your request has been dispatched, please wait for approval";
			AlertUtil.setMessage(message,request);
		} catch (Exception e) {
			log.error("can't launch activate flow", e);
			AlertUtil.setMessage(AlertUtil.getExceptionMessage(e), request);
		}
		return VIEW_TREE;
	}

	/**
	 * Override this method to configure the tree control (define the columns,
	 * ...)
	 * 
	 * @param tree
	 * @param request
	 */
	protected abstract void prepareTree(Tree tree, HttpServletRequest request);

	/**
	 * Prepare the context menu of the tree. This is called during renderTree
	 * 
	 * @param tree
	 * @param request
	 */
	protected abstract void prepareContextMenu(Tree tree,
			HttpServletRequest request);

}
/**
 * One time usage thread to send an async email
 * @author niko
 *
 */
class MailSender implements Runnable {
	static Logger logt = Logger.getLogger("MailSender");
	private String smtpHost = "localhost";
	private String from = "MagnoliaWorkflow";
	private String subject = "Workflow Request";
	private String list = "jackie_juju@hotmail.com";
	private String pathSelected;

	public MailSender(String pathSelected)
	{
		this.pathSelected = pathSelected;
	}

	public void run() {
		try {
			MailHandler mh = new MailHandler(smtpHost, 1, 0);
			mh.setFrom(from);
			mh.setSubject(subject);
			mh.setToList(list);
			mh.setBody("The following page is waiting for approval"+pathSelected);
			mh.sendMail();
		} catch (Exception e) {
			logt.error("Could not send email", e);
		}
	}
}
