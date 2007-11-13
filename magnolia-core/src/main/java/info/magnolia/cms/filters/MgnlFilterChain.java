/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.cms.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Handles the bypassing
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

    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (position == filters.length) {
            originalChain.doFilter(request, response);
        }
        else {
            position++;
            MgnlFilter filter = filters[position - 1];
            if (!filter.bypasses((HttpServletRequest)request)) {
                filter.doFilter(request, response, this);
            } else {
                doFilter(request, response);
            }
        }
    }

    public void reset() {
        position = 0;
    }
}
