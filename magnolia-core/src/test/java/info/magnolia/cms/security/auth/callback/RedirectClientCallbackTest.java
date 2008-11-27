/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.security.auth.callback;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RedirectClientCallbackTest extends TestCase {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RedirectClientCallback callback;

    protected void setUp() throws Exception {
        request = createStrictMock(HttpServletRequest.class);
        response = createStrictMock(HttpServletResponse.class);
        callback = new RedirectClientCallback();
    }

    protected void tearDown() throws Exception {
        replay(request, response);
        callback.handle(request, response);
        verify(request, response);
    }

    public void testExternalUrlsArePassedAsIs() throws Exception {
        callback.setLocation("http://www.magnolia-cms.com");
        expect(request.getRequestURI()).andReturn("/really/does/not/matter");
        response.sendRedirect("http://www.magnolia-cms.com");
    }

    public void testRelativeURLsAreSupported() /* although I hardly see how that could be any useful */ throws Exception {
        callback.setLocation("bar");
        expect(request.getRequestURI()).andReturn("/really/does/not/matter");
        response.sendRedirect("bar");
    }

    public void testAbsoluteURLsArePrefixedWithContextPath() throws Exception {
        callback.setLocation("/bar");
        expect(request.getContextPath()).andReturn("/foo");
        expect(request.getRequestURI()).andReturn("/really/does/not/matter");
        response.sendRedirect("/foo/bar");
    }

    public void testDoesNothingIfCurrentRequestURLIsTarget() throws Exception {
        callback.setLocation("/some/path");
        expect(request.getContextPath()).andReturn("/foo");
        expect(request.getRequestURI()).andReturn("/foo/some/path");
        // nothing happens - TODO - maybe this should throw an exception ?
    }
}
