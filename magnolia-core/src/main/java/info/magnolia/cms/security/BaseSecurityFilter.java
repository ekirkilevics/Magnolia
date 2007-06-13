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

import info.magnolia.cms.filters.AbstractMagnoliaFilter;
import info.magnolia.cms.security.auth.callback.HttpClientCallback;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Provides basic infrastructure to authenticate request using form or basic realm
 * @author Sameer Charles
 * $Id$
 */
public abstract class BaseSecurityFilter extends AbstractMagnoliaFilter {

    private HttpClientCallback clientCallback;

    /**
     * Continue with the magnolia defined filter chain if isAllowed returns true
     * else send an authentication request to the client as configured
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (isAllowed(request, response)) {
            chain.doFilter(request, response);
        } else {
            doAuthenticate(request, response);
        }

    }

    public abstract boolean isAllowed (HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * In most cases this will provide a standard login mechanism, override this to support
     * other login strategies
     * */
    public void doAuthenticate(HttpServletRequest request, HttpServletResponse response) {
        HttpSession httpsession = request.getSession(false);
        if (httpsession != null) {
            httpsession.invalidate();
        }
        getClientCallback().handle(request, response);
    }

    public HttpClientCallback getClientCallback() {
        return clientCallback;
    }

    public void setClientCallback(HttpClientCallback clientCallback) {
        this.clientCallback = clientCallback;
    }

}
