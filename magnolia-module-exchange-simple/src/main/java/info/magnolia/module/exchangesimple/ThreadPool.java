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
package info.magnolia.module.exchangesimple;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple thread pool used to speed up activation to multiple subscribers.
 * @author had
 *
 */
public class ThreadPool {

    private static final Logger log = LoggerFactory.getLogger(ThreadPool.class);

    private ThreadGroup group = new ThreadGroup("PooledActivators");
    
    private Vector runnables = new Vector();
    
    private static ThreadPool instance = new ThreadPool();

    /**
     * Creates new pool of threads and starts them. 
     */
    private ThreadPool() {
        for (int i = 0; i < 10; i++) {
            PooledThread pt = new PooledThread(group, "" + i);
            pt.start();
            
        }
    }
    
    /**
     * Gets single instance of the thread pool.
     * @return Single instance of pool per VM/classloader.
     */
    public static ThreadPool getInstance() {
        return instance;
    }
    
    /**
     * Schedules task for execution. Method returns immediatelly even when all worker threads are busy at the moment.
     * @param r Runnable task.
     */
    public void run(Runnable r) {
        runnables .add(r);
        synchronized (this) {
            notifyAll();
        }
    }
    
    /**
     * Thread instance with infinite loop in run method to ensure periodic execution.
     * @author had
     *
     */
    class PooledThread extends Thread {
        
        public PooledThread(ThreadGroup group, String name) {
            super(group, name);
            // make sure those threads don't stop VM from exiting.
            setDaemon(true);
        }
        
        public void run() {
            while (true ) {
                try {
                    Runnable r = null;
                    try {
                        r = (Runnable) runnables.remove(0);
                    } catch (IndexOutOfBoundsException e) {
                        // no item found sleep and retry
                        try {
                            // sleep for while
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                           // waked up from outside ... ignore
                        }
                        continue;
                    }
                    if (r != null) {
                        r.run();
                    }
                } catch (Throwable t) {
                    log.error("Activation error detected.", t);
                }
            }
        }
        
    }
}
