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

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogBox extends DialogControlImpl {

    public static final int BOXTYPE_2COLS = 0;

    public static final int BOXTYPE_1COL = 1;

    private int boxType = BOXTYPE_2COLS;

    public void setBoxType(int i) {
        this.boxType = i;
    }

    public int getBoxType() {
        String configBoxType = this.getConfigValue("boxType"); //$NON-NLS-1$
        if (configBoxType.equals("1Col")) { //$NON-NLS-1$
            return BOXTYPE_1COL;
        }
        return this.boxType;

    }

    public void drawHtmlPre(Writer out) throws IOException {
        if (this.getConfigValue("lineSemi", "false").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            out.write(new DialogLine().getHtml(1, 1));
        }
        else if (this.getConfigValue("line", "true").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            out.write(new DialogLine().getHtml());
        }
        out.write("<tr>"); //$NON-NLS-1$
        if (this.getBoxType() == BOXTYPE_2COLS) {
            out.write("<td style=\"width:1%\" class=\"" + CssConstants.CSSCLASS_BOXLABEL + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            // write the label
            out.write(this.getMessage(this.getLabel()));
            if (this.isRequired()) {
                out.write("(*)");
            }
            if (StringUtils.isNotEmpty(this.getConfigValue("labelDescription"))) { //$NON-NLS-1$
                String desc = this.getConfigValue("labelDescription"); //$NON-NLS-1$
                desc = this.getMessage(desc);
                out.write("<div class=\"" + CssConstants.CSSCLASS_DESCRIPTION + "\">" + desc + "</div>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            out.write("</td>"); //$NON-NLS-1$
            String cssClass = CssConstants.CSSCLASS_BOXINPUT;
            if (this.getClass().getName().indexOf("DialogStatic") != -1 //$NON-NLS-1$
                || this.getClass().getName().indexOf("DialogButton") != -1) { //$NON-NLS-1$
                cssClass = CssConstants.CSSCLASS_BOXLABEL;
            }
            out.write("<td style=\"width:100%\" class=\"" + cssClass + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
            out.write("<td style=\"width:100%\" colspan=\"2\" class=\"" + CssConstants.CSSCLASS_BOXLABEL + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            if (StringUtils.isNotEmpty(this.getLabel())) {
                out.write("<div class=\"" //$NON-NLS-1$
                    + CssConstants.CSSCLASS_BOXLABEL
                    + "\">" //$NON-NLS-1$
                    + this.getMessage(this.getLabel())
                    + (this.isRequired() ? "(*)" : "")
                    + "</div>"); //$NON-NLS-1$
            }
            if (StringUtils.isNotEmpty(this.getConfigValue("labelDescription"))) { //$NON-NLS-1$
                String desc = this.getConfigValue("labelDescription"); //$NON-NLS-1$
                out.write("<div class=\"" //$NON-NLS-1$
                    + CssConstants.CSSCLASS_DESCRIPTION
                    + "\">" //$NON-NLS-1$
                    + this.getMessage(desc)
                    + "</div>"); //$NON-NLS-1$
            }
        }
    }

    public void drawHtmlPost(Writer out) throws IOException {
        out.write(this.getHtmlDescription());

        if (this.getConfigValue("saveHandler") != null) {
            out.write("<input type=\"hidden\" name=\"");
            out.write(this.getName());
            out.write("_saveHandler\" value=\"");
            out.write(this.getConfigValue("saveHandler"));
            out.write("\" />");

            out.write("<input type=\"hidden\" name=\"");
            out.write(this.getName());
            out.write("_configNode\" value=\"");
            out.write(this.getConfigValue("handle"));
            out.write("\" />");
        }

        out.write("</td></tr>\n"); //$NON-NLS-1$
    }

    public String getHtmlDescription() {

        // use div to force a new line
        if (StringUtils.isNotEmpty(this.getDescription())) {
            String desc = this.getDescription();
            desc = this.getMessage(desc);
            return "<div class=\"" + CssConstants.CSSCLASS_DESCRIPTION + "\">" + desc + "</div>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        return StringUtils.EMPTY;
    }

}
