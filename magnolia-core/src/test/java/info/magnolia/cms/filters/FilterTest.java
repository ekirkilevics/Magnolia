/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.filters;

import static org.easymock.EasyMock.*;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.voting.voters.URIStartsWithVoter;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class FilterTest extends MgnlTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        org.apache.log4j.Logger.getLogger(MagnoliaMainFilter.class).setLevel(org.apache.log4j.Level.INFO);
    }

    public void testInitialization() throws IOException, RepositoryException, ServletException{
        String conf =
            "server.filters.first.prop1=val1\n" +
            "server.filters.first.class=info.magnolia.cms.filters.FilterTest$TestFilter\n" +
            "server.filters.second.prop1=val2\n" +
            "server.filters.second.class=info.magnolia.cms.filters.FilterTest$TestFilter\n";

        initConfigRepository(conf);

        MagnoliaMainFilter mf = initMainFilter();

        assertEquals("val1", ((TestFilter)mf.getFilters()[0]).getProp1());
        assertEquals("first", mf.getFilters()[0].getName());

        assertEquals("val2", ((TestFilter)mf.getFilters()[1]).getProp1());
        assertEquals("second", mf.getFilters()[1].getName());

    }

    protected MagnoliaMainFilter initMainFilter() throws ServletException {
        MagnoliaMainFilter.rootFilter = null;
        MagnoliaMainFilter mf = new MagnoliaMainFilter();
        mf.init(createMock(FilterConfig.class));
        return mf;
    }

    public void testDecoratedFilter() throws UnsupportedRepositoryOperationException, IOException, RepositoryException, ServletException{
        String conf =
            "server.filters.decorated.config.param1=value1\n" +
            "server.filters.decorated.class=info.magnolia.cms.filters.FilterDecorator\n" +
            "server.filters.decorated.decoratedFilter.class=info.magnolia.cms.filters.FilterTest$NotMagnoliaFilter\n";

        initConfigRepository(conf);


        MagnoliaMainFilter mf = initMainFilter();

        FilterDecorator fd = (FilterDecorator)mf.getFilters()[0];
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
            "server.filters.first.class=info.magnolia.cms.filters.FilterTest$TestFilter\n" +
            "server.filters.first.bypasses.dot.class=" + URIStartsWithVoter.class.getName() + "\n" +
            "server.filters.first.bypasses.dot.pattern=.\n" +
            "server.filters.second.class=info.magnolia.cms.filters.FilterTest$TestFilter\n";

        initConfigRepository(conf);

        MagnoliaMainFilter mf = initMainFilter();

        HttpServletRequest request = createMock(HttpServletRequest.class);
        WebContext webCtx = createMock(WebContext.class);
        AggregationState aggState = new AggregationState();
        aggState.setCharacterEncoding("UTF-8");
        aggState.setCurrentURI(".magnolia/something.html");
        expect(webCtx.getAggregationState()).andStubReturn(aggState);

        MgnlContext.setInstance(webCtx);

        HttpServletResponse response = createMock(HttpServletResponse.class);
        FilterChain chain = createMock(FilterChain.class);

        replay(request, webCtx);

        mf.doFilter(request, response, chain);

        assertEquals(false, ((TestFilter)mf.getFilters()[0]).executed);
        assertEquals(true, ((TestFilter)mf.getFilters()[1]).executed);
        verify(request, webCtx);
    }

    public static class TestFilter extends AbstractMagnoliaFilter{
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
