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
 * Created by IntelliJ IDEA.
 * User: enz
 * Date: May 18, 2004
 * Time: 2:20:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edit extends ControlSuper {
	String rows="1";

	public Edit() {

	}

	public Edit(String name,String value) {
		super(name,value);
	}

	public Edit(String name,Content websiteNode) {
		super(name,websiteNode);
	}

	public void setRows(String s) {this.rows=s;}
	public String getRows() {return this.rows;}

	public String getHtml() {
		String html="";

		String id=this.getId();
		if (id==null) id=this.getName();

		if (this.getRows().equals("1")) {
			html+="<input type=\"text\"";
			html+=" name=\""+this.getName()+"\"";
			html+=" id=\""+id+"\"";
			html+=" value=\""+this.getValue()+"\"";
			html+=getHtmlEvents();
			html+=this.getHtmlCssClass();
			html+=this.getHtmlCssStyles();
			html+=">";
		}
		else {
			html+="<textarea";
			html+=" name=\""+this.getName()+"\"";
			html+=" id=\""+id+"\"";
			html+=" rows=\""+this.getRows()+"\"";
			html+=this.getHtmlCssClass();
			html+=this.getHtmlCssStyles();
			html+=getHtmlEvents();
			html+=">";
			html+=this.getValue();
			html+="</textarea>";
		}
		if (this.getSaveInfo()) html+=this.getHtmlSaveInfo();
		return html;
	}

}
