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
import info.magnolia.cms.Dispatcher;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.VirtualMap;
import info.magnolia.cms.core.CacheHandler;
import info.magnolia.cms.core.CacheProcess;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Listener;
import info.magnolia.cms.security.Lock;
import info.magnolia.cms.security.SecureURI;
import info.magnolia.cms.security.SessionAccessControl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * This is the main http servlet which will be called for any resource request this servlet will dispacth or process
 * requests according to their nature -- all resource requests will go to ResourceDispatcher -- all page requests will
 * be handed over to the defined JSP or Servlet (template).
 * @author Sameer Charles
 * @version 2.0
 */
public class EntryServlet extends HttpServlet {

    public static final String INTERCEPT = "mgnlIntercept";

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(EntryServlet.class);

    private static final String REQUEST_INTERCEPTOR = "/RequestInterceptor";

    /**
     * <p/>This makes browser and proxy caches work more effectively, reducing the load on server and network resources.
     * </p>
     * @param request
     * @return last modified time in miliseconds since 1st Jan 1970 GMT
     */
    public long getLastModified(HttpServletRequest request) {
        return info.magnolia.cms.beans.runtime.Cache.getCreationTime(request);
    }

    /**
     * All HTTP/s requests are handled here.
     * @param req
     * @param res
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            /**
             * Try to find out what the preferred language of this user is.
             */
            // TranslationEngine.findPreferredLanguage(req);
            if (isAllowed(req, res)) {
                if (redirect(req, res)) {
                    return;
                }
                intercept(req, res);
                /* try to stream from cache first */
                if (info.magnolia.cms.beans.runtime.Cache.isCached(req)) {
                    if (CacheHandler.streamFromCache(req, res)) {
                        return; /* if success return */
                    }
                }
                /* aggregate content */
                Aggregator aggregator = new Aggregator(req, res);
                boolean success = aggregator.collect();
                aggregator = null;
                try {
                    Dispatcher.dispatch(req, res, getServletContext());
                    if (success) {
                        if (info.magnolia.cms.beans.config.Cache.isCacheable()) {
                            CacheProcess cache = new CacheProcess(new CacheRequest(req));
                            cache.start();
                        }
                    }
                }
                catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * <p/>All requests are handles by get handler
     * </p>
     * @param req
     * @param res
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

    /**
     * <p/>checks access from Listener / Authenticator / AccessLock
     * </p>
     * @param req HttpServletRequest as received by the service method
     * @param res HttpServletResponse as received by the service method
     * @return boolean
     */
    private boolean isAllowed(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (Lock.isSystemLocked()) {
            res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }
        else if (SessionAccessControl.isSecuredSession(req)) {
            return true;
        }
        else if ((SecureURI.isProtected(getURI(req)))) {
            return authenticate(req, res);
        }
        else if (!Listener.isAllowed(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    /**
     * <p/>Authenticate on basic headers
     * </p>
     * @param req
     * @param res
     */
    private boolean authenticate(HttpServletRequest req, HttpServletResponse res) {
        try {
            if (!Authenticator.authenticate(req)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setHeader("WWW-Authenticate", "BASIC realm=\"" + Server.getBasicRealm() + "\"");
                return false;
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * <p/>redirect based on the mapping in config/server/.node.xml
     * </p>
     * @param request
     * @param response
     */
    private boolean redirect(HttpServletRequest request, HttpServletResponse response) {
        String uri = this.getURIMap(request);
        if (!uri.equals("")) {
            try {
                request.getRequestDispatcher(uri).forward(request, response);
            }
            catch (Exception e) {
                log.error("Failed to forward - " + uri);
                log.error(e.getMessage(), e);
            }
            return true;
        }
        return false;
    }

    /**
     * <p/>attach Interceptor servlet if interception needed
     * </p>
     * @param request
     * @param response
     */
    private void intercept(HttpServletRequest request, HttpServletResponse response) {
        if (request.getParameter(INTERCEPT) != null) {
            try {
                request.getRequestDispatcher(REQUEST_INTERCEPTOR).include(request, response);
            }
            catch (Exception e) {
                log.error("Failed to Intercept");
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @return URI mapping as in ServerInfo
     */
    private String getURIMap(HttpServletRequest request) {
        return VirtualMap.getInstance().getURIMapping(
            StringUtils.substringAfter(request.getRequestURI(), request.getContextPath()));
    }

    /**
     * Extracts uri and extension.
     * @param request
     */
    private String getURI(HttpServletRequest request) {
        return StringUtils.substringBeforeLast(StringUtils.substringAfter(request.getRequestURI(), request
            .getContextPath()), ".");
    }

    /**
     * <p>
     * Simply a copy of the original request used by CacheProcess
     * </p>
     */
    private static class CacheRequest implements HttpServletRequest {

        Map attributes = new HashMap();

        Map headers = new HashMap();

        String uri;

        String contextPath;

        public CacheRequest(HttpServletRequest originalRequest) {
            this.contextPath = originalRequest.getContextPath();
            // remember URI
            uri = StringUtils.substringAfter(originalRequest.getRequestURI(), this.contextPath);
            // copy neccessary attributes
            attributes.put(Aggregator.EXTENSION, originalRequest.getAttribute(Aggregator.EXTENSION));
            attributes.put(Aggregator.ACTPAGE, originalRequest.getAttribute(Aggregator.ACTPAGE));

            // copy headers
            String authHeader = originalRequest.getHeader("Authorization");
            if (authHeader != null) {
                headers.put("Authorization", authHeader);
            }
        }

        public String getRequestURI() {
            return uri;
        }

        public String getHeader(String key) {
            return (String) this.headers.get(key);
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        public StringBuffer getRequestURL() {
            throw new UnsupportedOperationException();
        }

        public String getServletPath() {
            throw new UnsupportedOperationException();
        }

        public HttpSession getSession(boolean b) {
            throw new UnsupportedOperationException();
        }

        public HttpSession getSession() {
            throw new UnsupportedOperationException();
        }

        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException();
        }

        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException();
        }

        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException();
        }

        public boolean isRequestedSessionIdFromUrl() {
            throw new UnsupportedOperationException();
        }

        public String getAuthType() {
            throw new UnsupportedOperationException();
        }

        public Cookie[] getCookies() {
            throw new UnsupportedOperationException();
        }

        public long getDateHeader(String s) {
            throw new UnsupportedOperationException();
        }

        public Enumeration getHeaders(String s) {
            throw new UnsupportedOperationException();
        }

        public Enumeration getHeaderNames() {
            throw new UnsupportedOperationException();
        }

        public int getIntHeader(String s) {
            throw new UnsupportedOperationException();
        }

        public String getMethod() {
            throw new UnsupportedOperationException();
        }

        public String getPathInfo() {
            throw new UnsupportedOperationException();
        }

        public String getPathTranslated() {
            throw new UnsupportedOperationException();
        }

        public String getContextPath() {
            return this.contextPath;
        }

        public String getQueryString() {
            throw new UnsupportedOperationException();
        }

        public String getRemoteUser() {
            throw new UnsupportedOperationException();
        }

        public boolean isUserInRole(String s) {
            throw new UnsupportedOperationException();
        }

        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException();
        }

        public String getRequestedSessionId() {
            throw new UnsupportedOperationException();
        }

        public Enumeration getAttributeNames() {
            throw new UnsupportedOperationException();
        }

        public String getCharacterEncoding() {
            throw new UnsupportedOperationException();
        }

        public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException();
        }

        public int getContentLength() {
            throw new UnsupportedOperationException();
        }

        public String getContentType() {
            throw new UnsupportedOperationException();
        }

        public ServletInputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getParameter(String s) {
            throw new UnsupportedOperationException();
        }

        public Enumeration getParameterNames() {
            throw new UnsupportedOperationException();
        }

        public String[] getParameterValues(String s) {
            throw new UnsupportedOperationException();
        }

        public Map getParameterMap() {
            throw new UnsupportedOperationException();
        }

        public String getProtocol() {
            throw new UnsupportedOperationException();
        }

        public String getScheme() {
            throw new UnsupportedOperationException();
        }

        public String getServerName() {
            throw new UnsupportedOperationException();
        }

        public int getServerPort() {
            throw new UnsupportedOperationException();
        }

        public BufferedReader getReader() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getRemoteAddr() {
            throw new UnsupportedOperationException();
        }

        public String getRemoteHost() {
            throw new UnsupportedOperationException();
        }

        public void setAttribute(String s, Object o) {
            throw new UnsupportedOperationException();
        }

        public void removeAttribute(String s) {
            throw new UnsupportedOperationException();
        }

        public Locale getLocale() {
            throw new UnsupportedOperationException();
        }

        public Enumeration getLocales() {
            throw new UnsupportedOperationException();
        }

        public boolean isSecure() {
            throw new UnsupportedOperationException();
        }

        public RequestDispatcher getRequestDispatcher(String s) {
            throw new UnsupportedOperationException();
        }

        public String getRealPath(String s) {
            throw new UnsupportedOperationException();
        }

        public int getRemotePort() {
            throw new UnsupportedOperationException();
        }

        public String getLocalName() {
            throw new UnsupportedOperationException();
        }

        public String getLocalAddr() {
            throw new UnsupportedOperationException();
        }

        public int getLocalPort() {
            throw new UnsupportedOperationException();
        }

    }

}
