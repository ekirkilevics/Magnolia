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
package info.magnolia.cms.taglibs;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;


/**
 * @author Marcel Salathe
 * @version $Revision$ ($Author$)
 */
public class LoadPagePath extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        LoadPage parent = (LoadPage) findAncestorWithClass(this, LoadPage.class);
        if (parent == null) {
            throw new JspException("nesting error");
        }
        String path = getBodyContent().getString();
        if (path != null) {
            parent.setPath(path.trim());
        }
        return EVAL_PAGE;
    }
}
