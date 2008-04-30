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
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class GZipFilter extends AbstractMgnlFilter {


    // TODO : this is completely temporary while testing and being lazy
    public boolean bypasses(HttpServletRequest request) {
        if (request.getRequestURI().indexOf("/.") >= 0) {
            return true;
        }
        if (request.getRequestURI().indexOf(".html") < 0) {
            return true;
        }
        //

        //TODO : voters for mimetypes
        // TODO : extends OncePerRequest filter ?

        final String uri = (String) request.getAttribute("javax.servlet.include.request_uri");
        final boolean includeRequest = !(uri == null);
        if (includeRequest) {
            return true;
        }

        return false;

    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        // TODO : check if we can gzip this (voters ?)

        // TODO : or always gzip (provided bypasses allowed it, obviously) - let CacheFilter dezip if needed

        // we can't tee the outputstream, because we need to setContentLength before writing content ...
        // (otherwise Tomcat adds a Transfer-Encoding: chunked header, which seems to cause trouble
        // to browsers ...
        final ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        final GZIPOutputStream gzout = new GZIPOutputStream(compressed);
        final SimpleServletOutputStream wrappedOut = new SimpleServletOutputStream(gzout);

        // Handle the request
        final CacheResponseWrapper wrapper = new CacheResponseWrapper(response, wrappedOut);
        addAndVerifyHeader(response, "Content-Encoding", "gzip");
        chain.doFilter(request, wrapper);

        gzout.flush();
        gzout.close();

        final byte[] compressedBytes = compressed.toByteArray();
        wrapper.setContentLength(compressedBytes.length);
        response.getOutputStream().write(compressedBytes);

    }

    protected void addAndVerifyHeader(HttpServletResponse response, String name, String value) {
        response.setHeader(name, value);
        boolean containsEncoding = response.containsHeader(name);
        if (!containsEncoding) {
            throw new IllegalStateException("Failure when attempting to set " + name + ": " + value);
        }
    }
}
