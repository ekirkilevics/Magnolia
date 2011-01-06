/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
import java.util.HashMap;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.util.CustomFilterConfig;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;

/**
 * Test for CompositeFilter.
 *
 * @see info.magnolia.cms.filters.FilterTest
 */
public class CompositeFilterTest extends MgnlTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);
    }

    private static class TestFilter extends AbstractMgnlFilter {

        private String _name;
        private boolean destroyed = false;

        private TestFilter(String name) {
            super.setName(name);
            this._name = name;
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            assertEquals(_name, filterConfig.getFilterName());
            assertNotNull(filterConfig.getInitParameter("ip"));
        }

        @Override
        public void destroy() {
            destroyed = true;
        }

        @Override
        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        }
    }

    public void testInitializationAndDestruction() throws ServletException {

        TestFilter testFilter1 = new TestFilter("TestFilterTest1");
        TestFilter testFilter2 = new TestFilter("TestFilterTest2");

        CompositeFilter compositeFilter = new CompositeFilter();
        compositeFilter.addFilter(testFilter1);
        compositeFilter.addFilter(testFilter2);

        HashMap<String, String> initParameters = new HashMap<String, String>();
        initParameters.put("ip", "ip");
        compositeFilter.init(new CustomFilterConfig("compositeFilter", null, initParameters));

        compositeFilter.destroy();

        assertTrue(testFilter1.destroyed);
        assertTrue(testFilter2.destroyed);
    }
}
