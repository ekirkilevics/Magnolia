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
 * */




package info.magnolia.cms.taglibs;


import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.util.Resource;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.HttpServletRequest;


/**
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author Sameer Charles
 * @version 1.1
 */



public class AdminOnly extends TagSupport {
	HttpServletRequest request;


    /**
     * <p>starts Edit tag</p>
     *
     * @return int
     */
    public int doStartTag() {



		this.request = (HttpServletRequest)pageContext.getRequest();
		String prev=(String) this.request.getSession().getAttribute("mgnlPreview");

		//if (Server.isAdmin() && !Resource.showPreview((HttpServletRequest)pageContext.getRequest()))
        if (Server.isAdmin() && prev==null)
            return EVAL_BODY_INCLUDE;
        return SKIP_BODY;
    }



    /**
     * <p>continue evaluating jsp</p>
     *
     * @return int
     */
    public int doEndTag() {
        return EVAL_PAGE;
    }



}
