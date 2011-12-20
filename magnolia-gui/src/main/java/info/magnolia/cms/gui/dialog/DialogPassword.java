/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
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

    private boolean useBcrypt;

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#drawHtml(Writer)
     */
    @Override
    public void drawHtml(Writer out) throws IOException {
        Password control = new Password(this.getName(), this.getValue());
        if (this.getConfigValue("saveInfo").equals("false")) {
            control.setSaveInfo(false);
        }
        control.setCssClass(CssConstants.CSSCLASS_EDIT);
        control.setCssStyles("width", this.getConfigValue("width", "100%"));
        if (useBcrypt) {
            control.setEncoding(ControlImpl.ENCRYPTION_HASH_BCRYPT);
        } else {
            control.setEncoding(ControlImpl.ENCRYPTION_NO_ENCODING_BASE64);
        }
        if (this.getConfigValue("onchange", null) != null) {
            control.setEvent("onchange", this.getConfigValue("onchange"));
        }
        this.drawHtmlPre(out);
        out.write(control.getHtml());
        if (this.getConfigValue("verification", "true").equals("true")) {
            Password control2 = new Password(this.getName() + "_verification", StringUtils.EMPTY);
            control2.setSaveInfo(false);
            control2.setCssClass(CssConstants.CSSCLASS_EDIT);
            control2.setCssStyles("width", this.getConfigValue("width", "100%"));
            control2.setEvent("onchange", "mgnlDialogPasswordVerify('" + this.getName() + "')");
            out.write("<div class=\"" + CssConstants.CSSCLASS_DESCRIPTION + "\">" + getMessage("dialog.password.verify") + "</div>");
            out.write(control2.getHtml());
        }
        this.drawHtmlPost(out);
    }

    @Override
    public boolean validate() {
        if (super.getConfigValue("verification", "true").equals("true")) {
            String newP = super.getRequest().getParameter(getName());
            String verP = super.getRequest().getParameter(getName() + "_verification");
            // we do not set passwords back and forth, we just send empty string of the same length of the original password (in case of base 64 or no encryption). So in this case trimmed newP has to be 0 as well as verP
            // in all other cases they have to match exactly.
            if (!(newP.trim().length() == 0 && verP.length() == 0) && !newP.equals(verP)) {
                setValidationMessage(getMessage("dialog.password.failed.js"));
                return false;
            }
        }
        return super.validate();
    }

    public boolean isBcrypt() {
        return useBcrypt;
    }

    public void setBcrypt(boolean useBcrypt) {
        this.useBcrypt = useBcrypt;
    }

}
