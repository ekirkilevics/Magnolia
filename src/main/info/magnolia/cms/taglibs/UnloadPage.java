/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.Aggregator;
import info.magnolia.cms.util.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;


/**
 * @author Marcel Salathe
 * @version $Revision: $ ($Author: $)
 */
public class UnloadPage extends BodyTagSupport
{

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag()
    {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        pageContext
            .setAttribute(Aggregator.CURRENT_ACTPAGE, Resource.getActivePage(request), PageContext.REQUEST_SCOPE);
        return EVAL_PAGE;
    }

}
