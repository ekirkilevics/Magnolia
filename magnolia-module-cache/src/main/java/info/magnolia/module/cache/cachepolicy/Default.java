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
package info.magnolia.module.cache.cachepolicy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicy;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.DefaultCacheKey;
import info.magnolia.module.cache.FlushPolicy;
import info.magnolia.voting.voters.VoterSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic CachePolicy driven by voters. This policy implementation uses
 * {@link info.magnolia.module.cache.DefaultCacheKey} to identify each cache entry.
 *
 * @author gjoseph
 * @version $Revision: 1821 $ ($Author: fgiust $)
 */
public class Default implements CachePolicy {

    public static final String UUID_KEY_MAP_KEY = "uuid-key-mapping";
    private static final Logger log = LoggerFactory.getLogger(Default.class);

    private VoterSet voters;
    
    private boolean refreshOnNoCacheRequests = false;

    public CachePolicyResult shouldCache(final Cache cache, final AggregationState aggregationState, final FlushPolicy flushPolicy) {
        final Object key = retrieveCacheKey(aggregationState);

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
            }
            else {
                return new CachePolicyResult(CachePolicyResult.store, key, null);
            }
        }
    }

    /**
     * Checks whether requested content should be served from cache or refreshed instead.
     * @return True if cache entry for the key should be recreated, false otherwise.
     */
    protected boolean shouldRefresh(AggregationState aggregationState, Object key) {
        if(isRefreshOnNoCacheRequests()){
            String cacheControl = ((WebContext) MgnlContext.getInstance()).getRequest().getHeader("Cache-Control");
            // TODO: check for pragma as well?? RFC says "HTTP/1.1 caches SHOULD treat "Pragma:
            // no-cache" as if the client had sent "Cache-Control: no-cache"
            return cacheControl != null && cacheControl.equals("no-cache");
        }
        return false;
    }

    protected boolean shouldBypass(AggregationState aggregationState, Object key) {
        final String uri;
        if (key instanceof DefaultCacheKey) {
            uri = ((DefaultCacheKey) key).getUri();
        } else {
            uri = key.toString();
        }
        // true if voters vote positively
        return voters.vote(uri) <= 0;
    }

    public Object retrieveCacheKey(final AggregationState aggregationState) {
        // get original URI - not using current URI since we want to cache original URIs, not those we forward to (parameters in virtual URIs, i18n, ...)
        final String uri = aggregationState.getOriginalURI();

        // get serverName and request params and from WebContext
        final String serverName;
        final Map<String, String> params;
        if (MgnlContext.isWebContext()) {
            serverName = MgnlContext.getWebContext().getRequest().getServerName();
            params = MgnlContext.getWebContext().getParameters();
        } else {
            serverName = null;
            params = null;
        }

        // get locale
        final String localeStr;
        final Locale locale = aggregationState.getLocale();
        if(locale != null){
            localeStr = locale.toString();
         } else {
            localeStr = null;
         }

        // create composite key so we can easily check each part of it later
        return new DefaultCacheKey(uri, serverName, localeStr, params);
    }

    public Object[] retrieveCacheKeys(final String uuid, final String repository) {
        final String uuidKey = repository + ":" + uuid;
        final Set<Object> keys = getUUIDKeySetFromCacheSafely(uuidKey);
        return keys.toArray();
    }

    public void persistCacheKey(final String repo, final String uuid, final Object key) {
        final String uuidKey = repo + ":" + uuid;
        final Set<Object> uuidToCacheKeyMapping = getUUIDKeySetFromCacheSafely(uuidKey);
        uuidToCacheKeyMapping.add(key);
    }

    public VoterSet getVoters() {
        return voters;
    }

    public void setVoters(VoterSet voters) {
        this.voters = voters;
    }

    public Object[] removeCacheKeys(final String uuid, final String repository) {
        final String uuidKey = repository + ":" + uuid;
        final Set keys = getUUIDKeySetFromCacheSafely(uuidKey);
        getUuidKeyCache().remove(uuidKey);
        return keys.toArray();
    }

    private Cache getUuidKeyCache() {
        final CacheFactory factory = ModuleRegistry.Factory.getInstance().getModuleInstance(CacheModule.class).getCacheFactory();
        return factory.getCache(UUID_KEY_MAP_KEY);
    }

    /**
     * Method to safely (without danger of blocking cache) obtain persistent mapping between UUIDs and cache keys.
     */
    private synchronized Set<Object> getUUIDKeySetFromCacheSafely(String uuidKey) {
        final Cache cache = getUuidKeyCache();
        synchronized (cache) {
            Set<Object> keys = (Set<Object>) cache.get(uuidKey);
            if (keys == null) {
                keys = Collections.synchronizedSet(new HashSet<Object>());
                cache.put(uuidKey, keys);
            }
            return keys;
        }
    }
    
    public boolean isRefreshOnNoCacheRequests() {
        return this.refreshOnNoCacheRequests;
    }
    
    public void setRefreshOnNoCacheRequests(boolean allowNoCacheHeader) {
        this.refreshOnNoCacheRequests = allowNoCacheHeader;
    }
}
