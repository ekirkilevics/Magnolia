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
package info.magnolia.module.cache.filter;

import info.magnolia.cms.filters.AbstractMgnlFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * This GZipFilter does not take care of the Accept-Encoding request header. The CacheFilter will
 * take care of serving the unzipped content if appropriate.
 *
 * By the default, the Magnolia main filter is not dispatched to in case of include requests - if
 * this is the case this filter has to be bypassed for such requests !
 *
 * @see info.magnolia.module.cache.filter.StandaloneGZipFilter if the cache filter is not in use.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class GZipFilter extends AbstractMgnlFilter {

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // we can't tee the outputstream, because we need to setContentLength before writing content ...
        // (otherwise Tomcat adds a Transfer-Encoding: chunked header, which seems to cause trouble
        // to browsers ...
        final ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        final GZIPOutputStream gzout = new GZIPOutputStream(compressed);
        final SimpleServletOutputStream wrappedOut = new SimpleServletOutputStream(gzout);

        // Handle the request
        final CacheResponseWrapper responseWrapper = new CacheResponseWrapper(response, wrappedOut) {
            public void setContentLength(int len) {
                // don't let the container set a (wrong) content-length too early,
                // we're going to set it later to the appropriate
                // value - once it's gzipped !
            }
        };
        addAndVerifyHeader(responseWrapper, "Content-Encoding", "gzip");
        addAndVerifyHeader(responseWrapper, "Vary", "Accept-Encoding"); // needed for proxies
        chain.doFilter(request, responseWrapper);

        responseWrapper.flushBuffer();
        gzout.flush();
        gzout.close();

        final byte[] compressedBytes = compressed.toByteArray();
        response.setContentLength(compressedBytes.length);
        response.getOutputStream().write(compressedBytes);

        // TODO :
         //return on error or redirect code, because response is already committed
//            int statusCode = wrapper.getStatusCode();
//            if (statusCode != HttpServletResponse.SC_OK) {
//                return;
//            }

         //Sanity checks
//            byte[] compressedBytes = compressed.toByteArray();
//            boolean shouldGzippedBodyBeZero = ResponseUtil.shouldGzippedBodyBeZero(compressedBytes, request);
//            boolean shouldBodyBeZero = ResponseUtil.shouldBodyBeZero(request, wrapper.getStatusCode());
//            if (shouldGzippedBodyBeZero || shouldBodyBeZero) {
//                compressedBytes = new byte[0];
//            }
    }

}
