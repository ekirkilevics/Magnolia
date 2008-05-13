/**
 * This file Copyright (c) 2008 Magnolia International
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
package info.magnolia.module.cache;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.voting.Voter;
import info.magnolia.voting.Voting;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

/**
 * A simplistic CachePolicy which will simply direct the usage
 * of the cache is the element has already been cached or not.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DefaultCachePolicy implements CachePolicy {
    private List bypasses = new LinkedList();
    private Voter[] voters = new Voter[0];

    public List getBypasses() {
        return bypasses;
    }

    public void addBypass(String bypass) {
        bypasses.add(bypass);
    }

    public Voter[] getVoters() {
        return voters;
    }

    public void addVoter(Voter voter) {
        voters = (Voter[]) ArrayUtils.add(this.voters, voter);
    }

    public CachePolicyResult shouldCache(final Cache cache, final AggregationState aggregationState) {
        final Object key = getCacheKey(aggregationState);

        if (shouldBypass(aggregationState, key)) {
            return new CachePolicyResult(CachePolicyResult.bypass, key, null);
        }

        // we need to synchronize on the cache instance, as multiple threads might be accessing this
        // concurrently, and we don't want to block the system if we're using a blocking cache.
        // (since hasElement() might place a mutex on the cache key)
        synchronized (cache) {
            if (cache.hasElement(key)) {
                final Object cachedEntry = cache.get(key);
                return new CachePolicyResult(CachePolicyResult.useCache, key, cachedEntry);
            } else {
                return new CachePolicyResult(CachePolicyResult.store, key, null);
            }
        }
    }

    protected boolean shouldBypass(AggregationState aggregationState, Object key) {
        final String uri = (String) key;
        final Iterator it = bypasses.iterator();
        while (it.hasNext()) {
            final String pattern = (String) it.next();
            if (new SimpleUrlPattern(pattern).match(uri)) {
                return true;
            }
        }

        // true if voters vote positively (and mostly they are "not" voters
        // - which means their positive votes will favor bypassing the cache
        return Voting.Factory.getDefaultVoting().vote(voters, null) > 0;
    }

    protected Object getCacheKey(final AggregationState aggregationState) {
        return aggregationState.getOriginalURI();
    }
}
