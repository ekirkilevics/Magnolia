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

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ModuleRegistration;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.beans.config.TemplateRendererManager;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.TemplateRenderer;
import info.magnolia.cms.core.Aggregator;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the main http servlet which will be called for any resource request this servlet will dispacth or process
 * requests according to their nature -- all resource requests will go to ResourceDispatcher -- all page requests will
 * be handed over to the defined JSP or Servlet (template). Updated to allow caching of virtual URI's
 * @author Sameer Charles
 * @version 3.0
 */
public class EntryServlet extends ContextSensitiveServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(EntryServlet.class);

    /**
     *
     */
    private static final String NODE_DATA_TEMPLATE = "nodeDataTemplate";

    private static final String VERSION_NUMBER = "mgnlVersion"; //$NON-NLS-1$

    /**
     * Allow caching of this specific resource. This method always returns <code>true</code>, and it's here to allow
     * an easy plug-in of application-specific logic by extending EntrySrvlet. If you need to disable cache for specific
     * requests (not based on the request URI, since this is configurable from adminCentral) you can override this
     * method.
     * @param req HttpServletRequest
     * @return <code>true</code> if the page returned by this request can be cached, <code>false</code> if cache
     * should not be used.
     */
    protected boolean allowCaching(HttpServletRequest req) {
        return true;
    }

    /**
     * All HTTP/s requests are handled here.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     * @throws ServletException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // Initialize magnolia context
        initializeContext(request);

        if (ConfigLoader.isBootstrapping()) {
            // @todo a nice page, with the log content...
            response.getWriter().write("Magnolia bootstrapping has failed, check bootstrap.log in magnolia/logs"); //$NON-NLS-1$
            return;
        }

        if (ModuleRegistration.getInstance().isRestartNeeded()) {
            response.sendRedirect(request.getContextPath() + "/.magnolia/pages/restart.html");
        }

        if (isAuthorized(request, response)) {

            // redirect: moved to a filter

            // intercept: moved to a filter

            try {
                // aggregate content
                boolean success = collect(request);

                if (success) {

                    Template template = (Template) request.getAttribute(Aggregator.TEMPLATE);

                    if (template != null) {
                        try {
                            String type = template.getType();
                            TemplateRenderer renderer = TemplateRendererManager.getInstance().getRenderer(type);

                            if (renderer == null) {
                                throw new RuntimeException("No renderer found for type " + type);
                            }
                            renderer.renderTemplate(template, request, response);
                        }
                        catch (Exception e) {
                            // @todo better handling of rendering exception
                            log.error(e.getMessage(), e);
                            if (!response.isCommitted()) {
                                response.reset();
                                response.setContentType("text/html");
                            }
                            throw new NestableRuntimeException(e);
                        }
                    }
                    else {
                        // direct request
                        handleResourceRequest(request, response);
                    }

                }
                else {
                    if (log.isDebugEnabled()) {
                        log.debug(
                            "Resource not found, redirecting request for [{}] to 404 URI", request.getRequestURI()); //$NON-NLS-1$
                    }

                    if (!response.isCommitted()) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                    else {
                        log.info("Unable to redirect to 404 page, response is already committed. URI was {}", //$NON-NLS-1$
                            request.getRequestURI());
                    }
                }
            }
            catch (AccessDeniedException e) {
                // don't log AccessDenied as errors, it can happen...
                log.warn(e.getMessage());
            }
            catch (RepositoryException e) {
                log.error(e.getMessage(), e);
                throw new ServletException(e.getMessage(), e);
            }
        }

    }

    /**
     * All requests are handles by get handler.
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     * @throws ServletException
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doGet(req, res);
    }

    /**
     * Uses access manager to authorise this request.
     * @param req HttpServletRequest as received by the service method
     * @param res HttpServletResponse as received by the service method
     * @return boolean true if read access is granted
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     */
    protected boolean isAuthorized(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (MgnlContext.getAccessManager(ContentRepository.WEBSITE) != null) {
            String path = StringUtils.substringBefore(Path.getURI(req), "."); //$NON-NLS-1$
            if (!MgnlContext.getAccessManager(ContentRepository.WEBSITE).isGranted(path, Permission.READ)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        return true;
    }

    /**
     * Get the requested resource and copy it to the ServletOutputStream, bit by bit.
     * @param request HttpServletRequest as given by the servlet container
     * @param response HttpServletResponse as given by the servlet container
     * @throws IOException standard servlet exception
     */
    private void handleResourceRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String resourceHandle = (String) request.getAttribute(Aggregator.HANDLE);

        log.debug("handleResourceRequest, resourceHandle=\"{}\"", resourceHandle); //$NON-NLS-1$

        if (StringUtils.isNotEmpty(resourceHandle)) {

            HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

            InputStream is = null;
            try {
                is = getNodedataAstream(resourceHandle, hm, response);
                if (null != is) {
                    // todo find better way to discover if resource could be compressed, implement as in "cache"
                    // browsers will always send header saying either it can decompress or not, but
                    // resources like jpeg which is already compressed should be not be written on
                    // zipped stream otherwise some browsers takes a long time to render
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

    /**
     * Collect content from the pre configured repository and attach it to the HttpServletRequest.
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    protected boolean collect(HttpServletRequest request) throws PathNotFoundException, RepositoryException {

        String uri = StringUtils.substringBeforeLast(Path.getURI(request), "."); //$NON-NLS-1$
        String extension = StringUtils.substringAfterLast(Path.getURI(request), "."); //$NON-NLS-1$

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

        Content requestedPage = null;
        NodeData requestedData = null;
        Template template = null;

        if (hierarchyManager.isPage(uri)) {
            requestedPage = hierarchyManager.getContent(uri);

            // check if its a request for a versioned page
            if (request.getParameter(VERSION_NUMBER) != null) {
                // get versioned state
                try {
                    requestedPage = requestedPage.getVersionedContent(request.getParameter(VERSION_NUMBER));
                }
                catch (RepositoryException re) {
                    log.debug(re.getMessage(), re);
                    log.error("Unable to get versioned state, rendering current state of {}", uri);
                }
            }

            String templateName = requestedPage.getMetaData().getTemplate();

            if (StringUtils.isBlank(templateName)) {
                log.error("No template configured for page [{}].", requestedPage.getHandle()); //$NON-NLS-1$
            }

            template = TemplateManager.getInstance().getInfo(templateName, extension);

            if (template == null) {
                log.error("Template [{}] for page [{}] not found.", //$NON-NLS-1$
                    templateName,
                    requestedPage.getHandle());
            }
        }
        else {
            if (hierarchyManager.isNodeData(uri)) {
                requestedData = hierarchyManager.getNodeData(uri);
            }
            else {
                // check again, resource might have different name
                int lastIndexOfSlash = uri.lastIndexOf("/"); //$NON-NLS-1$

                if (lastIndexOfSlash > 0) {

                    uri = StringUtils.substringBeforeLast(uri, "/"); //$NON-NLS-1$
                    try {
                        requestedData = hierarchyManager.getNodeData(uri);

                        // this is needed for binary nodedata, e.g. images are found using the path:
                        // /features/integration/headerImage instead of /features/integration/headerImage/header30_2

                    }
                    catch (PathNotFoundException e) {
                        // no page available
                        return false;
                    }
                    catch (RepositoryException e) {
                        log.debug(e.getMessage(), e);
                        return false;
                    }
                }
            }

            if (requestedData != null) {
                String templateName = requestedData.getAttribute(NODE_DATA_TEMPLATE); //$NON-NLS-1$

                if (!StringUtils.isEmpty(templateName)) {
                    template = TemplateManager.getInstance().getInfo(templateName, extension);
                }
            }
            else {
                return false;
            }
        }

        // Attach all collected information to the HttpServletRequest.
        if (requestedPage != null) {
            request.setAttribute(Aggregator.ACTPAGE, requestedPage);
            request.setAttribute(Aggregator.CURRENT_ACTPAGE, requestedPage);
        }
        if ((requestedData != null) && (requestedData.getType() == PropertyType.BINARY)) {
            File file = new File();
            file.setProperties(requestedData);
            file.setNodeData(requestedData);
            request.setAttribute(Aggregator.FILE, file);
        }

        request.setAttribute(Aggregator.HANDLE, uri);
        request.setAttribute(Aggregator.TEMPLATE, template);

        return true;
    }

}
