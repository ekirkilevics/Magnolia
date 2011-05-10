/**
 * This file Copyright (c) 2007-2011 Magnolia International
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
import java.lang.reflect.Field;
import javax.jcr.RepositoryException;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import com.mockrunner.mock.web.MockHttpServletRequest;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.util.CustomFilterConfig;
import info.magnolia.cms.util.ServletUtils;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockAggregationState;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.voting.DefaultVoting;
import info.magnolia.voting.Voting;
import static org.easymock.EasyMock.*;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ServletDispatchingFilterTest extends MgnlTestCase {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);
    }

    public void testEscapeMetaCharacters() {
        assertEquals("a\\{b\\)c\\(d\\}e\\^f\\]g\\[h\\*i\\$j\\+kl",
                Mapping.escapeMetaCharacters("a{b)c(d}e^f]g[h*i$j+kl"));
    }

    public void testSupportsDefaultMapping() throws Exception {
        shouldMatch("/dms/some-doc.pdf", "", "/dms/some-doc.pdf", "/");
        shouldMatch(null, "", "/", "/");
    }

    public void testSupportsPathMapping() throws Exception {
        shouldMatch("/hey/ho/lets/go", "/ramones", "/ramones/hey/ho/lets/go", "/ramones/*");
        shouldMatch(null, "/ramones", "/ramones/", "/ramones/*");
        shouldMatch("/wesh", "/yo", "/yo/wesh", "/yo/*");

        // path mapping end with /*, and as such /my/path should not match for /my/path/*
        shouldBypass("/ramones", "/ramones/*");
    }

    public void testSupportsExtensionMapping() throws Exception {
        shouldMatch(null, "/dms/some-doc.pdf", "/dms/some-doc.pdf", "*.pdf");
    }

    public void testSupportsRegexMappings() throws Exception {
        // the part of the request following the pattern match is the path info:
        shouldMatch("/123/456", "/test", "/test/123/456", "regex:/[a-z]{4}");

        // if pattern ends with a $, extra path info in the request uri does not match:
        shouldMatch(null, "/test", "/test", "regex:^/[a-z]{4}$");
        shouldBypass("/test/blah", "regex:^/[a-z]{4}$");
        shouldBypass("/testx", "regex:^/[a-z]{4}$");

        shouldMatch(null, "/foo/bar473", "/foo/bar473", "regex:^/foo/bar[0-9]{3}[A-Za-z]*");
        shouldMatch(null, "/foo/bar473wEsh", "/foo/bar473wEsh", "regex:^/foo/bar[0-9]{3}[A-Za-z]*");

        shouldMatch("/wEsh", "/foo/bar473", "/foo/bar473/wEsh", "regex:^/foo/bar[0-9]{3}");

        //shouldMatch("/wEsh", "/foo/bar473", "/foo/bar473wEsh", "regex:^/foo/bar[0-9]{3}");
        shouldBypass("/foo/bar473wEsh", "regex:^/foo/bar[0-9]{3}");

        // to avoid the previous case:
        shouldMatch(null, "/foo/bar473", "/foo/bar473", "regex:^/foo/bar[0-9]{3}");
        shouldMatch("/wEsh", "/foo/bar473", "/foo/bar473/wEsh", "regex:^/foo/bar[0-9]{3}");
    }

    public void testShouldNotBypassWhenPathMappingMatches() throws Exception {
        shouldMatch("/some-doc.pdf", "/$d}^(m)+{s*", "/$d}^(m)+{s*/some-doc.pdf", "/$d}^(m)+{s*/*");
    }

    public void testShouldNotBypassWhenExactMappingMatches() throws Exception {
        shouldMatch(null, "/exactMatch", "/exactMatch", "/exactMatch");
        shouldMatch(null, "/somepath*", "/somepath*", "/somepath*");
    }

    public void testShouldBypassWhenMappingDoesNotMatch() throws Exception {
        shouldBypass("/bleh/foo.bar", "/dms/*");
        shouldBypass("/exactMatch/rightPathInfo", "/exactMatch");
        shouldBypass("/nonSpecConformantMapping.html", "/nonSpecConformantMapping*");
        shouldBypass("/nonSpecConformantMapping*/somePage.html", "/nonSpecConformantMapping*");
    }

    public void testShouldBypassWhenMappingDoesNotMatchMAGNOLIA1984() throws Exception {
        shouldBypass("/modules/dms/managingdocs.html", "/dms/*");
    }

    public void testPathInfoShouldAdhereToServletSpec() throws Exception {
        shouldMatch("/pathInfo.html", "/servlet", "/servlet/pathInfo.html", "/servlet/*");
        // The following doesn't work - spec isn't clear as to whether it should or not
        //shouldMatch("/test", "/some-some.foo", "/some-some.foo/test", "*.foo");
    }

    public void testPathInfoShouldStateWhateverIsAfterTheRegexMapping() throws Exception {
        shouldMatch("/test", "/some-doc.pdf", "/some-doc.pdf/test", "regex:.*\\.pdf");
    }

    private void shouldBypass(String requestPath, String mapping) throws Exception {
        doTestBypassAndPathInfo(true, null, null, requestPath, mapping, false);
    }

    private void shouldMatch(final String expectedPathInfo, final String expectedServletPath, final String requestPath, String mapping) throws Exception {
        doTestBypassAndPathInfo(false, expectedPathInfo, expectedServletPath, requestPath, mapping, true);
    }

    private void doTestBypassAndPathInfo(final boolean shouldBypass, final String expectedPathInfo, final String expectedServletPath, final String requestPath, String mapping, boolean shouldCheckPathInfoAndServletPath) throws Exception {
        ComponentsTestUtil.setInstance(Voting.class, new DefaultVoting());
        WebContext ctx = createStrictMock(WebContext.class);
        MgnlContext.setInstance(ctx);
        final AggregationState state = new MockAggregationState();
        expect(ctx.getContextPath()).andReturn("/magnoliaAuthor").anyTimes();

        final FilterChain chain = createNiceMock(FilterChain.class);
        final HttpServletResponse res = createNiceMock(HttpServletResponse.class);
        final HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getAttribute(EasyMock.<String>anyObject())).andReturn(null).anyTimes();
        expect(ctx.getAggregationState()).andReturn(state).anyTimes();
        expect(req.getRequestURI()).andReturn("/magnoliaAuthor" + requestPath).anyTimes();
        expect(req.getContextPath()).andReturn("/magnoliaAuthor").anyTimes();
        expect(req.getServletPath()).andReturn("/magnoliaAuthor" + requestPath).anyTimes();
        expect(req.getPathInfo()).andReturn(null).anyTimes();
        expect(req.getQueryString()).andReturn(null).anyTimes();

        final Servlet servlet = createStrictMock(Servlet.class);
        if (!shouldBypass) {
            servlet.service(isA(HttpServletRequestWrapper.class), same(res));
            expectLastCall().andAnswer(new IAnswer<Object>() {
                @Override
                public Object answer() throws Throwable {
                    final HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) getCurrentArguments()[0];
                    final String pathInfo = requestWrapper.getPathInfo();
                    final String servletPath = requestWrapper.getServletPath();
                    assertEquals("pathInfo does not match", expectedPathInfo, pathInfo);
                    assertEquals("servletPath does not match", expectedServletPath, servletPath);
                    return null;
                }
            });
        }

        replay(chain, res, req, servlet, ctx);

        state.setCurrentURI(requestPath);
        final AbstractMgnlFilter filter = new ServletDispatchingFilter();
        final Field servletField = ServletDispatchingFilter.class.getDeclaredField("servlet");
        servletField.setAccessible(true);
        servletField.set(filter, servlet);

        filter.addMapping(mapping);

        assertEquals("Should " + (shouldBypass ? "" : "not ") + "have bypassed", shouldBypass, !filter.matches(req));
        if (!shouldBypass) {
            filter.doFilter(req, res, chain);
        }

        verify(chain, res, req, servlet, ctx);
    }

    public void testWrapperRespectsForwards() throws RepositoryException, IOException, Content2BeanException, ServletException {

        String testContent = "" +
                "/server/filters/servlets/test.class=" + ServletDispatchingFilter.class.getName() + "\n" +
                "/server/filters/servlets/test.servletClass=" + TestServlet.class.getName() + "\n" +
                "/server/filters/servlets/test.servletName=TestServlet\n" +
                "/server/filters/servlets/test.comment=TestComment\n" +
                "/server/filters/servlets/test.parameters.param1=value1\n" +
                "/server/filters/servlets/test.parameters.param2=value2\n" +
                "/server/filters/servlets/test.mappings.mapping.pattern=/mapping/*";

        MockHierarchyManager hm = MockUtil.createHierarchyManager(testContent);

        ServletDispatchingFilter filter = (ServletDispatchingFilter) Content2BeanUtil.toBean(hm.getContent("/server/filters/servlets/test"), true, new Content2BeanTransformerImpl());

        assertEquals("Wrapper for TestServlet servlet", filter.getName());
        assertEquals("TestComment", filter.getComment());
        assertEquals(2, filter.getParameters().size());
        assertEquals("value1", filter.getParameters().get("param1"));
        assertEquals("value2", filter.getParameters().get("param2"));
        assertEquals(1, filter.getMappings().size());
        filter.init(new CustomFilterConfig("test", null));

        MockHttpServletRequest mock = new MockHttpServletRequest();
        mock.setRequestURI("/magnoliaAuthor/mapping/test.html");
        mock.setContextPath("/magnoliaAuthor");
        mock.setServletPath("");
        mock.setPathInfo("/mapping/test.html");

        MgnlContext.getWebContext().getAggregationState().setCurrentURI(StringUtils.substringAfter(mock.getRequestURI(), mock.getContextPath()));
        filter.doFilter(mock, null, null);

        filter.destroy();
    }

    public static class TestServlet extends HttpServlet {

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertEquals("/magnoliaAuthor/mapping/test.html", req.getRequestURI());
            assertEquals("/magnoliaAuthor", req.getContextPath());
            assertEquals("/mapping", req.getServletPath());
            assertEquals("/test.html", req.getPathInfo());
            assertEquals(null, req.getQueryString());

            // simulate a forward

            MockHttpServletRequest mock = ServletUtils.getWrappedRequest(req, MockHttpServletRequest.class);
            mock.setRequestURI("/magnoliaAuthor/.magnolia/somepage.html");
            mock.setContextPath("/magnoliaAuthor");
            mock.setServletPath("");
            mock.setPathInfo("/.magnolia/somepage.html");

            // test that the ServletDispatcherFitler wrapper lets the new values through
            assertEquals("/magnoliaAuthor/.magnolia/somepage.html", req.getRequestURI());
            assertEquals("/magnoliaAuthor", req.getContextPath());
            assertEquals("", req.getServletPath());
            assertEquals("/.magnolia/somepage.html", req.getPathInfo());
            assertEquals(null, req.getQueryString());
        }
    }
}
