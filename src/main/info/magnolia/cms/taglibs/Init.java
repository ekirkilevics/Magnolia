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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * @author Sameer Charles
 * @version $Revision: $ ($Author: $)
 */
public class Init extends TagSupport
{

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private HttpServletRequest req;

    /**
     * <p>
     * starts init tag
     * </p>
     * @return int
     * @deprecated
     */
    public int doStartTag()
    {
        /*
         * this.req = (HttpServletRequest)pageContext.getRequest();
         * pageContext.setAttribute(Aggregator.CURRENT_ACTPAGE,Resource.getCurrentActivePage(this.req),PageContext.REQUEST_SCOPE);
         * pageContext.setAttribute(Aggregator.HIERARCHY_MANAGER,Resource.getHierarchyManager(this.req),PageContext.REQUEST_SCOPE);
         * checkRedirect();
         */
        return SKIP_BODY;
    }

    /**
     * <p>
     * end init
     * </p>
     * @return int
     */
    public int doEndTag()
    {
        return EVAL_PAGE;
    }

    // private void checkRedirect()
    // {
    // Content actpage = Resource.getCurrentActivePage(this.req);
    // String redirectURL = "";
    // try
    // {
    // redirectURL = actpage.getNodeData("redirectURL").getString();
    // if (!redirectURL.equals(""))
    // {
    // if (!Server.isAdmin() || Resource.showPreview(this.req))
    // ((HttpServletResponse) pageContext.getResponse()).sendRedirect(redirectURL);
    // }
    // }
    // catch (Exception e)
    // {
    // }
    // }

}