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



package info.magnolia.cms.gui.dialog;

import org.apache.log4j.Logger;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.Content;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */ 
public class DialogBox extends DialogSuper {
	private static Logger log = Logger.getLogger(DialogBox.class);
	//private String title;
	//private String description;
	public final static int BOXTYPE_2COLS=0;
	public final static int BOXTYPE_1COL=1;

	private int boxType=BOXTYPE_2COLS;


	public DialogBox() {

	}



	public DialogBox(ContentNode configNode,Content websiteNode) {
		super(configNode,websiteNode);
	}


	public void setBoxType(int i) {this.boxType=i;}
	public int getBoxType() {
		String configBoxType=this.getConfigValue("boxType");
		if (configBoxType.equals("1Col")) return BOXTYPE_1COL;
		else return this.boxType;
	}

	public void drawHtmlPre(JspWriter out) {
		try {
			if (this.getConfigValue("lineSemi","false").equals("true")) out.println(new DialogLine().getHtml(1,1));
			else if (this.getConfigValue("line","true").equals("true")) out.println(new DialogLine().getHtml());

			out.println("<tr>");
			if (this.getBoxType()==BOXTYPE_2COLS) {
				out.println("<td width=\"1%\" class=\""+CSSCLASS_BOXLABEL+"\">");
				out.println(this.getLabel());
				if (!this.getConfigValue("labelDescription").equals("")) out.println("<div class=\""+CSSCLASS_DESCRIPTION+"\">"+this.getConfigValue("labelDescription")+"</div>");
				out.println("</td>");

				String cssClass=CSSCLASS_BOXINPUT;
				if (this.getClass().getName().indexOf("DialogStatic")!=-1 || this.getClass().getName().indexOf("DialogButton")!=-1) cssClass=CSSCLASS_BOXLABEL;
				out.println("<td width=\"100%\" class=\""+cssClass+"\">");
			}
			else {
				out.println("<td width=\"100%\" colspan=\"2\" class=\""+CSSCLASS_BOXLABEL+"\">");
				if (!this.getLabel().equals("")) out.println("<div class=\""+CSSCLASS_BOXLABEL+"\">"+this.getLabel()+"</div>");
				if (!this.getConfigValue("labelDescription").equals("")) out.println("<div class=\""+CSSCLASS_DESCRIPTION+"\">"+this.getConfigValue("labelDescription")+"</div>");
			}
		}
		catch (IOException ioe) {log.error("");}
	}

	public void drawHtmlPost(JspWriter out) {
		try {
			out.println(this.getHtmlDescription());
			out.println("</td></tr>");
		}
		catch (IOException ioe) {log.error("");}
	}
}

