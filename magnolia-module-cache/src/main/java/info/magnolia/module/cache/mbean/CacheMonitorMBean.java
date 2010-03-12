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

import java.util.Map;

/**
 * @author had
 * @version $Id:$
 */
public interface CacheMonitorMBean {

    public Map<String, Integer> getAll();

    /**
     * Gets number of times since the last server restart the cache found and served the entry requested by client.
     * @return number of cache hits.
     */
    public int getHits();

    /**
     * Gets number of times since the last server restart the caching policy decided to bypass cache and serve request out of repository (i.e. for dynamic content).
     * @return number of cache bypasses.
     */
    public int getBypasses();

    /**
     * Gets number of times since the last server restart the cached entry for the request didn't exist and was put in the cache.
     * @return
     */
    public int getPuts();

    /**
     * Gets number of times Cache Module was stopped since the last server restart.
     * @return number of Cache Module stop() calls.
     */
    public int getStopCalls();

    /**
     * Gets number of times Cache Module was started since the last server restart.
     * @return number of Cache Module start() calls.
     */
    public int getStartCalls();

    /**
     * Gets number of times each configured cache have been flushed since the last server restart.
     * @return names of caches and number of times each of them have been completely flushed.
     */
    public Map<String, Integer> getFlushes();

    /**
     * Gets number of times the requests have been served for each configured domain since the last server restart.
     * If the name of the domain doesn't appear in the list, no request have been served for this domain since the restart, yet.
     * @return names of the domains and number of times the requests have been server for each of them.
     */
    public Map<String, Integer> getDomainAccesses();

    /**
     * Gets number of content uuids that are held in all known caches. There might be multiple entries per uuid in case multi-domain and/or multi-locale configurations are used.
     * @return number of unique content entries that are held in cache.
     */
    public int getCachedUUIDsCount();

    /**
     * Gets number of entries in all known caches even if those entries have been generated from content with same uuid (e.g. multiple language versions or multiple domain versions).
     * @return number of entries held by all caches.
     */
    public int getCachedKeysCount();

    /**
     * Will flush all entries from all configured caches.
     */
    public void flushAll() throws Exception;

    /**
     * Will flush all entries with key bound to given uuid from all configured caches. In multi domain and multi locale environments, it will flush all domain and language variations of the  page with given UUID from all caches.
     */
    public void flushByUUID(String repository, String uuid) throws Exception;
}