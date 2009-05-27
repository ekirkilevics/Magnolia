/**
 * This file Copyright (c) 2007-2009 Magnolia International
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

import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.voting.DefaultVoting;
import info.magnolia.voting.Voting;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;

import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ServletDispatchingFilterTest extends TestCase {
    protected void tearDown() throws Exception {
        FactoryUtil.clear();
        MgnlContext.setInstance(null); // not needed for this test - kept for consistency with other tests tearDown()
        super.tearDown();
    }

    public void testEscapeMetaCharacters() {
        assertEquals("a\\{b\\)c\\(d\\}e\\^f\\]g\\[h\\*i\\$j\\+kl",
                ServletDispatchingFilter.escapeMetaCharacters("a{b)c(d}e^f]g[h*i$j+kl"));
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
        FactoryUtil.setInstance(Voting.class, new DefaultVoting());

        final FilterChain chain = createNiceMock(FilterChain.class);
        final HttpServletResponse res = createNiceMock(HttpServletResponse.class);
        final HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getRequestURI()).andReturn("/magnoliaAuthor" + requestPath).anyTimes();
        expect(req.getContextPath()).andReturn("/magnoliaAuthor").anyTimes();

        final Servlet servlet = createStrictMock(Servlet.class);
        servlet.service(isA(HttpServletRequestWrapper.class), same(res));
        if (!shouldBypass) {
            expectLastCall().andAnswer(new IAnswer<Object>() {
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

        replay(chain, res, req, servlet);

        final ServletDispatchingFilter filter = new ServletDispatchingFilter();
        final Field servletField = ServletDispatchingFilter.class.getDeclaredField("servlet");
        servletField.setAccessible(true);
        servletField.set(filter, servlet);

        filter.addMapping(mapping);

        assertEquals("Should " + (shouldBypass ? "" : "not ") + "have bypassed", shouldBypass, filter.bypasses(req));
        filter.doFilter(req, res, chain);

        verify(chain, res, req, servlet);
    }
}
