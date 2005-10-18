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

import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.misc.CssConstants;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogTab extends DialogSuper {

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogTab() {
    }

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
            + parentId + "'].items[mgnlControlSets['" //$NON-NLS-1$
            + parentId + "'].items.length]='" //$NON-NLS-1$
            + id + "';"); //$NON-NLS-1$
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
