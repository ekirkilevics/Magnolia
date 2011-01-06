/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import info.magnolia.module.cache.util.GZipUtil;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MultiMap;


/**
 * Cache entry keeping the content in memory. Stores a gzipped and non-gzipped version.
 * @author pbaerfuss
 * @version $Id$
 *
 */
public class InMemoryCachedEntry extends ContentCachedEntry {

    private final byte[] gzippedContent;
    private final byte[] plainContent;

    public InMemoryCachedEntry(byte[] out, String contentType, String characterEncoding, int statusCode, MultiMap headers, long modificationDate) throws IOException {
        super(contentType, characterEncoding, statusCode, headers, modificationDate);
        // content which is actually of a compressed type must stay that way
        if (GZipUtil.isGZipped(out)) {
            this.gzippedContent = out;
            boolean isGzippedResponse = headers.containsKey("Content-Encoding") && ((Collection)headers.get("Content-Encoding")).contains("gzip");
            if(isGzippedResponse){
                this.plainContent = GZipUtil.ungzip(out);
            }
            // in case of serving a gzip file (gif for instance)
            else{
                this.plainContent = null;
            }
        } else {
            this.gzippedContent = GZipUtil.gzip(out);
            this.plainContent = out;
        }
    }

    @Override
    protected boolean canServeGzipContent() {
        return true;
    }

    @Override
    protected void writeContent(HttpServletRequest request, HttpServletResponse response, FilterChain chain, boolean acceptsGzipEncoding) throws IOException {
        final byte[] body;
        if (acceptsGzipEncoding || getPlainContent() == null) {
            body = getGzippedContent();
        } else {
            body = getPlainContent();
        }

        response.setContentLength(body.length);
        response.getOutputStream().write(body);
    }


    public byte[] getPlainContent() {
        return plainContent;
    }

    public byte[] getGzippedContent() {
        return gzippedContent;
    }

}
