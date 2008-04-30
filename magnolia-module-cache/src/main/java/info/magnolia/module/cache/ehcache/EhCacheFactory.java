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
package info.magnolia.module.cache.ehcache;

import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheFactory;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.blocking.BlockingCache;

import java.util.Arrays;
import java.util.List;

/**
 * A CacheFactory based on ehcache, which wraps BlockingCache instances.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EhCacheFactory implements CacheFactory {
    private final CacheManager cacheManager;

    public EhCacheFactory() {
        // TODO : pass a net.sf.ehcache.config.Configuration instance as populated by content2bean
        cacheManager = new CacheManager();
    }

    public Cache newCache(String name) {
        synchronized (this.getClass()) {
            cacheManager.addCache(name);

            final Ehcache cache = cacheManager.getEhcache(name);
            if (!(cache instanceof BlockingCache)) {
                final BlockingCache newBlockingCache = new BlockingCache(cache);
                cacheManager.replaceCacheWithDecoratedCache(cache, newBlockingCache);
            }
        }
        return getCache(name);
    }

    public List getCacheNames() {
        return Arrays.asList(cacheManager.getCacheNames());
    }

    public Cache getCache(String name) {
        final Ehcache ehcache = cacheManager.getEhcache(name);
        if (ehcache == null) {
            throw new IllegalArgumentException("No cache with name " + name);
        }
        return new EhCacheWrapper(ehcache);
    }

    public void stop() {
        cacheManager.shutdown();
    }

    public CacheManager getWrappedCacheManager() {
        return cacheManager;
    }
}
