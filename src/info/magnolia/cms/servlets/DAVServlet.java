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



package info.magnolia.cms.servlets;




import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

import com.obinary.webdav.DAVRequest;
import com.obinary.webdav.DAVResponse;
import com.obinary.webdav.Exception.InvalidDAVRequest;






/**
 * <p>
 * This servlet handles all webDAV requests
 * Currently we support :
 * OPTIONS, GET, HEAD, POST, DELETE, PROPFIND, PROPPATCH, COPY, MOVE, LOCK, UNLOCK
 * </p>
 *
 *
 * Date: Jan 15, 2004
 * Time: 10:25:45 AM
 * @author Sameer Charles
 * @version 2.0
 * */


public class DAVServlet extends HttpServlet {


    private static Logger log = Logger.getLogger(DAVServlet.class);



    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        DAVRequest davRequest = new DAVRequest(req,res);
        try {
            davRequest.executeMethod();
            DAVResponse davResponse = davRequest.getDAVResponse();

        } catch (InvalidDAVRequest e) {log.error(e.getMessage());}


    }



    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

    }






}
