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
package info.magnolia.cms.beans.config;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.filters.AbstractMagnoliaFilter;
import info.magnolia.cms.filters.FilterDecorator;
import info.magnolia.cms.filters.MagnoliaFilter;
import info.magnolia.cms.filters.MagnoliaMainFilter;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.context.WebContextImpl;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.easymock.EasyMock;

import sun.tools.jconsole.CreateMBeanDialog;

/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class FilterManagerTest extends MgnlTestCase {

    public void testPrioritySorting(){
        MagnoliaFilter f1 = createMock(MagnoliaFilter.class);
        expect(f1.getPriority()).andStubReturn(300);
        MagnoliaFilter f2 = createMock(MagnoliaFilter.class);
        expect(f2.getPriority()).andStubReturn(100);
        MagnoliaFilter f3 = createMock(MagnoliaFilter.class);
        expect(f3.getPriority()).andStubReturn(200);
        replay(f1, f2, f3);


        List filters = new ArrayList();
        filters.add(f1);
        filters.add(f2);
        filters.add(f3);

        Collections.sort(filters, FilterManager.PRIORITY_COMPERATOR);
        assertEquals(f2, filters.get(0));
        assertEquals(f3, filters.get(1));
        assertEquals(f1, filters.get(2));
    }

    public void testInitialization() throws IOException, RepositoryException{
        String conf =
            "server.filters.first.priority=800\n" +
            "server.filters.first.class=info.magnolia.cms.beans.config.FilterManagerTest$TestFilter\n" +
            "server.filters.second.priority=300\n" +
            "server.filters.second.class=info.magnolia.cms.beans.config.FilterManagerTest$TestFilter\n";

        initConfigRepository(conf);

        FilterManager fm = FilterManager.getInstance();

        assertEquals(300, fm.getFilters()[0].getPriority());
        assertEquals("second", fm.getFilters()[0].getName());

        assertEquals(800, fm.getFilters()[1].getPriority());
        assertEquals("first", fm.getFilters()[1].getName());

    }

    public void testDecoratedFilter() throws UnsupportedRepositoryOperationException, IOException, RepositoryException, ServletException{
        String conf =
            "server.filters.decorated.config.param1=value1\n" +
            "server.filters.decorated.class=info.magnolia.cms.filters.FilterDecorator\n" +
            "server.filters.decorated.decoratedFilter.class=info.magnolia.cms.beans.config.FilterManagerTest$NotMagnoliaFilter\n";

        initConfigRepository(conf);

        FilterManager fm = FilterManager.getInstance();

        FilterDecorator fd = (FilterDecorator)fm.getFilters()[0];
        FilterConfig filterConfig = createMock(FilterConfig.class);
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        FilterChain chain = createMock(FilterChain.class);

        fd.init(filterConfig);
        fd.doFilter(request, response, chain);

        assertEquals("value1", ((NotMagnoliaFilter)fd.getDecoratedFilter()).param1);
        assertEquals(true, ((NotMagnoliaFilter)fd.getDecoratedFilter()).executed);
    }

    public void testBypassing() throws UnsupportedRepositoryOperationException, IOException, RepositoryException, ServletException{
        // first filter ignores all uris starting with a .
        String conf =
            "server.filters.first.class=info.magnolia.cms.beans.config.FilterManagerTest$TestFilter\n" +
            "server.filters.first.bypasses.dot.pattern=.\n" +
            "server.filters.second.class=info.magnolia.cms.beans.config.FilterManagerTest$TestFilter\n";

        initConfigRepository(conf);

        FilterManager fm = FilterManager.getInstance();

        FilterConfig filterConfig = createMock(FilterConfig.class);
        HttpServletRequest request = createMock(HttpServletRequest.class);
        WebContext webCtx = createMock(WebContext.class);
        expect(webCtx.getAttribute(Path.MGNL_REQUEST_URI_CURRENT)).andStubReturn(".magnolia/something.html");

        MgnlContext.setInstance(webCtx);

        HttpServletResponse response = createMock(HttpServletResponse.class);
        FilterChain chain = createMock(FilterChain.class);

        replay(request, webCtx);

        Filter mainFilter = new MagnoliaMainFilter();
        mainFilter.init(filterConfig);

        mainFilter.doFilter(request, response, chain);

        assertEquals(false, ((TestFilter)fm.getFilters()[0]).executed);
        assertEquals(true, ((TestFilter)fm.getFilters()[1]).executed);
    }

    protected MockHierarchyManager initConfigRepository(String conf) throws IOException, RepositoryException, UnsupportedRepositoryOperationException {
        // ignore mapping warnings
        Logger.getLogger(ContentRepository.class).setLevel(Level.ERROR);

        MockHierarchyManager hm = MockUtil.createHierarchyManager(conf);

        mockObservation(hm);

        ((MockContext)MgnlContext.getInstance()).addHierarchyManager(ContentRepository.CONFIG, hm);
        ((MockContext)MgnlContext.getSystemContext()).addHierarchyManager(ContentRepository.CONFIG, hm);

        return hm;
    }

    protected void mockObservation(MockHierarchyManager hm) throws RepositoryException, UnsupportedRepositoryOperationException {
        // fake observation
        Workspace ws = createMock(Workspace.class);
        ObservationManager om = createMock(ObservationManager.class);

        om.addEventListener(isA(EventListener.class), anyInt(),isA(String.class),anyBoolean(), (String[])anyObject(), (String[]) anyObject(), anyBoolean());

        expect(ws.getObservationManager()).andStubReturn(om);
        hm.setWorkspace(ws);
        replay(ws, om);
    }

    public static class TestFilter extends AbstractMagnoliaFilter{
        public boolean executed = false;

        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
            this.executed = true;
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
