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
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.CacheLifecycleListener;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicyExecutor;
import info.magnolia.module.cache.CachePolicyResult;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheFilter extends AbstractMgnlFilter implements CacheLifecycleListener {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CacheFilter.class);

    private static final String MODULE_NAME = "cache";

    private String cacheConfigurationName = "default";
    private CacheConfiguration cacheConfig;
    private Cache cache;

    public String getCacheConfiguration() {
        return cacheConfigurationName;
    }

    public void setCacheConfiguration(String cacheConfiguration) {
        this.cacheConfigurationName = cacheConfiguration;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        getModule().register(this);
        // modules are started *after* filters - so we have to force a call onCacheModuleStart() here
        onCacheModuleStart();
    }

    public void onCacheModuleStart() {
        final CacheModule cacheModule = getModule();
        this.cacheConfig = cacheModule.getConfiguration(cacheConfigurationName);
        this.cache = cacheModule.getCacheFactory().getCache(cacheConfigurationName);
    }

    // TODO : maybe this method could be generalized ...
    protected CacheModule getModule() {
        return (CacheModule) ModuleRegistry.Factory.getInstance().getModuleInstance(MODULE_NAME);
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (cacheConfig == null || cache == null) {
            throw new IllegalStateException("CacheFilter is not properly configured, either cacheConfig(" + cacheConfig + ") or cache(" + cache + ") is null.");
        }

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        final CachePolicyResult cachePolicy = cacheConfig.getCachePolicy().shouldCache(cache, aggregationState, cacheConfig.getFlushPolicy());
        log.debug("Cache policy result: {}", cachePolicy);

        final CachePolicyResult.CachePolicyBehaviour behaviour = cachePolicy.getBehaviour();
        CachePolicyExecutor executor = cacheConfig.getExecutor(behaviour.getExecutorName());
        if (executor == null) {
            throw new IllegalStateException("Unexpected cache policy result: " + cachePolicy);
        }
        executor.processCacheRequest(request, response, chain, cache, cachePolicy);
    }

}
