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
package info.magnolia.cms.taglibs.util;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * @author Marcel Salathe
 * @version $Revision: $ ($Author: $)
 */
public class AAttribute extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private String value = "";

    private String name = "";

    /**
     * <p>
     * end of tag
     * </p>
     * @return int
     */
    public int doEndTag() throws JspException {
        AHref parent = (AHref) findAncestorWithClass(this, AHref.class);
        if (parent == null) {
            throw new JspException("nesting error");
        }
        else {
            parent.setAttribute(this.name, this.value);
        }
        return EVAL_PAGE;
    }

    /**
     * @param name , name of the attribute
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param value , value of the attribute
     */
    public void setValue(String value) {
        this.value = value;
    }
}
