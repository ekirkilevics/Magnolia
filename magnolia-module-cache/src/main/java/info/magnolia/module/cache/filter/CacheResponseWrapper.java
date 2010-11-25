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
package info.magnolia.module.cache.filter;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.RequestHeaderUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ThresholdingOutputStream;


/**
 * A response wrapper which records the status, headers and content. Unless the threshold is reached
 * the written content gets buffered and the content can get retrieved by
 * {@link #getBufferedContent()}. Once the threshold is reached either a tmp file is created which
 * can be retrieved with {@link #getContentFile()} or the content/headers are made transparent to
 * the original response if {@link #serveIfThresholdReached} is true.
 * @version $Revision: 14052 $ ($Author: gjoseph $)
 */
public class CacheResponseWrapper extends HttpServletResponseWrapper {

    public static final int DEFAULT_THRESHOLD = 500 * 1024;

    private final ServletOutputStream wrappedStream;
    private PrintWriter wrappedWriter = null;
    private final MultiMap headers = new MultiValueMap();
    private int status = SC_OK;
    private boolean isError;
    private String redirectionLocation;
    private HttpServletResponse originalResponse;
    private ByteArrayOutputStream inMemoryBuffer;
    private File contentFile;
    private long contentLength = -1;

    private ThresholdingOutputStream thresholdingOutputStream;
    private boolean serveIfThresholdReached;

    private String errorMsg;

    public CacheResponseWrapper(final HttpServletResponse response, int threshold, boolean serveIfThresholdReached) {
        super(response);
        this.serveIfThresholdReached = serveIfThresholdReached;
        this.originalResponse = response;
        this.inMemoryBuffer = new ByteArrayOutputStream();
        this.thresholdingOutputStream = new ThresholdingCacheOutputStream(threshold);
        this.wrappedStream = new SimpleServletOutputStream(thresholdingOutputStream);
    }

    public boolean isThesholdExceeded() {
        return thresholdingOutputStream.isThresholdExceeded();
    }

    public byte[] getBufferedContent(){
        return inMemoryBuffer.toByteArray();
    }

    public File getContentFile() {
        return contentFile;
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
        replaceHeader(name, new Long(date));
    }

    public void addDateHeader(String name, long date) {
        appendHeader(name, new Long(date));
    }

    public void setHeader(String name, String value) {
        replaceHeader(name, value);
    }

    public void addHeader(String name, String value) {
        appendHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
        replaceHeader(name, new Integer(value));
    }

    public void addIntHeader(String name, int value) {
        appendHeader(name, new Integer(value));
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    private void replaceHeader(String name, Object value) {
        appendHeader(name, value);
    }

    private void appendHeader(String name, Object value) {
        headers.put(name, value);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStatus(int status, String string) {
        this.status = status;
    }

    public void sendRedirect(String location) throws IOException {
        this.status = SC_MOVED_TEMPORARILY;
        this.redirectionLocation = location;
        super.sendRedirect(location);
    }

    public void sendError(int status, String errorMsg) throws IOException {
        this.errorMsg = errorMsg;
        this.status = status;
        this.isError = true;
    }

    public void sendError(int status) throws IOException {
        this.status = status;
        this.isError = true;
    }

    public void setContentLength(int len) {
        this.contentLength = len;
    }

    public int getContentLength() {
        return (int)(contentLength >=0 ? contentLength : thresholdingOutputStream.getByteCount());
    }

    public void replay(HttpServletResponse target) throws IOException {
        replayHeadersAndStatus(target);
        replayContent(target, true);
    }

    public void replayHeadersAndStatus(HttpServletResponse target) throws IOException {
        if(isError){
            if(errorMsg != null){
                target.sendError(status, errorMsg);
            }
            else{
                target.sendError(status);
            }
        }
        else if(redirectionLocation != null){
            target.sendRedirect(redirectionLocation);
        }
        else{
            target.setStatus(status);
        }

        target.setStatus(getStatus());

        final Iterator it = headers.keySet().iterator();
        while (it.hasNext()) {
            final String header = (String) it.next();

            final Collection values = (Collection) headers.get(header);
            final Iterator valIt = values.iterator();
            while (valIt.hasNext()) {
                final Object val = valIt.next();
                RequestHeaderUtil.setHeader(target, header, val);
            }
        }

        // TODO : cookies ?
        target.setContentType(getContentType());
        target.setCharacterEncoding(getCharacterEncoding());
    }

    public void replayContent(HttpServletResponse target, boolean setContentLength) throws IOException {
        if(setContentLength){
            target.setContentLength(getContentLength());
        }
        if(getContentLength()>0){
            if(isThesholdExceeded()){
                FileInputStream in = FileUtils.openInputStream(getContentFile());
                IOUtils.copy(in, target.getOutputStream());
                IOUtils.closeQuietly(in);
            }
            else{
                IOUtils.copy(new ByteArrayInputStream(inMemoryBuffer.toByteArray()), target.getOutputStream());
            }
            target.flushBuffer();
        }
    }

    public void release(){
        if(isThesholdExceeded()){
            getContentFile().delete();
        }
    }

    private final class ThresholdingCacheOutputStream extends ThresholdingOutputStream {
        OutputStream out = inMemoryBuffer;

        private ThresholdingCacheOutputStream(int threshold) {
            super(threshold);
        }

        @Override
        protected OutputStream getStream() throws IOException {
            return out;
        }

        @Override
        protected void thresholdReached() throws IOException {
            if(serveIfThresholdReached){
                replayHeadersAndStatus(originalResponse);
                out = originalResponse.getOutputStream();
            }
            else{
                contentFile = File.createTempFile("cacheStream", null, Path.getTempDirectory());
                out = new FileOutputStream(contentFile);
            }
            out.write(getBufferedContent());
        }
    }

}
