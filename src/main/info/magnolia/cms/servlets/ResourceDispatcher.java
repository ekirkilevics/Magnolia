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
import javax.jcr.Value;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Class ResourceDispatcher is responsible to gather data from the <strong>HttpServletRequest </strong> and write back
 * the requested resource on the <strong>ServletOutputStream </strong>.
 * @author Sameer Charles
 * @version 1.0
 */
public class ResourceDispatcher extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ResourceDispatcher.class);

    /**
     * @param req HttpServletRequest as given by the servlet container
     * @param res HttpServletResponse as given by the servlet container
     * @throws IOException standard servlet exception
     */
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        handleResourceRequest(req, res);
    }

    /**
     * Get the requested resource and copy it to the ServletOutputStream, bit by bit.
     * @param req HttpServletRequest as given by the servlet container
     * @param res HttpServletResponse as given by the servlet container
     * @throws IOException standard servlet exception
     */
    private void handleResourceRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String resourceHandle = (String) req.getAttribute(Aggregator.HANDLE);
        if (log.isDebugEnabled()) {
            log.debug("handleResourceRequest, resourceHandle=\"" + resourceHandle + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (StringUtils.isNotEmpty(resourceHandle)) {
            try {
                HierarchyManager hm = (HierarchyManager) req.getAttribute(Aggregator.HIERARCHY_MANAGER);
                InputStream is = getNodedataAstream(resourceHandle, hm, res);
                if (is != null) {
                    // todo always send as is, find better way to discover if resource could be compressed
                    sendUnCompressed(is, res);
                    is.close();
                    return;
                }
            }
            catch (IOException e) {
                // don't log at error level since tomcat tipically throws a
                // org.apache.catalina.connector.ClientAbortException if the user stops loading the page
                log.debug("Exception while dispatching resource  " + e.getClass().getName() + ": " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            catch (Exception e) {
                log.error("Exception while dispatching resource  " + e.getClass().getName() + ": " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Redirecting to 404 page \"" + Server.get404URI() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (!res.isCommitted()) {
            res.sendRedirect(req.getContextPath() + Server.get404URI());
        }
        else {
            log.info("Unable to redirect to 404 page, response is already committed"); //$NON-NLS-1$
        }

    }

    /**
     * Returns true if the request sender accepts GZIP compressed data.
     * @param request HttpServletRequest
     * @return <code>true</code> if the client accepts gzip encoding
     */
    private boolean canCompress(HttpServletRequest request) {
        String encoding = request.getHeader("Accept-Encoding"); //$NON-NLS-1$
        if (encoding != null) {
            return (encoding.toLowerCase().indexOf("gzip") > -1); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * Send data as GZIP output stream ;)
     * @param is Input stream for the resource
     * @param res HttpServletResponse as received by the service method
     * @throws IOException standard servlet exception
     */
    private void sendCompressed(InputStream is, HttpServletResponse res) throws IOException {
        res.setHeader("Content-Encoding", "gzip"); //$NON-NLS-1$ //$NON-NLS-2$
        GZIPOutputStream gzos = new GZIPOutputStream(res.getOutputStream());
        int bit;
        while ((bit = is.read()) != -1) {
            gzos.write(bit);
        }
        gzos.flush();
        gzos.close();
    }

    /**
     * Send data as is.
     * @param is Input stream for the resource
     * @param res HttpServletResponse as received by the service method
     * @throws IOException standard servlet exception
     */
    private void sendUnCompressed(InputStream is, HttpServletResponse res) throws IOException {
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
     * @param path path for nodedata in jcr repository
     * @param hm Hierarchy manager
     * @param res HttpServletResponse
     * @return InputStream or <code>null</code> if nodeData is not found
     */
    private InputStream getNodedataAstream(String path, HierarchyManager hm, HttpServletResponse res) {
        if (log.isDebugEnabled()) {
            log.debug("getAtomAsStream for path \"" + path + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        try {
            NodeData atom = hm.getNodeData(path);
            if (atom != null) {
                if (atom.getType() == PropertyType.BINARY) {
                    NodeData size = hm.getNodeData(path + "_properties/size"); //$NON-NLS-1$
                    int sizeInBytes = (new Long(size.getLong())).intValue();
                    res.setContentLength(sizeInBytes);
                }

                Value value = atom.getValue();
                if (value != null) {
                    return value.getStream();
                }
            }

            log.warn("Resource not found: [" + path + "]"); //$NON-NLS-1$ //$NON-NLS-2$

        }
        catch (PathNotFoundException e) {
            log.warn("Resource not found: [" + path + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (RepositoryException e) {
            log.error("RepositoryException while reading Resource [" + path + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }
}
