/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



package info.magnolia.cms.gui.inline;

import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.util.Resource;
import info.magnolia.cms.beans.config.ContentRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.jcr.access.Permission;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: enz
 * Date: Jul 26, 2004
 * Time: 11:10:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class ButtonEdit extends Button {
	String label="Edit";

	public ButtonEdit() {
	}


	public ButtonEdit(HttpServletRequest request) {
		this.setRequest(request);
	}

	public ButtonEdit(String path,String nodeCollectionName, String nodeName, String paragraph) {
		this.setPath(path);
		this.setNodeCollectionName(nodeCollectionName);
		this.setNodeName(nodeName);
		this.setParagraph(paragraph);
	}

	public ButtonEdit(HttpServletRequest request, String path,String nodeCollectionName, String nodeName, String paragraph) {
		this.setRequest(request);
		this.setPath(path);
		this.setNodeCollectionName(nodeCollectionName);
		this.setNodeName(nodeName);
		this.setParagraph(paragraph);
	}


	public void setDefaultOnclick() {
		String nodeCollectionName=this.getNodeCollectionName();
		if (nodeCollectionName==null) nodeCollectionName="";
		String nodeName=this.getNodeName();
		if (nodeName==null) nodeName="";

		//todo: dynamic repository
		String repository=ContentRepository.WEBSITE;

		this.setOnclick("mgnlOpenDialog('"+this.getPath()+"','"+nodeCollectionName+"','"+nodeName+"','"+this.getParagraph()+"','"+repository+"');");
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel (String s) {
		this.label=s;
	}

	/**
	* <p>draws the edit button</p>
	 * <p>request has to be set!</p>
	* */
	public void drawHtml(JspWriter out) throws IOException {
		//todo: attribute for preview name not static!
		//todo: a method to get preview?
		if (this.getRequest()!=null) {
			String prev=(String) this.getRequest().getSession().getAttribute("mgnlPreview");
			boolean isGranted=Resource.getActivePage(this.getRequest()).isGranted(Permission.SET_PROPERTY);
			if (prev==null && isGranted) {
				out.println(this.getHtml());
			}
		}
	}




}
