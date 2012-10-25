/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import org.apache.commons.collections.MultiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Used for large responses. Typically we don't want to cache big responses as they are so costly to keep in memory and
 * tend to evict so much from the cache where they're put in that net performance drops. This cache entry does not keep
 * the response in memory, instead it serves it from a file on disk or if there's no file it proceeds with rendering.
 * When it has used the file once it deletes it. The entry still remains in cache and serves as a token to prevent
 * caching in the future.
 *
 * The file is created by {@link CacheResponseWrapper} when it reaches its set threshold.
 *
 * @version $Id$
 * @see CacheResponseWrapper
 */
public class DelegatingBlobCachedEntry extends ContentCachedEntry {

    private static Logger log = LoggerFactory.getLogger(DelegatingBlobCachedEntry.class);


    public static final String CONTENT_FILE_ATTRIBUTE = DelegatingBlobCachedEntry.class.getName() + ".contentFile";

    private final long contentLength;

    public DelegatingBlobCachedEntry(long contentLength, String contentType, String characterEncoding, int statusCode, MultiMap headers, long modificationDate, String originalUrl, int timeToLiveInSeconds) throws IOException {
        super(contentType, characterEncoding, statusCode, headers, modificationDate, originalUrl, timeToLiveInSeconds);
        this.contentLength = contentLength;
    }

    @Override
    protected boolean canServeGzipContent() {
        return false;
    }

    @Override
    public void replay(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        File contentFile = getContentFileBoundToTheRequest(request);
        if(contentFile != null){
            log.debug("About to serve response from {}", contentFile.getAbsolutePath());
            super.replay(request, response, chain);
        }
        else{
            chain.doFilter(request, response);
        }
    }

    @Override
    protected void writeContent(HttpServletRequest request, HttpServletResponse response, FilterChain chain, boolean acceptsGzipEncoding) throws IOException, ServletException {
        response.setContentLength((int) contentLength);

        File contentFile = getContentFileBoundToTheRequest(request);
        if(contentFile != null){
            log.debug("Streaming out output from {}", contentFile.getAbsolutePath());
            FileInputStream contentStream = FileUtils.openInputStream(contentFile);
            try{
                IOUtils.copy(contentStream,response.getOutputStream());
            }finally{
                IOUtils.closeQuietly(contentStream);
                log.debug("About to delete temp file {}", contentFile.getAbsolutePath());
                if(!contentFile.delete()){
                    log.error("Can't delete file: " + contentFile);
                }
            }
        }
        else{
            // should not happen as we delegate to the filter chain in replay in that case and ignore all cached data
            throw new IllegalStateException("No content file attached to the request!");
        }
    }

    private File getContentFileBoundToTheRequest(HttpServletRequest request) {
        return (File) request.getAttribute(CONTENT_FILE_ATTRIBUTE);
    }

    public void bindContentFileToCurrentRequest(HttpServletRequest request, File contentFile){
        request.setAttribute(DelegatingBlobCachedEntry.CONTENT_FILE_ATTRIBUTE, contentFile);
    }

}
