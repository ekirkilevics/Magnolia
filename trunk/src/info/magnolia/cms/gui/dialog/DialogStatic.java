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

import javax.servlet.jsp.JspWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: enz
 * Date: Jun 4, 2004
 * Time: 9:17:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class DialogStatic extends DialogBox {
	private static Logger log = Logger.getLogger(DialogStatic.class);

	public DialogStatic() {

	}

	public DialogStatic(ContentNode configNode,Content websiteNode) {
		super(configNode,websiteNode);
	}



	public void drawHtml(JspWriter out) {

		this.drawHtmlPre(out);
		try {
			String value=this.getConfigValue("value",null);
			if (value==null) value=this.getValue();
			out.println(value);
		}
		catch (IOException ioe) {log.error("");}
		this.drawHtmlPost(out);
	}



}
