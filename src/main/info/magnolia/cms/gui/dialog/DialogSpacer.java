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

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogSpacer {

    public DialogSpacer() {
    }

    public String getHtml() {
        return this.getHtml(0, 0);
    }

    public String getHtml(int heightWidth) {
        return this.getHtml(heightWidth, heightWidth);
    }

    public String getHtml(int height, int width) {
        String h = "";
        String w = "";
        if (height != 0) {
            h = "height:" + height + ";";
        }
        if (width != 0) {
            w = "width:" + width + ";";
        }
        return ("<div class=\"" + DialogSuper.CSSCLASS_TINYVSPACE + "\" style=\"" + w + "" + h + "\"></div>");
    }
}
