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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.cache.BlockingCache;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.CacheModuleLifecycleListener;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicyExecutor;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.mbean.CacheMonitor;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.constructs.blocking.LockTimeoutException;

import java.io.IOException;

/**
 * Uses the CachePolicy to determine the cache behavior. Uses then the
 * CacheConfiguration to get the executors to be executed.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheFilter extends OncePerRequestAbstractMgnlFilter implements CacheModuleLifecycleListener {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CacheFilter.class);

    private static final String DEFAULT_CACHE_CONFIG = "default";

    private CacheMonitor monitor;
    private String cacheConfigurationName;
    private CacheConfiguration cacheConfig;
    private Cache cache;

    // to provide warning log messages when we run into timeouts we have to save the timeout
    private int blockingTimeout = -1;


    public String getCacheConfigurationName() {
        return cacheConfigurationName;
    }

    public void setCacheConfigurationName(String cacheConfigurationName) {
        this.cacheConfigurationName = cacheConfigurationName;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        CacheModule.getInstance().register(this);
        // modules are started *after* filters - so we have to force a call onCacheModuleStart() here
        onCacheModuleStart();
    }

    public void onCacheModuleStart() {
        if (cacheConfigurationName == null) {
            log.warn("The cacheConfigurationName property is not set for the {} CacheFilter, falling back to {}.", getName(), DEFAULT_CACHE_CONFIG);
            this.cacheConfigurationName = DEFAULT_CACHE_CONFIG;
        }

        final CacheModule cacheModule = getModule();
        this.cacheConfig = cacheModule.getConfiguration(cacheConfigurationName);
        this.cache = cacheModule.getCacheFactory().getCache(cacheConfigurationName);

        if(cache instanceof BlockingCache){
            blockingTimeout = ((BlockingCache)cache).getBlockingTimeout();
        }

        if (cacheConfig == null || cache == null) {
            log.error("The " + getName() + " CacheFilter is not properly configured, either cacheConfig(" + cacheConfig + ") or cache(" + cache + ") is null. Check if " + cacheConfigurationName + " is a valid cache configuration name. Will disable temporarily.");
            setEnabled(false);
        }

        monitor = CacheMonitor.getInstance();
    }

    protected CacheModule getModule() {
        return CacheModule.getInstance();
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        final CachePolicyResult cachePolicyResult = cacheConfig.getCachePolicy().shouldCache(cache, aggregationState, cacheConfig.getFlushPolicy());

        log.debug("Cache policy result: {}", cachePolicyResult);

        final CachePolicyResult.CachePolicyBehaviour behaviour = cachePolicyResult.getBehaviour();
        monitor.logBehavior(behaviour.getName());
        monitor.logAccess(cachePolicyResult.getCacheKey());
        final CachePolicyExecutor executor = cacheConfig.getExecutor(behaviour);
        if (executor == null) {
            throw new IllegalStateException("Unexpected cache policy result: " + cachePolicyResult);
        }

        try{
            final long start = System.currentTimeMillis();
            executor.processCacheRequest(request, response, chain, cache, cachePolicyResult);
            final long end = System.currentTimeMillis();

            if(blockingTimeout != -1 && (end-start) >= blockingTimeout){
                log.warn("The following URL took longer than {} seconds to render. This might cause timout exceptions on other requests to the same URI. [url={}], [key={}]", new Object[]{blockingTimeout/1000, request.getRequestURL(), cachePolicyResult.getCacheKey()});
            }
        }
        catch(LockTimeoutException timeout){
            log.warn("The following URL was blocked for longer than {} seconds. [url={}], [key={}]", new Object[]{blockingTimeout/1000, request.getRequestURL(), cachePolicyResult.getCacheKey()} );
            throw timeout;
        }
        catch (Throwable th) {
            if(cachePolicyResult.getBehaviour().equals(CachePolicyResult.store) && cache instanceof BlockingCache){
                log.error("A request started to cache but never put a cache entry into the blocking cache. This would block the cache key for ever. We are removing the cache entry manually. [url={}], [key={}]", new Object[]{request.getRequestURL(), cachePolicyResult.getCacheKey()});
                ((BlockingCache) cache).unlock(cachePolicyResult.getCacheKey());
            }
            throw new RuntimeException(th);
        }
    }

}
