/**
 * This file Copyright (c) 2013 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

/**
 * Test class for RangeSupportFilter.
 */
public class RangeSupportFilterTest {

    @Test
    public void testContentIsServedFromOutputStreamInNewRequestAfterHeadRequestWasExecuted() throws IOException, ServletException {
        // GIVEN
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final DummyFilter dummy = new DummyFilter();
        final FilterChain chain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                dummy.doFilter(request, response, this);
            }
        };
        final StringWriter stringWriter = new StringWriter();
        final DummyOutputStream output = new DummyOutputStream();

        when(response.getOutputStream()).thenReturn(output);
        when(request.getHeader("Range")).thenReturn("bytes=0-56000");
        when(request.getMethod()).thenReturn("HEAD").thenReturn("GET");
        when(request.getDateHeader("If-Modified-Since")).thenReturn(-1L);

        final RangeSupportFilter filter = new RangeSupportFilter();

        // WHEN
        filter.doFilter(request, response, chain); // HEAD request

        // THEN
        ServletOutputStream out = dummy.getResponse().getOutputStream();
        out.write("hi there".getBytes());
        out.flush();
        assertEquals("", output.toString());

        // WHEN
        filter.doFilter(request, response, chain); // GET request

        // THEN
        out = dummy.getResponse().getOutputStream();
        out.write("hi there".getBytes());
        out.flush();
        assertEquals("hi there", output.toString());
    }

    public static class DummyFilter extends AbstractMgnlFilter {

        private HttpServletResponse response;

        @Override
        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
            this.response = response;
        }

        public HttpServletResponse getResponse() {
            return response;
        }

    }

    public static class DummyOutputStream extends ServletOutputStream {
        private StringBuilder string = new StringBuilder();

        private OutputStream stream = new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                string.append((char) b);
            }

            @Override
            public String toString() {
                return string.toString();
            }
        };

        @Override
        public void write(int b) throws IOException {
            stream.write(b);
        }

        @Override
        public String toString() {
            return string.toString();
        }
    }

}