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

import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;

import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: enz
 * Date: May 17, 2004
 * Time: 11:17:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class DialogTab extends DialogSuper {
	private static Logger log = Logger.getLogger(DialogTab.class);

	public DialogTab(ContentNode configNode,Content websiteNode) {
		super(configNode,websiteNode);
	}

	public DialogTab() {
		
	}



	public void drawHtmlPreSubs(JspWriter out) {
		try {
			String parentId=this.getParent().getId();
			String id=this.getId();

			//add tab button to tab set
			if (!this.getLabel().equals("")) {
				Button control=new Button();
				control.setLabel(this.getLabel());
				control.setOnclick("mgnlDialogShiftTab('"+parentId+"','"+id+"');");
				this.getParent().addOption(control);
			}

			//add tab to js object
			out.println("<script type=\"text/javascript\">");
			out.println("mgnlControlSets['"+parentId+"'].items[mgnlControlSets['"+parentId+"'].items.length]='"+id+"';");
			out.println("</script>");

			//tab page
			out.println("<div id=\""+id+"_div\" class=\""+CSSCLASS_TAB+"\">");
			out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
			out.println("<tr><td class=\""+CSSCLASS_TAB+"\">");
			out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
			out.println("<tr>");
			out.println("<td width=\"1%\"><img src=\"/admindocroot/0.gif\" height=\"1\" width=\"200\"></td>");
			out.println("<td width=\"100%\"><img src=\"/admindocroot/0.gif\" height=\"1\" width=\"200\"></td>");
			out.println("</tr>");
		}
		catch (IOException ioe) {log.error("");}
	}

	public void drawHtmlPostSubs(JspWriter out) {
		try {
			//out.println("<td></td><td><select name=xx><option>abcalskjdf </select></td></tr>");
			//out.println("<td></td><td><iframe src=http://www.obinary.com></iframe>");
			//out.println("<br><br><br><br><br><br><br><br>XXX</td></tr>");
			out.println("</td></tr></table>");
			out.println("</table></div>");
		}
		catch (IOException ioe) {log.error("");}
	}

}
