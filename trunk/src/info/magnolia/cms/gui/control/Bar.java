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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: enz
 * Date: Jul 19, 2004
 * Time: 3:50:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class Bar extends ControlSuper {
	private ArrayList buttonsLeft=new ArrayList();
	private ArrayList buttonsRight=new ArrayList();
	private boolean small=true;

	public void setButtonsLeft(ArrayList buttons) {	this.buttonsLeft = buttons;}
	public void setButtonsLeft(Button button) {	this.getButtonsLeft().add(button);}
	public ArrayList getButtonsLeft() {return this.buttonsLeft;}

	public void setButtonsRight(ArrayList buttons) {this.buttonsRight = buttons;}
	public void setButtonsRight(Button button) {this.getButtonsRight().add(button);}
	public ArrayList getButtonsRight() {return this.buttonsRight;}


	public void setSmall(boolean b) {this.small=b;}
	public boolean getSmall() {return this.small;}


	public String getHtml() {
		String html="";
		String cssClass;
		if (this.getSmall()) cssClass=CSSCLASS_CONTROLBARSMALL;
		else cssClass=CSSCLASS_CONTROLBAR;
		html+="<table";
		html+=" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"";
		html+=this.getHtmlEvents();
		html+=" class=\""+cssClass+"\"";
		if (this.getId()!=null) html+=" id=\""+this.getId()+"\"";
		html+=">";

		html+="<tr><td class=\""+cssClass+"\">";

		Iterator itLeft=this.getButtonsLeft().iterator();
		while (itLeft.hasNext()) {
			Button b=(Button) itLeft.next();
			if (this.getSmall()) b.setSmall(true);
			b.setCssStyles("background","transparent");
			b.setSaveInfo(false);
			html+=b.getHtml()+"\n";
		}

		html+="</td><td class=\""+cssClass+"\" align=\"right\">";

		Iterator itRight=this.getButtonsRight().iterator();
		while (itRight.hasNext()) {
			Button b=(Button) itRight.next();
			if (this.getSmall()) b.setSmall(true);
			b.setCssStyles("background","transparent");
			b.setSaveInfo(false);
			html+=b.getHtml()+"\n";
		}

		html+="</td></tr></table>";

		return html;
	}


}

