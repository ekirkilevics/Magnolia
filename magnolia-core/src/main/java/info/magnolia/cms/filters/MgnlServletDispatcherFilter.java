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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class initializes the current context.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class MgnlServletDispatcherFilter implements Filter {

    /**
     * filterConfig.
     */
    private FilterConfig filterConfig;

    private Map servletMap = new Hashtable();

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MgnlServletDispatcherFilter.class);

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        addServlet("/ActivationHandler", "info.magnolia.cms.exchange.simple.SimpleExchangeServlet", null);
        addServlet("/.resources", "info.magnolia.cms.servlets.ClasspathSpool", null);
        addServlet("/.magnolia/trees/", "info.magnolia.module.admininterface.AdminTreeMVCServlet", null);
        addServlet("/.magnolia/dialogs/", "info.magnolia.module.admininterface.DialogMVCServlet", null);
        addServlet("/.magnolia/pages/", "info.magnolia.module.admininterface.PageMVCServlet", null);
        addServlet("/.magnolia/mgnl-export/", "info.magnolia.cms.servlets.ImportExportServlet", null);
        addServlet("/.magnolia/mgnl-import/", "info.magnolia.cms.servlets.ImportExportServlet", null);
        addServlet("/.magnolia/log4j/", "it.openutils.log4j.Log4jConfigurationServlet", null);
        addServlet(
            "/admintemplates/dialogs/fileThumbnail.jsp",
            "info.magnolia.module.admininterface.pages.FileThumbnailDialogPage",
            null);
        addServlet("/uuid/", "info.magnolia.cms.servlets.UUIDRequestDispatcher", null);
        addServlet("/.magnolia/cache-all/", "info.magnolia.cms.cache.CacheGeneratorServlet", null);
        addServlet("/dms/", "info.magnolia.module.dms.DMSDownloadServlet", null);
        addServlet("/dms-static/", "info.magnolia.module.dms.DMSDownloadServlet", null);
        addServlet("/.magnolia/flows", "info.magnolia.module.workflow.servlets.FlowDefServlet", null);
        addServlet("/.magnolia/mail/", "info.magnolia.cms.mail.servlets.MgnlMailServlet", null);

    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        servletMap.clear();
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
        ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String requestURI = getRequestUri(request);

        for (Iterator iter = servletMap.keySet().iterator(); iter.hasNext();) {
            String path = (String) iter.next();
            if (requestURI.startsWith(path)) {

                HttpServletRequest alteredRequest = new AlteredHttpServletRequest(request, path);
                log.info("Dispatching to servlet {} - pathInfo={} requestURI={}", new Object[]{
                    path,
                    alteredRequest.getPathInfo(),
                    requestURI});
                HttpServlet servlet = (HttpServlet) servletMap.get(path);

                servlet.service(new AlteredHttpServletRequest(request, path), response);

                return;
            }

        }

        chain.doFilter(request, response);

    }

    /**
     * @param request
     * @return
     */
    private String getRequestUri(HttpServletRequest request) {
        // handle includes
        String requestURI = (String) request.getAttribute("javax.servlet.include.path_info");

        // handle forwards
        if (StringUtils.isEmpty(requestURI)) {
            requestURI = (String) request.getAttribute("javax.servlet.forward.path_info");
        }

        // standard request
        if (StringUtils.isEmpty(requestURI)) {
            requestURI = request.getPathInfo();
        }
        return requestURI;
    }

    private void addServlet(String path, String className, Map params) {

        try {
            HttpServlet servlet = (HttpServlet) Class.forName(className).newInstance();
            servlet.init();
            servletMap.put(path, servlet);

            log.info("Registered servlet mapping from {} to {}", path, className);
        }
        catch (Throwable e) {
            log.error("Unable to load servlet "
                + className
                + " mapped to path "
                + path
                + " due to a "
                + e.getClass().getName()
                + " exception", e);
        }
    }

    private class AlteredHttpServletRequest extends HttpServletRequestWrapper {

        /**
         * The path to which this servlet is mapped.
         */
        private String mapping;

        /**
         * @param request
         */
        public AlteredHttpServletRequest(HttpServletRequest request, String mapping) {
            super(request);
            this.mapping = mapping;
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getPathInfo()
         */
        public String getPathInfo() {

            String pathInfo = StringUtils.substring(getRequestURI(), mapping.length());
            if (!pathInfo.startsWith("/")) {
                pathInfo = "/" + pathInfo;
            }
            return pathInfo;
        }

    }

}
