/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.cache.simple;

import info.magnolia.cms.cache.CacheManager;
import info.magnolia.cms.cache.CacheManagerFactory;

import javax.jcr.observation.EventIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class counsumes any jcr event as long as it occurs within the specified INACTIVE-WAITING time
 * @author Sameer Charles
 * $Id$
 */
class DeferredCleaner {

    private static final DeferredCleaner thisInstance = new DeferredCleaner();

    private static final Logger log = LoggerFactory.getLogger(DeferredCleaner.class);

    private static final int CONSUME_FOR = 30; // seconds

    private Listener listener;

    private DeferredCleaner() {}

    protected static DeferredCleaner getInstance() {
        return thisInstance;
    }

    protected synchronized void consume(EventIterator events) {
        if (null == this.listener) {
            this.startListener();
        } else {
            // make sure thread is still alive
            if (!this.listener.isAlive()) {
                this.startListener();
            }
            // simply defer it
            this.listener.resetTimer(CONSUME_FOR);
        }
    }

    private void startListener() {
        this.listener = new Listener(CONSUME_FOR);
        try {
            this.listener.setDaemon(true);
        } catch (SecurityException se) {
            log.warn("Unable to start DeferredCleaner as deamon thread");
            log.debug(se.getMessage(), se);
        }
        this.listener.start();
    }

    private class Listener extends Thread {

        private static final int YIELD_FOR = 30; // seconds

        private int runFor;

        private CacheManager cacheManager = CacheManagerFactory.getCacheManager();

        Listener(int runFor) {
            this.runFor = runFor;
        }

        public void resetTimer(int runFor) {
            this.runFor = runFor;
        }

        /**
         * Sure this wont be accurate but it does not matter here
         * */
        public void run() {
            while (true) {
                if (runFor > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // ignore since there is only one owner of this thread
                    }
                    runFor--;
                } else if (runFor == 0) {
                    this.cacheManager.flushAll();
                    runFor--;
                } else {
                    // yield...
                    try {
                        Thread.sleep(YIELD_FOR*1000);
                    } catch (InterruptedException e) {
                        // shouldn't happen, else just wake up :)
                    }
                }
            }
        }

    }

}
