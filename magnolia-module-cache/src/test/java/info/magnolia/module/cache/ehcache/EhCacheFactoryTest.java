/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.module.cache.Cache;
import junit.framework.TestCase;

/**
 * Test to assert whether there are still any issues with cache failing to un-block on access from multiple threads.
 * @author had
 * @version $Id:$
 */
public class EhCacheFactoryTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(EhCacheFactoryTest.class);
    private EhCacheFactory factory;


    @Override
    public void setUp() throws Exception {
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_CACHE_STARTDIR, "target/cacheTest");
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, ".");

        factory = new EhCacheFactory();
        // init with ehCache 1.5
        CacheConfiguration config = new CacheConfiguration();
        // init with ehCache 2.0
        //CacheConfiguration config = new CacheConfiguration("test", 0);
        config.setDiskPersistent(false);
        config.setOverflowToDisk(false);
        config.setTimeToIdleSeconds(0);
        config.setTimeToLiveSeconds(0);
        // comment out the line below and blocking test will fail with timeout exception (as of ehCache 1.5, works fine on 2.0)
        // ... if you don't believe it will block forever feel free to increase the timeout
        config.setMaxElementsInMemory(1);
        factory.setDefaultCacheConfiguration(config);
        factory.start();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        SystemProperty.getProperties().clear();
    }

    /**
     * Simple test for serial access to the cache.
     */
    public void testSerialAccess() throws Exception {
        final Cache ehCache = factory.getCache("test1");
        assertNull(ehCache.get("foo"));
        ehCache.put("foo", "xxx");
        assertEquals("xxx", ehCache.get("foo"));
    }

    /**
     * Simple test of item eviction.
     */
    public void testAddMoreThanMaxSize() throws Exception {
        // make sure there's only one item allowed
        assertEquals(1, factory.getDefaultCacheConfiguration().getMaxElementsInMemory());

        final Cache ehCache = factory.getCache("test2");
        assertNull(ehCache.get("foo"));
        // add first
        ehCache.put("foo", "xxx");
        // and check first is there
        assertEquals("xxx", ehCache.get("foo"));
        // add second (and evict first
        ehCache.put("boo", "xxx");
        // first is gone
        assertEquals(null, ehCache.get("foo"));
        // second is there
        assertEquals("xxx", ehCache.get("boo"));

    }

    /**
     * Ensure that cache unblocks all threads waiting for item to be cached.
     */
    public void testBlocking() throws Exception {
        // block forever, making sure taskX.get() will fail our tests if blockage occurs
        factory.setBlockingTimeout(0);
        final Cache ehCache = factory.getCache("test3");
        // 1st call - place mutex
        Object entry = ehCache.get("blah");
        log.debug("On first get: {}", entry );
        assertNull(entry);
        Executor ex = Executors.newFixedThreadPool(2);

        // 2nd call - wait until mutex is gone and entry is available
        FutureTask<Object> task2 = new FutureTask<Object>(new Callable<Object>() {
            public Object call() throws Exception {
                log.debug("2nd get called");
                Object res = ehCache.get("blah");
                log.debug("2nd unblocked");
                return res;
            }});
        ex.execute(task2);

        // 3rd call - wait until mutex is gone and entry is available
        FutureTask<Object> task3 = new FutureTask<Object>(new Callable<Object>() {
            public Object call() throws Exception {
                log.debug("3rd get called");
                Object res = ehCache.get("blah");
                log.debug("3rd unblocked");
                return res;
            }});
        ex.execute(task3);

        log.debug("Put");
        // add something in the main thread
        ehCache.put("blah", "boo");
        // try to read what you just put in (if the cache is misconfigured, you get the first block here, and since that would block whole test we skip it and rely on block to be exercised also in the tasks that will time out without blocking whole execution
        //assertEquals("boo", ehCache.get("blah"));

        log.debug("verify");
        // thread2
        Object result = task2.get(5, TimeUnit.SECONDS);
        log.debug("2nd get: {]", result);

        // thread3
        Object result2 = task3.get(5, TimeUnit.SECONDS);
        log.debug("3rd get: {}", result2);

        assertNotNull(result);
        assertNotNull(result2);

    }

    // commented out as this scenario fails now _every time_
    /**
     * Ensure cache unblocks and returns proper item to all the threads waiting for the item even if such is soon after evicted from the cache.
     */
    public void testBlockingAfterAddingMoreThanMaxSize() throws Exception {
        // make sure there's only one item allowed
        assertEquals(1, factory.getDefaultCacheConfiguration().getMaxElementsInMemory());
        // set timeout shorter then timeout on taskX.get()
        factory.setBlockingTimeout(1000);
        final Cache ehCache = factory.getCache("test4");
        // 1st call - place mutex
        Object entry = ehCache.get("blah");
        log.info("On first get: {}", entry );
        assertNull(entry);
        Executor ex = Executors.newFixedThreadPool(2);

        log.info("Put");
        // add something in the main thread - release mutex on "blah"
        ehCache.put("blah", "boo");
        // try to read what you just put in (if the cache is misconfigured, you get the first block here, and since that would block whole test we skip it and rely on block to be exercised also in the tasks that will time out without blocking whole execution
        // assertEquals("boo", ehCache.get("blah"));

        // instead make 2nd call on separate thread - cache entry should exist
        FutureTask<Object> task2 = new FutureTask<Object>(new Callable<Object>() {

            public Object call() throws Exception {
                log.info("2nd get called");
                Object res = ehCache.get("blah");
                log.info("2nd not blocked");
                return res;
            }});
        ex.execute(task2);
        Object result = task2.get(5, TimeUnit.SECONDS);
        log.info("2nd get: {}", result);
        assertEquals("boo", result);


        // put new item to evict old
        ehCache.put("foo", "xxx");
        // try to read evicted item (place the mutex on "blah" again)
        entry = ehCache.get("blah");
        assertNull(entry);

        // 3rd call - after evicted and before cached again == > block forever
        FutureTask<Object> task3 = new FutureTask<Object>(new Callable<Object>() {

            public Object call() throws Exception {
                log.info("3rd get called");
                Object res = "futureDummyNotModifiedByCacheGetCall";
                try {
                    res = ehCache.get("blah");
                    fail("should not get here. Cache config is wrong!");
                } catch (LockTimeoutException e) {
                    // expected
                }
                log.info("3rd unblocked");
                return res;
            }});
        ex.execute(task3);
        // thread3 - since mutex on "blah" is still in place, the call should end with LTE and return null
        Object result2 = task3.get(5, TimeUnit.SECONDS);
        log.info("3rd get: {}", result2);
        assertEquals("futureDummyNotModifiedByCacheGetCall", result2);
    }
}
