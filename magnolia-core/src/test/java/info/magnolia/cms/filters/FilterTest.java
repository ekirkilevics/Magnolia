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

import static org.easymock.classextension.EasyMock.*;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.voting.voters.URIStartsWithVoter;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class FilterTest extends MgnlTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        // shunt log4j
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    }

    public void testInitialization() throws IOException, RepositoryException, ServletException{
        String conf =
            "/server/filters@type=mgnl:content\n" +
            "/server/filters/first.prop1=val1\n" +
            "/server/filters/first.class=info.magnolia.cms.filters.FilterTest$TestFilter\n" +
            "/server/filters/second.prop1=val2\n" +
            "/server/filters/second.class=info.magnolia.cms.filters.FilterTest$TestFilter\n";

        initMockConfigRepository(conf);

        MgnlMainFilter mf = initMainFilter();
        CompositeFilter rootFilter = (CompositeFilter)  mf.getRootFilter();

        assertEquals("val1", ((TestFilter)rootFilter.getFilters()[0]).getProp1());
        assertEquals("first", rootFilter.getFilters()[0].getName());

        assertEquals("val2", ((TestFilter)rootFilter.getFilters()[1]).getProp1());
        assertEquals("second", rootFilter.getFilters()[1].getName());
    }

    protected MgnlMainFilter initMainFilter() throws ServletException {
        MgnlMainFilter mf = new MgnlMainFilter(){
            protected boolean isSystemUIMode() {
                return false;
            }
        };
        mf.init(createMock(FilterConfig.class));
        return mf;
    }

    public void testDecoratedFilter() throws IOException, RepositoryException, ServletException {
        String conf =
            "/server/filters@type=mgnl:content\n" +
            "/server/filters/decorated.config.param1=value1\n" +
            "/server/filters/decorated.class=info.magnolia.cms.filters.FilterDecorator\n" +
            "/server/filters/decorated/decoratedFilter.class=info.magnolia.cms.filters.FilterTest$NotMagnoliaFilter\n";

        initMockConfigRepository(conf);


        MgnlMainFilter mf = initMainFilter();

        CompositeFilter rootFilter = (CompositeFilter) mf.getRootFilter();
        FilterDecorator fd = (FilterDecorator) rootFilter.getFilters()[0];
        FilterConfig filterConfig = createMock(FilterConfig.class);
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        FilterChain chain = createMock(FilterChain.class);
        replay(filterConfig, request, response, chain);

        fd.init(filterConfig);
        fd.doFilter(request, response, chain);

        assertEquals("value1", ((NotMagnoliaFilter)fd.getDecoratedFilter()).param1);
        assertEquals(true, ((NotMagnoliaFilter)fd.getDecoratedFilter()).executed);

        verify(filterConfig, request, response, chain);
    }

    public void testBypassing() throws IOException, RepositoryException, ServletException {
        // first filter ignores all uris starting with a .
        String conf =
            "/server/filters@type=mgnl:content\n" +
            "/server/filters/first.class=info.magnolia.cms.filters.FilterTest$TestFilter\n" +
            "/server/filters/first/bypasses/dot.class=" + URIStartsWithVoter.class.getName() + "\n" +
            "/server/filters/first/bypasses/dot.pattern=.\n" +
            "/server/filters/second.class=info.magnolia.cms.filters.FilterTest$TestFilter\n";

        initMockConfigRepository(conf);

        MgnlMainFilter mf = initMainFilter();

        HttpServletRequest request = createMock(HttpServletRequest.class);
        // log statement
        expect(request.getRequestURI()).andReturn("blah").anyTimes();
        expect(request.getPathInfo()).andReturn("bleh").anyTimes();
        expect(request.getAttribute(EasyMock.<String>anyObject())).andReturn(null).anyTimes();

        WebContext webCtx = createMock(WebContext.class);

        AggregationState aggState = createMock(AggregationState.class);
        expect(aggState.getCurrentURI()).andReturn(".magnolia/something.html");
        expect(webCtx.getAggregationState()).andStubReturn(aggState);

        MgnlContext.setInstance(webCtx);

        HttpServletResponse response = createMock(HttpServletResponse.class);
        FilterChain chain = createMock(FilterChain.class);

        // in the main filter
        webCtx.push(request, response);
        webCtx.pop();
        // in the chain
        webCtx.push(request, response);
        webCtx.pop();
        webCtx.push(request, response);
        webCtx.pop();

        replay(request, webCtx, aggState);

        mf.doFilter(request, response, chain);
        CompositeFilter rootFilter = (CompositeFilter) mf.getRootFilter();

        assertEquals(false, ((TestFilter)rootFilter.getFilters()[0]).executed);
        assertEquals(true, ((TestFilter)rootFilter.getFilters()[1]).executed);
        verify(request, webCtx, aggState);
    }

    public static class TestFilter extends AbstractMgnlFilter{
        public boolean executed = false;

        private String prop1;

        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
            this.executed = true;
        }


        public String getProp1() {
            return this.prop1;
        }


        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }
    }

    public static class NotMagnoliaFilter implements Filter{

        public String param1;

        public boolean executed = false;

        public void destroy() {
        }

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            this.executed = true;
        }

        public void init(FilterConfig filterConfig) throws ServletException {
            param1 = filterConfig.getInitParameter("param1");
        }

    }
}
