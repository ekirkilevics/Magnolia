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
package info.magnolia.module.cache.util;

import info.magnolia.cms.util.RequestHeaderUtil;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.cache.CacheModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * Provide useful methods for working with gzip'd byte[]. Code originated
 * from net.sf.ehcache.constructs.web.PageInfo under the Apache License Version 2.0.
 *
 * @version $Revision: $ ($Author: $)
 */
public class GZipUtil {
    private static final List GZIP_MIMETYPES = Arrays.asList(new String[]{
            "application/x-compressed",
            "application/x-gzip",
            "application/gnutar",
            "multipart/x-gzip"
    });

    private static final int FOUR_KB = 4196;
    private static final int GZIP_MAGIC_NUMBER_BYTE_1 = 31;
    private static final int GZIP_MAGIC_NUMBER_BYTE_2 = -117;

    public static byte[] gzip(byte[] ungzipped) throws IOException {
        if (isGZipped(ungzipped)) {
            throw new IllegalStateException("The byte[] is already gzipped. It should not be gzipped again.");
        }
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bytes);
        gzipOutputStream.write(ungzipped);
        gzipOutputStream.close();
        return bytes.toByteArray();
    }

    public static byte[] ungzip(final byte[] gzipped) throws IOException {
        final GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(gzipped));
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(gzipped.length);
        final byte[] buffer = new byte[FOUR_KB];
        int bytesRead = 0;
        while (bytesRead != -1) {
            bytesRead = inputStream.read(buffer, 0, FOUR_KB);
            if (bytesRead != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        }
        byte[] ungzipped = byteArrayOutputStream.toByteArray();
        inputStream.close();
        byteArrayOutputStream.close();
        return ungzipped;
    }

    /**
     * Checks the first two bytes of the candidate byte array for the magic number 0x677a.
     */
    public static boolean isGZipped(byte[] candidate) {
        if (candidate == null || candidate.length < 2) {
            return false;
        } else {
            return (candidate[0] == GZIP_MAGIC_NUMBER_BYTE_1 && candidate[1] == GZIP_MAGIC_NUMBER_BYTE_2);
        }
    }

    public static boolean isGZipMimeType(String contentType) {
        return GZIP_MIMETYPES.contains(contentType);
    }

    /**
     * True if the response should be gzipped. Uses the compression configuration of the cache and checks if the client accepts gzip responses.
     */
    public static boolean isAcceptsGzip(HttpServletRequest request){
        CacheModule module = (CacheModule) ModuleRegistry.Factory.getInstance().getModuleInstance("cache");
        boolean compressionVote = module.getCompression().getVoters().vote(request)>0;
        boolean requestAcceptsGzip = RequestHeaderUtil.acceptsGzipEncoding(request);
        return requestAcceptsGzip && compressionVote;
    }

}
