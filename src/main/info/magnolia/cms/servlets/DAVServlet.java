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
package info.magnolia.cms.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * <p>
 * This servlet handles all webDAV requests. Currently we support : OPTIONS, GET, HEAD, POST, DELETE, PROPFIND,
 * PROPPATCH, COPY, MOVE, LOCK, UNLOCK
 * </p>
 * @author Sameer Charles
 * @version 2.0
 */

public class DAVServlet extends HttpServlet
{

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DAVServlet.class);

    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {

    }

}
