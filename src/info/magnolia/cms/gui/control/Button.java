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

import javax.servlet.jsp.JspWriter;
import java.util.Collection;
import java.util.Hashtable;

/**
 * 
 * User: enz
 * Date: May 18, 2004
 * Time: 9:41:41 AM
 *
 */
public class Button extends ControlSuper {
	private String label=null;
	private String iconSrc=null;
	private String onclick="";

	private final String htmlPreDivided="<table cellpadding=0 cellspacing=0 border=0><tr><td>"; //html before buttton (radio, checkbox)
	private final String htmlInterDivided="</td><td>";					//html between button and label
	private final String htmlPostDivided="</td></tr></table>";			//html after label (radio, checkbox)

	private final String htmlPrePush="";    //html before buttton (push button)
	private final String htmlInterPush="";
	private final String htmlPostPush="";	 //html after button
	private int state=BUTTONSTATE_NORMAL;
	private int buttonType=BUTTONTYPE_PUSHBUTTON;

	private int labelNbspPadding=3;

	private String pushButtonTag="span";

	private boolean small=false;

	public Button(){
	}

	public Button(String name,String value) {
		super(name,value);
	}

	public Button(String name,Content websiteNode) {
		super(name,websiteNode);
	}

	// why padding with &nbsp;s ?
	// css padding causes problems in td of width=1 (td width equals to text width, not entire button)
	public void setLabelNbspPadding(int i) {this.labelNbspPadding=i;}
	public int getLabelNbspPadding() {return this.labelNbspPadding;}
	public String getHtmlLabelNbspPadding() {
		String html="";
		for (int i=0;i<this.getLabelNbspPadding();i++) {
			html+="&nbsp;";
		}
		return html;
	}

	public void setLabel(String s) {this.label=s;}
	public String getLabel() {
		if (this.label!=null) return this.label;
		else return this.getValue();
	}
	public void setIconSrc(String s) {this.iconSrc=s;}
	public String getIconSrc() {
		if (iconSrc==null) return "";
		else return "<img src=\""+this.iconSrc+"\">";
	}

	public void setOnclick(String s) {this.onclick=s;}
	public String getOnclick() {return this.onclick;}

	public void setHtmlPre() {
		if (super.getHtmlPre(null)==null) {
			if (this.getButtonType()==BUTTONTYPE_PUSHBUTTON) this.setHtmlPre(this.htmlPrePush);
			else this.setHtmlPre(this.htmlPreDivided);
		}
	}

	public void setHtmlInter() {
		if (super.getHtmlInter(null)==null) {
			if (this.getButtonType()==BUTTONTYPE_PUSHBUTTON) this.setHtmlInter(this.htmlInterPush);
			else this.setHtmlInter(this.htmlInterDivided);
		}
	}

	public void setHtmlPost() {
		if (super.getHtmlPost(null)==null) {
			if (this.getButtonType()==BUTTONTYPE_PUSHBUTTON) this.setHtmlPost(this.htmlPostPush);
			else this.setHtmlPost(this.htmlPostDivided);
		}
	}

	public void setSmall(boolean b) {this.small=b;}
	public boolean getSmall() {return this.small;}


	public void setPushButtonTag(String s) {this.pushButtonTag=s;}
	public String getPushButtonTag() {return this.pushButtonTag;}


	public String getHtml() {
		String html="";

		this.setHtmlPre();
		this.setHtmlInter();
		this.setHtmlPost();

		html+=this.getHtmlPre();
		if (this.getButtonType()==BUTTONTYPE_PUSHBUTTON) html+=this.getHtmlPushbutton();
		else html+=this.getHtmlDividedbutton();
		html+=this.getHtmlPost();
		return html;
	}

	public String getHtmlDividedbutton() {
		String html="";
		String buttonType;
		if (this.getButtonType()==BUTTONTYPE_RADIO) buttonType="radio";
		else buttonType="checkbox";
		html+="<input type=\""+buttonType+"\"";
		html+=" name=\""+this.getName()+"\"";
		html+=" value=\""+this.getValue()+"\"";
		html+=" id=\""+this.getId()+"\"";
		if (!this.getOnclick().equals("")) html+=" onclick=\""+this.getOnclick()+"\"";
		if (this.getState()==BUTTONSTATE_PUSHED) html+=" checked";
		html+=this.getHtmlCssClass();
		html+=this.getHtmlCssStyles();
		html+=">";
		if (this.getSaveInfo()) html+=this.getHtmlSaveInfo();
		html+=this.getHtmlInter();
		html+="<a href=\"javascript:mgnlShiftDividedButton('"+this.getId()+"');"+this.getOnclick()+"\" "+this.getHtmlCssClass()+">";
		//html+="["+this.getLabel()+"]["+this.getValue()+"]";
		html+=this.getIconSrc();
		html+=this.getLabel();
		html+="</a>";
		return html;
	}

	public String getHtmlPushbutton() {
		String html="";
		html+="<"+this.getPushButtonTag();
		if (this.getCssClass().equals("")) {
			if (this.getSmall()) {
				this.setCssClass(CSSCLASS_CONTROLBUTTONSMALL);
				this.setLabelNbspPadding(1);
			}
			else this.setCssClass(CSSCLASS_CONTROLBUTTON);
		}
		if (this.getState()==BUTTONSTATE_PUSHED) this.setCssClass(this.getCssClass()+"_PUSHED");
		html+=" onclick=\"mgnlShiftPushButtonClick(this);"+this.getOnclick()+"\"";
		html+=" onmousedown=\"mgnlShiftPushButtonDown(this);\"";
		html+=" onmouseout=\"mgnlShiftPushButtonOut(this);\"";
		html+=this.getHtmlId();
		html+=this.getHtmlCssClass();
		html+=this.getHtmlCssStyles();
		html+=">";
		html+="<nobr>";
		html+=this.getHtmlLabelNbspPadding();
		html+=this.getIconSrc();
		html+=this.getLabel();
		html+=this.getHtmlLabelNbspPadding();
		html+="</nobr>";
		html+="</"+this.getPushButtonTag()+">";
		return html;
	}

	public void setState(int i) {this.state=i;}
	public int getState() {return this.state;}

	public void setButtonType(int i) {this.buttonType=i;}
	public int getButtonType() {return this.buttonType;}

}
