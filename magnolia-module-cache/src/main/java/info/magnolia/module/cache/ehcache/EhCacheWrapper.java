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
package info.magnolia.module.cache.ehcache;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.module.cache.mbean.CacheMonitor;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;

/**
 * Magnolia cache wrapper for underlying ehCache implementation.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EhCacheWrapper implements info.magnolia.module.cache.BlockingCache {

    private static final Logger log = LoggerFactory.getLogger(EhCacheWrapper.class);
    private final BlockingCache ehcache;
    private String name;

    public EhCacheWrapper(BlockingCache ehcache, String name) {
        this.ehcache = ehcache;
        this.name = name;
    }

    public EhCacheWrapper(Ehcache ehcache, String name) {
        this(castToBlockingCacheOrThrowException(ehcache), name);
    }

    private static BlockingCache castToBlockingCacheOrThrowException(Ehcache ehcache) {
        if(!(ehcache instanceof BlockingCache)){
            throw new RuntimeException("The current caching framework depends on the fact the a blocking cache is used.");
        }
        return (BlockingCache) ehcache;
    }

    public Object get(Object key) {
        final Element element = ehcache.get(key);
        try {
            return element != null ? element.getObjectValue() : null;
        } catch (LockTimeoutException e) {
            log.error("Detected 1 thread stuck in generating response for {}. This might be temporary if obtaining the response is resource intensive or when accessing remote resources.", key);
            throw e;
        }
    }

    public boolean hasElement(Object key) {
        // we can't use isKeyInCache(), as it does not check for the element's expiration
        // which may lead to unexpected results.
        // return ehcache.isKeyInCache(key);
        try {
            // get() and getQuiet() do check for expiration and return null if the element was expired.
            // we can't use getQuiet, as it's non-blocking which could lead to multiple copies of same page to be generated
            // if page is requested while previous request for same page is still being processed by different thread
            return ehcache.get(key) != null;
        } catch (LockTimeoutException e) {
            log.error("Detected 1 thread stuck in generating response for {}. This might be temporary if obtaining the response is resource intensive or when accessing remote resources.", key);
            // FYI: in case you want to return some value instead of re-throwing exception: this is a dilemma ... obviously resource does not exist yet, but being stuck here for while means that it is either being generated or it takes time to generate.
            // returning false would mean server attempts to generate the response again, possibly loosing another thread in the process
            // returning true means server will assume resource exists and will try to retrieve it later, possibly failing with the same error
            throw e;
        }
    }

    public void put(Object key, Object value) {
        final Element element = new Element(key, value);
        ehcache.put(element);
    }

    public void remove(Object key) {
        ehcache.remove(key);
    }

    public void clear() {
        CacheMonitor.getInstance().countFlush(this.name);
        ehcache.removeAll();
    }

    public void unlock(Object key) {
        if(ehcache.getQuiet(key) == null) {
            put(key, null);
            remove(key);
        }
    }

    public int getBlockingTimeout() {
        return ehcache.getTimeoutMillis();
    }

    public Ehcache getWrappedEhcache() {
        return ehcache;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return ehcache.getSize();
    }



}
