/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.gui.control.ControlImpl;
import info.magnolia.cms.gui.control.Password;
import info.magnolia.cms.gui.misc.CssConstants;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogPassword extends DialogBox {

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogPassword() {
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        Password control = new Password(this.getName(), this.getValue());
        if (this.getConfigValue("saveInfo").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
            control.setSaveInfo(false);
        }
        control.setCssClass(CssConstants.CSSCLASS_EDIT);
        control.setCssStyles("width", this.getConfigValue("width", "100%")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        control.setEncoding(ControlImpl.ENCODING_BASE64);
        if (this.getConfigValue("onchange", null) != null) { //$NON-NLS-1$
            control.setEvent("onchange", this.getConfigValue("onchange")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.drawHtmlPre(out);
        out.write(control.getHtml());
        if (this.getConfigValue("verification", "true").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Password control2 = new Password(this.getName() + "_verification", StringUtils.EMPTY); //$NON-NLS-1$
            // Password control2=new Password(this.getName()+"_verifiaction",this.getValue());
            // control2.setEncoding(ControlImpl.ENCODING_UNIX);
            control2.setSaveInfo(false);
            control2.setCssClass(CssConstants.CSSCLASS_EDIT);
            control2.setCssStyles("width", //$NON-NLS-1$
                this.getConfigValue("width", "100%")); //$NON-NLS-1$ //$NON-NLS-2$
            control2.setEvent("onchange", //$NON-NLS-1$
                "mgnlDialogPasswordVerify('" + this.getName() + "')"); //$NON-NLS-1$ //$NON-NLS-2$
            // todo: verification on submit; think about
            out.write("<div class=\"" //$NON-NLS-1$
                + CssConstants.CSSCLASS_DESCRIPTION
                + "\">" //$NON-NLS-1$
                + getMessage("dialog.password.verify") //$NON-NLS-1$
                + "</div>"); //$NON-NLS-1$
            out.write(control2.getHtml());
        }
        this.drawHtmlPost(out);
    }
}
