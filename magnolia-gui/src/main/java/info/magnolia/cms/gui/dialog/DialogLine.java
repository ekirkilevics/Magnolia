/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
        html.append("\n<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:" + width + ";\">"); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(this.getHtml(1));
        html.append("</table>\n"); //$NON-NLS-1$
        return html.toString();
    }
}
