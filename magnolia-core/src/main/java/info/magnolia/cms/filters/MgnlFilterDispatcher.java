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
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * Dispatches requests onto Magnolias filter chain, if the filter chain is bypassed for this request it passes it on to
 * the next filter in web.xml.
 *
 * @version $Id$
 * @see FilterManager
 * @see SafeDestroyMgnlFilterWrapper
 */
public class MgnlFilterDispatcher {

    private final SafeDestroyMgnlFilterWrapper.Switcher switcher = new SafeDestroyMgnlFilterWrapper.Switcher();

    /**
     * Calls the target filter if it matches the request otherwise passes the request on to the next filter in web.xml.
     */
    public void doDispatch(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        SafeDestroyMgnlFilterWrapper filterToUse = switcher.getFilterAndAcquireReadLock();
        if (filterToUse == null) {
            throw new ServletException("Filter chain not available");
        }

        try {
            if (filterToUse.matches((HttpServletRequest) request)) {
                filterToUse.doFilter(request, response, chain);
                return;
            }
        } finally {
            filterToUse.releaseReadLock();
        }

        // Pass request to next filter in web.xml without holding the read lock
        chain.doFilter(request, response);
    }

    /**
     * Replaces the current filter with a new one and returns the previous filter. See
     * {@link info.magnolia.cms.filters.SafeDestroyMgnlFilterWrapper.Switcher#replaceFilter(SafeDestroyMgnlFilterWrapper)}
     * for usage constraints on the returned filter.
     */
    public MgnlFilter replaceTargetFilter(MgnlFilter newFilter) {
        return switcher.replaceFilter(newFilter != null ? new SafeDestroyMgnlFilterWrapper(newFilter) : null);
    }

    /**
     * Returns the current target filter with a read lock held. The returned filter will not be destroyed before the
     * read lock is released.
     */
    public SafeDestroyMgnlFilterWrapper getTargetFilterAndAcquireReadLock() {
        return switcher.getFilterAndAcquireReadLock();
    }

    /**
     * Returns the current target filter, the returned instance will be destroyed if the target filter is replaced.
     *
     * @see info.magnolia.cms.filters.SafeDestroyMgnlFilterWrapper.Switcher#getFilter()
     */
    public MgnlFilter getTargetFilter() {
        SafeDestroyMgnlFilterWrapper filter = switcher.getFilter();
        return filter != null ? filter.getTargetFilter() : null;
    }
}
