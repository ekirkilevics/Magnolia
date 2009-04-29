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
     *
     * TODO : check how to handle request parameters
     */
    CachePolicyResult shouldCache(final Cache cache, final AggregationState aggregationState, final FlushPolicy flushPolicy);

    /**
     * Returns cache key for the given item or null if such key can't be created or policy doesn't want to share it.
     */
    Object getCacheKey(final AggregationState aggregationState);


    /**
     * Returns cache key for the given item or null if such key can't be created or policy doesn't want to share it.
     */
    Object getCacheKey(final String uuid, final String repository);
}
