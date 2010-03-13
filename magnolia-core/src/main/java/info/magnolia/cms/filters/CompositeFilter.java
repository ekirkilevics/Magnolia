/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A single filter which in turn executes a chain of other filters.
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class CompositeFilter extends AbstractMgnlFilter {
    private static final Logger log = LoggerFactory.getLogger(CompositeFilter.class);

    private MgnlFilter[] filters = new MgnlFilter[0];

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        FilterChain fullchain = new MgnlFilterChain(chain, filters);

        fullchain.doFilter(request, response);
    }

    public MgnlFilter[] getFilters() {
        return filters;
    }

    public void addFilter(MgnlFilter filter) {
        this.filters = (MgnlFilter[]) ArrayUtils.add(this.filters, filter);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        initFilters(filterConfig);
    }

    /**
     * The first time called by the main filter.
     */
    public void initFilters(FilterConfig filterConfig) {
        for (int j = 0; j < filters.length; j++) {
            MgnlFilter filter = filters[j];

            try {
                log.info("Initializing filter [{}]", filter.getName());
                filter.init(filterConfig);
            }
            catch (Exception e) {
                log.error("Error initializing filter [" + filter.getName() + "]", e);
            }
        }
    }

    public void destroy() {
        for (int j = 0; j < filters.length; j++) {
            Filter filter = filters[j];
            filter.destroy();
        }
        filters = new MgnlFilter[0];
    }

}
