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
package info.magnolia.cms.security;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.security.auth.login.LoginException;
import java.io.IOException;

/**
 * Optional filter which wont be a part of default filter chain
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * $Id$
 */
public class ForceLoginFilter implements Filter {

    protected static final String ATTRIBUTE_LOGINERROR = "mgnlLoginError";

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        if (!Authenticator.isAuthenticated(httpRequest)) {
            doForceLogin(httpRequest);
        }
        filterChain.doFilter(httpRequest, httpResponse);
    }

    private void doForceLogin(HttpServletRequest request) {
        // MAGNOLIA-1385 allow to login without having to request a secure uri
        if (request.getParameter(Authenticator.PARAMETER_USER_ID) != null
            && request.getParameter(Authenticator.PARAMETER_PSWD) != null) {
            try {
                Authenticator.authenticate(request);
            }
            catch (LoginException e) {
                // set a request parameter that can be used in page to detect an unsuccessful login attempt
                request.setAttribute(ATTRIBUTE_LOGINERROR, e);
            }
        }
    }

    public void destroy() {
    }
}
