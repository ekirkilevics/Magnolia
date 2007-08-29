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
