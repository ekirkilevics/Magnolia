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
import javax.jcr.RepositoryException;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */ 
public class DialogButton extends DialogBox {
	private static Logger log = Logger.getLogger(DialogButton.class);


	public DialogButton(ContentNode configNode,Content websiteNode) throws RepositoryException {
		super(configNode,websiteNode);
	}

	public DialogButton() {
		
	}

	public void drawHtml(JspWriter out) {
		Button control=new Button();
		control.setSaveInfo(false);
		control.setLabel(this.getConfigValue("buttonLabel"));
		control.setOnclick(this.getConfigValue("onclick"));
		if (this.getConfigValue("small").equals("true")) control.setSmall(true);

		this.drawHtmlPre(out);
		try {
			out.println(control.getHtml());
		}
		catch (IOException ioe) {log.error("");}
		this.drawHtmlPost(out);
	}

}
