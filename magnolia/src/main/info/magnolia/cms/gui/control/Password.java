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
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;


/**
 * Password field.
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Password extends ControlSuper {

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
