/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.cms.security.auth.callback.HttpClientCallback;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Provides basic infrastructure to authenticate request using form or basic realm.
 *
 * @author Sameer Charles
 * $Id$
 */
public abstract class BaseSecurityFilter extends AbstractMgnlFilter {

    /**
     * Used to tell the client that he has to login: serve a login page, set http headers or redirect.
     */
    private HttpClientCallback clientCallback;

    /**
     * Continue with the Magnolia defined filter chain if isAllowed returns true
     * else send an authentication request to the client as configured.
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isAllowed(request, response)) {
            chain.doFilter(request, response);
        } else {
            doAuthenticate(request, response);
        }
    }

    protected abstract boolean isAllowed(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * In most cases this will provide a standard login mechanism, override this to support
     * other login strategies.
     */
    protected void doAuthenticate(HttpServletRequest request, HttpServletResponse response) {
        /*
        HttpSession httpsession = request.getSession(false);
        if (httpsession != null) {
            httpsession.invalidate();
        }
        */
        getClientCallback().handle(request, response);
    }

    public HttpClientCallback getClientCallback() {
        return clientCallback;
    }

    public void setClientCallback(HttpClientCallback clientCallback) {
        this.clientCallback = clientCallback;
    }

}
