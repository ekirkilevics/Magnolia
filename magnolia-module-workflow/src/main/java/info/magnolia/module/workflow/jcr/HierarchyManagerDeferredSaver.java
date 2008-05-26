/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.module.workflow.jcr;

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
    static HierarchyManagerDeferredSaver startInThread(String workspaceName, long sleepDelayMs, long nopingDelayMs, long maxSaveDelayMs) {
        final HierarchyManagerDeferredSaver hmSaver = new HierarchyManagerDeferredSaver(workspaceName, sleepDelayMs, nopingDelayMs, maxSaveDelayMs);
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

    HierarchyManagerDeferredSaver(String workspaceName, long sleepDelayMs, long nopingDelayMs, long maxSaveDelayMs) {
        super(workspaceName);
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
