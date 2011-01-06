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
package info.magnolia.module.cache;

/**
 * CachePolicy returns instances of this class - depending on the value
 * of the behaviour property, the cacheKey and cachedEntry values might
 * be set or not.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CachePolicyResult {
    public static final CachePolicyBehaviour store = new CachePolicyBehaviour("store");
    public static final CachePolicyBehaviour useCache = new CachePolicyBehaviour("useCache");
    public static final CachePolicyBehaviour bypass = new CachePolicyBehaviour("bypass");

    private final CachePolicyBehaviour behaviour;
    private final Object cacheKey;
    private Object cachedEntry;

    public CachePolicyResult(CachePolicyBehaviour behaviour, Object cacheKey, Object cachedEntry) {
        this.behaviour = behaviour;
        this.cacheKey = cacheKey;
        this.cachedEntry = cachedEntry;
    }

    public CachePolicyBehaviour getBehaviour() {
        return behaviour;
    }

    public Object getCacheKey() {
        return cacheKey;
    }

    public Object getCachedEntry() {
        return cachedEntry;
    }

    public String toString() {
        return "CachePolicyResult{" +
                "behaviour=" + behaviour +
                ", cacheKey=" + cacheKey +
                ", cachedEntry=" + cachedEntry +
                '}';
    }

    /**
     * Descriptor of the cache policy behavior used by this cache policy result.
     */
    public final static class CachePolicyBehaviour {
        private final String name;

        private CachePolicyBehaviour(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }
    }

    public void setCachedEntry(Object entry) {
        cachedEntry = entry;
    }

}
