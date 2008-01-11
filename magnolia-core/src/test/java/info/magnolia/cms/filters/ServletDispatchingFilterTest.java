/**
 * This file Copyright (c) 2007-2008 Magnolia International
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

import info.magnolia.cms.util.FactoryUtil;
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
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ServletDispatchingFilterTest extends TestCase {
    public void testShouldNotBypassWhenDefaultMappingMatches() throws Exception {
        doTestBypassAndPathInfo(false, null, "/dms/some-doc.pdf", "/");
    }
    
    public void testShouldNotBypassWhenPathMappingMatches() throws Exception {
        doTestBypassAndPathInfo(false, "/some-doc.pdf", "/dms/some-doc.pdf", "/dms/*");
        doTestBypassAndPathInfo(false, "/.html", "/nonSpecConformantMapping.html", "/nonSpecConformantMapping*");
    }

    public void testShouldNotBypassWhenExactMappingMatches() throws Exception {
        doTestBypassAndPathInfo(false, null, "/exactMatch", "/exactMatch");
    }
    
    public void testShouldBypassWhenMappingDoesNotMatch() throws Exception {
        doTestBypassAndPathInfo(true, null, "/bleh/foo.bar", "/dms/*");
        doTestBypassAndPathInfo(true, null, "/exactMatch/rightPathInfo", "/exactMatch");
    }

    public void testShouldBypassWhenMappingDoesNotMatchMAGNOLIA1984() throws Exception {
        doTestBypassAndPathInfo(true, null, "/modules/dms/managingdocs.html", "/dms/*");
    }

    /*
    public void testPathInfoShouldStateWhateverIsAfterTheMapping() throws Exception {
        // extension mappings not supported
        doTestBypassAndPathInfo(false, "/test", "/some-doc.pdf/test", "*.pdf");
    }
    */

    private void doTestBypassAndPathInfo(final boolean expectedBypass, final String expectedPathInfo, final String requestPath, String mapping) throws Exception {
        FactoryUtil.setInstance(Voting.class, new DefaultVoting());

        final FilterChain chain = createNiceMock(FilterChain.class);
        final HttpServletResponse res = createNiceMock(HttpServletResponse.class);
        final HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getServletPath()).andReturn(requestPath).anyTimes();
        expect(req.getRequestURI()).andReturn("/magnoliaAuthor" + requestPath).anyTimes();
        expect(req.getContextPath()).andReturn("/magnoliaAuthor").anyTimes();

        final Servlet servlet = createStrictMock(Servlet.class);
        servlet.service(isA(HttpServletRequestWrapper.class), same(res));
        if (expectedPathInfo != null) {
            expectLastCall().andAnswer(new IAnswer<Object>() {
                public Object answer() throws Throwable {
                    final HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) getCurrentArguments()[0];
                    final String pathInfo = requestWrapper.getPathInfo();
                    assertEquals(expectedPathInfo, pathInfo);
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

        assertEquals(expectedBypass, filter.bypasses(req));
        filter.doFilter(req, res, chain);

        verify(chain, res, req, servlet);
    }
}
