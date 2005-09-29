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

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class StrToObj extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private String var;

    private String delims;

    public void setVar(String var) {
        this.var = var;
    }

    public void setDelims(String delims) {
        this.delims = delims;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        String str = getBodyContent().getString();
        if (StringUtils.isNotEmpty(str)) {
            String[] obj = str.split(StringUtils.defaultString(this.delims, "\n")); //$NON-NLS-1$
            pageContext.setAttribute(this.var, obj, PageContext.PAGE_SCOPE);

        }
        else {
            pageContext.setAttribute(this.var, StringUtils.EMPTY, PageContext.PAGE_SCOPE);
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#release()
     */
    public void release() {
        this.var = null;
        this.delims = null;
        super.release();
    }
}
