/**
 * This file Copyright (c) 2008 Magnolia International
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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.util.FactoryUtil;
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
import info.magnolia.module.cache.util.GZipUtil;
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
import java.util.Set;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheFilterTest extends TestCase {
    private AggregationState aggregationState;
    private CacheFactory cacheFactory;
    private CachePolicy cachePolicy;
    private Cache cache;
    private CacheFilter filter;
    private WebContext webContext;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    public void testFilterUsesGivenConfigAndCacheName() throws Exception {
        final ModuleRegistry moduleRegistry = new ModuleRegistryImpl();
        FactoryUtil.setInstance(ModuleRegistry.class, moduleRegistry);

        final CacheModule cacheModule = new CacheModule();
        final CacheConfiguration c1 = new CacheConfiguration();
        c1.setName("wrong");
        cacheModule.addConfiguration("fake-config", c1);
        final CacheConfiguration c2 = new CacheConfiguration();
        c2.setName("right");
        cacheModule.addConfiguration("the-config-name", c2);

        final CacheFactory cacheFactory = createStrictMock(CacheFactory.class);
        expect(cacheFactory.getCache("cachefilter-the-config-name")).andReturn(createStrictMock(Cache.class));
        cacheModule.setCacheFactory(cacheFactory);

        final CacheFilter filter = new CacheFilter();
        filter.setName("the-filter-name");
        filter.setCacheConfiguration("the-config-name");

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
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.store, "/test-page", null));

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
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        expect(response.getCharacterEncoding()).andReturn("UTF-8");

        response.flushBuffer();

        expect(response.getContentType()).andReturn("some content type");
        expect(response.getCharacterEncoding()).andReturn("UTF-8");

        cache.put(eq("/test-page"), isA(CachedPage.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                final CachedPage cachedEntry = ((CachedPage) args[1]);
                assertTrue(Arrays.equals("hello".getBytes(), cachedEntry.getDefaultContent()));
                assertEquals("some content type", cachedEntry.getContentType());
                assertEquals("UTF-8", cachedEntry.getCharacterEncoding());
                assertEquals(200, cachedEntry.getStatusCode());

                return null;
            }
        });

        executeFilterAndVerify();
    }

    public void testBlindlyObeysCachePolicyAndGetsStuffOutOfCacheWhenAskedToDoSo() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";

        final CachedPage cachedPage = new CachedPage(dummyContent.getBytes(), "text/plain", "ASCII", 123, new MultiValueMap());
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(-1l);
        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration("foo", "gzip", "bar"));
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        response.setContentLength(dummyContent.length());
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        executeFilterAndVerify();

        assertEquals(dummyContent, fakedOut.toString());
    }

    public void testServesUnzippedContentAndRemovesGzipHeadersIfClientDoesNotAcceptGZipEncoding() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";
        final byte[] gzipped = GZipUtil.gzip(dummyContent.getBytes());
        final MultiValueMap headers = new MultiValueMap();
        headers.put("Content-Encoding", "gzip");
        headers.put("Vary", "Accept-Encoding");
        headers.put("Dummy", "Some Value");
        final CachedPage cachedPage = new CachedPage(gzipped, "text/plain", "ASCII", 123, headers);
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));

        expect(request.getDateHeader("If-Modified-Since")).andReturn(-1l);
        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration());
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        response.addHeader("Dummy", "Some Value");
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        response.setContentLength(dummyContent.length());
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        executeFilterAndVerify();

        assertEquals(dummyContent, fakedOut.toString());
    }

    public void testServesGZippedContentIfClientAcceptsGZipEncoding() throws Exception {
        final String dummyContent = "hello i'm a page that was cached";
        final byte[] gzipped = GZipUtil.gzip(dummyContent.getBytes());
        final CachedPage cachedPage = new CachedPage(gzipped, "text/plain", "ASCII", 123, new MultiValueMap());
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(-1l);
        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration("foo", "gzip", "bar"));
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        response.setContentLength(gzipped.length);
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        executeFilterAndVerify();

        assertTrue(Arrays.equals(gzipped, fakedOut.toByteArray()));
    }

    public void testDoesNothingIfCachePolicyCommandsToBypass() throws Exception {
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.bypass, "/test-page", null));
        filterChain.doFilter(same(request), same(response));

        executeFilterAndVerify();
    }

    public void testJustSends304WithNoBodyIfRequestHeadersAskForIt() throws Exception {
        final CachedPage cachedPage = new CachedPage("dummy".getBytes(), "text/plain", "ASCII", 200, new MultiValueMap());
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(System.currentTimeMillis() + 1000); // use some date in the future, so we're ahead of what cachedPage will say
        expect(request.getHeader("If-None-Match")).andReturn(null);

        response.setStatus(304);
        // since we don't expect response.getOuputStream(), we actually assert nothing is written to the body

        executeFilterAndVerify();
    }

    public void testPageShouldBeServedIfIfNoneMatchHeaderWasPassed() throws Exception {
        final String dummyContent = "i'm a dummy page that was cached earlier on";
        final CachedPage cachedPage = new CachedPage(dummyContent.getBytes(), "text/plain", "ASCII", 200, new MultiValueMap());
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));
        expect(request.getDateHeader("If-Modified-Since")).andReturn(System.currentTimeMillis() + 1000); // use some date in the future, so we're ahead of what cachedPage will say
        expect(request.getHeader("If-None-Match")).andReturn("Some value");
        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration("foo", "gzip", "bar"));

        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(200);
        response.setContentType("text/plain");
        response.setCharacterEncoding("ASCII");
        response.setContentLength(dummyContent.length());
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        executeFilterAndVerify();

        assertEquals(dummyContent, fakedOut.toString());
    }

    public void testRedirectsAreCached() throws Exception {
        final String redirectLocation = "/some-target-location";

        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.store, "/some-redirect", null));

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                ((CacheResponseWrapper) args[1]).sendRedirect(redirectLocation);
                return null;
            }
        });
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.sendRedirect(redirectLocation);
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
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/some-redirect", cachedRedirect));

        response.sendRedirect(redirectLocation);
        executeFilterAndVerify();
    }

    public void testErrorsAreCached() throws Exception {
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.store, "/non-existing", null));

        filterChain.doFilter(same(request), isA(CacheResponseWrapper.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final Object[] args = getCurrentArguments();
                ((CacheResponseWrapper) args[1]).sendError(404);
                return null;
            }
        });
        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.sendError(404);
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
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/non-existing", cachedError));

        response.sendError(404);
        executeFilterAndVerify();
    }

    private void executeFilterAndVerify() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {
        // let's first assert the Filter did not forget to register itself
        final ModuleRegistry mr = (ModuleRegistry) FactoryUtil.getSingleton(ModuleRegistry.class);
        final CacheModule module = (CacheModule) mr.getModuleInstance("cache");
        final Field field = module.getClass().getDeclaredField("listeners");
        field.setAccessible(true);
        final Set listeners = (Set) field.get(module);
        assertEquals(1, listeners.size());
        assertEquals(filter, listeners.iterator().next());

        // and now get down to the real business
        replay(cache, cachePolicy, request, response, filterChain);
        filter.doFilter(request, response, filterChain);

        verify(cache, cacheFactory, cachePolicy, webContext, request, response, filterChain);
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    protected void setUp() throws Exception {
        super.setUp();
        aggregationState = new AggregationState();

        cacheFactory = createStrictMock(CacheFactory.class);
        cachePolicy = createStrictMock(CachePolicy.class);
        cache = createStrictMock(Cache.class);
        request = createStrictMock(HttpServletRequest.class);
        response = createStrictMock(HttpServletResponse.class);
        filterChain = createStrictMock(FilterChain.class);

        final CacheModule cacheModule = new CacheModule();
        final ModuleRegistry moduleRegistry = new ModuleRegistryImpl();
        FactoryUtil.setInstance(ModuleRegistry.class, moduleRegistry);
        moduleRegistry.registerModuleInstance("cache", cacheModule);
        final CacheConfiguration cfg = new CacheConfiguration();
        cfg.setName("my-config");
        cfg.setCachePolicy(cachePolicy);
        cacheModule.addConfiguration("my-config", cfg);
        cacheModule.setCacheFactory(cacheFactory);

        expect(cacheFactory.getCache("cachefilter-my-config")).andReturn(cache);
        replay(cacheFactory);

        filter = new CacheFilter();
        filter.setName("cache-filter");
        filter.setCacheConfiguration("my-config");
        filter.init(null);
        // called by init() : filter.onCacheModuleStart();

        webContext = createStrictMock(WebContext.class);
        expect(webContext.getAggregationState()).andReturn(aggregationState);
        replay(webContext);
        MgnlContext.setInstance(webContext);
    }
}
