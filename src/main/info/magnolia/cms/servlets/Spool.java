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

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.SecureURI;
import info.magnolia.cms.security.SessionAccessControl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * Date: Feb 9, 2005 Time: 3:51:20 PM
 * @author Sameer Charles
 * @version 2.1
 */
public class Spool extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Spool.class);

    /**
     * This makes browser and proxy caches work more effectively, reducing the load on server and network resources.
     * @param request
     * @return last modified time in miliseconds since 1st Jan 1970 GMT
     */
    public long getLastModified(HttpServletRequest request) {
        File resource = new File(getServletContext().getRealPath(Path.getURI(request)));
        if (resource.exists()) {
            return resource.lastModified();
        }
        return -1;
    }

    /**
     * All static resource requests are handled here.
     * @param request
     * @param response
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (SecureURI.isProtected(Path.getURI(request))) {
            if (!this.authenticate(request, response)) {
                return;
            }
        }
        File resource = new File(getServletContext().getRealPath(Path.getURI(request)));
        if (!resource.exists() || resource.isDirectory()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        this.setResponseHeaders(resource, response);
        boolean success = this.spool(resource, response);
        if (!success) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param resource
     * @param response
     */
    private boolean spool(File resource, HttpServletResponse response) {
        try {
            FileInputStream in = new FileInputStream(resource);
            ServletOutputStream os = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = in.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
            os.flush();
            os.close();
            in.close();
        }
        catch (IOException e) {
            log.error(e);
            return false;
        }
        return true;
    }

    /**
     * <p>
     * Set content length. content type is set by the filters as defined in server/MIMEMappings
     * </p>
     * @param resource
     * @param response
     */
    private void setResponseHeaders(File resource, HttpServletResponse response) {
        response.setContentLength((int) resource.length());
    }

    /**
     * Authenticate on basic headers.
     * @param request
     * @param response
     */
    private boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
        if (SessionAccessControl.isSecuredSession(request)) {
            return true;
        }
        try {
            if (!Authenticator.authenticate(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("WWW-Authenticate", "BASIC realm=\"" + Server.getBasicRealm() + "\"");
                return false;
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

}
