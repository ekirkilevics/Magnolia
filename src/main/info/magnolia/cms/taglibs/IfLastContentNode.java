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

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * @author Marcel Salathe
 * @version $Revision: $ ($Author: $)
 */
public class IfLastContentNode extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        int size = ((Integer) pageContext.getAttribute(ContentNodeIterator.SIZE, PageContext.REQUEST_SCOPE)).intValue();
        int currentIndex = ((Integer) pageContext.getAttribute(
            ContentNodeIterator.CURRENT_INDEX,
            PageContext.REQUEST_SCOPE)).intValue();
        if (currentIndex == (size - 1)) {
            return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        return EVAL_PAGE;
    }
}
