/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.gui.control.Hidden;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogHidden extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogHidden.class);

    public DialogHidden(ContentNode configNode, Content websiteNode) throws RepositoryException {
        super(configNode, websiteNode);
    }

    public DialogHidden() {
    }

    public void drawHtml(JspWriter out) throws IOException {
        Hidden control = new Hidden(this.getName(), this.getValue());
        if (this.getConfigValue("saveInfo").equals("false"))
            control.setSaveInfo(false);

        out.println(control.getHtml());
    }
}
