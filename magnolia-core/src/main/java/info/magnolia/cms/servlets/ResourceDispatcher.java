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

import info.magnolia.cms.Aggregator;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private static Logger log = LoggerFactory.getLogger(ResourceDispatcher.class);

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
            HierarchyManager hm = (HierarchyManager) req.getAttribute(Aggregator.HIERARCHY_MANAGER);
            InputStream is = null;
            try {
                is = getNodedataAstream(resourceHandle, hm, res);
                if (null != is) {
                    // todo find better way to discover if resource could be compressed, implement as in "cache"
                    // browsers will always send header saying either it can decompress or not, but
                    // resources like jpeg which is already compressed should be not be written on
                    // zipped stream otherwise some browsers takes a long time to render
                    sendUnCompressed(is, res);
                    IOUtils.closeQuietly(is);
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
            finally {
                IOUtils.closeQuietly(is);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Resource not found, redirecting request for [" + req.getRequestURI() + "] to 404 URI"); //$NON-NLS-1$
        }

        if (!res.isCommitted()) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            log.info("Unable to redirect to 404 page, response is already committed"); //$NON-NLS-1$
        }

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
        IOUtils.closeQuietly(os);
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

                    String sizeString = atom.getAttribute("size"); //$NON-NLS-1$
                    if (NumberUtils.isNumber(sizeString)) {
                        res.setContentLength(Integer.parseInt(sizeString));
                    }
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
