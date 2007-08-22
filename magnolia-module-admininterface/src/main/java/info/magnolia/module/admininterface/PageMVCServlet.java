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
import info.magnolia.cms.util.RequestFormUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Philipp Bracher
 * @version $Id$
 */
public class PageMVCServlet extends MVCServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(PageMVCServlet.class);

    /**
     * {@inheritDoc}
     */
    protected MVCServletHandler getHandler(HttpServletRequest request, HttpServletResponse response) {

        String pageName = RequestFormUtil.getParameter(request, "mgnlPage"); //$NON-NLS-1$
        if (StringUtils.isEmpty(pageName)) {
            if (StringUtils.isNotEmpty((String) request.getAttribute("javax.servlet.include.servlet_path"))) {
                pageName = (String) request.getAttribute("javax.servlet.include.path_info");
            }

            if (StringUtils.isEmpty(pageName)) {
                pageName = request.getPathInfo();
            }

            // strip off leading path
            pageName = StringUtils.substringAfterLast(pageName, "/");
            // strip any extension
            pageName = StringUtils.substringBeforeLast(pageName, ".");
        }

        PageMVCHandler handler = null;

        if (StringUtils.isNotEmpty(pageName)) {
            // try to get a registered handler
            try {
                handler = PageHandlerManager.getInstance().getPageHandler(pageName, request, response);
            }
            catch (InvalidDialogPageHandlerException e) {
                log.error("no page found with name \"{}\" ({})", pageName, e.getMessage()); //$NON-NLS-1$
            }
        }
        else {
            log.warn("No dialogpage name passed for url {}", request.getRequestURI()); //$NON-NLS-1$
        }

        return handler;
    }

}