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

import info.magnolia.cms.util.RequestHeaderUtil;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.filter.CacheResponseWrapper;
import info.magnolia.module.cache.filter.CachedEntry;
import info.magnolia.module.cache.filter.CachedError;
import info.magnolia.module.cache.filter.CachedPage;
import info.magnolia.module.cache.filter.CachedRedirect;
import info.magnolia.module.cache.filter.SimpleServletOutputStream;
import info.magnolia.module.cache.util.GZipUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.jackrabbit.commons.packaging.ContentPackage;

/**
 * Wraps the response and stores the content in a cache Entry.
 *
 * @author pbracher
 * @version $Revision: $ ($Author: $)
 */
public class Store extends AbstractExecutor {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Store.class);
    private Map compressible;


    public void processCacheRequest(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, Cache cache,
            CachePolicyResult cachePolicy) throws IOException, ServletException {
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
            final long modificationDate = System.currentTimeMillis();
            responseWrapper.setDateHeader("Last-Modified", modificationDate);
            chain.doFilter(request, responseWrapper);

            if ((responseWrapper.getStatus() != HttpServletResponse.SC_MOVED_TEMPORARILY) && (responseWrapper.getStatus() != HttpServletResponse.SC_NOT_MODIFIED) && !responseWrapper.isError()) {
                //handle gzip headers (have to be written BEFORE commiting the response
                final boolean acceptsGzipEncoding = RequestHeaderUtil.acceptsGzipEncoding(request) && compressible != null && compressible.values().contains(response.getContentType());
                if (acceptsGzipEncoding) {
                    RequestHeaderUtil.addAndVerifyHeader(responseWrapper, "Content-Encoding", "gzip");
                    RequestHeaderUtil.addAndVerifyHeader(responseWrapper, "Vary", "Accept-Encoding"); // needed for proxies
                }
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

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            // have to put cache entry no matter what even if it is null to release lock.
            cache.put(cachePolicy.getCacheKey(), cachedEntry);
            if (cachedEntry == null ) {
                cache.remove(cachePolicy.getCacheKey());
            } else {
                cachePolicy.setCachedEntry(cachedEntry);
            }
        }
    }

    protected CachedEntry makeCachedEntry(CacheResponseWrapper cacheResponse, ByteArrayOutputStream cachingStream) throws IOException {
        // TODO : handle more of the 30x codes - although CacheResponseWrapper currently only sets the 302.
        if (cacheResponse.getStatus() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
            return new CachedRedirect(cacheResponse.getStatus(), cacheResponse.getRedirectionLocation());
        }
        if (cacheResponse.getStatus() == HttpServletResponse.SC_NOT_MODIFIED) {
            return null;
        }
        if (cacheResponse.isError()) {
            return new CachedError(cacheResponse.getStatus());
        }

        final long modificationDate = cacheResponse.getLastModified();
        final byte[] aboutToBeCached = cachingStream.toByteArray();
        final String contentType = cacheResponse.getContentType();
        return new CachedPage(aboutToBeCached,
                contentType,
                cacheResponse.getCharacterEncoding(),
                cacheResponse.getStatus(),
                cacheResponse.getHeaders(),
                modificationDate, compressible != null && compressible.values().contains(contentType));
    }

    /**
     * @deprecated not used, since 3.6.1, the modificationDate is retrieved from the CacheResponseWrapper, using the
     * appropriate header. It has been set by processCacheRequest() and possibly overwritten by another filter or servlet.
     */
    protected CachedEntry makeCachedEntry(CacheResponseWrapper cacheResponse, ByteArrayOutputStream cachingStream, long modificationDate) throws IOException {
        return makeCachedEntry(cacheResponse, cachingStream);
    }

    public Map getCompressible() {
        return compressible;
    }

    public void setCompressible(Map compressible) {
        this.compressible = compressible;
    }
}
