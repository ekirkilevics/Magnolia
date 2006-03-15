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
import info.magnolia.cms.Dispatcher;
import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the main http servlet which will be called for any resource request this servlet will dispacth or process
 * requests according to their nature -- all resource requests will go to ResourceDispatcher -- all page requests will
 * be handed over to the defined JSP or Servlet (template). Updated to allow caching of virtual URI's
 * @author Sameer Charles
 * @version 2.1
 */
public class EntryServlet extends ContextSensitiveServlet {

    /**
     * Request parameter: the INTERCEPT holds the name of an administrative action to perform.
     */
    public static final String INTERCEPT = "mgnlIntercept"; //$NON-NLS-1$

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(EntryServlet.class);

    /**
     * The default request interceptor path, defined in web.xml.
     */
    private static final String REQUEST_INTERCEPTOR = "/RequestInterceptor"; //$NON-NLS-1$

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
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     * @throws ServletException
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        // Initialize magnolia context
        super.doGet(req, res);

        if (ConfigLoader.isBootstrapping()) {
            // @todo a nice page, with the log content...
            res.getWriter().write("Magnolia bootstrapping has failed, check bootstrap.log in magnolia/logs"); //$NON-NLS-1$
            return;
        }

        try {
            if (isAuthorized(req, res)) {

                if (redirect(req, res)) {

                    return;
                }
                intercept(req, res);
                // aggregate content
                Aggregator aggregator = new Aggregator(req, res);
                boolean success = aggregator.collect();
                if (success) {
                    try {
                        Dispatcher.dispatch(req, res, getServletContext());
                    }
                    catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
                else {
                    if (log.isDebugEnabled()) {
                        log.debug("Resource not found, redirecting request for [{}] to 404 URI", req.getRequestURI()); //$NON-NLS-1$
                    }

                    if (!res.isCommitted()) {
                        res.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                    else {
                        log.info("Unable to redirect to 404 page, response is already committed"); //$NON-NLS-1$
                    }
                }
            }
        }
        catch (AccessDeniedException e) {
            // don't log AccessDenied as errors, it can happen...
            log.warn(e.getMessage());
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        catch (RuntimeException e) {
            log.error(e.getMessage(), e);
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
     * Redirect based on the mapping in config/server/.node.xml
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return <code>true</code> if request has been redirected, <code>false</code> otherwise
     */
    private boolean redirect(HttpServletRequest request, HttpServletResponse response) {
        String uri = this.getURIMap(request);
        if (StringUtils.isNotEmpty(uri)) {
            try {
                request.getRequestDispatcher(uri).forward(request, response);
            }
            catch (Exception e) {
                log.error("Failed to forward - {}", uri); //$NON-NLS-1$
                log.error(e.getMessage(), e);
            }
            return true;
        }
        return false;
    }

    /**
     * Attach Interceptor servlet if interception needed
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    private void intercept(HttpServletRequest request, HttpServletResponse response) {
        if (request.getParameter(INTERCEPT) != null) {
            try {
                request.getRequestDispatcher(REQUEST_INTERCEPTOR).include(request, response);
            }
            catch (Exception e) {
                log.error("Failed to Intercept"); //$NON-NLS-1$
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @return URI mapping as in ServerInfo
     * @param request HttpServletRequest
     */
    private String getURIMap(HttpServletRequest request) {
        return VirtualURIManager.getInstance().getURIMapping(
            StringUtils.substringAfter(request.getRequestURI(), request.getContextPath()));
    }

}
