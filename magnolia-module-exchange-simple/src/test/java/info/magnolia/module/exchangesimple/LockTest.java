/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.exchangesimple;

import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockUtil;

import static org.easymock.EasyMock.*;

/**
 * Test to verify releasing of locks on activation.
 * @author had
 * @version $Id:$
 */
public class LockTest extends RepositoryTestCase {

    // while normally not using logs in test, multithreaded nature of this test makes it hard to debug without log messages in case of failure
    private static final Logger log = LoggerFactory.getLogger(LockTest.class);

    /**
     * Lock check task.
     * @author had
     * @version $Id:$
     */
    public class LockCheck implements Runnable {

        private boolean locked;

        public void run() {
            MockUtil.initMockContext();
            try {
                //LockTest.this.initDefaultImplementations();
                LockTest.this.modifyContextesToUseRealRepository();
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            while (true) {
                HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
                try {
                    locked = hm.getContent("/page").isLocked();
                    if (locked) {
                        try {
                            hm.getContent("/page").unlock();
                            fail("This thead has to run in separate session, thus can't be allowed to unlock content locked by that other session. Either it is not running in separate session or if it is, the locking is not configured properly.");
                        } catch (LockException e) {
                            assertTrue("Unexpected error message: " + e.getMessage(), e.getMessage().matches("Node not locked( by session)?: " + hm.getContent("/page").getUUID()));
                        }
                    }
                    Thread.sleep(500);
                } catch (RepositoryException e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            }
        }

        /**
         * @return
         */
        public boolean isLocked() {
            return locked;
        }

    }


    /**
     * Lock check task for the receive filter.
     */
    public class ReceiveFilterLockCheck implements Runnable {

        private boolean locked;
        private boolean retry = false;
        private HttpServletRequest request;

        public boolean isRetry() {
            return retry;
        }

        public void setRetry(boolean retry) {
            this.retry = retry;
        }

        public void run() {
            MockUtil.initMockContext();
            try {
                //LockTest.this.initDefaultImplementations();
                LockTest.this.modifyContextesToUseRealRepository();
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            ReceiveFilter rf = new ReceiveFilter();
            rf.setRetryWait(0);
            rf.setUnlockRetries(1);


            while (true) {
                try {
                    log.debug("will try:" + retry);
                    if (retry) {
                        log.debug("trying");
                        rf.applyLock(request);
                        log.debug("success");
                        locked = false;
                    }
                } catch (ExchangeException e) {
                    log.debug("failure");
                    locked = true;
                }
                retry = false;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            }
        }

        /**
         * @return
         */
        public boolean isLocked() {
            return locked;
        }

        /**
         * @param request2
         */
        public void setRequest(HttpServletRequest request2) {
            this.request = request2;
        }

    }
    public void testLocks() throws Exception {
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        Content node = hm.createContent("/", "page", ItemType.CONTENT.getSystemName());
        node.createContent("paragraph", ItemType.CONTENTNODE.getSystemName());
        hm.save();

        hm.getContent("/page").lock(true, true);

        assertTrue(hm.getContent("/page").isLocked());
        LockCheck check = new LockCheck();
        Thread t2 = new Thread(check);

        t2.start();
        // give it time to run through the loop
        Thread.sleep(1000);
        assertTrue(check.isLocked());
        hm.getContent("/page").unlock();
        Thread.sleep(1000);
        assertFalse(check.isLocked());
        t2.interrupt();
    }

    public void testLockReceiveFilter() throws Exception {
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        Content node = hm.createContent("/", "page", ItemType.CONTENT.getSystemName());
        node.createContent("paragraph", ItemType.CONTENTNODE.getSystemName());
        hm.save();

        ReceiveFilter rf = new ReceiveFilter();
        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        //apply lock
        expect (request.getHeader(BaseSyndicatorImpl.PARENT_PATH)).andReturn("/page").times(2);
        expect (request.getHeader(BaseSyndicatorImpl.REPOSITORY_NAME)).andReturn("magnolia");
        expect (request.getHeader(BaseSyndicatorImpl.WORKSPACE_NAME)).andReturn(ContentRepository.WEBSITE);
        // cleanup
        expect(request.getHeader(BaseSyndicatorImpl.ACTION)).andReturn(BaseSyndicatorImpl.ACTIVATE);
        expect (request.getHeader(BaseSyndicatorImpl.PARENT_PATH)).andReturn("/page");
        expect (request.getHeader(BaseSyndicatorImpl.REPOSITORY_NAME)).andReturn("magnolia");
        expect (request.getHeader(BaseSyndicatorImpl.WORKSPACE_NAME)).andReturn(ContentRepository.WEBSITE);
        expect (request.getHeader(BaseSyndicatorImpl.PARENT_PATH)).andReturn("/page").times(2);
        expect (request.getHeader(BaseSyndicatorImpl.REPOSITORY_NAME)).andReturn("magnolia");
        expect (request.getHeader(BaseSyndicatorImpl.WORKSPACE_NAME)).andReturn(ContentRepository.WEBSITE);
        expect(request.getSession(false)).andReturn(null);

        HttpServletRequest request2 = createStrictMock(HttpServletRequest.class);
        // first check retry 1
        expect (request2.getHeader(BaseSyndicatorImpl.PARENT_PATH)).andReturn("/page").times(2);
        expect (request2.getHeader(BaseSyndicatorImpl.REPOSITORY_NAME)).andReturn("magnolia");
        expect (request2.getHeader(BaseSyndicatorImpl.WORKSPACE_NAME)).andReturn(ContentRepository.WEBSITE);

        //first check retry 2
        expect (request2.getHeader(BaseSyndicatorImpl.PARENT_PATH)).andReturn("/page").times(2);
        expect (request2.getHeader(BaseSyndicatorImpl.REPOSITORY_NAME)).andReturn("magnolia");
        expect (request2.getHeader(BaseSyndicatorImpl.WORKSPACE_NAME)).andReturn(ContentRepository.WEBSITE);

        //second check retry 1
        expect (request2.getHeader(BaseSyndicatorImpl.PARENT_PATH)).andReturn("/page").times(2);
        expect (request2.getHeader(BaseSyndicatorImpl.REPOSITORY_NAME)).andReturn("magnolia");
        expect (request2.getHeader(BaseSyndicatorImpl.WORKSPACE_NAME)).andReturn(ContentRepository.WEBSITE);

        Object[] objs = new Object[] {request, request2};
        replay(objs);

        rf.applyLock(request);
        assertTrue(hm.getContent("/page").isLocked());
        log.debug("locked in session1!");


        ReceiveFilterLockCheck check = new ReceiveFilterLockCheck();
        check.setRequest(request2);
        Thread t2 = new Thread(check);

        t2.start();
        check.setRetry(true);
        // give it time to run through the loop
        Thread.sleep(1000);
        assertTrue(check.isLocked());
        log.debug("lock in session2!");
        rf.cleanUp(request);
        log.debug("unlocked in session1!");
        assertFalse(hm.getContent("/page").isLocked());
        log.debug("verified unlocked in session1!");
        check.setRetry(true);
        Thread.sleep(1000);
        assertFalse(check.isLocked());
        log.debug("locked by session2!");

        verify(objs);
        t2.interrupt();
    }

}
