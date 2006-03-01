/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * @author Marcel Salathe
 * @version $Revision$ ($Author$)
 */
public class Attribute extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Value of the attribute
     */
    private Object value;

    /**
     * Name of the attribute.
     */
    private String name;

    /**
     * @param name name of the attribute
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param value value of the attribute
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        Include parent = (Include) findAncestorWithClass(this, Include.class);
        if (parent == null) {
            throw new JspException("nesting error"); //$NON-NLS-1$
        }
        parent.setAttribute(this.name, this.value);
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        this.name = null;
        this.value = null;
        super.release();
    }
}
