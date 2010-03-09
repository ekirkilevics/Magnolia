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
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CompositeCacheKey;

import java.util.HashMap;
import java.util.Map;

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

    // mbean exposed methods

    public void flush() {
        CacheFactory factory = ModuleRegistry.Factory.getInstance().getModuleInstance(CacheModule.class).getCacheFactory();
        for (String name : caches.keySet()) {
            factory.getCache(name).clear();
        }
    }

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
}
