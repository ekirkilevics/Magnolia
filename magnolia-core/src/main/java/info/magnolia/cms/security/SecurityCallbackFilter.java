/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.security;

import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter;
import info.magnolia.cms.security.auth.callback.HttpClientCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


import static info.magnolia.cms.util.ExceptionUtil.rethrow;
import static info.magnolia.cms.util.ExceptionUtil.wasCausedBy;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * A filter which handles 401, 403 HTTP response codes, as well as {@link javax.jcr.AccessDeniedException}s,
 * and renders an appropriate "login form" (which can consist of a redirect or anything else just as well).
 *
 * A number of {@link HttpClientCallback}s can be configured for this filter, each with a different configuration,
 * and behavior. The {@link info.magnolia.cms.security.auth.callback.AbstractHttpClientCallback} provides a number
 * of filtering capabilities (using url, host or voters).
 *
 *
 * This functionality used to live in {@link BaseSecurityFilter}, {@link URISecurityFilter}, as well as {@link ContentSecurityFilter}.
 * These filters now merely set an HTTP response code or throw an exception, which is handled here.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SecurityCallbackFilter extends OncePerRequestAbstractMgnlFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityCallbackFilter.class);

    /**
     * Used to tell the client that he has to login: serve a login page, set http headers or redirect.
     */
    private final List<HttpClientCallback> clientCallbacks;

    public SecurityCallbackFilter() {
        this.clientCallbacks = new ArrayList<HttpClientCallback>();
    }

    @Override
    public boolean bypasses(HttpServletRequest request) {
        return super.bypasses(request);
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse originalResponse, FilterChain chain) throws IOException, ServletException {
        final StatusSniffingResponseWrapper response = new StatusSniffingResponseWrapper(originalResponse);
        try {
            chain.doFilter(request, response);
            if (needsCallback(response)) {
                selectAndHandleCallback(request, response);
            }
        } catch (Throwable e) {
            // an exception was thrown in the filter chain, let's see if it wraps an AccessDeniedException
            if (wasCausedBy(e, javax.jcr.AccessDeniedException.class)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                selectAndHandleCallback(request, response);
            } else {
                rethrow(e, IOException.class, ServletException.class);
            }
        }
    }

    protected boolean needsCallback(StatusSniffingResponseWrapper response) {
        final int status = response.getStatus();
        return status == SC_FORBIDDEN || status == SC_UNAUTHORIZED;
    }

    protected void selectAndHandleCallback(HttpServletRequest request, StatusSniffingResponseWrapper response) {
        selectClientCallback(request).handle(request, response);
    }

    protected HttpClientCallback selectClientCallback(HttpServletRequest request) {
        for (HttpClientCallback clientCallback : clientCallbacks) {
            if (clientCallback.accepts(request)) {
                return clientCallback;
            }
        }
        throw new IllegalStateException("No configured callback accepted this request " + request.toString());
    }

    // ---- configuration methods
    public void addClientCallback(HttpClientCallback clientCallback) {
        this.clientCallbacks.add(clientCallback);
    }

    // TODO needs to be public to be seen by c2b!?
    public List<HttpClientCallback> getClientCallbacks() {
        return clientCallbacks;
    }

    public static class StatusSniffingResponseWrapper extends HttpServletResponseWrapper {
        private int status = SC_OK;

        public StatusSniffingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        public int getStatus() {
            return status;
        }

        @Override
        public void reset() {
            super.reset();
            status = SC_OK;
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            this.status = sc;
        }

        @Override
        public void setStatus(int sc, String sm) {
            super.setStatus(sc, sm);
            this.status = sc;
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            super.sendRedirect(location);
            this.status = SC_MOVED_TEMPORARILY;
        }

        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            this.status = sc;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            this.status = sc;
        }
    }
}
