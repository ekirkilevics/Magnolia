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
import java.util.Hashtable;
import java.util.Iterator;

/**
 *
 * User: enz
 * Date: Jun 9, 2004
 * Time: 10:54:57 AM
 *
 */
public class Select  extends ControlSuper {
	private ArrayList options=new ArrayList();

	public Select() {

	}

	public Select(String name,String value) {
		super(name,value);
	}

	public Select(String name,Content websiteNode) {
		super(name,websiteNode);
	}

	public void setOptions(ArrayList l) {	this.options = l;}
	public void setOptions(SelectOption option) {	this.getOptions().add(option);}
	//public void setOptions(Hashtable option) {	this.getOptions().add(option);}
	public void setOptions(String label, String value) {this.getOptions().add(new SelectOption(label,value));}
	public ArrayList getOptions() {return this.options;}


	public String getHtml() {
		String html="";

		html+="<select";
		html+=" name=\""+this.getName()+"\"";
		html+=" id=\""+this.getName()+"\"";
		html+=this.getHtmlCssClass();
		html+=this.getHtmlCssStyles();
		html+=this.getHtmlEvents();
		html+=">";

		Iterator it=this.getOptions().iterator();
		while (it.hasNext()) {
			SelectOption o=(SelectOption) it.next();

			if (!this.getValue().equals("")) {
				if (this.getValue().equals(o.getValue())) o.setSelected(true);
				else o.setSelected(false);
			}

			html+=o.getHtml();
		}

		html+="</select>";

		if (this.getSaveInfo()) html+=this.getHtmlSaveInfo();
		return html;
	}
}