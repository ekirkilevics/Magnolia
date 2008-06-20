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
import info.magnolia.module.cache.filter.CachedEntry;
import info.magnolia.module.cache.filter.CachedError;
import info.magnolia.module.cache.filter.CachedPage;
import info.magnolia.module.cache.filter.CachedRedirect;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MultiMap;

/**
 * Serves the content from the cache.
 *
 * @author pbracher
 * @version $Revision: $ ($Author: $)
 */
public class UseCache extends AbstractExecutor {

    public void processCacheRequest(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, Cache cache,
            CachePolicyResult cachePolicy) throws IOException, ServletException {
        final CachedEntry cached = (CachedEntry) cachePolicy.getCachedEntry();
        processCachedEntry(cached, request, response);
    }

    protected void processCachedEntry(CachedEntry cached, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (cached instanceof CachedPage) {
            final CachedPage page = (CachedPage) cached;
            if (!ifModifiedSince(request, page.getLastModificationTime())) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            writePage(request, response, page);
            response.flushBuffer();
        } else if (cached instanceof CachedError) {
            final CachedError error = (CachedError) cached;
            response.sendError(error.getStatusCode());
        } else if (cached instanceof CachedRedirect) {
            final CachedRedirect redir = (CachedRedirect) cached;
            // we'll ignore the redirection code for now - especially since the servlet api doesn't really let us choose anyway
            // except if someone sets the header manually ?
            response.sendRedirect(redir.getLocation());
        } else {
            throw new IllegalStateException("Unexpected CachedEntry type: " + cached);
        }
    }

    /**
     * Check if server cache is newer then the client cache
     * @param request The servlet request we are processing
     * @return boolean true if the server resource is newer
     */
    protected boolean ifModifiedSince(HttpServletRequest request, long lastModified) {
        // TODO : what is this magic 1sec gap all about ?
        try {
            long headerValue = request.getDateHeader("If-Modified-Since");
            if (headerValue != -1) {
                // If an If-None-Match header has been specified, if modified since
                // is ignored.
                // the header defines only seconds so we ignore the milliseconds
                if ((request.getHeader("If-None-Match") == null)
                        && (lastModified > 0 && (lastModified - (lastModified % 1000) >= headerValue ))) {
                    return false;
                }
            }
        } catch (IllegalArgumentException e) {
            // can happen per spec if the header value can't be converted to a date ...
            return true;
        }
        return true;
    }

    protected void writePage(final HttpServletRequest request, final HttpServletResponse response, final CachedPage cachedEntry) throws IOException {
        final boolean acceptsGzipEncoding = RequestHeaderUtil.acceptsGzipEncoding(request);

        response.setStatus(cachedEntry.getStatusCode());
        addHeaders(cachedEntry, acceptsGzipEncoding, response);

        if(cachedEntry.getLastModificationTime()>0){
            response.setDateHeader("Last-Modified", cachedEntry.getLastModificationTime());
        }

        // TODO : cookies ?
        response.setContentType(cachedEntry.getContentType());
        response.setCharacterEncoding(cachedEntry.getCharacterEncoding());
        writeContent(response, cachedEntry, acceptsGzipEncoding);
    }

    /**
     * Set the headers in the response object
     */
    protected void addHeaders(final CachedPage cachedEntry, final boolean acceptsGzipEncoding, final HttpServletResponse response) {
        final MultiMap headers = cachedEntry.getHeaders();

        final Iterator it = headers.keySet().iterator();
        while (it.hasNext()) {
            final String header = (String) it.next();
            if (!acceptsGzipEncoding) {
                if ("Content-Encoding".equals(header) || "Vary".equals(header)) {
                    continue;
                }
            }

            final Collection values = (Collection) headers.get(header);
            final Iterator valIt = values.iterator();
            while (valIt.hasNext()) {
                final Object val = valIt.next();
                if (val instanceof Long) {
                    response.addDateHeader(header, ((Long) val).longValue());
                } else if (val instanceof Integer) {
                    response.addIntHeader(header, ((Integer) val).intValue());
                } else if (val instanceof String) {
                    response.addHeader(header, (String) val);
                } else {
                    throw new IllegalStateException("Unrecognized type for header [" + header + "], value is: " + val);
                }

            }
        }
    }

    protected void writeContent(final HttpServletResponse response, final CachedPage cachedEntry, boolean acceptsGzipEncoding) throws IOException {
        final byte[] body;
        if (!acceptsGzipEncoding && cachedEntry.getUngzippedContent() != null) {
            body = cachedEntry.getUngzippedContent();
        } else {
            body = cachedEntry.getDefaultContent();
        }

        // TODO : check for empty responses
        // (HttpServletResponse.SC_NO_CONTENT, HttpServletResponse.SC_NOT_MODIFIED, or 20bytes which is an empty gzip
//        if (shouldBodyBeEmpty) {
//            body = new byte[0];
//        }

        response.setContentLength(body.length);
        response.getOutputStream().write(body);
    }

}
