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
package info.magnolia.cms.cache.noop;

import info.magnolia.cms.cache.Cache;
import info.magnolia.cms.cache.CacheConfig;
import info.magnolia.cms.cache.CacheableEntry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A <code>Cache</code> implementation that does nothing.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public class NoOpCache implements Cache {

    /**
     * Does nothing.
     */
    public void cacheRequest(HttpServletRequest request) {
    }

    /**
     * Does nothing.
     */
    public void flush() {
    }

    /**
     * Does nothing.
     * @return <code>Cache.UNKNOWN_CREATION_TIME</code>
     */
    public long getCreationTime(HttpServletRequest request) {
        return Cache.UNKNOWN_CREATION_TIME;
    }

    /**
     * Does nothing.
     */
    public void start(CacheConfig config) {
    }

    /**
     * Does nothing.
     * @return <code>false</code>
     */
    public boolean isCached(String request) {
        return false;
    }

    /**
     * Does nothing.
     */
    public void stop() {
    }

    /**
     * Does nothing.
     * @return <code>false</code>
     */
    public boolean streamFromCache(HttpServletRequest request, HttpServletResponse response, boolean canCompress) {
        return false;
    }

    /**
     * @see info.magnolia.cms.cache.Cache#cacheRequest(info.magnolia.cms.cache.CacheKey,
     * info.magnolia.cms.cache.CacheableEntry, boolean)
     */
    public void cacheRequest(String key, CacheableEntry out, boolean canCompress) {

    }

    /**
     * @see info.magnolia.cms.cache.Cache#getCreationTime(info.magnolia.cms.cache.CacheKey)
     */
    public long getCreationTime(String request) {
        return 0;
    }

    /**
     * @see info.magnolia.cms.cache.Cache#streamFromCache(info.magnolia.cms.cache.CacheKey,
     * javax.servlet.http.HttpServletResponse, boolean)
     */
    public boolean streamFromCache(String key, HttpServletResponse response, boolean canCompress) {
        return false;
    }

    /**
     * @see info.magnolia.cms.cache.Cache#flushEntry(info.magnolia.cms.cache.CacheKey)
     */
    public void remove(String key) {

    }

}
