/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.cache.mbean;

import info.magnolia.cms.util.MBeanUtil;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CompositeCacheKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;

/**
 * @author had
 * @version $Id:$
 */
public class MgnlCacheStats implements MgnlCacheStatsMBean {

    private static MgnlCacheStats instance = new MgnlCacheStats();
    private int start;
    private int stop;
    private Map<String, Integer> calls = new HashMap<String, Integer>();
    private Map<String, Integer> caches = new HashMap<String, Integer>();
    private Map<String, Integer> domains = new HashMap<String, Integer>();

    private CacheFactory getCacheFactory() {
        CacheFactory factory = ModuleRegistry.Factory.getInstance().getModuleInstance(CacheModule.class).getCacheFactory();
        return factory;
    }


    public MgnlCacheStats() {
        MBeanUtil.registerMBean("MgnlCacheStats", this);
    }

    public static MgnlCacheStats getInstance() {
        return instance;
    }

    public void countStart() {
        start++;
    }

    public void stop() {
        stop++;
    }

    /**
     * @param name
     */
    public void logBehavior(String name) {
        Integer count = calls.get(name);
        calls.put(name, count == null ? 1 : ++count);
    }
    public void countFlush(String name) {
        caches.put(name, caches.get(name) + 1);
    }

    public void addCache(String name) {
        caches.put(name, 0);
    }

    public void logAccess(Object cacheKey) {
        if (cacheKey == null || !(cacheKey instanceof CompositeCacheKey)) {
            return;
        }
        CompositeCacheKey key = (CompositeCacheKey) cacheKey;
        Integer count = this.domains.get(key.getDomain());
        this.domains.put(key.getDomain(), count == null ? 1 : ++count);
    }

    // mbean exposed operations
    public void flush() {
        CacheFactory factory = getCacheFactory();
        for (String name : caches.keySet()) {
            factory.getCache(name).clear();
        }
    }

    public void flushByUUID(String repository, String uuid) {
        final String uuidKey = repository + ":" + uuid;
        final Set<CompositeCacheKey> set = new HashSet<CompositeCacheKey>();
        final CacheFactory factory = getCacheFactory();
        final Set<String> cacheNames = caches.keySet();

        // retrieve keys from all caches
        for (String name : cacheNames) {
            final Cache cache = factory.getCache(name);
            final Multimap<String, CompositeCacheKey> multimap = CacheModule.getInstance().getUUIDKeyMapFromCacheSafely(cache);
            set.addAll(multimap.get(uuidKey));
        }

        // flush each key from all caches
        for (Object key : set) {
            for (String name : cacheNames) {
                final Cache cache = factory.getCache(name);
                cache.remove(key);
            }
        }
    }

    // mbean exposed attributes
    public Map<String, Integer> getAll() {
        return calls;
    }

    public int getHits() {
        return calls.get("useCache");
    }

    public int getBypasses() {
        return calls.get("bypass");
    }

    public int getPuts() {
        return calls.get("store");
    }

    public int getStopCalls() {
        return stop;
    }

    public int getStartCalls() {
        return start;
    }

    public Map<String, Integer> getFlushes() {
        return caches;
    }

    public Map<String, Integer> getDomainAccesses() {
        return domains;
    }

    public int getCachedKeysCount() {
        // there's most likely gonna be ever just one map in the default cache, but let's not assume that and search all configured caches
        int count = 0;
        CacheFactory factory = getCacheFactory();
        for (String name : caches.keySet()) {
            Cache cache = factory.getCache(name);
            Multimap<String, CompositeCacheKey> multimap = CacheModule.getInstance().getUUIDKeyMapFromCacheSafely(cache);
            count += multimap.keys().size();
        }
        return count;
    }

    public int getCachedUUIDsCount() {
        // there's most likely gonna be ever just one map in the default cache, but let's not assume that and search all configured caches
        Set set = new HashSet();
        CacheFactory factory = getCacheFactory();
        for (String name : caches.keySet()) {
            Cache cache = factory.getCache(name);
            Multimap<String, CompositeCacheKey> multimap = CacheModule.getInstance().getUUIDKeyMapFromCacheSafely(cache);
            set.addAll(multimap.keySet());
        }
        return set.size();
    }
}
