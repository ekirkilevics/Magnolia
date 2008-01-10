/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.cms.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class CacheResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream cachingStream;

    private PrintWriter cachingWriter = null;

    private CacheableEntry cacheableEntry;

    private int status = SC_OK;

    public CacheResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public CacheableEntry getCacheableEntry() {
        if (cachingStream == null) {
            return null;
        }
        cacheableEntry = new CacheableEntry(cachingStream.toByteArray());
        cacheableEntry.setContentType(getContentType());
        cacheableEntry.setCharacterEncoding(getCharacterEncoding());
        return cacheableEntry;
    }

    /**
     * @see javax.servlet.ServletResponseWrapper#getOutputStream()
     */
    public ServletOutputStream getOutputStream() throws IOException {
        // MAGNOLIA-1996: can be called multiple times, e.g. by chunk writers, but always from a single thread.
        if (cachingStream == null) {
            cachingStream = new ByteArrayOutputStream();
        }
        return new MultiplexServletOutputStream(super.getOutputStream(), cachingStream);
    }

    public PrintWriter getWriter() throws IOException {
        if (cachingWriter == null) {
            String encoding = getCharacterEncoding();
            cachingWriter = encoding != null
                ? new PrintWriter(new OutputStreamWriter(getOutputStream(), encoding))
                : new PrintWriter(new OutputStreamWriter(getOutputStream()));
        }

        return cachingWriter;
    }

    public void flushBuffer() throws IOException {
        super.flushBuffer();

        if (cachingStream != null) {
            cachingStream.flush();
        }

        if (cachingWriter != null) {
            cachingWriter.flush();
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        super.setStatus(status);
        this.status = status;
    }

    public void setStatus(int status, String string) {
        super.setStatus(status, string);
        this.status = status;
    }

    public void sendRedirect(String location) throws IOException {
        this.status = SC_MOVED_TEMPORARILY;
        super.sendRedirect(location);
    }

    public void sendError(int status, String string) throws IOException {
        super.sendError(status, string);
        this.status = status;
    }

    public void sendError(int status) throws IOException {
        super.sendError(status);
        this.status = status;
    }

    public void reset() {
        super.reset();
        if (cachingStream != null) {
            cachingStream.reset();
        }
        cachingWriter = null;
        status = SC_OK;
    }

    public void resetBuffer() {
        super.reset();
        if (cachingStream != null) {
            cachingStream.reset();
        }
        cachingWriter = null;
        status = SC_OK;
    }

}
