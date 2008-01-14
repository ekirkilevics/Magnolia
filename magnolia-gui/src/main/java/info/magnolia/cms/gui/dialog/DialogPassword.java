/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
