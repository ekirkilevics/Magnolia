/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.cms.filters;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter will process any incoming requests containing Range or If-Range headers and swallow all produced output except for that matching the requested range.
 * 
 * @version $Id$
 */
public class RangeSupportFilter extends AbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(RangeSupportFilter.class);

    private boolean wrapWriter = true;

    private boolean isRangedRequest = false;

    private boolean isServeContent = true;

    long lastModTime = -1;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        isRangedRequest = request.getHeader("Range") != null;
        // react only on ranged requests
        if (isRangedRequest) {
            response = wrapResponse(request, response);

            // client might just check on us to see if we support ranged requests before actually requesting the content
            isServeContent = !"HEAD".equalsIgnoreCase(request.getMethod());
        }
        chain.doFilter(request, response);

    }

    public boolean isWrapWriter() {
        return wrapWriter;
    }

    /**
     * RFP defines only byte ranges, however writers operate on characters which might be more then one byte long. We might be cutting the character in half at the boundary of range which might make some clients unhappy even tho they asked for it. Default value is true.
     * 
     * @param wrapWriter
     */
    public void setWrapWriter(boolean wrapWriter) {
        this.wrapWriter = wrapWriter;
    }

    private HttpServletResponse wrapResponse(final HttpServletRequest request, final HttpServletResponse response) {
        return new HttpServletResponseWrapper(response) {

            /** default length is max. We hope that the underlying code will set proper content length as a header before we proceed serving some bytes. */
            private int length = Integer.MAX_VALUE;

            private final Map<String, Object> headers = new HashMap<String, Object>();

            private String eTag;

            private List<RangeInfo> ranges;

            private RangeInfo full;

            private ServletOutputStream stream;

            private PrintWriter writer;

            @Override
            public void addDateHeader(String name, long date) {
                super.addDateHeader(name, date);
                this.headers.put(name, date);
                if ("Last-Modified".equalsIgnoreCase(name)) {
                    lastModTime = date;
                }
            }

            @Override
            public void setDateHeader(String name, long date) {
                super.setDateHeader(name, date);
                this.headers.put(name, date);
                if ("Last-Modified".equalsIgnoreCase(name)) {
                    lastModTime = date;
                }
            }

            @Override
            public void addHeader(String name, String value) {
                if ("Content-Disposition".equalsIgnoreCase(name) && log.isDebugEnabled()) {
                    log.warn("content disposition enforced by underlying filter/servlet");
                }
                super.addHeader(name, value);
                this.headers.put(name, value);
            }

            @Override
            public void setHeader(String name, String value) {
                if ("Content-Disposition".equalsIgnoreCase(name) && log.isDebugEnabled()) {
                    log.warn("content disposition enforced by underlying filter/servlet");
                }
                super.setHeader(name, value);
                this.headers.put(name, value);
            }

            @Override
            public void addIntHeader(String name, int value) {
                super.addIntHeader(name, value);
                this.headers.put(name, value);
            }

            @Override
            public void setIntHeader(String name, int value) {
                super.setIntHeader(name, value);
                this.headers.put(name, value);
            }

            @Override
            public void setContentLength(int len) {
                this.length = len;
                // do not propagate length up. We might not be able to change it once it is set. We will set it ourselves once we are ready to serve bytes.
            }

            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                // make sure we set stream only once. Multiple calls to this method are allowed.
                if (this.stream == null) {
                    ServletOutputStream stream = super.getOutputStream();
                    // wrap the response to filter out everything except desired range
                    this.stream = addRangeSupportWrapper(request, response, stream);

                    if (!isServeContent || this.stream == null) {
                        // swallow output on head requests
                        this.stream = new ServletOutputStream() {

                            @Override
                            public void write(int b) throws IOException {
                                // do nothing, we do not write any output now
                            }
                        };
                    }
                }
                return stream;
            }

            private ServletOutputStream addRangeSupportWrapper(final HttpServletRequest request, final HttpServletResponse response, ServletOutputStream stream) throws IOException {
                if (!processContent(request, response)) {
                    // we might have to return null stream instead as the previous method already called res.sendError();
                    return null;
                }

                if (headers.containsKey("Content-Range")) {
                    // doesn't work for tomcat as it accesses underlying stream under our hands!!!
                    log.debug("Range request was handled by underlying filter/servlet.");
                    return stream;
                }
                if (ranges == null || ranges.isEmpty()) {
                    // no op, serve all as usual
                    log.debug("Didn't find any range to speak of. Serving all content as usual.");
                    if (length != Integer.MAX_VALUE) {
                        // set real length when we know it
                        response.setContentLength(length);
                    }
                } else if (ranges.size() == 1) {
                    RangeInfo range = ranges.get(0);
                    log.debug("Serving range [{}].", range);
                    response.setContentLength(range.lengthOfRange);
                    // setting 206 header is essential for some clients. The would abort if response is set to 200
                    response.setStatus(SC_PARTIAL_CONTENT);
                    stream = new RangedOutputStream(stream, range);
                } else {
                    log.error("Requested multiple ranges [{}].", ranges.size());
                    // TODO: add support for multiple ranges (sent as multipart request), for now just send error back
                    response.setHeader("Content-Range", "bytes */" + length);
                    response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    // again we might have to return null stream after calling sendError() as the original stream might no longer be valid
                }
                return stream;
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                if (!wrapWriter) {
                    return super.getWriter();
                }
                if (this.writer == null) {
                    this.writer = new PrintWriter(new OutputStreamWriter(getOutputStream()));
                }
                return writer;
            }

            private boolean processContent(HttpServletRequest request, HttpServletResponse response) throws IOException {
                log.debug("Serving binary on uri {} was last modified at {}", new Object[] { request.getRequestURI(), lastModTime });
                if (!isRequestValid(request, response)) {
                    log.debug("Skipping request {} since it doesn't require body", new Object[] { request.getRequestURI() });
                    return false;
                }
                if (!processRange(request)) {
                    log.debug("Could not process range of request {}", new Object[] { request.getRequestURI() });
                    return false;
                }
                return true;
            }

            private boolean processRange(HttpServletRequest request) throws IOException {
                full = new RangeInfo(0, length - 1, length);
                ranges = new ArrayList<RangeInfo>();

                String range = request.getHeader("Range");

                // Valid range header format is "bytes=n-n,n-n,n-n...". If not, then return 416.
                if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                    response.setHeader("Content-Range", "bytes */" + length);
                    response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return false;
                }

                // If-Range header must match ETag or be greater then LastModified. If not, then return full file.
                String ifRange = request.getHeader("If-Range");
                if (ifRange != null && !ifRange.equals(eTag)) {
                    try {
                        long ifRangeTime = request.getDateHeader("If-Range");
                        if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModTime) {
                            ranges.add(full);
                        }
                    } catch (IllegalArgumentException ignore) {
                        // happens when if-range contains something else then date
                        ranges.add(full);
                    }
                }

                // in case there were no invalid If-Range headers, then look at requested byte ranges.
                if (ranges.isEmpty()) {
                    for (String part : range.substring(6).split(",")) {
                        int start = intSubstring(StringUtils.substringBefore(part, "-"));
                        int end = intSubstring(StringUtils.substringAfter(part, "-"));

                        if (start == -1) {
                            start = length - end;
                            end = length - 1;
                        } else if (end == -1 || end > length - 1) {
                            end = length - 1;
                        }

                        // Is range valid?
                        if (start > end) {
                            response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                            return false;
                        }

                        // Add range.
                        ranges.add(new RangeInfo(start, end, length));
                    }
                }

                response.setHeader("ETag", eTag);
                if (ranges.size() == 1) {
                    RangeInfo r = ranges.get(0);
                    response.setHeader("Accept-Ranges", "bytes");
                    response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.totalLengthOfServedBinary);
                    length = r.lengthOfRange;
                }
                return true;
            }

            private int intSubstring(String value) {
                return value.length() > 0 ? Integer.parseInt(value) : -1;
            }

            @Override
            public void flushBuffer() throws IOException {
                if (writer != null) {
                    writer.flush();
                }
                if (stream != null) {
                    stream.flush();
                }

                super.flushBuffer();
            }

            private boolean isRequestValid(HttpServletRequest request, HttpServletResponse response) throws IOException {
                String fileName = StringUtils.substringAfterLast(request.getRequestURI(), "/");
                eTag = fileName + "_" + length + "_" + lastModTime;

                // If-None-Match header should contain "*" or ETag.
                String ifNoneMatch = request.getHeader("If-None-Match");
                if (ifNoneMatch != null && matches(ifNoneMatch, eTag)) {
                    response.setHeader("ETag", eTag); // Required in 304.
                    log.debug("Returning {} on header If-None-Match", HttpServletResponse.SC_NOT_MODIFIED);
                    response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                    return false;
                }

                // If-Modified-Since header must be greater than LastModified. ignore if If-None-Match header exists
                long ifModifiedSince = request.getDateHeader("If-Modified-Since");
                if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModTime) {
                    response.setHeader("ETag", eTag); // Required in 304.
                    // 304 response should contain Date header unless running on timeless server (see 304 response docu)
                    response.addDateHeader("Date", lastModTime);
                    log.debug("Returning {} on header If-Modified-Since", HttpServletResponse.SC_NOT_MODIFIED);
                    response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                    return false;
                }

                // If-Match header should contain "*" or ETag.
                String ifMatch = request.getHeader("If-Match");
                if (ifMatch != null && !matches(ifMatch, eTag)) {
                    log.debug("Returning {} on header If-Match", HttpServletResponse.SC_PRECONDITION_FAILED);
                    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }

                // If-Unmodified-Since header must be greater than LastModified.
                long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
                if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModTime) {
                    log.debug("Returning {} on header If-Unmodified-Since", HttpServletResponse.SC_PRECONDITION_FAILED);
                    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }

                log.debug("Passed all precondition checkes for request {}", request.getRequestURI());
                return true;
            }
        };
    }

    private boolean matches(String matchHeader, String toMatch) {
        String[] matchValues = matchHeader.split("\\s*,\\s*");
        Arrays.sort(matchValues);
        return Arrays.binarySearch(matchValues, toMatch) > -1
                || Arrays.binarySearch(matchValues, "*") > -1;
    }

    /**
     * Requested byte range.
     * 
     * @version $Id$
     */
    protected class RangeInfo {
        final int start;
        final int end;
        final int lengthOfRange;
        final int totalLengthOfServedBinary;

        public RangeInfo(int start, int end, int totalLengthOfServedBinary) {
            this.start = start;
            this.end = end;
            this.lengthOfRange = end - start + 1;
            this.totalLengthOfServedBinary = totalLengthOfServedBinary;
        }

        @Override
        public String toString() {
            return "Start: " + start + ", end: " + end + ", len: " + lengthOfRange + ", totalLengthOfServedBinary: " + totalLengthOfServedBinary;
        }
    }
}
