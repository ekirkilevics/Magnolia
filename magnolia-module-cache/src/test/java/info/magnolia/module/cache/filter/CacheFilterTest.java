/**
 * This file Copyright (c) 2008-2009 Magnolia International
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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicy;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.ContentCompression;
import info.magnolia.module.cache.FlushPolicy;
import info.magnolia.module.cache.executor.Bypass;
import info.magnolia.module.cache.executor.Store;
import info.magnolia.module.cache.executor.UseCache;
import info.magnolia.module.cache.util.GZipUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.voting.DefaultVoting;
import info.magnolia.voting.Voting;
import info.magnolia.voting.voters.ResponseContentTypeVoter;
import info.magnolia.voting.voters.UserAgentVoter;
import info.magnolia.voting.voters.VoterSet;
import static info.magnolia.test.TestUtil.enumeration;
import junit.framework.TestCase;
import org.apache.commons.collections.map.MultiValueMap;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheFilterTest extends TestCase {
    private AggregationState aggregationState;
    private CacheFactory cacheFactory;
    private CachePolicy cachePolicy;
    private FlushPolicy flushPolicy;
    private Cache cache;
    private CacheFilter filter;
    private WebContext webContext;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
//    private HierarchyManager configHm;


    public void testFilterUsesGivenConfigAndCacheName() throws Exception {
        final ModuleRegistry moduleRegistry = new ModuleRegistryImpl();
        ComponentsTestUtil.setInstance(ModuleRegistry.class, moduleRegistry);

        final CacheModule cacheModule = new CacheModule();
        final CacheConfiguration c1 = new CacheConfiguration();
        c1.setName("wrong");
        cacheModule.addConfiguration("fake-config", c1);
        final CacheConfiguration c2 = new CacheConfiguration();
        c2.setName("right");
        cacheModule.addConfiguration("the-config-name", c2);

        final CacheFactory cacheFactory = createStrictMock(CacheFactory.class);
        expect(cacheFactory.getCache("the-config-name")).andReturn(createStrictMock(Cache.class));
        cacheModule.setCacheFactory(cacheFactory);

        final CacheFilter filter = new CacheFilter();
        filter.setName("the-filter-name");
        filter.setCacheConfigurationName("the-config-name");

        moduleRegistry.registerModuleInstance("cache", cacheModule);
        final FilterConfig filterConfig = createStrictMock(FilterConfig.class);

        replay(filterConfig, cacheFactory);
        filter.init(filterConfig);
        // called by init: filter.onCacheModuleStart();
        verify(filterConfig, cacheFactory);

        // just to shunt the normal setup/teardown:
        shuntSetupAndTeardownBecauseThisTestUsesItsOwnMocks();
    }

    // just to shunt the normal setup/teardown:
    private void shuntSetupAndTeardownBecauseThisTestUsesItsOwnMocks() {
        replay(cache, cachePolicy, request, response, filterChain);
        webContext.getAggregationState();
    }

    public void testStoresInCacheAndRenders() throws Exception {
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.store, "/test-page", null));

//        response.setDateHeader("Last-Modified", anyLong());

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                ((CacheResponseWrapper) args[1]).getWriter().print("hello");
                return null;
            }
        });


        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        fakedOut.write("my test".getBytes());
        //expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.setDateHeader(eq("Last-Modified"), anyLong());
        expect(response.getCharacterEncoding()).andReturn("UTF-8");
        expect(response.getContentType()).andReturn("text/html");

        expect(request.getHeaders("Accept-Encoding")).andReturn(new Enumeration() {
            private boolean has = true;
            public boolean hasMoreElements() {
                return has;
            }
            public Object nextElement() {
                has = false;
                return "gzip";
            }});

        response.addHeader("Content-Encoding", "gzip");
        expect(response.containsHeader("Content-Encoding")).andReturn(true);

        response.addHeader("Vary", "Accept-Encoding");
        expect(response.containsHeader("Vary")).andReturn(true);
        expect(response.isCommitted()).andReturn(false);
        expect(request.getDateHeader("If-Modified-Since")).andReturn(new Long(-1));
        response.flushBuffer();

        // by default unknown content types are not compresible
        expect(response.getContentType()).andReturn("some content type").times(2);
        expect(response.getCharacterEncoding()).andReturn("UTF-8");

        cache.put(eq("/test-page"), isA(CachedPage.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                final CachedPage cachedEntry = ((CachedPage) args[1]);
                // default should be unzipped for unknown content types!!!
                assertEquals("hello", new String(cachedEntry.getDefaultContent()));
                assertEquals("some content type", cachedEntry.getContentType());
                assertEquals("UTF-8", cachedEntry.getCharacterEncoding());
                assertEquals(200, cachedEntry.getStatusCode());

                return null;
            }
        });

        executeFilterAndVerify();
    }

    public void testStoresCompressedInCacheAndRenders() throws Exception {
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.store, "/test-page", null));

//        response.setDateHeader("Last-Modified", anyLong());

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                ((CacheResponseWrapper) args[1]).getWriter().print("hello");
                return null;
            }
        });


        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        fakedOut.write("my test".getBytes());
        //expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.setDateHeader(eq("Last-Modified"), anyLong());
        expect(response.getCharacterEncoding()).andReturn("UTF-8");
        expect(response.getContentType()).andReturn("text/html");

        expect(request.getHeaders("Accept-Encoding")).andReturn(new Enumeration() {
            private boolean has = true;
            public boolean hasMoreElements() {
                return has;
            }
            public Object nextElement() {
                has = false;
                return "gzip";
            }});

        response.addHeader("Content-Encoding", "gzip");
        expect(response.containsHeader("Content-Encoding")).andReturn(true);

        response.addHeader("Vary", "Accept-Encoding");
        expect(response.containsHeader("Vary")).andReturn(true);
        expect(response.isCommitted()).andReturn(false);
        expect(request.getDateHeader("If-Modified-Since")).andReturn(new Long(-1));
        response.flushBuffer();

        expect(response.getContentType()).andReturn("text/html").times(2);
        expect(response.getCharacterEncoding()).andReturn("UTF-8");

        cache.put(eq("/test-page"), isA(CachedPage.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                final CachedPage cachedEntry = ((CachedPage) args[1]);
                assertTrue(Arrays.equals(GZipUtil.gzip("hello".getBytes()), cachedEntry.getDefaultContent()));
                assertEquals("text/html", cachedEntry.getContentType());
                assertEquals("UTF-8", cachedEntry.getCharacterEncoding());
                assertEquals(200, cachedEntry.getStatusCode());

                return null;
            }
        });

        executeFilterAndVerify();
    }

    public void testBlindlyObeysCachePolicyAndGetsStuffOutOfCacheWhenAskedToDoSo() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";

        final MultiValueMap headers = new MultiValueMap();
        headers.put("Last-Modified", 2000l);
        headers.put("Dummy", "dummy");
        headers.put("Dummy", "dummy2");

        final CachedPage cachedPage = new CachedPage(dummyContent.getBytes(), "text/plain", "ASCII", 123, headers, System.currentTimeMillis());
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(-1l);
        expect(request.getHeader("User-Agent")).andReturn("Mozilla/4.0 (MSIE 7.0; Windows NT 5.1)").anyTimes();
        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration("foo", "gzip", "bar"));
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        expect(response.containsHeader("Last-Modified")).andReturn(false);
        response.addDateHeader("Last-Modified", 2000);
        expect(response.containsHeader("Dummy")).andReturn(false);
        response.addHeader("Dummy", "dummy");
        response.addHeader("Dummy", "dummy2");
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        expect(response.isCommitted()).andReturn(false);
        expect(response.containsHeader("Content-Encoding")).andReturn(false);
        response.addHeader("Content-Encoding", "gzip");
        expect(response.containsHeader("Content-Encoding")).andReturn(true);
        response.addHeader("Vary", "Accept-Encoding");
        expect(response.containsHeader("Vary")).andReturn(true);
        response.setContentLength(GZipUtil.gzip(dummyContent.getBytes()).length);
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

//        replay(configHm);
        executeFilterAndVerify();

        assertTrue(Arrays.equals(GZipUtil.gzip(dummyContent.getBytes()), fakedOut.toByteArray()));
    }

    public void testIgnoreEncodingAndServeContentFlatWhenUserAgentIsIE6() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";

        final MultiValueMap headers = new MultiValueMap();
        headers.put("Last-Modified", 2000l);
        headers.put("Dummy", "dummy");
        headers.put("Dummy", "dummy2");

        final CachedPage cachedPage = new CachedPage(dummyContent.getBytes(), "text/plain", "ASCII", 123, headers, System.currentTimeMillis());
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(-1l);
        expect(request.getHeader("User-Agent")).andReturn("Mozilla/4.0 (MSIE 6.0; Windows NT 5.1)").anyTimes();
//        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration("foo", "gzip", "bar"));
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        expect(response.containsHeader("Last-Modified")).andReturn(false);
        response.addDateHeader("Last-Modified", 2000);
        expect(response.containsHeader("Dummy")).andReturn(false);
        response.addHeader("Dummy", "dummy");
        response.addHeader("Dummy", "dummy2");
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        response.setContentLength(32);
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

//        replay(configHm);
        executeFilterAndVerify();

        assertTrue(Arrays.equals(dummyContent.getBytes(), fakedOut.toByteArray()));
    }

    public void testServesUnzippedContentAndRemovesGzipHeadersIfClientDoesNotAcceptGZipEncoding() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";
        final byte[] gzipped = GZipUtil.gzip(dummyContent.getBytes());
        final MultiValueMap headers = new MultiValueMap();
        headers.put("Content-Encoding", "gzip");
        headers.put("Vary", "Accept-Encoding");
        headers.put("Dummy", "Some Value");
        final CachedPage cachedPage = new CachedPage(dummyContent.getBytes(), "text/plain", "ASCII", 123, headers, System.currentTimeMillis());
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));

        expect(request.getDateHeader("If-Modified-Since")).andReturn(-1l);
        expect(request.getHeader("User-Agent")).andReturn("Mozilla/4.0 (MSIE 7.0; Windows NT 5.1)").anyTimes();
        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration());
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        expect(response.containsHeader("Dummy")).andReturn(false);
        response.addHeader("Dummy", "Some Value");
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        response.setContentLength(dummyContent.length());
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

//        replay(configHm);
        executeFilterAndVerify();

        assertEquals(dummyContent, fakedOut.toString());
    }

    public void testServesGZippedContentIfClientAcceptsGZipEncoding() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";
        final CachedPage cachedPage = new CachedPage(dummyContent.getBytes(), "text/plain", "ASCII", 123, new MultiValueMap(), System.currentTimeMillis(), true);
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(-1l);
        expect(request.getHeader("User-Agent")).andReturn("Mozilla/4.0 (MSIE 7.0; Windows NT 5.1)").anyTimes();
        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration("foo", "gzip", "bar"));
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        expect(response.isCommitted()).andReturn(false);
        expect(response.containsHeader("Content-Encoding")).andReturn(false);
        response.addHeader("Content-Encoding", "gzip");
        expect(response.containsHeader("Content-Encoding")).andReturn(true);
        response.addHeader("Vary", "Accept-Encoding");
        expect(response.containsHeader("Vary")).andReturn(true);
        byte[] gzipped = GZipUtil.gzip(dummyContent.getBytes());
        response.setContentLength(gzipped.length);
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

//        replay(configHm);
        executeFilterAndVerify();

        assertTrue(Arrays.equals(gzipped, fakedOut.toByteArray()));
    }

    public void testDoesNothingIfCachePolicyCommandsToBypass() throws Exception {
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.bypass, "/test-page", null));
        filterChain.doFilter(same(request), same(response));

        executeFilterAndVerify();
    }

    public void testJustSends304WithNoBodyIfRequestHeadersAskForIt() throws Exception {
        long timeStampInSeconds = System.currentTimeMillis() / 1000 * 1000;

        final CachedPage cachedPage = new CachedPage("dummy".getBytes(), "text/plain", "ASCII", 200, new MultiValueMap(), timeStampInSeconds);
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(timeStampInSeconds); // use some date in the future, so we're ahead of what cachedPage will say
        expect(request.getHeader("If-None-Match")).andReturn(null);
        expect(response.isCommitted()).andReturn(false);

        response.setStatus(304);
        // since we don't expect response.getOuputStream(), we actually assert nothing is written to the body

        executeFilterAndVerify();
    }

    public void testJustSends304WithNoBodyIfRequestHeadersAskForItEvenOnCommitedResponse() throws Exception {
        long timeStampInSeconds = System.currentTimeMillis() / 1000 * 1000;

        final byte[] gzipped = GZipUtil.gzip("dummy".getBytes());
        final CachedPage cachedPage = new CachedPage("dummy".getBytes(), "text/plain", "ASCII", 200, new MultiValueMap(), timeStampInSeconds);
        cachedPage.setPreCacheStatusCode(304);
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(timeStampInSeconds); // use some date in the future, so we're ahead of what cachedPage will say
        expect(request.getHeader("If-None-Match")).andReturn(null);
        // the main diff to the test above - response is already committed here and status (304) was set by another executed as indicated in preCacheStatusCode of the CachedPage
        expect(response.isCommitted()).andReturn(true);

        response.setStatus(304);
        // since the status is correct we don't expect response.getOuputStream(), we actually assert nothing is written to the body

        executeFilterAndVerify();
    }

    public void testDontJustSends304WithNoBodyIfRequestHeadersAskForItButResponseIsCommitted() throws Exception {
        long timeStampInSeconds = System.currentTimeMillis() / 1000 * 1000;

        final CachedPage cachedPage = new CachedPage("dummy".getBytes(), "text/plain", "ASCII", 200, new MultiValueMap(), timeStampInSeconds);
        final byte[] gzipped = GZipUtil.gzip("dummy".getBytes());
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(timeStampInSeconds); // use some date in the future, so we're ahead of what cachedPage will say
        expect(request.getHeader("If-None-Match")).andReturn(null);
        expect(request.getHeader("User-Agent")).andReturn("Mozilla/4.0 (MSIE 7.0; Windows NT 5.1)").anyTimes();
        expect(response.isCommitted()).andReturn(true);
        // we can't change status of the already committed response ... the only option left here is to proceed and send the data

        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration("foo", "gzip", "bar"));
        response.setStatus(200);
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        expect(response.isCommitted()).andReturn(false);
        expect(response.containsHeader("Content-Encoding")).andReturn(false);
        response.addHeader("Content-Encoding", "gzip");
        expect(response.containsHeader("Content-Encoding")).andReturn(true);
        response.addHeader("Vary", "Accept-Encoding");
        expect(response.containsHeader("Vary")).andReturn(true);
        response.setContentLength(gzipped.length);
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

//        replay(configHm);
        executeFilterAndVerify();
    }

    public void testPageShouldBeServedIfIfNoneMatchHeaderWasPassed() throws Exception {
        final String dummyContent = "i'm a dummy page that was cached earlier on";
        final byte[] gzipped = GZipUtil.gzip(dummyContent.getBytes());
        final CachedPage cachedPage = new CachedPage(dummyContent.getBytes(), "text/plain", "ASCII", 200, new MultiValueMap(), System.currentTimeMillis());
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(System.currentTimeMillis() + 1000); // use some date in the future, so we're ahead of what cachedPage will say
        expect(request.getHeader("If-None-Match")).andReturn("Some value");
        expect(request.getHeader("User-Agent")).andReturn("Mozilla/4.0 (MSIE 7.0; Windows NT 5.1)").anyTimes();
        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration("foo", "gzip", "bar"));

        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(200);
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        expect(response.isCommitted()).andReturn(false);
        expect(response.containsHeader("Content-Encoding")).andReturn(false);
        response.addHeader("Content-Encoding", "gzip");
        expect(response.containsHeader("Content-Encoding")).andReturn(true);
        response.addHeader("Vary", "Accept-Encoding");
        expect(response.containsHeader("Vary")).andReturn(true);
        response.setContentLength(gzipped.length);
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

//        replay(configHm);
        executeFilterAndVerify();

        assertTrue(Arrays.equals(gzipped, fakedOut.toByteArray()));
    }

    public void testRedirectsAreCached() throws Exception {
        final String redirectLocation = "/some-target-location";

        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.store, "/some-redirect", null));

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                ((CacheResponseWrapper) args[1]).sendRedirect(redirectLocation);
                return null;
            }
        });
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        //expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.setDateHeader(eq("Last-Modified"), anyLong());
        response.sendRedirect(redirectLocation);
        expect(response.isCommitted()).andReturn(false);
        expect(request.getDateHeader("If-Modified-Since")).andReturn(new Long(-1));
        response.flushBuffer();

        cache.put(eq("/some-redirect"), isA(CachedRedirect.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                final CachedRedirect cachedEntry = ((CachedRedirect) args[1]);
                assertEquals(302, cachedEntry.getStatusCode());
                assertEquals(redirectLocation, cachedEntry.getLocation());
                return null;
            }
        });

        executeFilterAndVerify();
        assertEquals("nothing should have been written to the output", 0, fakedOut.size());
    }

    public void testCachedRedirectsAreServed() throws Exception {
        final String redirectLocation = "/some-target-location";
        final CachedRedirect cachedRedirect = new CachedRedirect(333, redirectLocation);
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/some-redirect", cachedRedirect));
        expect(response.isCommitted()).andReturn(false);
        response.sendRedirect(redirectLocation);
        executeFilterAndVerify();
    }

    public void testErrorsAreCached() throws Exception {
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.store, "/non-existing", null));

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                ((CacheResponseWrapper) args[1]).sendError(404);
                return null;
            }
        });
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        //expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.setDateHeader(eq("Last-Modified"), anyLong());
        response.sendError(404);
        //after sending error, response is committed
        expect(response.isCommitted()).andReturn(true);
        response.flushBuffer();

        cache.put(eq("/non-existing"), isA(CachedError.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                final CachedError cachedEntry = ((CachedError) args[1]);
                assertEquals(404, cachedEntry.getStatusCode());
                return null;
            }
        });

        executeFilterAndVerify();
        assertEquals("nothing should have been written to the output", 0, fakedOut.size());
    }

    public void testCachedErrorsAreServed() throws Exception {
        final CachedError cachedError = new CachedError(404);
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/non-existing", cachedError));
        expect(response.isCommitted()).andReturn(false);
        response.sendError(404);
        executeFilterAndVerify();
    }

    public void testLastModifiedHeaderCanBeOverriddenByFurtherFiltersAndIsProperlyStoredAndReturned() throws Exception {
        final Calendar cal = Calendar.getInstance();
        cal.set(2008, 7, 8, 18, 0, 0);
        final Date expectedLastModified = cal.getTime();

        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(
                new CachePolicyResult(CachePolicyResult.store, "/dummy", null));

        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        //expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        // the header set by the Store executor
        response.setDateHeader(eq("Last-Modified"), anyLong());

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));

        // some filter or servlet down the chain sets Last-Modified
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                ((CacheResponseWrapper) args[1]).setDateHeader("Last-Modified", expectedLastModified.getTime());
                return null;
            }
        });
        response.setDateHeader(eq("Last-Modified"), eq(expectedLastModified.getTime()));
        expect(response.getContentType()).andReturn("text/html");

        expect(request.getHeaders("Accept-Encoding")).andReturn(new Enumeration() {
            private boolean has = true;
            public boolean hasMoreElements() {
                return has;
            }
            public Object nextElement() {
                has = false;
                return "gzip";
            }});

        response.addHeader("Content-Encoding", "gzip");
        expect(response.containsHeader("Content-Encoding")).andReturn(true);

        response.addHeader("Vary", "Accept-Encoding");
        expect(response.containsHeader("Vary")).andReturn(true);
        expect(response.isCommitted()).andReturn(false);
        expect(request.getDateHeader("If-Modified-Since")).andReturn(new Long(-1));
        response.flushBuffer();

        // when setting response headers and when instanciating CachedPage:
        expect(response.getContentType()).andReturn("some content type").times(2);
        expect(response.getCharacterEncoding()).andReturn("UTF-8");

        cache.put(eq("/dummy"), isA(CachedPage.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                final CachedPage cachedEntry = ((CachedPage) args[1]);
                assertEquals(expectedLastModified.getTime(), cachedEntry.getLastModificationTime());
                return null;
            }
        });

        executeFilterAndVerify();
    }

    public void test304IsNotCached() throws Exception {
        expect(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).andReturn(
                new CachePolicyResult(CachePolicyResult.store, "/dummy", null));

        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        // the header set by the Store executor
        response.setDateHeader(eq("Last-Modified"), anyLong());

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));

        // some filter or servlet down the chain sends a 304
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                ((CacheResponseWrapper) args[1]).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return null;
            }
        });
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

        // this is what Store does if makeCachedEntry() returns null
        cache.put(eq("/dummy"), isNull());
        cache.remove(eq("/dummy"));
    }

    private void executeFilterAndVerify() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // let's first assert the Filter did not forget to register itself
        final ModuleRegistry mr = Components.getSingleton(ModuleRegistry.class);
        final CacheModule module = (CacheModule) mr.getModuleInstance("cache");
        final Field field = module.getClass().getDeclaredField("listeners");
        field.setAccessible(true);
        final Set listeners = (Set) field.get(module);
        assertEquals(1, listeners.size());
        assertEquals(filter, listeners.iterator().next());


        // and now get down to the real business
        replay(webContext, cache, cachePolicy, request, response, filterChain);
        filter.doFilter(request, response, filterChain);

        verify(cache, cacheFactory, cachePolicy, webContext, request, response, filterChain);
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    protected void setUp() throws Exception {
        super.setUp();
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, ".");
        MockUtil.initMockContext();
        aggregationState = new AggregationState();

        cacheFactory = createStrictMock(CacheFactory.class);
        cachePolicy = createStrictMock(CachePolicy.class);
        cache = createStrictMock(Cache.class);
        request = createStrictMock(HttpServletRequest.class);
        response = createStrictMock(HttpServletResponse.class);
        filterChain = createStrictMock(FilterChain.class);
//        configHm = createStrictMock(HierarchyManager.class);

        final CacheModule cacheModule = new CacheModule();
        final ModuleRegistry moduleRegistry = new ModuleRegistryImpl();
        ComponentsTestUtil.setInstance(ModuleRegistry.class, moduleRegistry);
        moduleRegistry.registerModuleInstance("cache", cacheModule);
        final CacheConfiguration cfg = new CacheConfiguration();
        cfg.setName("my-config");
        cfg.setCachePolicy(cachePolicy);
        // add the default executors
        cfg.addExecutor(CachePolicyResult.bypass.getName(), new Bypass());
        cfg.addExecutor(CachePolicyResult.useCache.getName(), new UseCache());
        final Store store = new Store();
        ComponentsTestUtil.setInstance(Voting.class, new DefaultVoting());
        final java.util.Map compressibleTypes = new java.util.HashMap();
        compressibleTypes.put("1", "text/html");
        cfg.addExecutor(CachePolicyResult.store.getName(), store);

        cacheModule.addConfiguration("my-config", cfg);
        cacheModule.setCacheFactory(cacheFactory);

        HashMap map = new HashMap();
        ContentCompression compression = new ContentCompression();
        cacheModule.setCompression(compression);
        UserAgentVoter voter = new UserAgentVoter();
        voter.addRejected(".*MSIE 6.*");
        voter.setNot(true);
        ResponseContentTypeVoter voter2 = new ResponseContentTypeVoter();
        voter2.addAllowed("text/html");
        voter2.addAllowed("application/x-javascript");
        voter2.addAllowed("text/css");
        voter2.setNot(true);
        compression.setVoters(new VoterSet());
        compression.getVoters().addVoter(voter);
        compression.getVoters().addVoter(voter2);

        expect(cacheFactory.getCache("my-config")).andReturn(cache);
        replay(cacheFactory);

        filter = new CacheFilter();
        filter.setName("cache-filter");
        filter.setCacheConfigurationName("my-config");
        filter.init(null);
        // called by init() : filter.onCacheModuleStart();

        webContext = createStrictMock(WebContext.class);
        expect(webContext.getAggregationState()).andReturn(aggregationState);
        MgnlContext.setInstance(webContext);
    }
}
