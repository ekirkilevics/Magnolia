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
public class CachedPage implements CachedEntry, Serializable {

    private static final ToStringStyle BYTE_ARRAY_SIZE_STYLE = new ToStringStyle() {
        protected void appendDetail(StringBuffer buffer, String fieldName,
                byte[] array) {
            super.appendDetail(buffer, fieldName, array.length + " bytes");
        }
    };

    // TODO : headers and cookies ?
    private final byte[] defaultContent;
    private final byte[] ungzippedContent;
    private final String contentType;
    private final String characterEncoding;
    private final int statusCode;
    private transient MultiMap headers;
    private Map serializableHeadersBackingList;
    private final long lastModificationTime;
    // slightly fishy, but other executors needs to know if this is freshly created CachedPage and StatusCode have been manipulated or not.
    private transient int preCacheStatusCode;

    /**
     * @deprecated not used, since 3.6.2, as it will compress all not gzipped content of every entry created using this constructor which is not desirable for already compressed content (e.g. jpg & tif images)
     */
    public CachedPage(byte[] out, String contentType, String characterEncoding, int statusCode, MultiMap headers, long modificationDate) throws IOException {
        this(out, contentType, characterEncoding, statusCode, headers, modificationDate, true);
    }

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
    public CachedPage(byte[] out, String contentType, String characterEncoding, int statusCode, MultiMap headers, long modificationDate, boolean shouldCompress) throws IOException {
        // content which is actually of a compressed type must stay that way
        if (GZipUtil.isGZipped(out) || !shouldCompress) {
            this.defaultContent = out;
            this.ungzippedContent = null;
        } else {
            this.defaultContent = GZipUtil.gzip(out);
            this.ungzippedContent = out;
        }
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


    public byte[] getUngzippedContent() {
        return ungzippedContent;
    }

    public byte[] getDefaultContent() {
        return defaultContent;
    }

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
    
    public int getPreCacheStatusCode() {
        // preCached is transient and will be 0 after deserialization (or after going through UseCache for that matter)
        if (preCacheStatusCode == 0) {
            return statusCode;
        }
        return preCacheStatusCode;
    }

    public void setPreCacheStatusCode(int preCacheStatusCode) {
        this.preCacheStatusCode = preCacheStatusCode;
    }

}
