/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.module.cache.executor;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.filters.WebContainerResources;
import info.magnolia.cms.filters.WebContainerResourcesImpl;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.ContentCompression;
import info.magnolia.module.cache.cachepolicy.Default;
import info.magnolia.module.cache.ehcache.EhCacheFactory;
import info.magnolia.module.cache.filter.CacheFilter;
import info.magnolia.module.cache.filter.CacheResponseWrapper;
import info.magnolia.module.cache.mbean.CacheMonitor;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.voting.DefaultVoting;
import info.magnolia.voting.Voting;
import info.magnolia.voting.voters.TrueVoter;
import info.magnolia.voting.voters.VoterSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.RepositoryException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.IteratorUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Blocking cache test.
 *
 */
public class StoreTest {

    private Default cachePolicy;
    private CacheFilter filter;
    private EhCacheFactory factory;
    private File cacheFolder = new File("target" + File.separator + "tmp");
    private static final char[] CONTENT = new char[CacheResponseWrapper.DEFAULT_THRESHOLD * 2];
    private final String errorMessage = "Cannot run the test - the expected cache temp folder '" + cacheFolder.getAbsolutePath() + " doesnt exists.";

    private static final Producer producer = new Producer(){
        @Override
        public void produce(ServletRequest request, ServletResponse response) throws IOException {
            response.getWriter().write(CONTENT);
        }
    };

    private static final Logger log = LoggerFactory.getLogger(StoreTest.class);

    @Test
    public void testClearCacheWhenExceptionOccurs() throws Exception {

        //WHEN
        try {
            doRequest(producer);
        } catch (Throwable e) { //IOException
           //must happen
        }

        if (!cacheFolder.exists()) {
              log.error(errorMessage);
              fail(errorMessage);
        }

        //THEN
        assertTrue("The tmp folder should be empty now.", cacheFolder.listFiles().length == 0);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        factory.stop();
    }

    @Before
    public void setUp() throws Exception {
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_CACHE_STARTDIR, "target" + File.separator + "cacheTest");
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, ".");
        final CacheMonitor cacheMonitor = new CacheMonitor(createStrictMock(CommandsManager.class));
        factory  = new EhCacheFactory(cacheMonitor);

        MockUtil.initMockContext();
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);

        cachePolicy = new Default();
        VoterSet voters = new VoterSet();
        TrueVoter tVoter = new TrueVoter();
        voters.addVoter(tVoter);
        cachePolicy.setVoters(voters);

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

        cfg.addExecutor(CachePolicyResult.store.getName(), store);

        final Map<String, String> compressibleTypes = new HashMap<String, String>();
        compressibleTypes.put("1", "text/html");
        cacheModule.addConfiguration("my-config", cfg);

        ContentCompression compression = new ContentCompression();
        compression.setVoters(new VoterSet());
        cacheModule.setCompression(compression);

        net.sf.ehcache.config.CacheConfiguration ehConfig = new net.sf.ehcache.config.CacheConfiguration();
        ehConfig.setName("my-config");
        ehConfig.setMaxElementsInMemory(1000);
        ehConfig.setEternal(true);
        ehConfig.setDiskPersistent(false);
        ehConfig.setOverflowToDisk(false);

        factory.setDefaultCacheConfiguration(ehConfig);
        factory.setBlockingTimeout(1000);
        factory.start();
        cacheModule.setCacheFactory(factory);

        filter = new CacheFilter(cacheModule, cacheMonitor);
        filter.setName("cache-filter");
        filter.setCacheConfigurationName("my-config");
        filter.init(null);

        MgnlContext.getAggregationState().setCurrentContent(new MockContent("boo"));
    }

    private CachePolicyResult doRequest(final Producer producer) throws IOException, ServletException, RepositoryException {

        MockWebContext webContext;
        HttpServletRequest request;
        HttpServletResponse response;

        webContext = new MockWebContext();
        MgnlContext.setInstance(webContext);

        request = createNiceMock(HttpServletRequest.class);
        response = createMock(HttpServletResponse.class);

        webContext.setRequest(request);
        webContext.setResponse(response);
        expect(request.getServerName()).andStubReturn("serverName");
        expect(request.getRequestURL()).andStubReturn(new StringBuffer("/theURL"));
        expect(request.isSecure()).andStubReturn(false);
        expect(request.getDateHeader("If-Modified-Since")).andStubReturn(Long.valueOf(-1));
        expect(request.getHeaders("Accept-Encoding")).andStubReturn(IteratorUtils.asEnumeration(IteratorUtils.EMPTY_ITERATOR));

        expect(response.getContentType()).andStubReturn("text/html");
        expect(response.containsHeader("Last-Modified")).andReturn(false);
        expect(response.getCharacterEncoding()).andStubReturn("UTF-8");
        response.setStatus(200);
        response.addDateHeader(eq("Last-Modified"), anyLong());
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(CONTENT.length);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        expect(response.getOutputStream()).andStubReturn(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }
        });
        response.flushBuffer();

        MgnlContext.setInstance(webContext);
        MockContent blah = new MockContent("blah");
        blah.setNodeData(MgnlNodeType.JCR_PRIMARY_TYPE, "mgnl:content");

        MgnlContext.getAggregationState().setCurrentContent(blah);

        replay(request, response);

        filter.doFilter(request, response, new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                producer.produce(request, response);
                throw new IOException();
            }
        });

        return CachePolicyResult.getCurrent();
    }

    /**
     * Producer interface.
     *
     */
    interface Producer {
        void produce(ServletRequest request, ServletResponse response) throws IOException;
    }
}
