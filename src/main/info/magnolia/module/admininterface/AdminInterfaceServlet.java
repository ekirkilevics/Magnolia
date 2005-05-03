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

import info.magnolia.cms.beans.config.ContentRepository;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Main admin interface servlet. Generates the content for the main admincentral iframe.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class AdminInterfaceServlet extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;
    
    private static Logger log = Logger.getLogger(AdminInterfaceServlet.class);

    /**
     * @see HttpServlet#doPost(HttpServletRequest,HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        doGet(request, response);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

        request.setCharacterEncoding("UTF-8");

        // why do i have to change it if request was setted? But i have to!
        response.setCharacterEncoding("UTF-8");

        // Atention: the treeHandlerName is not automatically the name of the used repository
        
        // TODO: do not use repository use treeHandlerName instead!!
        String handlerName = request.getParameter("repository"); //request.getParameter("treeHandler");
        if (StringUtils.isEmpty(handlerName)) {
            handlerName = ContentRepository.WEBSITE;
        }

        PrintWriter out = response.getWriter();
        response.setContentType("text/html; charset=UTF-8");

        
        AdminTree treeHandler = Store.getInstance().getTreeHandler(handlerName, request);
        log.debug("treehandler: " + handlerName);
        String command = treeHandler.getCommand();
        log.debug("calling command: " + command);
        String view = treeHandler.execute(command);
        log.debug("calling view: " + view);

        String htmlString = treeHandler.renderHtml(view);
        //log.debug("html: " + htmlString);
        
        response.setContentLength(htmlString.getBytes().length);
        out.write(htmlString);
    }
}