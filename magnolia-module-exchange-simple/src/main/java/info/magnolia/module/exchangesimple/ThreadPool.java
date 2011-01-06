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

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * Simple thread pool used to speed up activation to multiple subscribers.
 * @author had
 *
 */
final class ThreadPool {
    
    /**
     * Private constructor to prevent accidental instantiation.
     */
    private ThreadPool() {
        throw new AssertionError("not instantiable");
    }

    /**
     * The fixes number of threads to use in the pool.
     */
    private static final int NUM_THREADS = 10;
    
    private static final PooledExecutor instance;
    
    static {
        // possibly have an infinite number of jobs
        instance = new PooledExecutor(new LinkedQueue());
        // always have NUM_THREADS running
        instance.setMinimumPoolSize(NUM_THREADS);
        instance.setMaximumPoolSize(NUM_THREADS);
    }

    /**
     * Gets single instance of the thread pool.
     * @return Single instance of pool per VM/classloader.
     */
    public static Executor getInstance() {
        return instance;
    }
    
}
