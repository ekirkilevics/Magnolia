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

        @Override
        public void run() {
            executed = true;
            LoggerFactory.getLogger(DelayedExecutorTest.class).debug("executed");
        }
    }

}
