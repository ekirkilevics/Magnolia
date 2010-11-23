/**
 * This file Copyright (c) 2010 Magnolia International
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


/**
 * Used for big files. The content will be served from the repository rather than by storing in the cache entry.
 * @author pbaerfuss
 * @version $Id$
 *
 */
public class InRepositoryCachedPage extends CachedPage {


    private static final String CONTENT_FILE_ATTIBUTE = InRepositoryCachedPage.class.getName() + ".contentFile";

    private long contentLength;

    public InRepositoryCachedPage(long contentLength, String contentType, String characterEncoding, int statusCode, MultiMap headers, long modificationDate) throws IOException {
        super(contentType, characterEncoding, statusCode, headers, modificationDate);
        this.contentLength = contentLength;
    }

    @Override
    protected boolean isSupportsGzip() {
        return false;
    }

    @Override
    protected void writeContent(HttpServletRequest request, HttpServletResponse response, FilterChain chain, boolean acceptsGzipEncoding) throws IOException, ServletException {
        response.setContentLength((int) contentLength);
        File tmpFile = getContentFileBoundToTheRequest(request);
        if(tmpFile != null){
            FileInputStream tmpFileStream = FileUtils.openInputStream(tmpFile);
            IOUtils.copy(tmpFileStream,response.getOutputStream());
            IOUtils.closeQuietly(tmpFileStream);
            tmpFile.delete();
        }
        else{
            chain.doFilter(request, response);
        }
    }

    private File getContentFileBoundToTheRequest(HttpServletRequest request) {
        return (File) request.getAttribute(CONTENT_FILE_ATTIBUTE);
    }

    public void bindContentFileToCurrentRequest(HttpServletRequest request, File contentFile){
        request.setAttribute(InRepositoryCachedPage.CONTENT_FILE_ATTIBUTE, contentFile);
    }

}
