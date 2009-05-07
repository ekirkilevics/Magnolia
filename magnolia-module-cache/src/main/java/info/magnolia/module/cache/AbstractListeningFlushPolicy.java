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

import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.module.ModuleRegistry;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractListeningFlushPolicy implements FlushPolicy {

    private static final Logger log = LoggerFactory.getLogger(AbstractListeningFlushPolicy.class);

    private List repositories = new ArrayList();
    private Map registeredListeners = new HashMap();

    /**
     * The repositories to which the listener is attached - upon any event on these,
     * the cache is cleared.
     */
    public List getRepositories() {
        return repositories;
    }

    public void setRepositories(List repositories) {
        this.repositories = repositories;
    }

    public void addRepository(String repository) {
        repositories.add(repository);
    }

    public void start(Cache cache) {
        for (Iterator iter = repositories.iterator(); iter.hasNext();) {
            final String repository = (String) iter.next();
            final CacheCleaner cacheCleaner = new CacheCleaner(cache, repository);
            final EventListener listener = ObservationUtil.instanciateDeferredEventListener(cacheCleaner, 5000, 30000);
            try {
                ObservationUtil.registerChangeListener(repository, "/", listener);
            } catch (Exception e) {
                log.warn("Failed to register cache flushing observation for repository {} due to {}", repository, e.getMessage());
                log.warn("Publishing any content to {} will not result in update of the cache. Please flush the cache manually.");
            }
            registeredListeners.put(repository, listener);
        }
    }

    public void stop(Cache cache) {
        final Iterator i = registeredListeners.keySet().iterator();
        while (i.hasNext()) {
            final String repository = (String) i.next();
            final EventListener listener = (EventListener) registeredListeners.get(repository);
            if (listener == null) {
                // happens on restart of cache module after someone configures new listener repository ... we are trying to stop the listener which was not created yet
                continue;
            }
            ObservationUtil.unregisterChangeListener(repository, listener);
        }
    }

    /**
     * Implement this method to react on buffered events on a given cache and repository.
     * @return true if single events should be processed as well, false otherwise.
     */
    protected abstract boolean preHandleEvents(Cache cache, String repository);

    /**
     * Implement this method to wrap up flushing process after all single events have been processed.
     * This method will be invoked only if {@link #preHandleEvents(Cache, String)} returns true;
     */
    protected abstract void postHandleEvents(Cache cache, String repository);


    /**
     * Implement this method to react on each and every event on a given cache and repository,
     * even if multiple where buffered.
     * This method will be invoked only if {@link #preHandleEvents(Cache, String)} returns true;
     */
    protected abstract void handleSingleEvent(Cache cache, String repository, Event event);

    /**
     * Flushes all content related to given uuid&repository combination from provided cache.
     * Note that more then only one pages can be flushed when this method is called.
     * @param uuid
     */
    protected void flushByUUID(String uuid, String repository, Cache cache) {
        CacheModule cacheModule = ((CacheModule) ModuleRegistry.Factory.getInstance().getModuleInstance("cache"));

            CacheConfiguration config = cacheModule.getConfiguration(cache.getName());
            Object[] cacheEntryKeys = config.getCachePolicy().retrieveCacheKeys(uuid, repository);
            log.debug("Flushing {} due to detected content update.", ToStringBuilder.reflectionToString(cacheEntryKeys));

            if (cacheEntryKeys == null || cacheEntryKeys.length == 0) {
                // nothing to remove
                return;
            }
            for (Object key : cacheEntryKeys) {
                if (log.isDebugEnabled()) {
                    // cache.hasElement() is blocking method, so don't call it unless really necessary
                    log.debug("In cache {} Found key {} :: {}", new Object[] {cache.getName(), key, "" + cache.hasElement(key)});
                }
                cache.put(key, null);
            }
            // we are done here
    }

    protected class CacheCleaner implements EventListener {
        private final Cache cache;
        private final String repository;

        public CacheCleaner(Cache cache, String repository) {
            this.cache = cache;
            this.repository = repository;
        }

        public void onEvent(EventIterator events) {
            boolean shouldContinue = preHandleEvents(cache, repository);
            if (shouldContinue) {
                while (events.hasNext()) {
                    final Event event = events.nextEvent();
                    handleSingleEvent(cache, repository, event);
                }
                postHandleEvents(cache, repository);
            }
        }
    }
}
