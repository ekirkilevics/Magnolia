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
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.gui.control.Password;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogPassword extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogPassword.class);

    public DialogPassword(ContentNode configNode, Content websiteNode) throws RepositoryException {
        super(configNode, websiteNode);
    }

    public DialogPassword() {
    }

    public void drawHtml(JspWriter out) throws IOException {
        Password control = new Password(this.getName(), this.getValue());
        if (this.getConfigValue("saveInfo").equals("false")) {
            control.setSaveInfo(false);
        }
        control.setCssClass(CSSCLASS_EDIT);
        control.setCssStyles("width", this.getConfigValue("width", "100%"));
        control.setEncoding(ControlSuper.ENCODING_BASE64);
        if (this.getConfigValue("onchange", null) != null) {
            control.setEvent("onchange", this.getConfigValue("onchange"));
        }
        this.drawHtmlPre(out);
        out.println(control.getHtml());
        if (this.getConfigValue("verification", "true").equals("true")) {
            Password control2 = new Password(this.getName() + "_verification", "");
            // Password control2=new Password(this.getName()+"_verifiaction",this.getValue());
            // control2.setEncoding(ControlSuper.ENCODING_UNIX);
            control2.setSaveInfo(false);
            control2.setCssClass(CSSCLASS_EDIT);
            control2.setCssStyles("width", this.getConfigValue("width", "100%"));
            control2.setEvent("onchange", "mgnlDialogPasswordVerify('" + this.getName() + "')");
            // todo: verification on submit; think about
            out.println("<div class=\"" + CSSCLASS_DESCRIPTION + "\">Please verify your entry:</div>");
            out.println(control2.getHtml());
        }
        this.drawHtmlPost(out);
    }
}
