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

import info.magnolia.cms.core.Path;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Use this servlet to spool static resources from the servlet context.
 *
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
    private static Logger log = LoggerFactory.getLogger(Spool.class);

    /**
     * This makes browser and proxy caches work more effectively, reducing the load on server and network resources.
     *
     * @param request HttpServletRequest
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
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException for error in accessing the resource or the servlet output stream
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File resource = new File(getServletContext().getRealPath(Path.getURI(request)));
        if (!resource.exists() || resource.isDirectory()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        this.setResponseHeaders(resource, response);
        boolean success = this.spool(resource, response);
        if (!success && !response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param resource File to be returned to the client
     * @param response HttpServletResponse
     * @return <code>true</code> if the file has been written to the servlet output stream without errors
     * @throws IOException for error in accessing the resource or the servlet output stream
     */
    private boolean spool(File resource, HttpServletResponse response) throws IOException {
        FileInputStream in = new FileInputStream(resource);

        try {
            ServletOutputStream os = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = in.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
            os.flush();
            IOUtils.closeQuietly(os);
        }
        catch (IOException e) {
            // only log at debug level, tomcat usually throws a ClientAbortException anytime the user stop loading the
            // page
            if (log.isDebugEnabled())
                log.debug("Unable to spool resource due to a " + e.getClass().getName() + " exception", e); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        finally {
            IOUtils.closeQuietly(in);
        }
        return true;
    }

    /**
     * Set content length. content type is set by the filters as defined in server/MIMEMappings.
     *
     * @param resource File to be returned to the client
     * @param response HttpServletResponse
     */
    private void setResponseHeaders(File resource, HttpServletResponse response) {
        response.setContentLength((int) resource.length());
    }

}
