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
package info.magnolia.cms.taglibs.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Draws an html table.
 * 
 * @jsp.tag name="table" body-content="JSP"
 *
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class TableTag extends BodyTagSupport {

    /**
     * logger.
     */
    private static Logger log = LoggerFactory.getLogger(TableTag.class);

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private boolean header;

    private Map htmlAttributes = new HashMap();

    /**
     * Use first row as column headers.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setHeader(boolean header) {
        this.header = header;
    }

    /**
     * Standard html attribute.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setClass(String value) {
        this.htmlAttributes.put("class", value);
    }

    /**
     * Standard html attribute.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setStyle(String value) {
        this.htmlAttributes.put("style", value);
    }

    /**
     * Standard html attribute.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setId(String value) {
        this.htmlAttributes.put("id", value);
    }

    /**
     * Standard html attribute.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setCellspacing(String value) {
        this.htmlAttributes.put("cellspacing", value);
    }

    /**
     * Standard html attribute.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setCellpadding(String value) {
        this.htmlAttributes.put("cellpadding", value);
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {
        String data = getBodyContent().getString();
        JspWriter out = pageContext.getOut();

        if (StringUtils.isEmpty(data)) {
            return EVAL_PAGE;
        }

        try {
            out.print("<table cellspacing=\"0\" ");
            writeAttributes(out, htmlAttributes);
            out.print(">\n");

            String[] rows = data.split("\n");

            int startingRow = 0;

            if (header && rows.length > 0) {
                startingRow = 1; // for body
                out.print("<thead>\n");
                out.print("<tr>\n");

                String[] cols = StringUtils.splitPreserveAllTokens(rows[0], "\t");
                for (int col = 0; col < cols.length; col++) {
                    out.print("<th>");
                    out.print(cols[col]);
                    out.print("</th>\n");
                }

                out.print("</tr>\n");
                out.print("</thead>\n");

            }

            if (rows.length > startingRow) {
                out.print("<tbody>\n");

                for (int row = startingRow; row < rows.length; row++) {

                    out.print("<tr");

                    out.print(" class=\"");
                    out.print(row % 2 == 0 ? "even" : "odd");

                    out.print("\">\n");

                    String[] cols = StringUtils.splitPreserveAllTokens(rows[row], "\t");

                    for (int col = 0; col < cols.length; col++) {
                        out.print("<td>");
                        out.print(cols[col]);
                        out.print("</td>\n");
                    }
                    out.print("</tr>\n");

                }
                out.print("</tbody>\n");
            }
            out.print("</table>\n");
        }
        catch (IOException e) {
            // should never happen
            log.debug(e.getMessage(), e);
        }

        return EVAL_PAGE;
    }

    /**
     * @param out
     * @throws IOException
     */
    private void writeAttributes(JspWriter out, Map attributes) throws IOException {
        for (Iterator iter = attributes.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String value = (String) attributes.get(name);
            if (StringUtils.isNotBlank(value)) {
                out.write(name);
                out.write("=\"");
                out.write(value);
                out.write("\" ");
            }
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();
        header = false;
        htmlAttributes.clear();
    }
}
