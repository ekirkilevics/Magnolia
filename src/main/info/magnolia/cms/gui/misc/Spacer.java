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
package info.magnolia.cms.gui.misc;

/**
 * Utility class. Draw a simple spacer div.
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Spacer {

    /**
     * utility class, don't instantiate.
     */
    private Spacer() {
    }

    /**
     * Draw a spacer div with the specified width and height.
     * @param height div height
     * @param width div width
     * @return html for the spacer
     */
    public static String getHtml(int height, int width) {

        StringBuffer buffer = new StringBuffer(120);
        buffer.append("<div class=\""); //$NON-NLS-1$
        buffer.append(CssConstants.CSSCLASS_TINYVSPACE);
        buffer.append("\" "); //$NON-NLS-1$

        if (height != 0 || width != 0) {
            buffer.append("style=\""); //$NON-NLS-1$
            if (height != 0) {
                buffer.append("height:" + height + "px;"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (width != 0) {
                buffer.append("width:" + width + "px;"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            buffer.append("\" "); //$NON-NLS-1$
        }

        buffer.append("><!-- ie --></div>"); //$NON-NLS-1$

        return buffer.toString();
    }
}
