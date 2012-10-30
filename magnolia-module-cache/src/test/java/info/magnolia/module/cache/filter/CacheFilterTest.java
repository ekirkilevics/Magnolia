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

import static info.magnolia.test.TestUtil.enumeration;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.filters.WebContainerResources;
import info.magnolia.cms.filters.WebContainerResourcesImpl;
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
import info.magnolia.module.cache.executor.CompositeExecutor;
import info.magnolia.module.cache.executor.Store;
import info.magnolia.module.cache.executor.UseCache;
import info.magnolia.module.cache.mbean.CacheMonitor;
import info.magnolia.module.cache.util.GZipUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.voting.DefaultVoting;
import info.magnolia.voting.Voting;
import info.magnolia.voting.voters.ResponseContentTypeVoter;
import info.magnolia.voting.voters.UserAgentVoter;
import info.magnolia.voting.voters.VoterSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.MultiValueMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Basic cache filter test.
 * @version $Id$
 */

public class CacheFilterTest {

    private static final String originalURL = "/original";

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

    @Before
    public void setUp() throws Exception {
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, ".");
        webContext = mock(WebContext.class);
        MgnlContext.setInstance(webContext);
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);

        aggregationState = new AggregationState();

        cacheFactory = mock(CacheFactory.class);
        cachePolicy = mock(CachePolicy.class);
        cache = mock(Cache.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        final CacheMonitor cacheMonitor = new CacheMonitor(null);
        final CacheModule cacheModule = new CacheModule(null, cacheMonitor);
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

        when(cacheFactory.getCache("my-config")).thenReturn(cache);

        filter = new CacheFilter(cacheModule, cacheMonitor);
        filter.setName("cache-filter");
        filter.setCacheConfigurationName("my-config");
        filter.init(null);
        // called by init() : filter.onCacheModuleStart();

        webContext = mock(WebContext.class);
        when(webContext.getAggregationState()).thenReturn(aggregationState);
        MgnlContext.setInstance(webContext);
    }

    @Test
    public void testFilterUsesGivenConfigAndCacheName() throws Exception {
        final ModuleRegistry moduleRegistry = new ModuleRegistryImpl();
        ComponentsTestUtil.setInstance(ModuleRegistry.class, moduleRegistry);

        final CacheMonitor cacheMonitor = new CacheMonitor(null);
        final CacheModule cacheModule = new CacheModule(null, cacheMonitor);
        final CacheConfiguration c1 = new CacheConfiguration();
        c1.setName("wrong");
        cacheModule.addConfiguration("fake-config", c1);
        final CacheConfiguration c2 = new CacheConfiguration();
        c2.setName("right");
        cacheModule.addConfiguration("the-config-name", c2);

        final CacheFactory cacheFactory = mock(CacheFactory.class);
        when(cacheFactory.getCache("the-config-name")).thenReturn(cache);
        cacheModule.setCacheFactory(cacheFactory);

        final CacheFilter filter = new CacheFilter(cacheModule, cacheMonitor);
        filter.setName("the-filter-name");
        filter.setCacheConfigurationName("the-config-name");

        moduleRegistry.registerModuleInstance("cache", cacheModule);
        final FilterConfig filterConfig = mock(FilterConfig.class);

        filter.init(filterConfig);
        verify(cacheFactory).getCache("the-config-name");
    }

    @Test
    public void test304IsNotCached() throws Exception {
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.store, "/dummy", null));

        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new SimpleServletOutputStream(fakedOut));
        // the header set by the Store executor
        response.setDateHeader(eq("Last-Modified"), anyLong());

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));

        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

        // this is what Store does if makeCachedEntry() returns null
        cache.put(eq("/dummy"), isNull());
        cache.remove(eq("/dummy"));
    }

    @Test
    public void testStoresInCacheAndRenders() throws Exception {
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.store, "/test-page", null));

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));

        response.setDateHeader(eq("Last-Modified"), anyLong());
        // used to build the writer
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        // checks this is not a if-modified request
        when(response.isCommitted()).thenReturn(false);
        when(request.getDateHeader("If-Modified-Since")).thenReturn(new Long(-1));

        // by default unknown content types are not compresible
        when(response.getContentType()).thenReturn("some content type");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");

        cache.put(eq("/test-page"), isA(ContentCachedEntry.class));

        executeCacheFilterAndVerify();
    }

    @Test
    public void testStoresCompressedInCacheAndRenders() throws Exception {
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.store, "/test-page", null));

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));

        response.setDateHeader(eq("Last-Modified"), anyLong());
        // used to build the writer
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        // checks this is not a if-modified request
        when(response.isCommitted()).thenReturn(false);
        when(request.getDateHeader("If-Modified-Since")).thenReturn(new Long(-1));

        // by default unknown content types are not compresible
        when(response.getContentType()).thenReturn("text/html");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");

        cache.put(eq("/test-page"), isA(ContentCachedEntry.class));

        executeCacheFilterAndVerify();
    }

    @Test
    public void testBlindlyObeysCachePolicyAndGetsStuffOutOfCacheWhenAskedToDoSo() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";

        final MultiValueMap headers = new MultiValueMap();
        headers.put("Last-Modified", 2000l);
        headers.put("Dummy", "dummy");
        headers.put("Dummy", "dummy2");

        final InMemoryCachedEntry cachedPage = new InMemoryCachedEntry(dummyContent.getBytes(), "text/plain", "ASCII", 123, headers, System.currentTimeMillis(), originalURL, -1);
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        when(request.getDateHeader("If-Modified-Since")).thenReturn(-1l);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/4.0 (MSIE 7.0; Windows NT 5.1)");//.anyTimes();
        when(request.getHeaders("Accept-Encoding")).thenReturn(enumeration("foo", "gzip", "bar"));
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        when(response.containsHeader("Last-Modified")).thenReturn(false);
        response.addDateHeader("Last-Modified", 2000);
        when(response.containsHeader("Dummy")).thenReturn(false);
        response.addHeader("Dummy", "dummy");
        response.addHeader("Dummy", "dummy2");
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        when(response.isCommitted()).thenReturn(false);
        when(response.containsHeader("Content-Encoding")).thenReturn(false);
        response.addHeader("Content-Encoding", "gzip");
        when(response.containsHeader("Content-Encoding")).thenReturn(true);
        response.addHeader("Vary", "Accept-Encoding");
        when(response.containsHeader("Vary")).thenReturn(true);
        response.setContentLength(GZipUtil.gzip(dummyContent.getBytes()).length);
        when(response.getOutputStream()).thenReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        when(webContext.getResponse()).thenReturn(response);

        executeNoCacheFilterAndVerify();

        assertTrue(Arrays.equals(GZipUtil.gzip(dummyContent.getBytes()), fakedOut.toByteArray()));
    }

    @Ignore
    @Test
    public void testIgnoreEncodingAndServeContentFlatWhenUserAgentIsIE6() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";

        final MultiValueMap headers = new MultiValueMap();
        headers.put("Last-Modified", 2000l);
        headers.put("Dummy", "dummy");
        headers.put("Dummy", "dummy2");

        final InMemoryCachedEntry cachedPage = new InMemoryCachedEntry(dummyContent.getBytes(), "text/plain", "ASCII", 123, headers, System.currentTimeMillis(), originalURL, -1);
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        when(request.getDateHeader("If-Modified-Since")).thenReturn(-1l);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/4.0 (MSIE 6.0; Windows NT 5.1)");
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        when(response.containsHeader("Last-Modified")).thenReturn(false);
        response.addDateHeader("Last-Modified", 2000l);
        when(response.containsHeader("Dummy")).thenReturn(false);
        response.addHeader("Dummy", "dummy");
        response.addHeader("Dummy", "dummy2");
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        response.setContentLength(32);
        when(response.getOutputStream()).thenReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        when(webContext.getResponse()).thenReturn(response);
        when(request.getHeaders("Accept-Encoding")).thenReturn(enumeration("foo", "gzip", "bar"));
        when(response.containsHeader("Content-Encoding")).thenReturn(true);
        executeNoCacheFilterAndVerify();

        assertTrue(Arrays.equals(dummyContent.getBytes(), fakedOut.toByteArray()));
    }

    @Test
    public void testServesUnzippedContentAndRemovesGzipHeadersIfClientDoesNotAcceptGZipEncoding() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";

        final MultiValueMap headers = new MultiValueMap();
        headers.put("Content-Encoding", "gzip");
        headers.put("Vary", "Accept-Encoding");
        headers.put("Dummy", "Some Value");
        final InMemoryCachedEntry cachedPage = new InMemoryCachedEntry(dummyContent.getBytes(), "text/plain", "ASCII", 123, headers, System.currentTimeMillis(), originalURL, -1);
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));

        when(request.getDateHeader("If-Modified-Since")).thenReturn(-1l);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/4.0 (MSIE 7.0; Windows NT 5.1)");
        when(request.getHeaders("Accept-Encoding")).thenReturn(enumeration());
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        when(response.containsHeader("Dummy")).thenReturn(false);
        response.addHeader("Dummy", "Some Value");
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        response.setContentLength(dummyContent.length());
        when(response.getOutputStream()).thenReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        when(webContext.getResponse()).thenReturn(response);
        executeNoCacheFilterAndVerify();

        assertEquals(dummyContent, fakedOut.toString());
    }

    @Test
    public void testServesGZippedContentIfClientAcceptsGZipEncoding() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";
        final InMemoryCachedEntry cachedPage = new InMemoryCachedEntry(dummyContent.getBytes(), "text/plain", "ASCII", 123, new MultiValueMap(), System.currentTimeMillis(), originalURL, -1);
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        when(request.getDateHeader("If-Modified-Since")).thenReturn(-1l);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/4.0 (MSIE 7.0; Windows NT 5.1)");
        when(request.getHeaders("Accept-Encoding")).thenReturn(enumeration("foo", "gzip", "bar"));
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        when(response.isCommitted()).thenReturn(false);
        when(response.containsHeader("Content-Encoding")).thenReturn(false);
        response.addHeader("Content-Encoding", "gzip");
        when(response.containsHeader("Content-Encoding")).thenReturn(true);
        response.addHeader("Vary", "Accept-Encoding");
        when(response.containsHeader("Vary")).thenReturn(true);
        byte[] gzipped = GZipUtil.gzip(dummyContent.getBytes());
        response.setContentLength(gzipped.length);
        when(response.getOutputStream()).thenReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        when(webContext.getResponse()).thenReturn(response);
        executeNoCacheFilterAndVerify();

        assertTrue(Arrays.equals(gzipped, fakedOut.toByteArray()));
    }

    @Test
    public void testDoesNothingIfCachePolicyCommandsToBypass() throws Exception {
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.bypass, "/test-page", null));
        filterChain.doFilter(same(request), same(response));

        executeNoCacheFilterAndVerify();
    }

    @Test
    public void testJustSends304WithNoBodyIfRequestHeadersAskForIt() throws Exception {
        long timeStampInSeconds = System.currentTimeMillis() / 1000 * 1000;

        final InMemoryCachedEntry cachedPage = new InMemoryCachedEntry("dummy".getBytes(), "text/plain", "ASCII", 200, new MultiValueMap(), timeStampInSeconds, originalURL, -1);
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        when(request.getDateHeader("If-Modified-Since")).thenReturn(timeStampInSeconds); // use some date in the future, so we're ahead of what cachedPage will say
        when(request.getHeader("If-None-Match")).thenReturn(null);
        when(response.isCommitted()).thenReturn(false);

        response.setStatus(304);
        // since we don't when response.getOuputStream(), we actually assert nothing is written to the body

        executeNoCacheFilterAndVerify();
    }

    @Test
    public void testJustSends304WithNoBodyIfRequestHeadersAskForItEvenOnCommitedResponse() throws Exception {
        long timeStampInSeconds = System.currentTimeMillis() / 1000 * 1000;

        final InMemoryCachedEntry cachedPage = new InMemoryCachedEntry("dummy".getBytes(), "text/plain", "ASCII", 200, new MultiValueMap(), timeStampInSeconds, originalURL, -1);
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        when(request.getDateHeader("If-Modified-Since")).thenReturn(timeStampInSeconds); // use some date in the future, so we're ahead of what cachedPage will say
        when(request.getHeader("If-None-Match")).thenReturn(null);
        // the main diff to the test above - response is already committed here and status (304) was set by another executed as indicated in preCacheStatusCode of the ContentCachedEntry
        when(response.isCommitted()).thenReturn(true);

        response.setStatus(304);
        // since the status is correct we don't when response.getOuputStream(), we actually assert nothing is written to the body

        when(webContext.getResponse()).thenReturn(response);
        when(request.getHeaders("Accept-Encoding")).thenReturn(enumeration("foo", "gzip", "bar"));
        when(response.containsHeader("Content-Encoding")).thenReturn(true);
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new SimpleServletOutputStream(fakedOut));
        executeNoCacheFilterAndVerify();
    }

    @Test
    public void testDontJustSends304WithNoBodyIfRequestHeadersAskForItButResponseIsCommitted() throws Exception {
        long timeStampInSeconds = System.currentTimeMillis() / 1000 * 1000;

        final InMemoryCachedEntry cachedPage = new InMemoryCachedEntry("dummy".getBytes(), "text/plain", "ASCII", 200, new MultiValueMap(), timeStampInSeconds, originalURL, -1);
        final byte[] gzipped = GZipUtil.gzip("dummy".getBytes());
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        when(request.getDateHeader("If-Modified-Since")).thenReturn(timeStampInSeconds); // use some date in the future, so we're ahead of what cachedPage will say
        when(request.getHeader("If-None-Match")).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/4.0 (MSIE 7.0; Windows NT 5.1)");
        when(response.isCommitted()).thenReturn(true);
        // we can't change status of the already committed response ... the only option left here is to proceed and send the data

        when(request.getHeaders("Accept-Encoding")).thenReturn(enumeration("foo", "gzip", "bar"));
        response.setStatus(200);
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        when(response.isCommitted()).thenReturn(false);
        when(response.containsHeader("Content-Encoding")).thenReturn(false);
        response.addHeader("Content-Encoding", "gzip");
        when(response.containsHeader("Content-Encoding")).thenReturn(true);
        response.addHeader("Vary", "Accept-Encoding");
        when(response.containsHeader("Vary")).thenReturn(true);
        response.setContentLength(gzipped.length);
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        executeNoCacheFilterAndVerify();
    }

    @Test
    public void testPageShouldBeServedIfIfNoneMatchHeaderWasPassed() throws Exception {
        final String dummyContent = "i'm a dummy page that was cached earlier on";
        final byte[] gzipped = GZipUtil.gzip(dummyContent.getBytes());
        final InMemoryCachedEntry cachedPage = new InMemoryCachedEntry(dummyContent.getBytes(), "text/plain", "ASCII", 200, new MultiValueMap(), System.currentTimeMillis(), originalURL, -1);
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        when(request.getDateHeader("If-Modified-Since")).thenReturn(System.currentTimeMillis() + 1000); // use some date in the future, so we're ahead of what cachedPage will say
        when(request.getHeader("If-None-Match")).thenReturn("Some value");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/4.0 (MSIE 7.0; Windows NT 5.1)");
        when(request.getHeaders("Accept-Encoding")).thenReturn(enumeration("foo", "gzip", "bar"));

        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(200);
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        when(response.isCommitted()).thenReturn(false);
        when(response.containsHeader("Content-Encoding")).thenReturn(false);
        response.addHeader("Content-Encoding", "gzip");
        when(response.containsHeader("Content-Encoding")).thenReturn(true);
        response.addHeader("Vary", "Accept-Encoding");
        when(response.containsHeader("Vary")).thenReturn(true);
        response.setContentLength(gzipped.length);
        when(response.getOutputStream()).thenReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        when(webContext.getResponse()).thenReturn(response);
        executeNoCacheFilterAndVerify();

        assertTrue(Arrays.equals(gzipped, fakedOut.toByteArray()));
    }

    @Test
    public void testRedirectsAreCached() throws Exception {
        final String redirectLocation = "/some-target-location";

        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.store, "/some-redirect", null));

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));

        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();

        response.setDateHeader(eq("Last-Modified"), anyLong());
        response.sendRedirect(redirectLocation);
        when(response.isCommitted()).thenReturn(false);
        when(request.getDateHeader("If-Modified-Since")).thenReturn(new Long(-1));
        response.flushBuffer();

        cache.put(eq("/some-redirect"), isA(CachedRedirect.class));

        executeCacheFilterAndVerify();
        assertEquals("nothing should have been written to the output", 0, fakedOut.size());
    }

    @Test
    public void testCachedRedirectsAreServed() throws Exception {
        final String redirectLocation = "/some-target-location";
        final CachedRedirect cachedRedirect = new CachedRedirect(333, redirectLocation, originalURL, -1);
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.useCache, "/some-redirect", cachedRedirect));
        when(response.isCommitted()).thenReturn(false);
        response.sendRedirect(redirectLocation);
        executeNoCacheFilterAndVerify();
    }

    @Test
    public void testErrorsAreCached() throws Exception {
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.store, "/non-existing", null));

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));

        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();

        response.setDateHeader(eq("Last-Modified"), anyLong());
        response.sendError(404);
        //after sending error, response is committed
        when(response.isCommitted()).thenReturn(true);
        response.flushBuffer();

        cache.put(eq("/non-existing"), isA(CachedError.class));

        executeCacheFilterAndVerify();
        assertEquals("nothing should have been written to the output", 0, fakedOut.size());
    }

    @Test
    public void testCachedErrorsAreServed() throws Exception {
        final CachedError cachedError = new CachedError(404, originalURL, -1);
        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.useCache, "/non-existing", cachedError));
        when(response.isCommitted()).thenReturn(false);
        response.sendError(404);
        executeNoCacheFilterAndVerify();
    }

    @Test
    public void testLastModifiedHeaderCanBeOverriddenByFurtherFiltersAndIsProperlyStoredAndReturned() throws Exception {
        final Calendar cal = Calendar.getInstance();
        cal.set(2008, 7, 8, 18, 0, 0);
        final Date expectedLastModified = cal.getTime();

        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(
                new CachePolicyResult(CachePolicyResult.store, "/dummy", null));

        // the header set by the Store executor
        response.setDateHeader(eq("Last-Modified"), anyLong());

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));

        response.setDateHeader(eq("Last-Modified"), eq(expectedLastModified.getTime()));
        when(response.getContentType()).thenReturn("text/html");

        when(request.getHeaders("Accept-Encoding")).thenReturn(new Enumeration() {
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

        response.addHeader("Content-Encoding", "gzip");
        when(response.containsHeader("Content-Encoding")).thenReturn(true);

        response.addHeader("Vary", "Accept-Encoding");
        when(response.containsHeader("Vary")).thenReturn(true);
        when(response.isCommitted()).thenReturn(false);
        when(request.getDateHeader("If-Modified-Since")).thenReturn(new Long(-1));
        response.flushBuffer();

        // when setting response headers and when instantiating ContentCachedEntry:
        when(response.getContentType()).thenReturn("some content type");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");

        cache.put(eq("/dummy"), isA(ContentCachedEntry.class));

        executeCacheFilterAndVerify();
    }

    @Test
    public void testIfWeAreNotWriteContentToResponseTwiceWhenTimeToLiveInSecondsIsZero() throws Exception {
        // GIVEN
        // let's first assert the Filter did not forget to register itself
        final ModuleRegistry mr = Components.getSingleton(ModuleRegistry.class);
        final CacheModule module = (CacheModule) mr.getModuleInstance("cache");
        final Field field = module.getClass().getDeclaredField("listeners");
        field.setAccessible(true);
        final Set listeners = (Set) field.get(module);
        assertEquals(1, listeners.size());
        assertEquals(filter, listeners.iterator().next());
        when(webContext.getAggregationState()).thenReturn(aggregationState);

        // add Store and UseCache executor
        CompositeExecutor storeAndUseCache = new CompositeExecutor();
        storeAndUseCache.addExecutor(new Store());
        storeAndUseCache.addExecutor(new UseCache());
        module.getConfiguration("my-config").addExecutor(CachePolicyResult.store.getName(), storeAndUseCache);

        when(cachePolicy.shouldCache(cache, aggregationState, flushPolicy)).thenReturn(new CachePolicyResult(CachePolicyResult.store, "/test-page", null));

        StringBuffer buffer = new StringBuffer("/test-page");
        when(request.getRequestURL()).thenReturn(buffer);

        FilterChain filterChain = new FilterChain() {

            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                ((CacheResponseWrapper)response).addHeader("Cache-Control", "no-cache");
                response.getOutputStream().print("Test content");
            }
        };

        when(webContext.getResponse()).thenReturn(response);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Chrome/18.0.1025.151 Safari/535.19");
        when(response.getContentType()).thenReturn("text/plain");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        when(request.getHeaders("Accept-Encoding")).thenReturn(enumeration("foo", "gzip", "bar"), enumeration("foo", "gzip", "bar"));
        when(response.containsHeader("Content-Encoding")).thenReturn(false, true);
        when(response.containsHeader("Vary")).thenReturn(true);
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new SimpleServletOutputStream(fakedOut));

        // WHEN
        filter.doFilter(request, response, filterChain);

        //THEN
        assertTrue(GZipUtil.isGZipped(fakedOut.toByteArray()));
        assertEquals(32, fakedOut.size());
        assertEquals("Test content", new String(GZipUtil.ungzip(fakedOut.toByteArray())));

        verify(cacheFactory).getCache("my-config");
        verify(cachePolicy).shouldCache(cache, aggregationState, flushPolicy);
    }

    private void executeCacheFilterAndVerify() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // let's first assert the Filter did not forget to register itself
        final ModuleRegistry mr = Components.getSingleton(ModuleRegistry.class);
        final CacheModule module = (CacheModule) mr.getModuleInstance("cache");
        final Field field = module.getClass().getDeclaredField("listeners");
        field.setAccessible(true);
        final Set listeners = (Set) field.get(module);
        assertEquals(1, listeners.size());
        assertEquals(filter, listeners.iterator().next());
        when(webContext.getAggregationState()).thenReturn(aggregationState);

        // and now get down to the real business
        StringBuffer buffer = new StringBuffer("some/path");
        when(request.getRequestURL()).thenReturn(buffer);
        filter.doFilter(request, response, filterChain);

        verify(cacheFactory).getCache("my-config");
        verify(cachePolicy).shouldCache(cache, aggregationState, flushPolicy);
    }

    private void executeNoCacheFilterAndVerify() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // let's first assert the Filter did not forget to register itself
        final ModuleRegistry mr = Components.getSingleton(ModuleRegistry.class);
        final CacheModule module = (CacheModule) mr.getModuleInstance("cache");
        final Field field = module.getClass().getDeclaredField("listeners");
        field.setAccessible(true);
        final Set listeners = (Set) field.get(module);
        assertEquals(1, listeners.size());
        assertEquals(filter, listeners.iterator().next());

        // and now get down to the real business
        filter.doFilter(request, response, filterChain);

        verify(cacheFactory).getCache("my-config");
        verify(cachePolicy).shouldCache(cache, aggregationState, flushPolicy);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }
}