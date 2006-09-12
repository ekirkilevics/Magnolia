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
package info.magnolia.module.admininterface.pages;

import info.magnolia.module.admininterface.PageMVCHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class LogoutPage extends PageMVCHandler {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(LogoutPage.class);

    /**
     * @param name
     * @param request
     * @param response
     */
    public LogoutPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandler#renderHtml(java.lang.String)
     */
    public void renderHtml(String view) throws IOException {
        HttpSession session = getRequest().getSession(false);
        if (session != null) {
            session.invalidate();
            log.info("Logging out user");
        }
        getResponse().sendRedirect(request.getContextPath() + "/");
    }

}
