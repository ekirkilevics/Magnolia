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
package info.magnolia.module.cache.filter;

import info.magnolia.cms.util.RequestHeaderUtil;
import info.magnolia.module.cache.util.GZipUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Wraps a page response. It is assumed that the given content is gzipped
 * if appropriate (i.e if the gzip filter is in the chain) and this class
 * thus ungzips it to be able to serve both contents.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class ContentCachedEntry implements CachedEntry, Serializable {

    private static final ToStringStyle BYTE_ARRAY_SIZE_STYLE = new ToStringStyle() {
        @Override
        protected void appendDetail(StringBuffer buffer, String fieldName,
                byte[] array) {
            super.appendDetail(buffer, fieldName, array.length + " bytes");
        }
    };

    private final String contentType;
    private final String characterEncoding;
    private final int statusCode;
    private transient MultiMap headers;
    private Map serializableHeadersBackingList;
    private final long lastModificationTime;

    /**
     * @param out Cached content.
     * @param contentType MIME type of the cached content.
     * @param characterEncoding Character encoding of the cached content.
     * @param statusCode HTTP response status code (E.g. 200 - OK);
     * @param headers Additional HTTP headers to be sent when serving this cached content.
     * @param modificationDate Content modification date to set in the response.
     * @param shouldCompress Flag marking this content as desirable to be sent in compressed form (should the client support such compression). Setting this to true means cache entry will contain both, compressed and flat version of the content. Compression is applied here only if content is not gzipped already.
     * @throws IOException when failing to compress the content.
     */
    public ContentCachedEntry(String contentType, String characterEncoding, int statusCode, MultiMap headers, long modificationDate) throws IOException {
        this.contentType = contentType;
        this.characterEncoding = characterEncoding;
        this.statusCode = statusCode;
        this.headers = headers;
        this.lastModificationTime = modificationDate;
    }

    // TODO : replacing getOut() with streamTo(OutputStream out) could help subclasses stream content
    // TODO : from a File buffer for example, instead of holding byte[]s.
    // TODO : but this would require pushing a dependency on servlet api in here - because we need
    // TODO : to know if we can push gzipped content... or this would need to be passed as an explicit
    // TODO : parameter, which isn't too exciting either...


    public String getContentType() {
        return contentType;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public MultiMap getHeaders() {
        return headers;
    }

    public long getLastModificationTime() {
        return lastModificationTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, BYTE_ARRAY_SIZE_STYLE);
    }

    // serialization support until commons  collection 3.3 is released
    private void writeObject(ObjectOutputStream out) throws IOException {
        serializableHeadersBackingList = new HashMap();
        Iterator iter = headers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Entry) iter.next();
            serializableHeadersBackingList.put(entry.getKey(), new ArrayList((Collection)entry.getValue()));
        }
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        headers = new MultiValueMap();
        Iterator iter = serializableHeadersBackingList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Entry) iter.next();
            Collection c = (Collection) entry.getValue();
            for (Iterator ic = c.iterator(); ic.hasNext();) {
                headers.put(entry.getKey(), ic.next());
            }
        }
        serializableHeadersBackingList = null;
   }

    @Override
    public void replay(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        response.setStatus(getStatusCode());

        boolean acceptsGzipEncoding = isAcceptsGzip(request) && canServeGzipContent();
        addHeaders(acceptsGzipEncoding, response);

        // TODO : cookies ?
        response.setContentType(getContentType());
        response.setCharacterEncoding(getCharacterEncoding());

        writeContent(request, response, chain, acceptsGzipEncoding);
    }

    protected abstract void writeContent(HttpServletRequest request, HttpServletResponse response, FilterChain chain, boolean acceptsGzipEncoding) throws IOException, ServletException;

    /**
     * Sets headers in the response object.
     */
    protected void addHeaders(final boolean acceptsGzipEncoding, final HttpServletResponse response) {
        final MultiMap headers = getHeaders();

        final Iterator it = headers.keySet().iterator();
        while (it.hasNext()) {
            final String header = (String) it.next();
            if (!acceptsGzipEncoding) {
                //TODO: this should not be necessary any more ...
                if ("Content-Encoding".equals(header) || "Vary".equals(header)) {
                    continue;
                }
            }
            if (response.containsHeader(header)) {
                // do not duplicate headers. Some of the headers we have to set in Store to have them added to the cache entry, on the other hand we don't want to duplicate them if they are already set.
                continue;
            }

            final Collection values = (Collection) headers.get(header);
            final Iterator valIt = values.iterator();
            while (valIt.hasNext()) {
                final Object val = valIt.next();
                RequestHeaderUtil.setHeader(response, header, val);
            }
        }

        if(acceptsGzipEncoding){
            // write the headers as well (if not written already)
            if (!response.containsHeader("Content-Encoding")) {
                RequestHeaderUtil.addAndVerifyHeader(response, "Content-Encoding", "gzip");
                RequestHeaderUtil.addAndVerifyHeader(response, "Vary", "Accept-Encoding"); // needed for proxies
            }
        }
    }

    protected boolean isAcceptsGzip(HttpServletRequest request){
        return GZipUtil.isAcceptsGzip(request);
    }

    abstract protected boolean canServeGzipContent();

}
