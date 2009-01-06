/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;


/**
 * There are many places where we like to delay an execution (in case of a serie of fired events). But we want to ensure
 * the the task is executed after a certain maximum delay.
 * @author philipp
 * @version $Id$
 */
public class DelayedExecutor {

    private static final Logger log = LoggerFactory.getLogger(DelayedExecutor.class);

    protected ClockDaemon timer = new ClockDaemon();

    protected long delay;

    protected long maxDelay;

    protected long timestamp;

    protected Runnable command;

    /**
     * The current taskId
     */
    protected Object taskId;

    /**
     * @param delay milliseconds
     * @param maxDelay milliseconds
     */
    public DelayedExecutor(final Runnable command, long delay, long maxDelay) {
        this.command = new RunnableWrapper(command);
        this.delay = delay;
        this.maxDelay = maxDelay;
    }

    public synchronized void trigger() {
        long now = System.currentTimeMillis();
        log.debug("execution triggered");

        if (timestamp == 0) {
            timestamp = now;
        }
        if (taskId != null && (timestamp + maxDelay >= now)) {
            log.debug("execution canceled");
            timer.cancel(taskId);
        }
        taskId = timer.executeAfterDelay(delay, command);
    }

    /**
     * Resets the timestamp after starting the execution. Synchronizes the execution.
     */
    protected final class RunnableWrapper implements Runnable {

        private final Runnable command;

        protected RunnableWrapper(Runnable command) {
            this.command = command;
        }

        public synchronized void run() {
            timestamp = 0;
            this.command.run();
        }
    }

}
