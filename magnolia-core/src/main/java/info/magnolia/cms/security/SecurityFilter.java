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
package info.magnolia.cms.security;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Path;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class SecurityFilter implements Filter {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    /**
     * filter config login form
     */
    private static final String LOGIN_FORM = "LoginForm";

    /**
     * filter config unsecured URI
     */
    private static final String UNSECURED_URI = "UnsecuredPath";

    /**
     * Authentication type
     */
    private static final String AUTH_TYPE = "AuthType";

    /**
     * Authentication type Basic
     */
    private static final String AUTH_TYPE_BASIC = "Basic";

    /**
     * filter config
     */
    private FilterConfig filterConfig;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // unused
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
        ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (isAllowed(request, response)) {
            chain.doFilter(request, response);
        }
    }

    /**
     * Checks access from Listener / Authenticator / AccessLock.
     * @param req HttpServletRequest as received by the service method
     * @param res HttpServletResponse as received by the service method
     * @return boolean <code>true</code> if access to the resource is allowed
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     */
    protected boolean isAllowed(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (Lock.isSystemLocked()) {
            res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }
        else if (Authenticator.isAuthenticated(req)) {
            return true;
        }
        else if (SecureURI.isUnsecure(Path.getURI(req)))
            return true;
        else if (SecureURI.isProtected(Path.getURI(req))) {
            return authenticate(req, res);
        }
        else if (!Listener.isAllowed(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    /**
     * Authenticate on basic headers.
     * @param request HttpServletRequest
     * @param response HttpServletResponst
     * @return <code>true</code> if the user is authenticated
     */
    private boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (Path.getURI(request).startsWith(this.filterConfig.getInitParameter(UNSECURED_URI))) {
                return true;
            }
            if (!Authenticator.authenticate(request)) {
                // invalidate previous session

                HttpSession httpsession = request.getSession(false);
                if (httpsession != null) {
                    httpsession.invalidate();
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                if (StringUtils.equalsIgnoreCase(this.filterConfig.getInitParameter(AUTH_TYPE), AUTH_TYPE_BASIC)) {
                    response.setHeader("WWW-Authenticate", "BASIC realm=\"" + Server.getBasicRealm() + "\"");
                }
                else {
                    request.getRequestDispatcher(this.filterConfig.getInitParameter(LOGIN_FORM)).include(
                        request,
                        response);
                }
                return false;
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

}
