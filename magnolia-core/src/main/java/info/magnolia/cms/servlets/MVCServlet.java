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
package info.magnolia.cms.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The servlet gets a MVCHandler with the method getHandler. The method getCommand of this returned handler is called to
 * map the request parameters to a command. Then execute() is called which uses reflection to call a method. Each method
 * returns a string defining the view. After that, renderHtml is called.
 * <p>
 * Make a subclass to provide you own handler(s).
 * @author Philipp Bracher
 * @version $Id$
 */
public abstract class MVCServlet extends ContextSensitiveServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static Logger log = LoggerFactory.getLogger(MVCServlet.class);

    /**
     * @see HttpServlet#doGet(HttpServletRequest,HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        super.doPost(request, response);

        // http://issues.apache.org/bugzilla/show_bug.cgi?id=22666
        //
        // 1. The Coyote HTTP/1.1 connector has a useBodyEncodingForURI attribute which
        // if set to true will use the request body encoding to decode the URI query
        // parameters.
        // - The default value is true for TC4 (breaks spec but gives consistent
        // behaviour across TC4 versions)
        // - The default value is false for TC5 (spec compliant but there may be
        // migration issues for some apps)
        // 2. The Coyote HTTP/1.1 connector has a URIEncoding attribute which defaults to
        // ISO-8859-1.
        // 3. The parameters class (o.a.t.u.http.Parameters) has a QueryStringEncoding
        // field which defaults to the URIEncoding. It must be set before the parameters
        // are parsed to have an effect.
        //
        // Things to note regarding the servlet API:
        // 1. HttpServletRequest.setCharacterEncoding() normally only applies to the
        // request body NOT the URI.
        // 2. HttpServletRequest.getPathInfo() is decoded by the web container.
        // 3. HttpServletRequest.getRequestURI() is not decoded by container.
        //
        // Other tips:
        // 1. Use POST with forms to return parameters as the parameters are then part of
        // the request body.

        // this can throw an exception in jetty
        try {
            request.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        }
        catch (java.lang.IllegalStateException e) {
            log.error("can't set character encoding for the request", e); //$NON-NLS-1$
        }

        // why do i have to change it if request was setted? But i have to!
        response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$

        response.setContentType("text/html; charset=UTF-8"); //$NON-NLS-1$

        MVCServletHandler handler = getHandler(request, response);

        if (handler == null) {
            log.error("no handler found"); //$NON-NLS-1$
            return;
        }

        try {
            handler.init();
        }
        catch (Exception e) {
            log.error("can't initialize handler [" + handler.getName() + "]", e); //$NON-NLS-1$
        }

        String command = handler.getCommand();
        String view = handler.execute(command);
        handler.renderHtml(view);
    }

    /**
     * @param request
     * @return
     */
    protected abstract MVCServletHandler getHandler(HttpServletRequest request, HttpServletResponse response);
}