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

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.beans.config.TemplateRendererManager;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.TemplateRenderer;
import info.magnolia.cms.core.*;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class initializes the current context.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class MgnlCmsFilter implements Filter {

    private static final String BYPASS_PARAM = "bypass";

    private static Logger log = LoggerFactory.getLogger(MgnlCmsFilter.class);

    private String[] bypass;

    private static final String NODE_DATA_TEMPLATE = "nodeDataTemplate";

    private static final String VERSION_NUMBER = "mgnlVersion"; //$NON-NLS-1$


    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // unused
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        this.bypass = StringUtils.split(filterConfig.getInitParameter(BYPASS_PARAM), ", ");
        if (this.bypass == null) {
            this.bypass = new String[0];
        }
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
        ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String requestURI = Path.getURI(request);
        String pathInfo = request.getPathInfo();

        boolean success = true;
        if (pathInfo == null && !startsWithAny(bypass, requestURI)) {
            success = handle(request, response);
        }

        if (success) {
            chain.doFilter(request, response);
        }
    }

    /**
     * All HTTP/s requests are handled here.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     * @throws ServletException
     */
    public boolean handle(HttpServletRequest request, HttpServletResponse response) throws IOException,
        ServletException {

        if (ConfigLoader.isBootstrapping()) {
            // @todo a nice page, with the log content...
            response.getWriter().write("Magnolia bootstrapping has failed, check bootstrap.log in magnolia/logs"); //$NON-NLS-1$
            return false;
        }

        setHandleAndMapping(request);

        try {
            authorize(request);
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
                return false;
            }
        }
        catch (AccessDeniedException e) {
            // don't throw further, simply return error and break filter chain
            log.debug(e.getMessage());
            if (!response.isCommitted())
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }

        return true;
    }

    /**
     * Sets the proper handle, selector and extension into the request variables
     * @param request
     */
    protected void setHandleAndMapping(HttpServletRequest request) {
        String uri = Path.getURI(request);
        int firstDotPos = StringUtils.indexOf(uri, '.', StringUtils.lastIndexOf(uri, '/'));
        String handle;
        String selector;
        String extension;
        if (firstDotPos > -1) {
            int lastDotPos = StringUtils.lastIndexOf(uri, '.');
            handle = StringUtils.substring(uri, 0, firstDotPos);
            selector = StringUtils.substring(uri, firstDotPos + 1, lastDotPos);
            extension = StringUtils.substring(uri, lastDotPos + 1);
        }
        else {
            // no dots (and no extension)
            handle = uri;
            selector = "";
            extension = "";
        }

        URI2RepositoryMapping mapping = URI2RepositoryManager.getInstance().getMapping(uri);

        // remove prefix if any
        handle = mapping.getHandle(handle);

        request.setAttribute(Aggregator.REPOSITORY, mapping.getRepository());
        request.setAttribute(Aggregator.MAPPING, mapping);
        request.setAttribute(Aggregator.HANDLE, handle);
        request.setAttribute(Aggregator.SELECTOR, selector);
        request.setAttribute(Aggregator.EXTENSION, extension);
    }

    /**
     * Uses access manager to authorize this request.
     * @param request HttpServletRequest as received by the service method
     * @throws AccessDeniedException if the given request is not authorized
     */
    protected void authorize(HttpServletRequest request) throws AccessDeniedException {
        AccessManager accessManager = MgnlContext.getAccessManager(getRepository(request));
        if (null != accessManager) {
            Access.isGranted(accessManager, Path.getHandle(request), Permission.READ);
        }
    }

    /**
     * Return the repository used for this request. This uses a url to repository mapping.
     * @return repository name
     */
    protected String getRepository(HttpServletRequest req) {
        return (String) req.getAttribute(Aggregator.REPOSITORY);
    }

    /**
     * Get the requested resource and copy it to the ServletOutputStream, bit by bit.
     * @param request HttpServletRequest as given by the servlet container
     * @param response HttpServletResponse as given by the servlet container
     * @throws IOException standard servlet exception
     */
    protected void handleResourceRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String resourceHandle = (String) request.getAttribute(Aggregator.HANDLE);

        log.debug("handleResourceRequest, resourceHandle=\"{}\"", resourceHandle); //$NON-NLS-1$

        if (StringUtils.isNotEmpty(resourceHandle)) {

            HierarchyManager hm = MgnlContext.getHierarchyManager(getRepository(request));

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

    /**
     * Collect content from the pre configured repository and attach it to the HttpServletRequest.
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    protected boolean collect(HttpServletRequest request) throws PathNotFoundException, RepositoryException {
        String handle = Path.getHandle(request);
        String extension = Path.getExtension(request);
        String repository = getRepository(request);
        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(repository);

        Content requestedPage = null;
        NodeData requestedData = null;
        Template template = null;

        if (hierarchyManager.isPage(handle)) {
            requestedPage = hierarchyManager.getContent(handle);

            // check if its a request for a versioned page
            if (request.getParameter(VERSION_NUMBER) != null) {
                // get versioned state
                try {
                    requestedPage = requestedPage.getVersionedContent(request.getParameter(VERSION_NUMBER));
                }
                catch (RepositoryException re) {
                    log.debug(re.getMessage(), re);
                    log.error("Unable to get versioned state, rendering current state of {}", handle);
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
            if (hierarchyManager.isNodeData(handle)) {
                requestedData = hierarchyManager.getNodeData(handle);
            }
            else {
                // check again, resource might have different name
                int lastIndexOfSlash = handle.lastIndexOf("/"); //$NON-NLS-1$

                if (lastIndexOfSlash > 0) {

                    handle = StringUtils.substringBeforeLast(handle, "/"); //$NON-NLS-1$

                    try {
                        requestedData = hierarchyManager.getNodeData(handle);
                        // set the new handle pointing to the real node
                        request.setAttribute(Aggregator.HANDLE, handle);

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
                String templateName = requestedData.getAttribute(NODE_DATA_TEMPLATE);

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

        request.setAttribute(Aggregator.TEMPLATE, template);

        return true;
    }

    boolean startsWithAny(String[] array, String check) {
        for (int j = 0; j < array.length; j++) {
            String string = array[j];
            if (check.startsWith(string)) {
                return true;
            }
        }
        return false;
    }

}
