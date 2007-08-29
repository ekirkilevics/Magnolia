/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.util;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class DelayedExecutorTest extends TestCase {

    public DelayedExecutorTest() {
        // can't use a debuger to solve problems
        // Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    public void testDelayedExecution() throws InterruptedException{
        TestRunnable runnable = new TestRunnable();
        DelayedExecutor executor = new DelayedExecutor(runnable, 100, 500);
        executor.trigger();
        Thread.sleep(50);
        assertEquals(false, runnable.executed);
        Thread.sleep(70);
        assertEquals(true, runnable.executed);
    }

    public void testMultipleDelayedExecution() throws InterruptedException{
        TestRunnable runnable = new TestRunnable();
        DelayedExecutor executor = new DelayedExecutor(runnable, 100, 500);

        for(int i=0; i<5; i++){
            executor.trigger();
            Thread.sleep(10);
        }
        assertEquals(false, runnable.executed);
        Thread.sleep(100);
        assertEquals(true, runnable.executed);
    }

    public void testExecutionAfterMaxDelay() throws InterruptedException{
        TestRunnable runnable = new TestRunnable();
        DelayedExecutor executor = new DelayedExecutor(runnable, 100, 200);

        for(int i=0; i<4; i++){
            executor.trigger();
            Thread.sleep(100);
        }
        assertEquals(true, runnable.executed);
    }

    /**
     * Knows when it was executed
     */
    final class TestRunnable implements Runnable {

        boolean executed = false;

        public void run() {
            executed = true;
            LoggerFactory.getLogger(DelayedExecutorTest.class).debug("executed");
        }
    }

}
