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

import info.magnolia.cms.Aggregator;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * <p>
 * Class ResourceDispatcher is responsible to gather data from the <b>HttpServletRequest </b> and write back the
 * requested resource on the <b>ServletOutputStream </b>
 * </p>
 * @author Sameer Charles
 * @version 1.0
 */
public class ResourceDispatcher extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static Logger log = Logger.getLogger(ResourceDispatcher.class);

    /**
     * @param req HttpServletRequest as given by the servlet container
     * @param res HttpServletResponse as given by the servlet comtainer
     * @throws ServletException
     * @throws IOException
     */
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        handleResourceRequest(req, res);
    }

    /**
     * <p>
     * get the requested resource and copy it to the ServletOutputStream , bit by bit
     * </p>
     * @throws IOException
     */
    private void handleResourceRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String resourceHandle = (String) req.getAttribute(Aggregator.HANDLE);
        if (log.isDebugEnabled()) {
            log.debug("handleResourceRequest, resourceHandle=\"" + resourceHandle + "\"");
        }
        if (StringUtils.isNotEmpty(resourceHandle)) {
            try {
                HierarchyManager hm = (HierarchyManager) req.getAttribute(Aggregator.HIERARCHY_MANAGER);
                InputStream is = getAtomAsStream(resourceHandle, hm, res);
                if (is != null) {
                    // todo always send as is, find better way to dicover if resource could be compressed
                    sendUnCompressed(is, res);
                    is.close();
                    return;
                }
            }
            catch (Exception e) {
                log.info("Exception while dispatching resource  " + e.getClass().getName() + ": " + e.getMessage(), e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Redirecting to 404 page \"" + Server.get404URI() + "\"");
        }
        res.sendRedirect(req.getContextPath() + Server.get404URI());
    }

    /**
     * <p>
     * returns true if the request sender accepts GZIP compressed data
     * </p>
     * @return boolean
     */
    private boolean canCompress(HttpServletRequest req) {
        String encoding = req.getHeader("Accept-Encoding");
        if (encoding != null) {
            return (encoding.toLowerCase().indexOf("gzip") > -1);
        }
        return false;
    }

    /**
     * <p>
     * send data as GZIP output stream ;)
     * </p>
     * @param is Input stream for the resource
     * @param res HttpServletResponse as received by the service method
     * @throws Exception
     */
    private void sendCompressed(InputStream is, HttpServletResponse res) throws Exception {
        res.setHeader("Content-Encoding", "gzip");
        GZIPOutputStream gzos = new GZIPOutputStream(res.getOutputStream());
        int bit;
        while ((bit = is.read()) != -1) {
            gzos.write(bit);
        }
        gzos.flush();
        gzos.close();
    }

    /**
     * <p>
     * send data as is
     * </p>
     * @param is Input stream for the resource
     * @param res HttpServletResponse as received by the service method
     * @throws IOException
     */
    private void sendUnCompressed(InputStream is, HttpServletResponse res) throws Exception {
        ServletOutputStream os = res.getOutputStream();
        byte[] buffer = new byte[8192];
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
        }
        os.flush();
        os.close();
    }

    /**
     *
     */
    private InputStream getAtomAsStream(String path, HierarchyManager hm, HttpServletResponse res)
        throws RepositoryException {
        if (log.isDebugEnabled()) {
            log.debug("getAtomAsStream for path \"" + path + "\"");
        }
        try {
            NodeData atom = hm.getNodeData(path);
            if (atom.getType() == PropertyType.BINARY) {
                NodeData size = hm.getNodeData(path + "_properties/size");
                int sizeInBytes = (new Long(size.getLong())).intValue();
                res.setContentLength(sizeInBytes);
            }
            return atom.getValue().getStream();
        }
        catch (PathNotFoundException e) {
            log.error("Resource not found - " + path);
        }
        return null;
    }
}
