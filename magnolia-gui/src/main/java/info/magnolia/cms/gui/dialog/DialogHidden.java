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

import info.magnolia.cms.gui.control.Hidden;

import java.io.IOException;
import java.io.Writer;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogHidden extends DialogBox {

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogHidden() {
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        Hidden control = new Hidden(this.getName(), this.getValue());
        if (this.getConfigValue("saveInfo").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
            control.setSaveInfo(false);
        }
        if (this.getConfigValue("type", null) != null) { //$NON-NLS-1$
            control.setType(this.getConfigValue("type"));
        }

        out.write(control.getHtml());
    }
}
