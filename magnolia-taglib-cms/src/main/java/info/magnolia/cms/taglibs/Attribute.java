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
package info.magnolia.cms.taglibs;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * Adds an attribute to the request within an includeTemplate tag.
 * @jsp.tag name="attribute" body-content="empty"
 *
 * @author Marcel Salathe
 * @version $Revision$ ($Author$)
 */
public class Attribute extends TagSupport {

    /**
     * Value of the attribute.
     */
    private Object value;

    /**
     * Name of the attribute.
     */
    private String name;

    /**
     * @param name name of the attribute
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param value value of the attribute
     * @jsp.attribute required="false" rtexprvalue="true" type="java.lang.Object"
     */
    public void setValue(Object value) {
        this.value = value;
    }

    public int doEndTag() throws JspException {
        Include parent = (Include) findAncestorWithClass(this, Include.class);
        if (parent == null) {
            throw new JspException("nesting error"); //$NON-NLS-1$
        }
        parent.setAttribute(this.name, this.value);
        return EVAL_PAGE;
    }

    public void release() {
        this.name = null;
        this.value = null;
        super.release();
    }
}
