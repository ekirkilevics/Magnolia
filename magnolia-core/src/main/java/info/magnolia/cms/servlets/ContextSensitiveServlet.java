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
package info.magnolia.cms.servlets;

import info.magnolia.context.MgnlContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet which sets the context properly.
 *
 * @deprecated the context is now set through a filter.
 *
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class ContextSensitiveServlet extends HttpServlet {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContextSensitiveServlet.class);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        initializeContext(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        initializeContext(req, resp);
    }

    /**
     * Initialize Magnolia context. It creates a context and initialize the user only if these do not exist yet.
     * <strong>Note</strong>: the implementation may get changed
     *
     * @param request the current request
     */
    protected void initializeContext(HttpServletRequest request, HttpServletResponse response) {
        if (!MgnlContext.hasInstance()) {
            MgnlContext.initAsWebContext(request, response, null);
        } else {
            // this will happen if a virtual uri mapping is pointing again to a virtual uri
            if (log.isDebugEnabled()) {
                log.debug("context of thread was already set");
            }
        }
    }

}
