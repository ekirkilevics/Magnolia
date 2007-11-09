/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.workflow.jcr;

import info.magnolia.cms.core.HierarchyManager;

import javax.jcr.RepositoryException;

/**
 * A HierarchyManagerWrapper which defers save() calls to try and increase performance.
 * 
 * TODO : exposing this as a HierarchyManager and delegate all non-save calls would probably be possible if HM was an interface.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class HierarchyManagerDeferredSaver extends HierarchyManagerWrapperDelegator implements Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HierarchyManagerDeferredSaver.class);

    /**
     * A factory method to instanciate a new HierarchyManagerDeferredSaver
     * and start it in a new daemon thread.
     */
    static HierarchyManagerDeferredSaver startInThread(HierarchyManager hierarchyManager, long sleepDelayMs, long nopingDelayMs, long maxSaveDelayMs) {
        final HierarchyManagerDeferredSaver hmSaver = new HierarchyManagerDeferredSaver(hierarchyManager, sleepDelayMs, nopingDelayMs, maxSaveDelayMs);
        final Thread saverThread = new Thread(hmSaver);
        saverThread.setDaemon(true);
        saverThread.start();
        return hmSaver;
    }

    private final long sleepDelayMs;
    private final long nopingDelayMs; // the delay after no ping happened where we need to save
    private final long maxSaveDelayMs; // even if we're being pinged constantly, we will save at least once every MAX_SAVE_DELAY_MS

    private long lastSaveTimestamp;
    private long lastPingTimestamp;

    HierarchyManagerDeferredSaver(HierarchyManager hierarchyManager, long sleepDelayMs, long nopingDelayMs, long maxSaveDelayMs) {
        super(hierarchyManager);
        this.sleepDelayMs = sleepDelayMs;
        this.nopingDelayMs = nopingDelayMs;
        this.maxSaveDelayMs = maxSaveDelayMs;
    }

    /**
     * Ping the saver - telling it there's gonna be something to do.
     */
    public void save() {
        lastPingTimestamp = System.currentTimeMillis();
        if (lastSaveTimestamp == 0) { // initialize lastSaveTimeStamp on first ping
            lastSaveTimestamp = lastPingTimestamp - 1; // stuffToBeSaved must be true
        }
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(sleepDelayMs);
                final long now = System.currentTimeMillis();
                boolean itemsToBeSaved = lastPingTimestamp > lastSaveTimestamp;
                boolean lastPingWasNopingDelayAgo = lastPingTimestamp < now - nopingDelayMs;
                boolean lastSaveWasMaxSaveDelayAgo = lastSaveTimestamp < now - maxSaveDelayMs;
                // log.debug("[" + getWorkspaceName() + "] : itemsToBeSaved = " + itemsToBeSaved + "; lastPingWasNopingDelayAgo = " + lastPingWasNopingDelayAgo + "; lastSaveWasMaxSaveDelayAgo = " + lastSaveWasMaxSaveDelayAgo);
                if (itemsToBeSaved && (lastPingWasNopingDelayAgo || lastSaveWasMaxSaveDelayAgo)) {
                    try {
                        synchronized (this) {
                            log.debug("Will save [" + getWorkspaceName() + "] : itemsToBeSaved = " + itemsToBeSaved + "; lastPingWasNopingDelayAgo = " + lastPingWasNopingDelayAgo + "; lastSaveWasMaxSaveDelayAgo = " + lastSaveWasMaxSaveDelayAgo);
                            getHierarchyManager().save();
                            lastSaveTimestamp = System.currentTimeMillis();
                        }
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e); // TODO
                    }
                }

            } catch (InterruptedException e) {
            }
        }
    }

}
