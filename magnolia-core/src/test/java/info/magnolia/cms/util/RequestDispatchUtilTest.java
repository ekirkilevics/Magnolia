/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.util;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

/**
 * @version $Id$
 */
public class RequestDispatchUtilTest {

    private static final String FRAGMENT = "/magnoliaTest/.magnolia/pages/adminCentral.html";
    /**
     * RequestDispatchUtil internally calls HttpServletResponse#encodeRewriteURL(String) - with the above FRAGMENT
     * running in a ServletContainer nothing would get replaced but the sessionid would be appended.
     */
    private static final String ENCODED_FRAGMENT = "/magnoliaTest/.magnolia/pages/adminCentral.html;jsessionid=17bcou1dvqju5";
    private static final String EXTERNAL_URL = "http://www.something.com";

    @Test
    public void testDispatchWithNullURI() {
        // GIVEN
        final String targetUri = null;

        // WHEN
        boolean result = RequestDispatchUtil.dispatch(targetUri, null, null);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testDispatchRedirectInternal() throws Exception {
        // GIVEN
        final String contextPath = "contextPath";
        final String targetUri = RequestDispatchUtil.REDIRECT_PREFIX + FRAGMENT;
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        HttpServletResponse response = createNiceMock(HttpServletResponse.class);

        expect(request.getContextPath()).andReturn(contextPath);

        expect(response.encodeRedirectURL(contextPath + FRAGMENT)).andReturn(contextPath  + ENCODED_FRAGMENT);
        // next line states we're expecting such call (after replay)
        response.sendRedirect(contextPath + ENCODED_FRAGMENT);
        Object[] mocks = new Object[] {request, response};
        replay(mocks);

        // WHEN
        boolean result = RequestDispatchUtil.dispatch(targetUri, request, response);

        // THEN
        assertTrue(result);
        verify(mocks);
    }

    @Test
    public void testDispatchRedirectNonInternal() throws Exception {
        // GIVEN
        final String targetUri = RequestDispatchUtil.REDIRECT_PREFIX + EXTERNAL_URL + FRAGMENT;
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        HttpServletResponse response = createNiceMock(HttpServletResponse.class);

        expect(response.encodeRedirectURL(EXTERNAL_URL + FRAGMENT)).andReturn(EXTERNAL_URL  + ENCODED_FRAGMENT);
        // we're expecting call to void method below (after replay)
        response.sendRedirect(EXTERNAL_URL + ENCODED_FRAGMENT);
        Object[] mocks = new Object[] {request, response};
        replay(mocks);

        // WHEN
        boolean result = RequestDispatchUtil.dispatch(targetUri, request, response);

        // THEN
        assertTrue(result);
        verify(mocks);
    }

    @Test
    public void testDispatchRedirectNonInternalFailure() throws Exception{
        // GIVEN
        final String targetUri = RequestDispatchUtil.REDIRECT_PREFIX + EXTERNAL_URL + FRAGMENT;
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        HttpServletResponse response = createNiceMock(HttpServletResponse.class);

        expect(response.encodeRedirectURL(EXTERNAL_URL + FRAGMENT)).andReturn(EXTERNAL_URL  + ENCODED_FRAGMENT);
        // next 3 lines state we're expecting a call to sendRedirect that throws an exception
        response.sendRedirect(EXTERNAL_URL + ENCODED_FRAGMENT);
        Exception exception = new IOException("Something went wrong!");
        expectLastCall().andThrow(exception);

        Object[] mocks = new Object[] {request, response};
        replay(mocks);

        // WHEN
        boolean result = RequestDispatchUtil.dispatch(targetUri, request, response);

        // THEN
        assertTrue(result);
        verify(mocks);
    }

}
