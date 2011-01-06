/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.module.cache;

import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.cache.mbean.CacheMonitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The CacheModule holds several named CacheConfiguration instances and a CacheFactory.
 * @see CacheConfiguration
 * @see CacheFactory
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheModule implements ModuleLifecycle {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CacheModule.class);

    private final Set<CacheModuleLifecycleListener> listeners = new HashSet<CacheModuleLifecycleListener>();
    private CacheFactory cacheFactory;
    private Map<String, CacheConfiguration> configurations = new HashMap<String, CacheConfiguration>();
    private ContentCompression compression;
    private static final CacheMonitor cacheMonitor = CacheMonitor.getInstance();

    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }

    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    public Map<String, CacheConfiguration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<String, CacheConfiguration> configurations) {
        this.configurations = configurations;
    }

    public void addConfiguration(String name, CacheConfiguration config) {
        configurations.put(name, config);
    }

    public CacheConfiguration getConfiguration(String name) {
        return configurations.get(name);
    }

    public void setCompression(ContentCompression compression) {
        this.compression = compression;
    }

    public ContentCompression getCompression() {
        return compression;
    }

    public void register(CacheModuleLifecycleListener listener) {
        listeners.add(listener);
    }

    // TODO : i still feel like we should separate module config bean and lifecycle
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        // TODO : this is implementation dependent - some factories might need or want to be notified also on restart..
        // if (moduleLifecycleContext.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_STARTUP) {
        cacheFactory.start();
        // }

        cacheMonitor.countStart();

        for (CacheConfiguration cfg : configurations.values()) {
            final String name = cfg.getName();
            final Cache cache = cacheFactory.getCache(name);
            cacheMonitor.addCache(name);
            if (cfg.getFlushPolicy() != null) {
                cfg.getFlushPolicy().start(cache);
            } else {
                log.warn("Flush Policy is not configured properly for {} cache configuration.", name);
            }
        }

        for (CacheModuleLifecycleListener listener : listeners) {
            listener.onCacheModuleStart();
        }

        // if we're starting up the system, flush caches if we the system has just been installed or updated
        if (moduleLifecycleContext.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_STARTUP) {
            final InstallContext installContext = ModuleManager.Factory.getInstance().getInstallContext();
            // InstallContext.status is set by ModuleManagerImpl.performInstallOrUpdate()
            if (installContext.getStatus() != null) {
                log.info("Flushing all caches ...");
                for (CacheConfiguration config : configurations.values()) {
                    cacheFactory.getCache(config.getName()).clear();
                    log.info("  flushed cache: {}", config.getName());
                }
            }
        }
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        cacheMonitor.stop();
        for (CacheConfiguration cfg : configurations.values()) {
            final String name = cfg.getName();
            Cache cache = null;
            try {
                cache = cacheFactory.getCache(name);
            } catch (IllegalStateException e) {
                log.warn("Cache {} is not running anymore. Check your configuration and log files to find out if there were any errors forcing cache shutdown.", name);
            }
            if (cfg.getFlushPolicy() != null) {
                cfg.getFlushPolicy().stop(cache);
            } else {
                log.warn("Flush Policy is not configured properly for {} cache configuration.", name);
            }
        }

        // TODO : this is implementation dependent - some factories might need or want to be notified also on restart..
        // TODO : there was a reason for this checking  of the phase, but i can't remember ...
        // if (moduleLifecycleContext.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_SHUTDOWN) {
        cacheFactory.stop();
        // }
    }

    public static CacheModule getInstance() {
        return ModuleRegistry.Factory.getInstance().getModuleInstance(CacheModule.class);
    }
}
