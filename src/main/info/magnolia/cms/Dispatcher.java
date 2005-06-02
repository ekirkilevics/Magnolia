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
package info.magnolia.cms;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public final class Dispatcher {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Dispatcher.class);

    /**
     * Utility class, don't instantiate.
     */
    private Dispatcher() {
        // unused
    }

    /**
     * <p>
     * dispatches the current requested to the handler JSP / Servlet
     * </p>
     * @throws ServletException
     * @throws IOException
     */
    public static void dispatch(HttpServletRequest req, HttpServletResponse res, ServletContext sc)
        throws ServletException, IOException {
        if (sc == null) {
            log.error("null ServletContext received - aborting request");
            return;
        }
        String requestReceiver = (String) req.getAttribute(Aggregator.REQUEST_RECEIVER);

        if (requestReceiver == null) {
            log.error("requestReceiver is missing, returning a 404 error");
            res.sendError(404);
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Dispatching request for [" + req.getRequestURL() + "] - forward to [" + requestReceiver + "]");
        }

        if (res.isCommitted()) {
            log.error("Can't forward to ["
                + requestReceiver
                + "] for request ["
                + req.getRequestURL()
                + "]. Response is already committed.");
            return;
        }

        RequestDispatcher rd = sc.getRequestDispatcher(requestReceiver);
        rd.forward(req, res);
        rd = null;
    }
}
