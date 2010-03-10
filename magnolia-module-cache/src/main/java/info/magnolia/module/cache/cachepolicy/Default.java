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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicy;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.CompositeCacheKey;
import info.magnolia.module.cache.FlushPolicy;
import info.magnolia.objectfactory.Components;
import info.magnolia.voting.voters.VoterSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;


/**
 * A basic CachePolicy which will drive the usage of the cache based on the fact that the element has already been
 * cached or not. It also supports a simple bypass list and voters. You can set the "multiplehosts" property to true if
 * cache entries must be based on the request hostname+uri rather than just the uri.
 * @author gjoseph
 * @version $Revision: 1821 $ ($Author: fgiust $)
 */
public class Default implements CachePolicy {

    private static final Logger log = LoggerFactory.getLogger(Default.class);

    private final I18nContentSupport i18nContentSupport = Components.getSingleton(I18nContentSupport.class);

    private VoterSet voters;

    public CachePolicyResult shouldCache(final Cache cache, final AggregationState aggregationState,
        final FlushPolicy flushPolicy) {
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
     * @param aggregationState
     * @param key
     * @return True if cache entry for the key should be recreated, false otherwise.
     */
    protected boolean shouldRefresh(AggregationState aggregationState, Object key) {
        String cacheControl = ((WebContext) MgnlContext.getInstance()).getRequest().getHeader("Cache-Control");
        // TODO: check for pragma as well?? RFC says "HTTP/1.1 caches SHOULD treat "Pragma:
        // no-cache" as if the client had sent "Cache-Control: no-cache"
        return cacheControl != null && cacheControl.equals("no-cache");
    }

    protected boolean shouldBypass(AggregationState aggregationState, Object key) {
        final String uri;
        if (key instanceof CompositeCacheKey) {
            uri = ((CompositeCacheKey) key).getUri();
        } else {
            uri = key.toString();
        }
        // true if voters vote positively
        return voters.vote(uri) <= 0;
    }

    public Object retrieveCacheKey(final AggregationState aggregationState) {
        // get original uri //TODO: check why original and not current?
        final String uri = aggregationState.getOriginalURI();

        //get serverName and request params and uuid of current content from WebContext/AggregationState
        // assuming "currentContent" is a page as there is no url to access paragraphs. However once there is, the uuid can easily point to a paragraph
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
        final String locale;
        if(i18nContentSupport!= null && i18nContentSupport.isEnabled()){
            locale = i18nContentSupport.getLocale().toString();
        } else {
            locale = null;
        }

        // create composite key so we can easily check each part of it later
        return new CompositeCacheKey(uri, serverName, locale, params);
    }

    public Object[] retrieveCacheKeys(final String uuid, final String repository) {
        final String uuidKey = repository + ":" + uuid;
        final List<Object> keys = new ArrayList<Object>();
        final CacheFactory factory = ModuleRegistry.Factory.getInstance().getModuleInstance(CacheModule.class).getCacheFactory();
        for (CacheConfiguration config : CacheModule.getInstance().getConfigurations().values()) {
            final Cache cache = factory.getCache(config.getName());
            final Multimap<String, CompositeCacheKey> multimap = CacheModule.getInstance().getUUIDKeyMapFromCacheSafely(cache);
            keys.addAll(multimap.get(uuidKey));
        }
        return keys.toArray();
    }

    public VoterSet getVoters() {
        return voters;
    }

    public void setVoters(VoterSet voters) {
        this.voters = voters;
    }
}
