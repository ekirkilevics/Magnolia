/**
 * This file Copyright (c) 2011 Magnolia International
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

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.filters.WebContainerResources;
import info.magnolia.cms.filters.WebContainerResourcesImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.ContentCompression;
import info.magnolia.module.cache.cachepolicy.Default;
import info.magnolia.module.cache.ehcache.EhCacheFactory;
import info.magnolia.module.cache.executor.Bypass;
import info.magnolia.module.cache.executor.Store;
import info.magnolia.module.cache.executor.UseCache;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.voting.DefaultVoting;
import info.magnolia.voting.Voting;
import info.magnolia.voting.voters.TrueVoter;
import info.magnolia.voting.voters.VoterSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;

import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingCacheTest extends TestCase {

    private static final String CONTENT = "CONTENT";

    private static final Producer SUCCEEDING_RENDERING = new Producer(){
        public void produce(ServletRequest request, ServletResponse response) throws IOException {
                response.getWriter().write(CONTENT);
        }
    };

    private static final Producer SLOW_RENDERING = new Producer(){
        public void produce(ServletRequest request, ServletResponse response) throws IOException {
            try {
                Thread.sleep(4000);
                response.getWriter().write(CONTENT);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private static final Producer FAILING_RENDERING = new Producer(){
        public void produce(ServletRequest request, ServletResponse response) throws IOException {
            throw new RuntimeException("Failing rendering.");
        }
    };



    private Default cachePolicy;
    private CacheFilter filter;
    private EhCacheFactory factory;

    private static final Logger log = LoggerFactory.getLogger(BlockingCacheTest.class);

    public void testLockTimeoutException() throws Exception{
        ExecutorService ex = Executors.newSingleThreadExecutor();


        Future<CachePolicyResult> futureResultOfFirstRequest = ex.submit(new Callable<CachePolicyResult>(){
            public CachePolicyResult call() throws Exception{
                return doRequest(SLOW_RENDERING);
            }
        });

        Thread.sleep(1000);

        try {
            doRequest(SLOW_RENDERING);
            fail("Cache didn't timeout.");
        } catch(LockTimeoutException e){
            // this is expected to happen
        }

        CachePolicyResult resultOfFirstRequest = futureResultOfFirstRequest.get();

        log.info("4th get: {}", resultOfFirstRequest);
        assertNotNull(resultOfFirstRequest);

        CachePolicyResult resultOfThirdRequest = doRequest(SLOW_RENDERING);
        // should be served by the cache
        log.info("4th get: {}", resultOfThirdRequest);
    }

    public void testThatAFailingRequestReleasesTheLock() throws Exception{
        try{
            doRequest(FAILING_RENDERING);
            fail("Should fail");
        }
        catch (Throwable th) {
            // this is expected to happen
        }

        // verify that the this doesn't block the cache for ever
        doRequest(SUCCEEDING_RENDERING);
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
        factory.stop();
    }

    protected void setUp() throws Exception {
        super.setUp();
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_CACHE_STARTDIR, "target/cacheTest");
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, ".");
        factory  = new EhCacheFactory();

        MockUtil.initMockContext();
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);

        cachePolicy = new Default();
        VoterSet voters = new VoterSet();
        TrueVoter tVoter = new TrueVoter();
        voters.addVoter(tVoter);
        cachePolicy.setVoters(voters);

        final CacheModule cacheModule = new CacheModule();
        final ModuleRegistry moduleRegistry = new ModuleRegistryImpl();
        ComponentsTestUtil.setInstance(ModuleRegistry.class, moduleRegistry);
        moduleRegistry.registerModuleInstance("cache", cacheModule);
        final info.magnolia.module.cache.CacheConfiguration cfg = new info.magnolia.module.cache.CacheConfiguration();
        cfg.setName("my-config");
        cfg.setCachePolicy(cachePolicy);
        // add the default executors
        cfg.addExecutor(CachePolicyResult.bypass.getName(), new Bypass());
        cfg.addExecutor(CachePolicyResult.useCache.getName(), new UseCache());
        final Store store = new Store();
        ComponentsTestUtil.setInstance(Voting.class, new DefaultVoting());

        cfg.addExecutor(CachePolicyResult.store.getName(), store);

        final java.util.Map compressibleTypes = new java.util.HashMap();
        compressibleTypes.put("1", "text/html");
        cacheModule.addConfiguration("my-config", cfg);

        ContentCompression compression = new ContentCompression();
        compression.setVoters(new VoterSet());
        cacheModule.setCompression(compression);

        CacheConfiguration config = new CacheConfiguration();
        config.setName("my-config");
        config.setMaxElementsInMemory(1000);
        config.setEternal(true);
        config.setDiskPersistent(false);
        config.setOverflowToDisk(false);

        factory.setDefaultCacheConfiguration(config);
        factory.setBlockingTimeout(1000);
        factory.start();
        cacheModule.setCacheFactory(factory);

        filter = new CacheFilter();
        filter.setName("cache-filter");
        filter.setCacheConfigurationName("my-config");
        filter.init(null);
    }

    private CachePolicyResult doRequest(final Producer producer) throws IOException, ServletException {
        MockWebContext webContext;
        HttpServletRequest request;
        HttpServletResponse response;

        webContext = new MockWebContext();
        MgnlContext.setInstance(webContext);

        request = createMock(HttpServletRequest.class);
        response = createMock(HttpServletResponse.class);

        webContext.setRequest(request);
        webContext.setResponse(response);

        expect(request.getServerName()).andStubReturn("serverName");
        expect(request.getRequestURL()).andStubReturn(new StringBuffer("/theURL"));
        expect(request.getDateHeader("If-Modified-Since")).andStubReturn(Long.valueOf(-1));
        expect(request.getHeaders("Accept-Encoding")).andStubReturn(IteratorUtils.asEnumeration(IteratorUtils.EMPTY_ITERATOR));

        expect(response.getContentType()).andStubReturn("text/html");
        expect(response.containsHeader("Last-Modified")).andReturn(false);
        expect(response.getCharacterEncoding()).andStubReturn("UTF-8");
        response.setStatus(200);
        response.addDateHeader(eq("Last-Modified"), anyLong());
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(CONTENT.length());

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        expect(response.getOutputStream()).andStubReturn(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }
        });
        response.flushBuffer();

        MgnlContext.setInstance(webContext);

        replay(request, response);

        filter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                producer.produce(request, response);
            }
        });

        return CachePolicyResult.getCurrent();
    }

    interface Producer{
        void produce(ServletRequest request, ServletResponse response) throws IOException;
    }



}