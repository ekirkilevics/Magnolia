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



package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.Content;

/**
 *
 * User: enz
 * Date: May 27, 2004
 * Time: 4:06:51 PM
 *
 */
public class Hidden extends ControlSuper {

	public Hidden() {

	}

	public Hidden(String name,String value) {
		super(name,value);
	}


	public Hidden(String name,String value,boolean saveInfo) {
		super(name,value);
		this.setSaveInfo(saveInfo);
	}


	public Hidden(String name,Content websiteNode) {
		super(name,websiteNode);
	}

	public String getHtml() {
		String html="";
		html+="<input type=\"hidden\"";
		html+=" name=\""+this.getName()+"\"";
		html+=" id=\""+this.getName()+"\"";
		html+=" value=\""+this.getValue()+"\"";
		html+=">";
		html+=this.getHtmlSaveInfo();
		return html;
	}

}
