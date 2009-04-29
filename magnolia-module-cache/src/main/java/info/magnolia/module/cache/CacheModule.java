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
package info.magnolia.module.cache;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CacheModule holds several named CacheConfiguration instances and a CacheFactory.
 * @see CacheConfiguration
 * @see CacheFactory
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheModule implements ModuleLifecycle {
    private static final Logger log = LoggerFactory.getLogger(CacheModule.class);

    private final Set<CacheModuleLifecycleListener> listeners = new HashSet<CacheModuleLifecycleListener>();
    private CacheFactory cacheFactory;
    private Map<String, CacheConfiguration> configurations = new HashMap<String, CacheConfiguration>();
    private ContentCompression compression;

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
//        if (moduleLifecycleContext.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_STARTUP) {
        cacheFactory.start();
//        }

        final Iterator<CacheConfiguration> it = configurations.values().iterator();
        while (it.hasNext()) {
            final CacheConfiguration cfg = it.next();
            final String name = cfg.getName();
            final Cache cache = cacheFactory.getCache(name);
            if (cfg.getFlushPolicy() != null) {
                cfg.getFlushPolicy().start(cache);
            } else {
                log.warn("Flush Policy is not configured properly for {} cache configuration.", name);
            }
        }

        final Iterator<CacheModuleLifecycleListener> itL = listeners.iterator();
        while (itL.hasNext()) {
            final CacheModuleLifecycleListener listener = itL.next();
            listener.onCacheModuleStart();
        }
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        final Iterator<CacheConfiguration> it = configurations.values().iterator();
        while (it.hasNext()) {
            final CacheConfiguration cfg = it.next();
            final String name = cfg.getName();
            final Cache cache = cacheFactory.getCache(name);
            if (cfg.getFlushPolicy() != null) {
                cfg.getFlushPolicy().stop(cache);
            } else {
                log.warn("Flush Policy is not configured properly for {} cache configuration.", name);
            }
        }

        // TODO : this is implementation dependant - some factories might need or want to be notified also on restart..
        // TODO : there was a reason for this checking  of the phase, but i can't remember ...
//        if (moduleLifecycleContext.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_SHUTDOWN) {
        cacheFactory.stop();
//        }
    }
}
