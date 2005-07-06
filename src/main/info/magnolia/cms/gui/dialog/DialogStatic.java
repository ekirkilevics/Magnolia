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

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogStatic extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogStatic.class);

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogStatic() {
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        this.drawHtmlPre(out);
        String value = this.getConfigValue("value", null); //$NON-NLS-1$
        if (value == null) {
            value = this.getValue();
        }
        out.write(value);
        this.drawHtmlPost(out);
    }
}
