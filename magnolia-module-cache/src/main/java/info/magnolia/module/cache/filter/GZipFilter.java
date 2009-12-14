/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
package info.magnolia.module.cache.filter;

import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter;
import info.magnolia.cms.util.RequestHeaderUtil;
import info.magnolia.module.cache.util.GZipUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This GZipFilter does not take care of the Accept-Encoding request header. The CacheFilter will
 * take care of serving the unzipped content if appropriate.
 *
 * <strong>By default, the Magnolia main filter is not dispatched to in case of include requests - if
 * this is the case this filter has to be bypassed for such requests !</strong>
 *
 * @see info.magnolia.module.cache.filter.StandaloneGZipFilter if the cache filter is not in use.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class GZipFilter extends OncePerRequestAbstractMgnlFilter {

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // we need to setContentLength before writing content ...
        // (otherwise Tomcat adds a Transfer-Encoding: chunked header, which seems to cause trouble
        // to browsers ...)
        final ByteArrayOutputStream flat = new ByteArrayOutputStream();
        final SimpleServletOutputStream wrappedOut = new SimpleServletOutputStream(flat);

        // Handle the request
        final CacheResponseWrapper responseWrapper = new CacheResponseWrapper(response,wrappedOut) {
            public void setContentLength(int len) {
                // don't let the container set a (wrong) content-length too early,
                // we're going to set it later to the appropriate
                // value - once it's gzipped !
            }

            public void flushBuffer() throws IOException {
                // let's ignore this for now, we're flushing manually ourselves a bit later.
                // TODO : See MAGNOLIA-2129, MAGNOLIA-2177 and MAGNOLIA-2178
                //DeprecationUtil.isDeprecated("Should not flush the response manually:");
            }
        };
        chain.doFilter(request, responseWrapper);

        // flush only internal buffers, not the actual response's !
        responseWrapper.flush();

        byte[] array = flat.toByteArray();

        //GZIP only 200 SC_OK responses as in other cases response might be already committed - only dump bytes and flush ...
        int statusCode = responseWrapper.getStatus();
        if (statusCode == HttpServletResponse.SC_OK) {

            if (!GZipUtil.isGZipped(array) && RequestHeaderUtil.acceptsGzipEncoding(request)) {
                array = GZipUtil.gzip(array);
            }

            // add headers only if not set yet.
            if (GZipUtil.isGZipped(array) && !response.containsHeader("Content-Encoding")) {
                RequestHeaderUtil.addAndVerifyHeader(responseWrapper, "Content-Encoding", "gzip");
                RequestHeaderUtil.addAndVerifyHeader(responseWrapper, "Vary", "Accept-Encoding"); // needed for proxies
            }

            response.setContentLength(array.length);
        }
        response.getOutputStream().write(array);
        response.flushBuffer();

        // TODO :
         //Sanity checks
//            byte[] compressedBytes = compressed.toByteArray();
//            boolean shouldGzippedBodyBeZero = ResponseUtil.shouldGzippedBodyBeZero(compressedBytes, request);
//            boolean shouldBodyBeZero = ResponseUtil.shouldBodyBeZero(request, wrapper.getStatusCode());
//            if (shouldGzippedBodyBeZero || shouldBodyBeZero) {
//                compressedBytes = new byte[0];
//            }
    }

}
