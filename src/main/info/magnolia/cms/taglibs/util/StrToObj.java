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

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version $Revision: $ ($Author: $)
 */
public class StrToObj extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Legger.
     */
    private static Logger log = Logger.getLogger(StrToObj.class);

    private String var;

    private String delims = "\n";

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
        if (!str.equals("")) {
            String[] obj = str.split(this.delims);
            try {
                pageContext.setAttribute(this.var, obj, PageContext.PAGE_SCOPE);
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        else {
            try {
                pageContext.setAttribute(this.var, "", PageContext.PAGE_SCOPE);
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return EVAL_PAGE;
    }
}
