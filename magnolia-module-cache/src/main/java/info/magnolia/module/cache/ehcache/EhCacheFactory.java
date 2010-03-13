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
package info.magnolia.module.cache.ehcache;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.MBeanUtil;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheFactory;

import java.util.Arrays;
import java.util.List;

import javax.management.MBeanServer;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.management.ManagementService;

/**
 * A CacheFactory based on ehcache, which wraps BlockingCache instances.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EhCacheFactory implements CacheFactory {
    private CacheManager cacheManager;
    private CacheConfiguration defaultCacheConfiguration;
    private String diskStorePath;

    public EhCacheFactory() {
        diskStorePath = Path.getCacheDirectoryPath();
    }

    public CacheConfiguration getDefaultCacheConfiguration() {
        return defaultCacheConfiguration;
    }

    public void setDefaultCacheConfiguration(CacheConfiguration defaultCacheConfiguration) {
        this.defaultCacheConfiguration = defaultCacheConfiguration;
    }

    public String getDiskStorePath() {
        return diskStorePath;
    }

    public void setDiskStorePath(String diskStorePath) {
        this.diskStorePath = diskStorePath;
    }

    public void start() {
        final Configuration cfg = ConfigurationFactory.parseConfiguration();
        cfg.setSource("ehcache defaults");
        if (defaultCacheConfiguration != null) {
            cfg.setDefaultCacheConfiguration(defaultCacheConfiguration);
            cfg.setSource(cfg.getConfigurationSource() + " + Magnolia-based defaultCacheConfiguration");
        }
        if (diskStorePath != null) {
            cfg.getDiskStoreConfiguration().setPath(diskStorePath);
            cfg.setSource(cfg.getConfigurationSource() + " + Magnolia-based diskStorePath");
        }
        cacheManager = new CacheManager(cfg);

        // TODO cacheManager.setName(...magnolia instance name ...);

        final MBeanServer mBeanServer = MBeanUtil.getMBeanServer();
        ManagementService.registerMBeans(cacheManager, mBeanServer, true, true, true, true);
    }

    public List getCacheNames() {
        return Arrays.asList(cacheManager.getCacheNames());
    }

    public Cache getCache(String name) {
        final Ehcache ehcache = cacheManager.getEhcache(name);
        if (ehcache == null) {
            createCache(name);
            return getCache(name);
        }
        return new EhCacheWrapper(ehcache, name);
    }

    protected void createCache(String name) {
        synchronized (this.getClass()) {
            cacheManager.addCache(name);

            final Ehcache cache = cacheManager.getEhcache(name);
            if (!(cache instanceof BlockingCache)) {
                final BlockingCache newBlockingCache = new BlockingCache(cache);
                cacheManager.replaceCacheWithDecoratedCache(cache, newBlockingCache);
            }
        }
    }

    public void stop() {
        cacheManager.shutdown();
    }

    public CacheManager getWrappedCacheManager() {
        return cacheManager;
    }
}
