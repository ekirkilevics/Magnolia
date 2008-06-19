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

import java.util.HashMap;
import java.util.Map;

/**
 * Each CacheConfiguration holds a CachePolicy, a FlushPolicy and a
 * BrowserCachePolicy. Based on the outcome of the cachePolicy the defined
 * executors will be called
 *
 * @see CachePolicy
 * @see FlushPolicy
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheConfiguration {

    private String name;
    private CachePolicy cachePolicy;
    private FlushPolicy flushPolicy;
    private BrowserCachePolicy browserCachePolicy;

    private Map executors = new HashMap();

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

    public CachePolicyExecutor getExecutor(CachePolicyResult.CachePolicyBehaviour behaviour) {
        return (CachePolicyExecutor) executors.get(behaviour.getName());
    }

    public BrowserCachePolicy getBrowserCachePolicy() {
        return browserCachePolicy;
    }

    public void setBrowserCachePolicy(BrowserCachePolicy browserCachePolicy) {
        this.browserCachePolicy = browserCachePolicy;
    }

    public Map getExecutors() {
        return executors;
    }

    public void setExecutors(Map executors) {
        this.executors = executors;
    }

    public void addExecutor(String name, CachePolicyExecutor executor){
        executor.setCacheConfiguration(this);
        this.executors.put(name, executor);
    }

}
