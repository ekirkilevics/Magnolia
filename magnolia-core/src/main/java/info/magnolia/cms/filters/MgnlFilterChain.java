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
package info.magnolia.cms.filters;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A implementation of {@link FilterChain} having a bypass mechanism.
 * @author philipp
 * @version $Id$
 *
 */
public class MgnlFilterChain implements FilterChain {

    private MgnlFilter[] filters;

    private int position;

    private FilterChain originalChain;

    public MgnlFilterChain(FilterChain originalChain, MgnlFilter[] filters) {
        this.filters = filters;
        this.originalChain = originalChain;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        Context ctx = MgnlContext.hasInstance() ? MgnlContext.getInstance() : null;
        boolean updateCtx = ctx instanceof WebContext && request instanceof HttpServletRequest && response instanceof HttpServletResponse;
        if (updateCtx) {
            ((WebContext) ctx).push((HttpServletRequest) request, (HttpServletResponse) response);
        }
        try {
            if (position == filters.length) {
                originalChain.doFilter(request, response);
            }
            else {
                position++;
                MgnlFilter filter = filters[position - 1];
                if (filter.matches((HttpServletRequest)request)) {
                    filter.doFilter(request, response, this);
                } else {
                    doFilter(request, response);
                }
            }
        } finally {
            if (updateCtx) {
                ((WebContext) ctx).pop();
            }
        }
    }

    public void reset() {
        position = 0;
    }
}
