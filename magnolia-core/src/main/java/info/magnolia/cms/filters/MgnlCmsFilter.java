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
package info.magnolia.cms.filters;

import info.magnolia.api.HierarchyManager;
import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.config.TemplateRendererManager;
import info.magnolia.cms.beans.runtime.TemplateRenderer;
import info.magnolia.cms.core.Aggregator;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CMS filter responsible for content aggregation and dispatching
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class MgnlCmsFilter extends AbstractMagnoliaFilter {

    private static Logger log = LoggerFactory.getLogger(MgnlCmsFilter.class);

    /**
     * All HTTP/s requests are handled here.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     * @throws ServletException
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{
        if (ConfigLoader.isBootstrapping()) {
            // @todo a nice page, with the log content...
            response.getWriter().write("Magnolia bootstrapping has failed, check bootstrap.log in magnolia/logs"); //$NON-NLS-1$
            return;
        }

        Template template = Aggregator.getTemplate();

        if (template != null) {
            try {
                String type = template.getType();
                TemplateRenderer renderer = TemplateRendererManager.getInstance().getRenderer(type);

                if (renderer == null) {
                    throw new RuntimeException("No renderer found for type " + type);
                }
                response.setStatus(HttpServletResponse.SC_OK);
                renderer.renderTemplate(template, request, response);
            }
            catch (IOException e) {
                log.error(e.getMessage(), e);
                throw e;
            }
            catch (ServletException e) {
                log.error(e.getMessage(), e);
                throw e;
            }
            catch (Exception e) {
                // @todo better handling of rendering exception
                log.error(e.getMessage(), e);
                if (!response.isCommitted()) {
                    response.setContentType("text/html");
                }
                throw new RuntimeException(e);
            }
        }
        else {
            // direct request
            handleResourceRequest(request, response);
        }

        // TODO don't make it a dead end
        //      currently we can't process the chain because there is no content/nop servlet
        // chain.doFilter(request, response);
    }

    /**
     * Get the requested resource and copy it to the ServletOutputStream, bit by bit.
     * @param request HttpServletRequest as given by the servlet container
     * @param response HttpServletResponse as given by the servlet container
     * @throws IOException standard servlet exception
     */
    protected void handleResourceRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String resourceHandle = Aggregator.getHandle();

        log.debug("handleResourceRequest, resourceHandle=\"{}\"", resourceHandle); //$NON-NLS-1$

        if (StringUtils.isNotEmpty(resourceHandle)) {

            HierarchyManager hm = MgnlContext.getHierarchyManager(Aggregator.getRepository());

            InputStream is = null;
            try {
                is = getNodedataAstream(resourceHandle, hm, response);
                if (null != is) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    sendUnCompressed(is, response);
                    IOUtils.closeQuietly(is);
                    return;
                }
            }
            catch (IOException e) {
                // don't log at error level since tomcat tipically throws a
                // org.apache.catalina.connector.ClientAbortException if the user stops loading the page
                if (log.isDebugEnabled()) {
                    log.debug(
                        "Exception while dispatching resource  " + e.getClass().getName() + ": " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            catch (Exception e) {
                log.error("Exception while dispatching resource  " + e.getClass().getName() + ": " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            finally {
                IOUtils.closeQuietly(is);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Resource not found, redirecting request for [{}] to 404 URI", request.getRequestURI()); //$NON-NLS-1$
        }

        if (!response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            log.info("Unable to redirect to 404 page, response is already committed"); //$NON-NLS-1$
        }

    }

    /**
     * Send data as is.
     * @param is Input stream for the resource
     * @param response HttpServletResponse as received by the service method
     * @throws IOException standard servlet exception
     */
    private void sendUnCompressed(InputStream is, HttpServletResponse response) throws IOException {
        ServletOutputStream os = response.getOutputStream();
        byte[] buffer = new byte[8192];
        int read;
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

        log.debug("getNodedataAstream for path \"{}\"", path); //$NON-NLS-1$

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

            log.warn("Resource not found: [{}]", path); //$NON-NLS-1$

        }
        catch (PathNotFoundException e) {
            log.warn("Resource not found: [{}]", path); //$NON-NLS-1$
        }
        catch (RepositoryException e) {
            log.error("RepositoryException while reading Resource [" + path + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }


}
