/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.cache.executor;

import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicy;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.filter.CacheResponseWrapper;
import info.magnolia.module.cache.filter.CachedEntry;
import info.magnolia.module.cache.filter.CachedError;
import info.magnolia.module.cache.filter.ContentCachedEntry;
import info.magnolia.module.cache.filter.CachedRedirect;
import info.magnolia.module.cache.filter.InMemoryCachedEntry;
import info.magnolia.module.cache.filter.DelegatingBlobCachedEntry;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Wraps the response and stores the content in a cache Entry.
 *
 * @author pbracher
 * @version $Revision: $ ($Author: $)
 */
public class Store extends AbstractExecutor {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Store.class);

    public void processCacheRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Cache cache, CachePolicyResult cachePolicyResult) throws IOException, ServletException {
        CachedEntry cachedEntry = null;
        try {

            final CacheResponseWrapper responseWrapper = new CacheResponseWrapper(response, CacheResponseWrapper.DEFAULT_THRESHOLD, false);

            // setting Last-Modified to when this resource was stored in the cache. This value might get overriden by further filters or servlets.
            final long cacheStorageDate = System.currentTimeMillis();
            responseWrapper.setDateHeader("Last-Modified", cacheStorageDate);
            chain.doFilter(request, responseWrapper);

            if (responseWrapper.getStatus() == HttpServletResponse.SC_NOT_MODIFIED) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            responseWrapper.flushBuffer();

            cachedEntry = makeCachedEntry(request, responseWrapper);

        } catch (Throwable t) {
            log.error("Failed to process cache request [" + request.getRequestURL() + "] : " + t.getMessage(), t);
        } finally {
            final Object key = cachePolicyResult.getCacheKey();
            // have to put cache entry no matter what even if it is null to release lock.
            cache.put(key, cachedEntry);
            if (cachedEntry == null ) {
                cache.remove(cachePolicyResult.getCacheKey());
            } else {
                cachePolicyResult.setCachedEntry(cachedEntry);
                // let policy know the uuid in case it wants to do something with it
                final Content content = MgnlContext.getAggregationState().getCurrentContent();
                if (content != null && content.isNodeType("mix:referenceable")) {
                    final String uuid = content.getUUID();
                    String repo = content.getHierarchyManager().getName();
                    getCachePolicy(cache).persistCacheKey(repo, uuid, key);
                }
            }
        }
    }

    protected CachedEntry makeCachedEntry(HttpServletRequest request, CacheResponseWrapper cachedResponse) throws IOException {
        int status = cachedResponse.getStatus();
        // TODO : handle more of the 30x codes - although CacheResponseWrapper currently only sets the 302 or 304.
        if (cachedResponse.getRedirectionLocation() != null) {
            return new CachedRedirect(cachedResponse.getStatus(), cachedResponse.getRedirectionLocation());
        }

        if (cachedResponse.isError()) {
            return new CachedError(cachedResponse.getStatus());
        }

        final long modificationDate = cachedResponse.getLastModified();
        final String contentType = cachedResponse.getContentType();

        ContentCachedEntry cacheEntry;
        if(!cachedResponse.isThresholdExceeded()){
            cacheEntry = new InMemoryCachedEntry(cachedResponse.getBufferedContent(),
                    contentType,
                    cachedResponse.getCharacterEncoding(),
                    status,
                    cachedResponse.getHeaders(),
                    modificationDate);
        }
        else{
            cacheEntry = new DelegatingBlobCachedEntry(cachedResponse.getContentLength(),
                contentType,
                cachedResponse.getCharacterEncoding(),
                status,
                cachedResponse.getHeaders(),
                modificationDate);

            // TODO remove this once we use a blob store
            // the file will be deleted once served in this request
            ((DelegatingBlobCachedEntry)cacheEntry).bindContentFileToCurrentRequest(request, cachedResponse.getContentFile());
        }

        return cacheEntry;
    }

    protected CachePolicy getCachePolicy(Cache cache) {
        return getModule().getConfiguration(cache.getName()).getCachePolicy();
    }

    protected CacheModule getModule() {
        return CacheModule.getInstance();
    }
}
