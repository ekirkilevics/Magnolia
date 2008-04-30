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
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;
import org.apache.commons.collections.map.MultiValueMap;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
        expect(cacheFactory.getCache("the-config-name")).andReturn(createStrictMock(Cache.class));
        cacheModule.setCacheFactory(cacheFactory);

        final CacheFilter filter = new CacheFilter();
        filter.setName("the-filter-name");
        filter.setCacheConfiguration("the-config-name");

        moduleRegistry.registerModuleInstance("cache", cacheModule);
        final FilterConfig filterConfig = createStrictMock(FilterConfig.class);

        replay(filterConfig, cacheFactory);
        filter.init(filterConfig);
        verify(filterConfig, cacheFactory);

        // just to shunt the normal setup/teardown:
        shuntSetupAndTeardownBecauseThisTestUsesItsOwnMocks();
    }

    // just to shunt the normal setup/teardown:
    private void shuntSetupAndTeardownBecauseThisTestUsesItsOwnMocks() {
        replay(cache, cachePolicy, request, response, filterChain);
        webContext.getAggregationState();
    }

    // TODO
//    public void testJustSendsHeaderIfIfModifiedSinceHeaderBlah() {
//        fail();
//    }

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
                assertTrue(Arrays.equals("hello".getBytes(), cachedEntry.getOut()));
                assertEquals("some content type", cachedEntry.getContentType());
                assertEquals("UTF-8", cachedEntry.getCharacterEncoding());
                assertEquals(200, cachedEntry.getStatusCode());

                return null;
            }
        });

        executeFilterAndVerify();
    }

    public void testBlindlyObeysCachePolicyAndGetsStuffOutOfCacheWhenAskedToDoSo() throws Exception {
        final String dummyContent = "hello";

        final CachedPage cachedPage = new CachedPage(dummyContent.getBytes(), "text/plain", "ASCII", 123, new MultiValueMap());
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.useCache, "/test-page", cachedPage));

        final ByteArrayOutputStream fakedOut = new ByteArrayOutputStream();
        response.setStatus(123);
        response.setContentType("text/plain");
//        response.setCharacterEncoding("ASCII");
        response.setContentLength(dummyContent.length());
        expect(response.getOutputStream()).andReturn(new SimpleServletOutputStream(fakedOut));
        response.flushBuffer();

        executeFilterAndVerify();

        assertEquals("hello", fakedOut.toString());
    }

    public void testDoesNothingIfCachePolicyCommandsToBypass() throws Exception {
        expect(cachePolicy.shouldCache(cache, aggregationState)).andReturn(new CachePolicyResult(CachePolicyResult.bypass, "/test-page", null));
        filterChain.doFilter(same(request), same(response));

        executeFilterAndVerify();
    }

    private void executeFilterAndVerify() throws IOException, ServletException {
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
        cfg.setName("cache-config");
        cfg.setCachePolicy(cachePolicy);
        cacheModule.addConfiguration("cache-config", cfg);
        cacheModule.setCacheFactory(cacheFactory);

        expect(cacheFactory.getCache("cache-config")).andReturn(cache);
        replay(cacheFactory);

        filter = new CacheFilter();
        filter.setName("cache-filter");
        filter.setCacheConfiguration("cache-config");
        filter.init(null);

        webContext = createStrictMock(WebContext.class);
        expect(webContext.getAggregationState()).andReturn(aggregationState);
        replay(webContext);
        MgnlContext.setInstance(webContext);
    }
}
