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
package info.magnolia.module.admininterface;

import java.util.Enumeration;
import java.util.Iterator;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.servlets.MVCServlet;
import info.magnolia.cms.servlets.MVCServletHandler;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Philipp Bracher
 * @version $Id$
 */
public class DialogPageMVCServlet extends MVCServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogPageMVCServlet.class);

    /**
     *
     */
    protected MVCServletHandler getHandler(HttpServletRequest request, HttpServletResponse response) {

        String pageName = RequestFormUtil.getParameter(request, "mgnlPage"); //$NON-NLS-1$
        if (StringUtils.isEmpty(pageName)) {
            pageName = (String) request.getAttribute("javax.servlet.include.request_uri"); //$NON-NLS-1$
            if (StringUtils.isEmpty(pageName)) {
                pageName = (String) request.getAttribute("javax.servlet.forward.servlet_path"); //$NON-NLS-1$
            }
            if (StringUtils.isEmpty(pageName)) {
                pageName = request.getRequestURI();
            }
            pageName = StringUtils.replaceOnce(StringUtils.substringAfterLast(pageName, "/dialogpages/"), ".html", //$NON-NLS-1$ //$NON-NLS-2$
                StringUtils.EMPTY);
        }

        DialogPageMVCHandler handler = null;

        if (pageName != null) {
            // try to get a registered handler
            try {
                handler = Store.getInstance().getDialogPageHandler(pageName, request, response);
            }
            catch (InvalidDialogPageHandlerException e) {
                log.error("no dialogpage found: " + pageName); //$NON-NLS-1$
            }
        }
        else{
            log.error("no dialogpage name passed"); //$NON-NLS-1$
        }

        return handler;
    }

}