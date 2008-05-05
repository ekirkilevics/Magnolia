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

import info.magnolia.module.cache.util.GZipUtil;
import org.apache.commons.collections.MultiMap;

import java.io.IOException;

/**
 * Wraps a page reponse. It is assumed that the given content is gzipped
 * if appropriate (i.e if the gzip filter is in the chain) and this class
 * thus ungzips it to be able to serve both contents.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public final class CachedPage implements CachedEntry {
    // TODO : headers and cookies ?
    private final byte[] defaultContent;
    private final byte[] ungzippedContent;
    private final String contentType;
    private final String characterEncoding;
    private final int statusCode;
    private final MultiMap headers;
    private final long lastModificationTime;

    CachedPage(byte[] out, String contentType, String characterEncoding, int statusCode, MultiMap headers) throws IOException {
        // content which is actually of a compressed type must stay that way
        if (!GZipUtil.isGZipMimeType(contentType) && GZipUtil.isGZipped(out)) {
            this.defaultContent = out;
            this.ungzippedContent = GZipUtil.ungzip(out);
        } else {
            this.defaultContent = out;
            this.ungzippedContent = null;
        }
        this.contentType = contentType;
        this.characterEncoding = characterEncoding;
        this.statusCode = statusCode;
        this.headers = headers;
        // TODO : should this be System.currentTimeMillis(), or the actual document's last modif date? - also, what about timezones ...
        this.lastModificationTime = System.currentTimeMillis();
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
        return "CachedPage{" +
                "defaultContent=" + defaultContent.length + " bytes" +
                ", ungzippedContent=" + (ungzippedContent != null ? ungzippedContent.length + " bytes" : null) +
                ", contentType='" + contentType + '\'' +
                ", characterEncoding='" + characterEncoding + '\'' +
                ", statusCode=" + statusCode +
                ", headers=" + headers +
                ", lastModificationTime=" + lastModificationTime +
                '}';
    }
}
