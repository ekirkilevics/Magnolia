/**
 * This file Copyright (c) 2008-2011 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicy;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.filter.CacheResponseWrapper;
import info.magnolia.module.cache.filter.CachedEntry;
import info.magnolia.module.cache.filter.CachedError;
import info.magnolia.module.cache.filter.CachedRedirect;
import info.magnolia.module.cache.filter.ContentCachedEntry;
import info.magnolia.module.cache.filter.DelegatingBlobCachedEntry;
import info.magnolia.module.cache.filter.InMemoryCachedEntry;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
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

    @Override
    public void processCacheRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Cache cache, CachePolicyResult cachePolicyResult) throws IOException, ServletException {
        CachedEntry cachedEntry = null;
        final Object key = cachePolicyResult.getCacheKey();

        final CacheResponseWrapper responseWrapper = new CacheResponseWrapper(response, CacheResponseWrapper.DEFAULT_THRESHOLD, false);

        // setting Last-Modified to when this resource was stored in the cache. This value might get overriden by further filters or servlets.
        final long cacheStorageDate = System.currentTimeMillis();
        responseWrapper.setDateHeader("Last-Modified", cacheStorageDate);
        chain.doFilter(request, responseWrapper);

        if (responseWrapper.getStatus() == HttpServletResponse.SC_NOT_MODIFIED) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        else{
            responseWrapper.flushBuffer();
            cachedEntry = makeCachedEntry(request, responseWrapper);
        }

        // also put null to unblock the cache
        cache.put(key, cachedEntry);

        if(cachedEntry != null){
            cachePolicyResult.setCachedEntry(cachedEntry);
            // let policy know the uuid in case it wants to do something with it
            final Node content = MgnlContext.getAggregationState().getCurrentContent();
            try {
                if (content != null && NodeUtil.isNodeType(content, "mix:referenceable")) {
                    final String uuid = content.getIdentifier();
                    String repo = content.getSession().getWorkspace().getName();
                    getCachePolicy(cache).persistCacheKey(repo, uuid, key);
                }
            } catch (RepositoryException e) {
                // TODO dlipp: apply consistent ExceptionHandling
                throw new RuntimeException(e);
            }

        }
    }

    protected CachedEntry makeCachedEntry(HttpServletRequest request, CacheResponseWrapper cachedResponse) throws IOException {
        // query params are handled by the cache key
        final String originalUrl = request.getRequestURL().toString();
        int status = cachedResponse.getStatus();
        // TODO : handle more of the 30x codes - although CacheResponseWrapper currently only sets the 302 or 304.
        if (cachedResponse.getRedirectionLocation() != null) {
            return new CachedRedirect(cachedResponse.getStatus(), cachedResponse.getRedirectionLocation(), originalUrl);
        }

        if (cachedResponse.isError()) {
            return new CachedError(cachedResponse.getStatus(), originalUrl);
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
                    modificationDate,
                    originalUrl);
        }
        else{
            cacheEntry = new DelegatingBlobCachedEntry(cachedResponse.getContentLength(),
                contentType,
                cachedResponse.getCharacterEncoding(),
                status,
                cachedResponse.getHeaders(),
                modificationDate,
                originalUrl);

            // TODO remove this once we use a blob store
            // the file will be deleted once served in this request
            ((DelegatingBlobCachedEntry)cacheEntry).bindContentFileToCurrentRequest(request, cachedResponse.getContentFile());
        }

        return cacheEntry;
    }

    protected CachePolicy getCachePolicy(Cache cache) {
        return getModule().getConfiguration(cache.getName()).getCachePolicy();
    }

    /**
     * @deprecated since 5.0, use IoC/CDI
     */
    protected CacheModule getModule() {
        return CacheModule.getInstance();
    }
}
