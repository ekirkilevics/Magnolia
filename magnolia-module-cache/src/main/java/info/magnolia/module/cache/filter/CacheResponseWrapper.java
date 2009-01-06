/**
 * This file Copyright (c) 2008-2009 Magnolia International
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

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Collection;
import java.util.Locale;

/**
 *
 * @author 
 * @version $Revision: 14052 $ ($Author: gjoseph $)
 */
public class CacheResponseWrapper extends HttpServletResponseWrapper {
    private final ServletOutputStream wrappedStream;
    private PrintWriter wrappedWriter = null;
    private final MultiMap headers = new MultiValueMap();
    private int status = SC_OK;
    private boolean isError;
    private String redirectionLocation;

    public CacheResponseWrapper(final HttpServletResponse response, final ServletOutputStream wrappedStream) {
        super(response);
        this.wrappedStream = wrappedStream;
    }

    // MAGNOLIA-1996: this can be called multiple times, e.g. by chunk writers, but always from a single thread.
    public ServletOutputStream getOutputStream() throws IOException {
        return wrappedStream;
    }

    public PrintWriter getWriter() throws IOException {
        if (wrappedWriter == null) {
            String encoding = getCharacterEncoding();
            wrappedWriter = encoding != null
                    ? new PrintWriter(new OutputStreamWriter(getOutputStream(), encoding))
                    : new PrintWriter(new OutputStreamWriter(getOutputStream()));
        }

        return wrappedWriter;
    }

    public void flushBuffer() throws IOException {
        super.flushBuffer();
        flush();
    }

    public void flush() throws IOException {
        wrappedStream.flush();

        if (wrappedWriter != null) {
            wrappedWriter.flush();
        }
    }


    public void reset() {
        super.reset();
//        if (wrappedStream instanceof ByteArrayOutputStream) {
//            ((ByteArrayOutputStream)wrappedStream).reset();
//        }
        wrappedWriter = null;
        status = SC_OK;

//         cookies.clear();
        headers.clear();
//        contentType = null;
//        contentLength = 0;
    }


    public void resetBuffer() {
        super.resetBuffer();
//        if (wrappedStream != null) {
//            ((ByteArrayOutputStream)wrappedStream).reset();
//        }
        wrappedWriter = null;
    }

    public int getStatus() {
        return status;
    }

    public boolean isError() {
        return isError;
    }

    public MultiMap getHeaders() {
        return headers;
    }

    public long getLastModified() {
        // we're using a MultiMap. And all this is to workaround code that would possibly set the Last-Modified header with a String value
        // it will also fail if multiple values have been set.
        final Collection values = (Collection) headers.get("Last-Modified");
        if (values == null || values.size() != 1) {
            throw new IllegalStateException("Can't get Last-Modified header : no or multiple values : " + values);
        }
        final Object value = values.iterator().next();
        if (value instanceof String) {
            return parseStringDate((String) value);
        } else if (value instanceof Long) {
            return ((Long)value).longValue();
        } else {
            throw new IllegalStateException("Can't get Last-Modified header : " + value);
        }
    }

    private long parseStringDate(String value) {
        try {
            final SimpleDateFormat f = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
            final Date date = f.parse(value);
            return date.getTime();
        } catch (ParseException e) {
            throw new IllegalStateException("Could not parse Last-Modified header with value " + value + " : " + e.getMessage());
        }
    }

    public String getRedirectionLocation() {
        return redirectionLocation;
    }

    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
        replaceHeader(name, new Long(date));
    }

    public void addDateHeader(String name, long date) {
        super.addDateHeader(name, date);
        appendHeader(name, new Long(date));
    }

    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        replaceHeader(name, value);
    }

    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        appendHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        replaceHeader(name, new Integer(value));
    }

    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        appendHeader(name, new Integer(value));
    }

    private void replaceHeader(String name, Object value) {
        headers.remove(name);
        appendHeader(name, value);
    }

    private void appendHeader(String name, Object value) {
        headers.put(name, value);
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
        this.redirectionLocation = location;
        super.sendRedirect(location);
    }

    public void sendError(int status, String string) throws IOException {
        super.sendError(status, string);
        this.status = status;
        this.isError = true;
    }

    public void sendError(int status) throws IOException {
        super.sendError(status);
        this.status = status;
        this.isError = true;
    }
}
