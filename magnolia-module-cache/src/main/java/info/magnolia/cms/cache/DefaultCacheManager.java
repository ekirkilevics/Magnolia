/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.cache.noop.NoOpCache;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.voting.Voting;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;


/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 *
 * @deprecated since 3.6, see the info.magnolia.module.cache package.
 */
public class DefaultCacheManager extends BaseCacheManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultCacheManager.class);

    private static final Cache NOOP_CACHE = new NoOpCache();

    private Cache cache = NOOP_CACHE;

    /**
     * @see info.magnolia.cms.cache.CacheManager#getCacheKey(javax.servlet.http.HttpServletRequest) TODO : we should
     * probably use the AggregationState instead of the HttpServletRequest
     */
    public String getCacheKey(HttpServletRequest request) {
        return MgnlContext.getAggregationState().getOriginalURI();
    }

    /**
     * In case we the voting is negative the request is not cached. Otherwise the defined deny or allow uri are checked.
     */
    public boolean isCacheable(HttpServletRequest request) {
        if (Voting.Factory.getDefaultVoting().vote(voters, request) < 0) {
            return false;
        }
        return getConfig().isUriCacheable(request);
    }

    /**
     * @see info.magnolia.cms.cache.CacheManager#canCompress(javax.servlet.http.HttpServletRequest)
     */
    public boolean canCompress(HttpServletRequest request) {

        String extension = StringUtils.substringAfter(request.getRequestURI(), ".");
        return getConfig().canCompress(extension);
    }

    public boolean cacheRequest(String key, CacheableEntry entry, boolean canCompress) {
        cache.cacheRequest(key, entry, canCompress);
        return true;
    }

    protected void doFlushAll() {
        this.cache.flush();
    }

    protected long doGetCreationTime(String key) {
        return this.cache.getCreationTime(key);
    }

    protected void doStart() {
        try {
            CacheConfig config = getConfig();

            Cache cache = newCache(config.getCacheImplementation());
            cache.start(config);

            this.cache = cache;

            registerObservation(config);
        }
        catch (ConfigurationException e) {
            // FIXME
        }
    }

    protected void doStop() {
        this.cache.stop();
        this.cache = NOOP_CACHE;
    }

    protected boolean doStreamFromCache(String key, HttpServletResponse response, boolean canCompress) {
        if (!isRunning()) {
            return false;
        }

        boolean didUseCache = this.cache.streamFromCache(key, response, canCompress);

        if (log.isDebugEnabled()) {
            if (didUseCache) {
                log.debug("Used cache for: '{}'.", key);
            }
            else {
                log.debug("Not found in cache: '{}'.", key);
            }
        }

        return didUseCache;
    }

    protected Cache getCache() {
        return this.cache;
    }

    private boolean isCached(String request) {
        return this.cache.isCached(request);
    }

    private Cache newCache(String cacheImplementation) throws ConfigurationException {
        try {
            return (Cache) ClassUtil.newInstance(cacheImplementation);
        }
        catch (ClassNotFoundException e) {
            throw new ConfigurationException("Cache class not found!", e);
        }
        catch (InstantiationException e) {
            throw new ConfigurationException("Could not instantiate Cache class!", e);
        }
        catch (IllegalAccessException e) {
            throw new ConfigurationException("Could not access Cache class!", e);
        }
        catch (ClassCastException e) {
            throw new ConfigurationException("Class does not implement Cache!", e);
        }
    }

    /**
     * override this to register different workspaces
     */
    protected void registerObservation(CacheConfig config) {
        for (Iterator iter = config.getRepositories().iterator(); iter.hasNext();) {
            String repository = (String) iter.next();
            ObservationUtil.registerDeferredChangeListener(repository, "/", new EventListener() {
                public void onEvent(EventIterator events) {
                    handleChangeEvents(events);
                }
            }, 5000, 30000);
        }
    }

    /**
     * Called based on the observation. The default implementation flushes the whole cache.
     */
    protected void handleChangeEvents(EventIterator events) {
        flushAll();
    }

}
