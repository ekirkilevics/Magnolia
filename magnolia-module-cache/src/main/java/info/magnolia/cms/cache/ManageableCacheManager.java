/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.MBeanUtil;


import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>CacheManager</code> that is JMX manageable.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public class ManageableCacheManager implements CacheManager, ManageableCacheManagerMBean {

    public static final Logger log = LoggerFactory.getLogger(ManageableCacheManager.class);

    private int cacheHits;

    private final CacheManager cacheManager;

    private int cacheMisses;

    private int cachePuts;

    public ManageableCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public boolean cacheRequest(String key, CacheableEntry entry, boolean canCompress) {
        boolean didCache = this.cacheManager.cacheRequest(key, entry, canCompress);

        if (didCache) {
            this.cachePuts++;
        }

        return didCache;
    }

    public void flushAll() {
        this.cacheManager.flushAll();
    }

    public int getCacheHits() {
        return this.cacheHits;
    }

    public int getCacheMisses() {
        return this.cacheMisses;
    }

    public int getCachePuts() {
        return this.cachePuts;
    }

    public long getCreationTime(String request) {
        return this.cacheManager.getCreationTime(request);
    }

    public void init(Content content) throws ConfigurationException {
        this.cacheManager.init(content);
        registerMBean();
    }

    public boolean isEnabled() {
        return this.cacheManager.isEnabled();
    }

    public boolean isPaused() {
        return this.cacheManager.isPaused();
    }

    public boolean isRunning() {
        return this.cacheManager.isRunning();
    }

    public boolean isStarted() {
        return this.cacheManager.isStarted();
    }

    public void pause() {
        this.cacheManager.pause();
    }

    public void resetStatistics() {
        this.cacheHits = 0;
        this.cacheMisses = 0;
        this.cachePuts = 0;
    }

    public void restart() {
        this.cacheManager.restart();
    }

    public void resume() {
        this.cacheManager.resume();
    }

    public void start() {
        this.cacheManager.start();
    }

    public void stop() {
        this.cacheManager.stop();
    }

    public boolean streamFromCache(HttpServletRequest request, ServletResponse response) {
        return false;
    }

    /**
     * @see info.magnolia.cms.cache.CacheManager#isCacheable(javax.servlet.http.HttpServletRequest)
     */
    public boolean isCacheable(HttpServletRequest request) {
        return this.cacheManager.isCacheable(request);
    }

    /**
     * @see info.magnolia.cms.cache.CacheManager#canCompress(javax.servlet.http.HttpServletRequest)
     */
    public boolean canCompress(HttpServletRequest request) {
        return this.cacheManager.canCompress(request);
    }

    public String getCacheKey(HttpServletRequest request) {
        return this.cacheManager.getCacheKey(request);
    }

    public boolean streamFromCache(String key, HttpServletResponse response, boolean canCompress) {
        boolean didUseCache = this.cacheManager.streamFromCache(key, response, canCompress);

        if (didUseCache) {
            this.cacheHits++;
        }
        else if (isRunning()) {
            this.cacheMisses++;
        }

        return didUseCache;
    }

    private void registerMBean() {
        String name = "CacheManager";

        MBeanUtil.registerMBean(name, this);
    }
}
