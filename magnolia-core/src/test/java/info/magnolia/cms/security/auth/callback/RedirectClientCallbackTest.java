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
package info.magnolia.cms.security.auth.callback;

import static org.easymock.EasyMock.*;

import java.util.HashMap;
import java.util.Map;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContextImpl;
import info.magnolia.test.ComponentsTestUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RedirectClientCallbackTest {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RedirectClientCallback callback;

    @Before
    public void setUp() throws Exception {
        request = createStrictMock(HttpServletRequest.class);
        response = createStrictMock(HttpServletResponse.class);
        callback = new RedirectClientCallback();
    }

    @After
    public void tearDown() throws Exception {
        replay(request, response);
        callback.handle(request, response);
        verify(request, response);
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

@Test
    public void testExternalUrlsArePassedAsIs() throws Exception {
        callback.setLocation("http://www.magnolia-cms.com");
        expect(request.getRequestURI()).andReturn("/really/does/not/matter");
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/really/does/not/matter"));

        response.sendRedirect("http://www.magnolia-cms.com");
    }

    @Test
    public void testRelativeURLsAreSupported() /* although I hardly see how that could be any useful */ throws Exception {
        callback.setLocation("bar");
        expect(request.getRequestURI()).andReturn("/really/does/not/matter");
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/really/does/not/matter"));

        response.sendRedirect("bar");
    }

    @Test
    public void testAbsoluteURLsArePrefixedWithContextPath() throws Exception {
        callback.setLocation("/bar");
        expect(request.getContextPath()).andReturn("/foo");
        expect(request.getRequestURI()).andReturn("/foo/does/not/matter");
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/foo/does/not/matter"));

        response.sendRedirect("/foo/bar");
    }

    @Test
    public void testDoesNothingIfCurrentRequestURLIsTarget() throws Exception {
        callback.setLocation("/some/path");
        expect(request.getContextPath()).andReturn("/foo");
        expect(request.getRequestURI()).andReturn("/foo/some/path");

        // nothing happens - TODO - maybe this should throw an exception ?
    }

    @Test
    public void testTargetUrlIsFormattedWithEncodedRequestURL() throws Exception {
        callback.setLocation("http://sso.mycompany.com/login/?backto={0}");
        expect(request.getRequestURI()).andReturn("/foo/some/path");
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/foo/some/path"));

        response.sendRedirect("http://sso.mycompany.com/login/?backto=http%3A%2F%2Flocalhost%2Ffoo%2Fsome%2Fpath");
    }

    @Test
    public void testRedirectWithParameters() throws Exception {
        final TestWebContext ctx = new TestWebContext();
        ctx.addParameter("parameter1", "value1");
        ctx.addParameter("parameter2", "value2");
        MgnlContext.setInstance(ctx);

        callback.setLocation("http://sso.mycompany.com/login");
        expect(request.getRequestURI()).andReturn("/foo/some/path");
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/foo/some/path"));

        response.sendRedirect("http://sso.mycompany.com/login?parameter2=value2&parameter1=value1");
    }

    private class TestWebContext extends WebContextImpl{

        private static final long serialVersionUID = 5982180356157623760L;

        private Map<String, String> parameters = new HashMap<String, String>();

        public void addParameter(String key, String value){
            parameters.put(key, value);
        }

        @Override
        public Map<String, String> getParameters() {
            return parameters;
        }

        @Override
        public String[] getParameterValues(String key){
            String [] field = new String[1];
            field[0] = parameters.get(key).toString();
            return field;
        }

        @Override
        protected AggregationState newAggregationState() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
