/**
 * This file Copyright (c) 2008 Magnolia International
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
package info.magnolia.module.cache.executor;

import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.filter.CacheResponseWrapper;
import info.magnolia.module.cache.filter.CachedEntry;
import info.magnolia.module.cache.filter.CachedError;
import info.magnolia.module.cache.filter.CachedPage;
import info.magnolia.module.cache.filter.CachedRedirect;
import info.magnolia.module.cache.filter.SimpleServletOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;

/**
 * Wraps the response and stores the content in a cache Entry.
 *
 * @author pbracher
 * @version $Revision: $ ($Author: $)
 */
public class Store extends AbstractExecutor {

    public void processCacheRequest(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, Cache cache,
            CachePolicyResult cachePolicy) throws IOException, ServletException {
        CachedEntry cachedEntry = null;
        try {

            // setting Last-Modified to when this resource was stored in the cache
            long modificationDate = System.currentTimeMillis();
            response.setDateHeader("Last-Modified", modificationDate);

            // will write to both the response stream and an internal byte array for caching
            final ByteArrayOutputStream cachingStream = new ByteArrayOutputStream();
            final TeeOutputStream teeOutputStream = new TeeOutputStream(response.getOutputStream(), cachingStream);
            final SimpleServletOutputStream out = new SimpleServletOutputStream(teeOutputStream);
            final CacheResponseWrapper responseWrapper = new CacheResponseWrapper(response, out);
            chain.doFilter(request, responseWrapper);

            try {
                responseWrapper.flushBuffer();
            } catch (IOException e) {
                //TODO better handling ?
                // ignore and don't cache, should be a ClientAbortException
                return;
            }

            cachedEntry = makeCachedEntry(request, responseWrapper, cachingStream, modificationDate);
        } finally {
            // have to put cache entry no matter what even if it is null to release lock.
            cache.put(cachePolicy.getCacheKey(), cachedEntry);
            if (cachedEntry == null ) {
                cache.remove(cachePolicy.getCacheKey());
            }
        }
    }

    protected CachedEntry makeCachedEntry(HttpServletRequest request, CacheResponseWrapper cacheResponse, ByteArrayOutputStream cachingStream, long modificationDate) throws IOException {
        // TODO : handle more of the 30x codes - although CacheResponseWrapper currently only sets the 302.
        if (cacheResponse.getStatus() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
            return new CachedRedirect(cacheResponse.getStatus(), cacheResponse.getRedirectionLocation());
        }
        if (cacheResponse.isError()) {
            return new CachedError(cacheResponse.getStatus());
        }
        if (cachingStream == null) {
            return null;
        }
        final byte[] aboutToBeCached = cachingStream.toByteArray();
                StringBuffer url = request.getRequestURL();
                String query = request.getQueryString();
                if (!StringUtils.isEmpty(query)) {
                    url.append("?").append(query);
                }
        return new CachedPage(aboutToBeCached,
                cacheResponse.getContentType(),
                cacheResponse.getCharacterEncoding(),
                cacheResponse.getStatus(),
                cacheResponse.getHeaders(),
                url.toString(),
                modificationDate);
    }

}
