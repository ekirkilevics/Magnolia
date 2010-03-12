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

import info.magnolia.cms.core.AggregationState;

/**
 * The CachePolicy determines is a requested page should be cached,
 * retrieved from the cache or not cached at all.
 * It is called for every request and should thus also take care of
 * any expiration policy - i.e if the page should be recached.
 *
 * The CacheFilter (or any other client component) can determine its
 * behaviour based on the return CachePolicyResult, which holds both
 * the behaviour to take and the cache key to use when appropriate.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface CachePolicy {

    /**
     * Implementations can chose whether to cache or not - but note that the
     * aggregationState might not be completely populated. Every request should be
     * cacheable, not only those processed through Magnolia's RenderingFilter.
     */
    CachePolicyResult shouldCache(final Cache cache, final AggregationState aggregationState, final FlushPolicy flushPolicy);

    /**
     * Returns cache key for the given item or null if such key can't be obtained or policy doesn't want to share it.
     * When using aggregationState, key is expected to be composed with aggregated request in mind and policy has to choose only
     * one such key denoting item to return to the client, if such an item indeed exists and can be served.
     * When not existing key might be used to store the cache entry. Since only one version of the content can be streamed back to
     * the client, it makes sense to create only one cache entry for the such a key as well.
     */
    Object retrieveCacheKey(final AggregationState aggregationState);


    /**
     * Returns cache keys for the given item or null if such keys can't be obtained or policy doesn't want to share it. Since in
     * this case uuid is used to retrieve the cache key, it is quite possible that multiple different representations of the content
     * denoted by uuid exist and all such keys should be returned leaving it up to requesting object to deal with such a multiplicity.
     * Please note that returned keys might not be necessary multiple representations of the content denoted by provided uuid, but
     * also quite possibly all the content deemed related or partially used to construct any of the returned cache keys.
     */
    Object[] retrieveCacheKeys(final String uuid, final String repository);

    /**
     * Presists mapping between uuid and cache key in case the given cache policy implementation cares about such details.
     */
    void persistCacheKey(String repo, String uuid, Object key);

    /**
     * Returns cache keys for the given item or null if such keys can't be obtained or policy doesn't want to share it. Since in
     * this case uuid is used to retrieve the cache key, it is quite possible that multiple different representations of the content
     * denoted by uuid exist and all such keys should be returned leaving it up to requesting object to deal with such a multiplicity.
     * Please note that returned keys might not be necessary multiple representations of the content denoted by provided uuid, but
     * also quite possibly all the content deemed related or partially used to construct any of the returned cache keys.
     * At the call to this method, cache policy should not only return the keys associated with the uuid, but also remove all returned uuid-cahce key mappings.
     */
    Object[] removeCacheKeys(String uuid, String repository);
}
