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
import javax.jcr.PropertyType;
import java.io.IOException;

/**
 *
 * User: enz
 * Date: Jun 9, 2004
 * Time: 2:53:43 PM
 *
 */
public class DialogLink extends DialogEditWithButton {
	private static Logger log = Logger.getLogger(DialogLink.class);

	public DialogLink(ContentNode configNode,Content websiteNode) {
		super(configNode,websiteNode);
		init();
	}

	public DialogLink(String name) {
		this.setName(name);
		init();
	}

	private void init() {
		String extension=this.getConfigValue("extension");
		this.getButton().setLabel("Internal link...");
		this.getButton().setSaveInfo(false);
		this.getButton().setOnclick("mgnlDialogOpenLinkBrowser('"+this.getName()+"','"+extension+"');");
	}

}
