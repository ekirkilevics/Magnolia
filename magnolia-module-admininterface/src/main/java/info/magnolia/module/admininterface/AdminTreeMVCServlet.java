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
package info.magnolia.module.admininterface;

import info.magnolia.cms.servlets.MVCServlet;
import info.magnolia.cms.servlets.MVCServletHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * Main admin interface servlet. Generates the content for the main admincentral iframe.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class AdminTreeMVCServlet extends MVCServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @see info.magnolia.cms.servlets.MVCServlet#getHandler(javax.servlet.http.HttpServletRequest)
     */
    protected MVCServletHandler getHandler(HttpServletRequest request, HttpServletResponse response) {
        String handlerName = request.getRequestURI();
        handlerName = StringUtils.replaceOnce(StringUtils.substringAfterLast(handlerName, "/trees/"), ".html", StringUtils.EMPTY);

        return Store.getInstance().getTreeHandler(handlerName, request, response);
    }
}