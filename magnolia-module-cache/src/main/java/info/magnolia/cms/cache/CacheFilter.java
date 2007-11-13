/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.cache;

import info.magnolia.cms.filters.AbstractMgnlFilter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0 $Id$
 */
public class CacheFilter extends AbstractMgnlFilter {

    public static String ALREADY_FILTERED = CacheFilter.class.getName();

    /**
     * the cache manager.
     */
    private CacheManager cacheManager;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        this.cacheManager = CacheManagerFactory.getCacheManager();
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        this.cacheManager = null;
    }

    /**
     * @see javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        if (cacheManager.isEnabled() && cacheManager.isRunning()) {
            boolean cacheable = cacheManager.isCacheable(request);

            // check if the request is cachable before trying to stream from cache.
            // if the same page has already been requested before without parameters we should not fetch it from the
            // cache
            // if this time there are parameters.
            if (cacheable) {
                String key = this.getCacheKey(request);
                if (!this.ifModifiedSince(request, cacheManager.getCreationTime(key))) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
                boolean canCompress = cacheManager.canCompress(request);
                boolean usedCache = cacheManager.streamFromCache(key, response, canCompress
                    && clientAcceptsGzip(request));
                if (!usedCache && !isAlreadyFiltered(request) && cacheManager.isCacheable(request)) {
                    // mark the request as already filtered, avoid recursion
                    request.setAttribute(ALREADY_FILTERED, Boolean.TRUE);

                    CacheResponseWrapper responseWrapper = new CacheResponseWrapper(response);

                    chain.doFilter(request, responseWrapper);

                    // if response has status 200 go cache it
                    if (responseWrapper.getStatus() == HttpServletResponse.SC_OK) {
                        responseWrapper.flushBuffer(); // be sure to flush before read
                        CacheableEntry cacheableEntry = responseWrapper.getCacheableEntry();
                        if (cacheableEntry != null && cacheableEntry.getOut().length > 0) {
                            this.cacheManager.cacheRequest(key, cacheableEntry, canCompress);
                        }
                    }
                    return;
                }
                else if (usedCache) {
                    // dont forward, response has already been commited if usedCache is true.
                    return;
                }
            }
        }

        // don't cache, just go on
        chain.doFilter(request, response);
    }

    /**
     * Check if server cache is newer then the client cache
     * @param request The servlet request we are processing
     * @return boolean true if the server resource is newer
     */
    public boolean ifModifiedSince(HttpServletRequest request, long lastModified) {
        try {
            long headerValue = request.getDateHeader("If-Modified-Since");
            if (headerValue != -1) {
                // If an If-None-Match header has been specified, if modified since
                // is ignored.
                if ((request.getHeader("If-None-Match") == null)
                    && (lastModified > 0 && lastModified <= headerValue + 1000)) {
                    return false;
                }
            }
        }
        catch (IllegalArgumentException illegalArgument) {
            return true;
        }
        return true;
    }

    /**
     * @return computed cache key
     */
    public String getCacheKey(HttpServletRequest request) {
        return getCacheManager().getCacheKey(request);
    }

    public CacheManager getCacheManager() {
        return this.cacheManager;
    }

    public boolean clientAcceptsGzip(HttpServletRequest request) {
        return StringUtils.contains(request.getHeader("Accept-Encoding"), "gzip");
    }

    protected boolean isAlreadyFiltered(HttpServletRequest request) {
        return request.getAttribute(ALREADY_FILTERED) != null;
    }

}
