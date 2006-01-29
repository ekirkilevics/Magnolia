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

import javax.servlet.jsp.tagext.TagSupport;


/**
 * No more used in magnolia 2.
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 * @deprecated
 */
public class Init extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     * @deprecated
     */
    public int doStartTag() {
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     * @deprecated
     */
    public int doEndTag() {
        return EVAL_PAGE;
    }
}