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
package info.magnolia.module.cache.cachepolicy;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CachePolicy;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.FlushPolicy;
import info.magnolia.voting.voters.VoterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic CachePolicy which will drive the usage of the cache
 * based on the fact that the element has already been cached
 * or not. It also supports a simple bypass list and voters.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class Default implements CachePolicy {

    private static final Logger log = LoggerFactory.getLogger(Default.class);

    private VoterSet voters;

    public CachePolicyResult shouldCache(final Cache cache, final AggregationState aggregationState, final FlushPolicy flushPolicy) {
        final Object key = getCacheKey(aggregationState);

        if (shouldBypass(aggregationState, key)) {
            return new CachePolicyResult(CachePolicyResult.bypass, key, null);
        }

        if (shouldRefresh(aggregationState, key)) {
            log.debug("Cache refresh requested for {}", key);
            return new CachePolicyResult(CachePolicyResult.store, key, null);
        }

        // we need to synchronize on the cache instance, as multiple threads might be accessing this
        // concurrently, and we don't want to block the system if we're using a blocking cache.
        // (since hasElement() might place a mutex on the cache key)
        synchronized (cache) {
            if (cache.hasElement(key)) {
                final Object cachedEntry = cache.get(key);
                return new CachePolicyResult(CachePolicyResult.useCache, key, cachedEntry);
            } else {
                return new CachePolicyResult(CachePolicyResult.store, key, null);
            }
        }
    }

    /**
     * Checks whether reuqested content should be served from cache or refreshed instead.
     * @param aggregationState
     * @param key
     * @return True if cache entry for the key should be recreated, false otherwise.
     */
    protected boolean shouldRefresh(AggregationState aggregationState, Object key) {
        String cacheControl = ((WebContext) MgnlContext.getInstance()).getRequest().getHeader("Cache-Control");
        // TODO: check for pragma as well?? RFC says "HTTP/1.1 caches SHOULD treat "Pragma: no-cache" as if the client had sent "Cache-Control: no-cache"
        return cacheControl != null && cacheControl.equals("no-cache");
    }

    protected boolean shouldBypass(AggregationState aggregationState, Object key) {
        final String uri = (String) key;
        // true if voters vote positively
        return voters.vote(uri) <= 0;
    }

    protected Object getCacheKey(final AggregationState aggregationState) {
        return aggregationState.getOriginalURI();
    }

    public VoterSet getVoters() {
        return voters;
    }

    public void setVoters(VoterSet voters) {
        this.voters = voters;
    }
}
