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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides basic infrastructure for filters which check if a request is authorized.
 *
 * @author Sameer Charles
 *         $Id$
 */
public abstract class BaseSecurityFilter extends AbstractMgnlFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BaseSecurityFilter.class);

    /**
     * Continue with the Magnolia defined filter chain if {@link #isAllowed} returns true,
     * else set an http response code 401.
     */
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isAllowed(request, response)) {
            chain.doFilter(request, response);
        } else {
            doAuthenticate(request, response);
        }
    }

    /**
     * Implementations need to return false if the request can not be proceeded with, but also need to set an appropriate error code.
     */
    protected abstract boolean isAllowed(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * TODO : verify we do have a correct http status code?.
     * @deprecated since 4.5, should not be needed, and does nothing.
     */
    protected void doAuthenticate(HttpServletRequest request, HttpServletResponse response) {
        /*
        HttpSession httpsession = request.getSession(false);
        if (httpsession != null) {
            httpsession.invalidate();
        }
        */
    }

}
