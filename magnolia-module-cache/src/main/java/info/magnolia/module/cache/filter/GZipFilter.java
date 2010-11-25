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
package info.magnolia.module.cache.filter;

import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter;
import info.magnolia.cms.util.RequestHeaderUtil;
import info.magnolia.module.cache.util.GZipUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @Override
    public boolean bypasses(HttpServletRequest request) {
        return !RequestHeaderUtil.acceptsGzipEncoding(request) || super.bypasses(request);
    }

    public void doFilter(HttpServletRequest request, final HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // we need to setContentLength before writing content ...
        // (otherwise Tomcat adds a Transfer-Encoding: chunked header, which seems to cause trouble
        // to browsers ...)

        final GZipCacheResponseWrapper responseWrapper = new GZipCacheResponseWrapper(response, CacheResponseWrapper.DEFAULT_THRESHOLD, true);

        chain.doFilter(request, responseWrapper);

        // flush only internal buffers, not the actual response's !
        responseWrapper.flush();

        // otherwise the content was already streamed
        if(!responseWrapper.isGzipResponseDetected() && !responseWrapper.isThesholdExceeded()){
            byte[] array = responseWrapper.getBufferedContent();

            //GZIP only 200 SC_OK responses as in other cases response might be already committed - only dump bytes and flush ...
            int statusCode = responseWrapper.getStatus();
            if (statusCode == HttpServletResponse.SC_OK) {
                responseWrapper.replayHeadersAndStatus(response);

                RequestHeaderUtil.addAndVerifyHeader(response, "Content-Encoding", "gzip");
                RequestHeaderUtil.addAndVerifyHeader(response, "Vary", "Accept-Encoding"); // needed for proxies

                array = GZipUtil.gzip(array);
                response.setContentLength(array.length);
                if (array.length > 0) {
                    response.getOutputStream().write(array);
                }
            }
            else{
                responseWrapper.replay(response);
            }

            response.flushBuffer();
        }
    }

    /**
     * Detects if the response has the "Content-Encoding" header set. If so it will stream it through.
     * @version $Id$
     */
    private final class GZipCacheResponseWrapper extends CacheResponseWrapper {

        private final HttpServletResponse response;

        private boolean gzipResponseDetected;

        private ServletOutputStream deferredOutputStream = new DeferredServletOutputStream();

        public boolean isGzipResponseDetected() {
            return gzipResponseDetected;
        }

        private GZipCacheResponseWrapper(HttpServletResponse response, int threshold, boolean serveIfThresholdReached) {
            super(response, threshold, serveIfThresholdReached);
            this.response = response;
        }

        @Override
        public void addHeader(String name, String value) {
            if(name.equals("Content-Encoding")){
                gzipResponseDetected = true;
            }
            super.addHeader(name, value);
        }

        private ServletOutputStream getWrappedOutputStream() throws IOException {
            return super.getOutputStream();
        }

        public ServletOutputStream getOutputStream() throws IOException {
            return deferredOutputStream;
        }

        /**
         * Only gets the real output stream once we are writing. If we have detected a gzip response we will just stream it through.
         * @version $Id$
         *
         */
        private final class DeferredServletOutputStream extends ServletOutputStream {

            ServletOutputStream stream;

            @Override
            public void write(int b) throws IOException {
                if(stream == null){
                    if(gzipResponseDetected){
                        replayHeadersAndStatus(response);
                        stream = response.getOutputStream();
                    }
                    else{
                        stream = getWrappedOutputStream();
                    }
                }
                stream.write(b);
            }
        }
    }

}
