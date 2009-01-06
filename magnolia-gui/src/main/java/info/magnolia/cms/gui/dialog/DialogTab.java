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

import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.misc.CssConstants;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogTab extends DialogControlImpl {

    public void drawHtmlPreSubs(Writer out) throws IOException {
        String parentId = this.getParent().getId();
        String id = this.getId();
        // add tab button to tab set
        if (StringUtils.isNotEmpty(this.getLabel())) {
            Button control = new Button();
            control.setLabel(this.getMessage(this.getLabel()));
            control.setOnclick("mgnlDialogShiftTab('" + parentId + "','" + id + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            this.getParent().addOption(control);
        }
        // add tab to js object
        out.write("<script type=\"text/javascript\">"); //$NON-NLS-1$
        out.write("mgnlControlSets['" //$NON-NLS-1$
            + parentId
            + "'].items[mgnlControlSets['" //$NON-NLS-1$
            + parentId
            + "'].items.length]='" //$NON-NLS-1$
            + id
            + "';"); //$NON-NLS-1$
        out.write("</script>"); //$NON-NLS-1$
        // tab page
        out.write("<div id=\"" + id + "_div\" class=\"" + CssConstants.CSSCLASS_TAB + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        out.write("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">"); //$NON-NLS-1$
        out.write("<tr><td class=\"" + CssConstants.CSSCLASS_TAB + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        out
            .write("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout:fixed\">"); //$NON-NLS-1$
        out.write("<col width=\"200\" /><col />"); //$NON-NLS-1$
    }

    public void drawHtmlPostSubs(Writer out) throws IOException {
        out.write("</table>"); //$NON-NLS-1$
        out.write("</td></tr></table></div>"); //$NON-NLS-1$
    }
}
