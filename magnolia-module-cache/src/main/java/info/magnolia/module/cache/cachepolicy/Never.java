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
package info.magnolia.module.cache.cachepolicy;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CachePolicy;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.FlushPolicy;

/**
 * Cache policy instructing cache not to store the generated content.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class Never implements CachePolicy {
    private static final CachePolicyResult NEVER = new CachePolicyResult(CachePolicyResult.bypass, null, null);

    public CachePolicyResult shouldCache(final Cache cache, final AggregationState aggregationState, final FlushPolicy flushPolicy) {
        return NEVER;
    }

    public Object retrieveCacheKey(AggregationState aggregationState) {
        // there are no keys since we don't cache
        return null;
    }

    public Object[] retrieveCacheKeys(String uuid, String repository) {
        // there are no keys since we don't cache
        return null;
    }

    public void persistCacheKey(String repo, String uuid, Object key) {
        // do nothing
    }

    public Object[] removeCacheKeys(String uuid, String repository) {
        // do nothing
        return null;
    }
}
