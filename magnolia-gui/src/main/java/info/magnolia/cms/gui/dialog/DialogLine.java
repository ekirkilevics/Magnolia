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

import info.magnolia.cms.gui.misc.CssConstants;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogLine {

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogLine() {
    }

    public String getHtml() {
        return this.getHtml(0, 2, 0);
    }

    public String getHtml(int colspanLine) {
        return this.getHtml(0, colspanLine, 0);
    }

    public String getHtml(int colspanBeforeLine, int colspanLine) {
        return this.getHtml(colspanBeforeLine, colspanLine, 0);
    }

    public String getHtml(int colspanBeforeLine, int colspanLine, int colspanAfterLine) {
        String pre = StringUtils.EMPTY;
        String post = StringUtils.EMPTY;
        if (colspanBeforeLine != 0) {
            pre = "<td colspan=\"" + colspanBeforeLine + "\"></td>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (colspanAfterLine != 0) {
            pre = "<td colspan=\"" + colspanAfterLine + "\"></td>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return "<tr>" //$NON-NLS-1$
            + pre
            + "<td colspan=\"" //$NON-NLS-1$
            + colspanLine
            + "\" class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_BOXLINE
            + "\"></td>" //$NON-NLS-1$
            + post
            + "</tr>"; //$NON-NLS-1$
    }

    public String getHtml(String width) {
        StringBuffer html = new StringBuffer();
        html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:" + width + ";\">"); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(this.getHtml(1));
        html.append("</table>"); //$NON-NLS-1$
        return html.toString();
    }
}
