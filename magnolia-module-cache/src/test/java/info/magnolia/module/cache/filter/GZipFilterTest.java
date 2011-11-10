/**
 * This file Copyright (c) 2008-2011 Magnolia International
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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import info.magnolia.cms.filters.WebContainerResources;
import info.magnolia.cms.filters.WebContainerResourcesImpl;
import info.magnolia.module.cache.util.GZipUtil;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Basic tests for gzip filter.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class GZipFilterTest extends MgnlTestCase {
    private static final String SOME_10CHARSLONG_CHAIN = "qwertzuiop";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);
    }

    // FIXME: MAGNOLIA-3413, commented out the failing tests
    @Ignore
    @Test
    public void testBufferIsFlushedProperlyWhenUsingWriterFurtherDownTheChainOfFilters() throws Exception {
        final int iterations = 5000;

        final FilterChain chain = createStrictMock(FilterChain.class);
        final HttpServletRequest mockRequest = createStrictMock(HttpServletRequest.class);
        expect(mockRequest.getAttribute(EasyMock.<String>anyObject())).andReturn(null).anyTimes();
        expect(mockRequest.getHeaders("Accept-Encoding")).andReturn(new Enumeration() {
            private boolean has = true;
            @Override
            public boolean hasMoreElements() {
                return has;
            }
            @Override
            public Object nextElement() {
                has = false;
                return "gzip";
            }});
        // using a nice mock for response, because the point here is to not enforce the specific usage of flushBuffer()
        // on the response by the GZipFilter, but rather to make sure that however this is handled, all content
        // is actually there (which last time check was solved by calling flushBuffer() indeed)
        final HttpServletResponse mockResponse = createNiceMock(HttpServletResponse.class);
        // expectations - which contradict the comment above - they'll move to another test soonish.
        expect(mockResponse.containsHeader("Content-Encoding")).andReturn(false);
        mockResponse.addHeader("Content-Encoding", "gzip");
        expect(mockResponse.containsHeader("Content-Encoding")).andReturn(true);
        mockResponse.addHeader("Vary", "Accept-Encoding");
        expect(mockResponse.containsHeader("Vary")).andReturn(true);
        expect(mockResponse.getCharacterEncoding()).andReturn("ASCII"); // called when creating the writer in CacheResponseWriter
        mockResponse.setContentLength(anyInt());

        // we need to wrap the mock response to be able to get the written output
        final ByteArrayOutputStream finalOutput = new ByteArrayOutputStream();
        final SimpleServletOutputStream servletOutput = new SimpleServletOutputStream(finalOutput);
        final HttpServletResponse testReponse = new HttpServletResponseWrapper(mockResponse) {
            // and we know GZipFilter will only call getOutputStream() on it.
            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return servletOutput;
            }
        };

        chain.doFilter(same(mockRequest), isA(CacheResponseWrapper.class));
        // fake some chained filter:
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                final ServletResponse responseFutherDownTheChain = ((ServletResponse) args[1]);
                // let's pretend we're some filter writing some content
                final PrintWriter out = responseFutherDownTheChain.getWriter();
                for (int i = 0; i < iterations; i++) {
                    out.println(SOME_10CHARSLONG_CHAIN);
                }
                return null;
            }
        });

        replay(mockRequest, mockResponse, chain);
        final GZipFilter filter = new GZipFilter();
        filter.doFilter(mockRequest, testReponse, chain);
        verify(mockRequest, mockResponse, chain);

        // now assert GZipFilter has written the expected amount of characters in the original response
        final byte[] compressedBytes = finalOutput.toByteArray();
        assertTrue("output should be gzipped", GZipUtil.isGZipped(compressedBytes));
        final byte[] uncompressed = GZipUtil.ungzip(compressedBytes);
        final int expectedLength = iterations * (SOME_10CHARSLONG_CHAIN.length() + System.getProperty("line.separator").length()); // n chars + newline
        assertEquals(expectedLength, uncompressed.length);
    }

    @Ignore
    @Test
    public void testBufferIsFlushedAndGZipNotSetOnError() throws Exception {

        final FilterChain chain = createStrictMock(FilterChain.class);
        final HttpServletRequest mockRequest = createStrictMock(HttpServletRequest.class);

        final HttpServletResponse mockResponse = createStrictMock(HttpServletResponse.class);
        // TODO: should not rather return all time default ISO-8859-1 as specified by RFC?
        expect(mockResponse.getCharacterEncoding()).andReturn("ASCII");
        // send the error
        mockResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        mockResponse.flushBuffer();

        // we need to wrap the mock response to be able to get the written output
        final ByteArrayOutputStream finalOutput = new ByteArrayOutputStream();
        final SimpleServletOutputStream servletOutput = new SimpleServletOutputStream(finalOutput);
        final HttpServletResponse testReponse = new HttpServletResponseWrapper(mockResponse) {
            // and we know GZipFilter will only call getOutputStream() on it.
            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return servletOutput;
            }
        };

        chain.doFilter(same(mockRequest), isA(CacheResponseWrapper.class));
        // fake some chained filter:
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                final ServletResponse responseFutherDownTheChain = ((ServletResponse) args[1]);
                // let's pretend we're some filter writing some content
                final PrintWriter out = responseFutherDownTheChain.getWriter();
                out.println(SOME_10CHARSLONG_CHAIN);
                // fake the content denied
                ((HttpServletResponse) responseFutherDownTheChain).sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return null;
            }
        });

        replay(mockRequest, mockResponse, chain);
        final GZipFilter filter = new GZipFilter();
        filter.doFilter(mockRequest, testReponse, chain);
        verify(mockRequest, mockResponse, chain);

        // now assert GZipFilter has written the expected amount of characters in the original response
        final byte[] uncompressed = finalOutput.toByteArray();
        // errors should be uncompressed
        assertFalse("output should be gzipped", GZipUtil.isGZipped(uncompressed));
        final int expectedLength = SOME_10CHARSLONG_CHAIN.length() + System.getProperty("line.separator").length(); // n chars + newline
        assertEquals(expectedLength, uncompressed.length);
    }
}
