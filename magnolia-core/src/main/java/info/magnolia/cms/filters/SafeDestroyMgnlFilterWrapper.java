/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.cms.filters;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * Wrapper for a MgnlFilter that using a read/write lock ensures that the destroy method waits for requests to complete
 * before destroying the target filter. All use of the wrapper must be done while holding the read lock. The exception
 * to this is the destroy method that MUST NOT be called while holding the read lock, doing so would result in dead
 * lock.
 *
 * @version $Id$
 */
public class SafeDestroyMgnlFilterWrapper implements MgnlFilter {

    private final MgnlFilter target;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public SafeDestroyMgnlFilterWrapper(MgnlFilter target) {
        if (target == null) {
            throw new NullPointerException("Target filter must not be null");
        }
        this.target = target;
    }

    public void acquireReadLock() {
        lock.readLock().lock();
    }

    public void releaseReadLock() {
        lock.readLock().unlock();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        target.init(filterConfig);
    }

    /**
     * Read lock must be held when invoking this method.
     */
    @Override
    public String getName() {
        return target.getName();
    }

    /**
     * Read lock must be held when invoking this method.
     */
    @Override
    public void setName(String name) {
        target.setName(name);
    }

    /**
     * Read lock must be held when invoking this method.
     */
    @Override
    public boolean matches(HttpServletRequest request) {
        return target.matches(request);
    }

    /**
     * Read lock must be held when invoking this method.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        target.doFilter(request, response, chain);
    }

    /**
     * Destroys the target filter after waiting for all requests to complete. Calling this method while holding the read
     * lock will result in dead lock. Therefore a request that has passed through this filter MUST NEVER call this
     * method.
     */
    @Override
    public void destroy() {

        // We could employ a ThreadLocal to throw an exception instead of dead locking. There is no other way
        // to see if the calling thread is holding the read lock.

        lock.writeLock().lock();
        try {
            target.destroy();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public MgnlFilter getTargetFilter() {
        return target;
    }

    /**
     * Construct for keeping a reference to a {@link SafeDestroyMgnlFilterWrapper} and change it with the guarantee that
     * the returned previous reference will not get more read locks. It's then safe to destroy the returned reference.
     *
     * @version $Id$
     */
    public static class Switcher {

        private SafeDestroyMgnlFilterWrapper filter;

        /**
         * Replaces the current filter with a new one and returns the previous filter. The returned filter is possibly still
         * in use by currently executing requests but it will not be used by any more requests after this method returns.
         * <p/>
         * Notes about the returned filter:
         * <ul>
         * <li>No methods other than destroy() should be called on the returned filter</li>
         * <li>The destroy() method will wait for all requests to complete before it is destroyed</li>
         * <li>Calling the destroy() method from a request that has entered the filter WILL RESULT IN DEAD LOCK.</li>
         * </ul>
         */
        public synchronized SafeDestroyMgnlFilterWrapper replaceFilter(SafeDestroyMgnlFilterWrapper newFilter) {
            SafeDestroyMgnlFilterWrapper oldFilter = filter;
            filter = newFilter;
            return oldFilter;
        }

        /**
         * Returns the current filter with a read lock held for the current thread. After use the thread must relinquish the
         * lock by calling releaseReadLock(). If no filter has been set this method returns null and no lock is taken.
         */
        public synchronized SafeDestroyMgnlFilterWrapper getFilterAndAcquireReadLock() {
            if (filter == null) {
                return null;
            }
            filter.acquireReadLock();
            return filter;
        }

        /**
         * This method is provided for introspection only. It is inherently UNSAFE as using this method and then
         * acquiring the read lock on the returned filter breaks the guarantee that a replaced reference wont get more
         * read locks.
         */
        public synchronized SafeDestroyMgnlFilterWrapper getFilter() {
            return filter;
        }
    }
}
