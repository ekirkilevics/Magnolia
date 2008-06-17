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

import info.magnolia.module.cache.filter.CachePolicyExecutor;
import info.magnolia.module.cache.filter.behaviours.UseCache;
import info.magnolia.module.cache.filter.behaviours.Bypass;
import info.magnolia.module.cache.filter.behaviours.Store;

import java.util.Map;
import java.util.HashMap;

/**
 * Each CacheConfiguration holds a CachePolicy and a FlushPolicy.
 * @see CachePolicy
 * @see FlushPolicy
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheConfiguration {
    // TODO : make these configurable.
    private final Map executors = new HashMap() {{
        put(CachePolicyResult.bypass, new Bypass());
        put(CachePolicyResult.store, new Store());
        put(CachePolicyResult.useCache, new UseCache());
    }};

    private String name;
    private CachePolicy cachePolicy;
    private FlushPolicy flushPolicy;

    public CacheConfiguration() {
    }

    /** for tests */
    CacheConfiguration(String name, CachePolicy cachePolicy, FlushPolicy flushPolicy) {
        this.name = name;
        this.cachePolicy = cachePolicy;
        this.flushPolicy = flushPolicy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CachePolicy getCachePolicy() {
        return cachePolicy;
    }

    public void setCachePolicy(CachePolicy cachePolicy) {
        this.cachePolicy = cachePolicy;
    }

    public FlushPolicy getFlushPolicy() {
        return flushPolicy;
    }

    public void setFlushPolicy(FlushPolicy flushPolicy) {
        this.flushPolicy = flushPolicy;
    }

    // TODO : review - class loading & exception handling
    public CachePolicyExecutor getExecutor(CachePolicyResult.CachePolicyBehaviour behaviour) {

        return (CachePolicyExecutor) executors.get(behaviour);
    }

}
