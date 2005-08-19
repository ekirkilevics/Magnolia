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
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Listener;
import info.magnolia.cms.security.Lock;
import info.magnolia.cms.security.SecureURI;
import info.magnolia.cms.security.SessionAccessControl;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class SecurityFilter implements Filter {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(SecurityFilter.class);

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        // unused
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
        else if (SessionAccessControl.isSecuredSession(req)) {
            return true;
        }
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
            if (!Authenticator.authenticate(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("WWW-Authenticate", "BASIC realm=\"" + Server.getBasicRealm() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                // invalidate previous session
                SessionAccessControl.invalidateUser(request);
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
