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

import info.magnolia.cms.core.Content;

import java.util.ArrayList;

/**
 *
 * User: enz
 * Date: Jun 9, 2004
 * Time: 1:37:54 PM
 *
 */
public class SelectOption extends ControlSuper {
	private ArrayList options=new ArrayList();
	private String label=null;
	private boolean selected=false;


	public SelectOption(){
	}

	public SelectOption(String label,String value) {
		this.setLabel(label);
		this.setValue(value);
	}

	public SelectOption(String label,Content websiteNode) {
		this.setLabel(label);
		this.setWebsiteNode(websiteNode);
	}

	public void setSelected(boolean b) {this.selected=b;}
	public boolean getSelected() {return this.selected;}


	public void setLabel(String s) {this.label=s;}
	public String getLabel() {
		if (this.label!=null) return this.label;
		else return this.getValue();
	}


	public String getHtml() {
		String html="";

		html+="<option value=\""+this.getValue()+"\"";
		html+=this.getHtmlCssClass();
		html+=this.getHtmlCssStyles();
		if (this.getSelected()) html+=" selected";
		html+=this.getHtmlId(); //id e.g. needed in rich editor
		html+=">";
		//html+="["+this.getLabel()+"]["+this.getValue()+"]";
		html+=this.getLabel();
		html+="</option>";
		return html;
	}


}
