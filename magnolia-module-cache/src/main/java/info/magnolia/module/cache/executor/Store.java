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
import info.magnolia.cms.util.RequestHeaderUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicy;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.filter.CacheResponseWrapper;
import info.magnolia.module.cache.filter.CachedEntry;
import info.magnolia.module.cache.filter.CachedError;
import info.magnolia.module.cache.filter.CachedPage;
import info.magnolia.module.cache.filter.CachedRedirect;
import info.magnolia.module.cache.filter.SimpleServletOutputStream;
import info.magnolia.voting.voters.ResponseContentTypeVoter;

import java.io.ByteArrayOutputStream;
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
            // will write to both the response stream and an internal byte array for caching
            final ByteArrayOutputStream cachingStream = new ByteArrayOutputStream();

            final SimpleServletOutputStream out = new SimpleServletOutputStream(cachingStream);
            final CacheResponseWrapper responseWrapper = new CacheResponseWrapper(response, out) {
                public void flushBuffer() throws IOException {
                    // do nothing we will flush later.
                }
            };

            // setting Last-Modified to when this resource was stored in the cache. This value might get overriden by further filters or servlets.
            final long cacheStorageDate = System.currentTimeMillis();
            responseWrapper.setDateHeader("Last-Modified", cacheStorageDate);
            chain.doFilter(request, responseWrapper);

            if ((responseWrapper.getStatus() != HttpServletResponse.SC_MOVED_TEMPORARILY) && (responseWrapper.getStatus() != HttpServletResponse.SC_NOT_MODIFIED) && !responseWrapper.isError()) {
                //handle gzip headers (have to be written BEFORE committing the response
                int vote = getCompressionVote(responseWrapper, ResponseContentTypeVoter.class);
                final boolean acceptsGzipEncoding = RequestHeaderUtil.acceptsGzipEncoding(request) && vote == 0;
                if (acceptsGzipEncoding) {
                    RequestHeaderUtil.addAndVerifyHeader(responseWrapper, "Content-Encoding", "gzip");
                    RequestHeaderUtil.addAndVerifyHeader(responseWrapper, "Vary", "Accept-Encoding"); // needed for proxies
                }
            }

            // change the status (if appropriate) before flushing the buffer.
            if (!response.isCommitted() && !ifModifiedSince(request, cacheStorageDate)) {
                responseWrapper.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

            }

            try {
                response.flushBuffer();
                responseWrapper.flush();

            } catch (IOException e) {
                //TODO better handling ?
                // ignore and don't cache, should be a ClientAbortException
                return;
            }

            cachedEntry = makeCachedEntry(responseWrapper, cachingStream);
            // cached page should have some body
            if (cachedEntry != null && (cachedEntry instanceof CachedPage) && ((CachedPage) cachedEntry).getDefaultContent().length == 0) {
                log.warn("Response body for {}:{} is empty.",  String.valueOf(responseWrapper.getStatus()), cachePolicyResult.getCacheKey());
            }

            // Cached page should be created only with 200 status and nothing else should go in
            if ((cachedEntry instanceof CachedPage) && ((CachedPage) cachedEntry).getStatusCode() != HttpServletResponse.SC_OK) {
                log.warn("Caching response {} for {}", String.valueOf(((CachedPage) cachedEntry).getStatusCode() ), cachePolicyResult.getCacheKey());
            }


        } catch (Throwable t) {
            log.error("Failed to process cache request : " + t.getMessage(), t);
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

    protected CachedEntry makeCachedEntry(CacheResponseWrapper cacheResponse, ByteArrayOutputStream cachingStream) throws IOException {
        int status = cacheResponse.getStatus();
        // TODO : handle more of the 30x codes - although CacheResponseWrapper currently only sets the 302 or 304.
        if (status == HttpServletResponse.SC_MOVED_TEMPORARILY) {
            return new CachedRedirect(cacheResponse.getStatus(), cacheResponse.getRedirectionLocation());
        }

        final byte[] aboutToBeCached = cachingStream.toByteArray();
        if (status == HttpServletResponse.SC_NOT_MODIFIED) {
            if (aboutToBeCached.length == 0) {
                return null;
            } else {
                // we got the content already so we might as well cache it
                status = HttpServletResponse.SC_OK;
            }
        }

        if (cacheResponse.isError()) {
            return new CachedError(cacheResponse.getStatus());
        }

        final long modificationDate = cacheResponse.getLastModified();
        final String contentType = cacheResponse.getContentType();

        int vote = getCompressionVote(cacheResponse, ResponseContentTypeVoter.class);
        CachedPage page = new CachedPage(aboutToBeCached,
                contentType,
                cacheResponse.getCharacterEncoding(),
                status,
                cacheResponse.getHeaders(),
                modificationDate, vote == 0);
        if (status != cacheResponse.getStatus()) {
            // since we have manipulated the status here, we need to provide original value to other executors not to confuse them
            page.setPreCacheStatusCode(cacheResponse.getStatus());
        }
        return page;
    }

    /**
     * @deprecated not used, since 3.6.1, the modificationDate is retrieved from the CacheResponseWrapper, using the
     * appropriate header. It has been set by processCacheRequest() and possibly overwritten by another filter or servlet.
     */
    protected CachedEntry makeCachedEntry(CacheResponseWrapper cacheResponse, ByteArrayOutputStream cachingStream, long modificationDate) throws IOException {
        return makeCachedEntry(cacheResponse, cachingStream);
    }

    protected CachePolicy getCachePolicy(Cache cache) {
        return getModule().getConfiguration(cache.getName()).getCachePolicy();
    }

    protected CacheModule getModule() {
        return CacheModule.getInstance();
    }
}
