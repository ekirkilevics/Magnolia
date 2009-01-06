/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;


/**
 * Password field.
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Password extends ControlImpl {

    public Password() {
    }

    public Password(String name, String value) {
        super(name, value);
    }

    public Password(String name, Content websiteNode) {
        super(name, websiteNode);
    }

    public String getHtml() {
        StringBuffer html = new StringBuffer();
        String value = StringUtils.EMPTY;
        if (this.getEncoding() == ENCODING_BASE64) {
            // show number of characters (using spaces)
            String valueDecoded = new String(Base64.decodeBase64(this.getValue().getBytes()));

            for (int i = 0; i < valueDecoded.length(); i++) {
                value += " "; //$NON-NLS-1$
            }
        }
        else if (this.getEncoding() == ENCODING_UNIX) {
            value = StringUtils.EMPTY;
        }
        else {
            value = this.getValue();
        }
        html.append("<input type=\"password\""); //$NON-NLS-1$
        html.append(" name=\"" + this.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(" id=\"" + this.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(" value=\"" + value + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(getHtmlEvents());
        html.append(this.getHtmlCssClass());
        html.append(this.getHtmlCssStyles());
        html.append(" />"); //$NON-NLS-1$
        if (this.getSaveInfo()) {
            html.append(this.getHtmlSaveInfo());
        }
        return html.toString();
    }
}
