/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.cms.taglibs.util;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;


/**
 * Converts text in the body of the tag adding <br /> tags at new lines or wrapping lines in paragraphs.
 * @jsp.tag name="newlines" body-content="JSP"
 *
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class ConvertNewLineTag extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Use paragraphs.
     */
    private boolean para;

    /**
     * If true each line will be wrapped in a <p> tag. Default to false (use br's).
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setPara(boolean paragraphs) {
        this.para = paragraphs;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        String bodyText = bodyContent.getString();

        if (StringUtils.isNotEmpty(bodyText)) {
            StringTokenizer bodyTk = new StringTokenizer(bodyText, "\n", false); //$NON-NLS-1$
            JspWriter out = pageContext.getOut();

            try {
                if (this.para) {
                    // wrap text in p
                    while (bodyTk.hasMoreTokens()) {
                        out.write("<p>"); //$NON-NLS-1$
                        out.write(StringUtils.replaceChars(bodyTk.nextToken(), (char) 63, '\''));
                        out.write("</p>"); //$NON-NLS-1$
                    }
                }
                else {
                    // add newlines
                    while (bodyTk.hasMoreTokens()) {
                        out.write(StringUtils.replaceChars(bodyTk.nextToken(), (char) 63, '\''));
                        if (bodyTk.hasMoreTokens()) {
                            out.write("<br/>"); //$NON-NLS-1$
                        }
                    }
                }
            }
            catch (IOException e) {
                throw new JspTagException(e.getMessage());
            }
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        para = false;
        super.release();
    }

}
