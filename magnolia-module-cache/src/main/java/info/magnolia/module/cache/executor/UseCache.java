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

import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.filter.CachedEntry;
import info.magnolia.module.cache.filter.CachedError;
import info.magnolia.module.cache.filter.CachedPage;
import info.magnolia.module.cache.filter.CachedRedirect;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serves the content from the cache.
 *
 * @author pbracher
 * @version $Revision: $ ($Author: $)
 */
public class UseCache extends AbstractExecutor {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UseCache.class);

    public void processCacheRequest(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, Cache cache,
            CachePolicyResult cachePolicy) throws IOException, ServletException {
        CachedEntry cached = (CachedEntry) cachePolicy.getCachedEntry();
        processCachedEntry(cached, request, response, chain);
    }

    protected void processCachedEntry(CachedEntry cached, HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("Serving {}", cached);
        if (cached instanceof CachedPage) {
            final CachedPage page = (CachedPage) cached;
            if (!ifModifiedSince(request, page.getLastModificationTime())) {
                if (response.isCommitted() && page.getPreCacheStatusCode() != HttpServletResponse.SC_NOT_MODIFIED) {
                    // this should not happen ... if it does, log it and _serve_the_data_ otherwise we will confuse client
                    log.warn("Unable to change status on already commited response {}.", response.getClass().getName());
                } else {
                    // not newly cached anymore, reset the code ...
                    page.setPreCacheStatusCode(0);
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
            }

            writePage(request, response, chain, page);
            response.flushBuffer();
        } else if (cached instanceof CachedError) {
            final CachedError error = (CachedError) cached;
            if (!response.isCommitted()) {
                response.sendError(error.getStatusCode());
            } else {
                //this usually happens first time the error occurs and is put in cache - since setting page as error causes it to be committed
                // TODO: is there a better work around to make sure we do not swallow some exception accidentally?
                log.debug("Failed to serve cached error due to response already committed.");
            }
        } else if (cached instanceof CachedRedirect) {
            final CachedRedirect redir = (CachedRedirect) cached;
            // we'll ignore the redirection code for now - especially since the servlet api doesn't really let us choose anyway
            // except if someone sets the header manually ?
            if (!response.isCommitted()) {
                response.sendRedirect(redir.getLocation());
            }
        } else if (cached == null) {
            // 304 or nothing to write to the output
            return;
        } else {
            throw new IllegalStateException("Unexpected CachedEntry type: " + cached);
        }
    }

    protected void writePage(final HttpServletRequest request, final HttpServletResponse response, FilterChain chain, final CachedPage cachedEntry) throws IOException, ServletException {
        cachedEntry.replay(request, response, chain);
    }

}
