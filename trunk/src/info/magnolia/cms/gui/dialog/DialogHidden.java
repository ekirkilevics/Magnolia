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
import info.magnolia.cms.gui.control.Hidden;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: enz
 * Date: May 27, 2004
 * Time: 4:05:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DialogHidden  extends DialogBox {
	private static Logger log = Logger.getLogger(DialogHidden.class);

	public DialogHidden(ContentNode configNode,Content websiteNode) {
		super(configNode,websiteNode);
	}

	public DialogHidden() {

	}


	//public void drawHtmlPre(JspWriter out,String id) {}
	//public void drawHtmlPost(JspWriter out,String id) {}


	public void drawHtml(JspWriter out) {
		Hidden control=new Hidden(this.getName(),this.getValue());
		if (this.getConfigValue("saveInfo").equals("false")) control.setSaveInfo(false);
		try {
			out.println(control.getHtml());
		}
		catch (IOException ioe) {log.error("");}
	}



}
