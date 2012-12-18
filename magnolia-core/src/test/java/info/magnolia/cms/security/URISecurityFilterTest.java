/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.cms.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for Security class.
 *
 * @version $Id$
 */
public class URISecurityFilterTest {
    private HttpServletRequest request;
    private HttpServletResponse response;

    @Before
    public void setUp() {
        final AggregationState aggregationState = mock(AggregationState.class);
        final WebContext ctx = mock(WebContext.class);
        MgnlContext.setInstance(ctx);
        when(ctx.getAggregationState()).thenReturn(aggregationState);

        final AccessManager accessManager = mock(AccessManager.class);
        when(ctx.getAccessManager("uri")).thenReturn(accessManager);
        when(accessManager.isGranted(null, PermissionUtil.convertPermissions(Session.ACTION_READ))).thenReturn(false);

        final User user = mock(User.class);
        when(ctx.getUser()).thenReturn(user);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        final IPSecurityManager ipSecurityManager = mock(IPSecurityManager.class);
        when(ipSecurityManager.isAllowed(request)).thenReturn(true);

        ComponentsTestUtil.setInstance(IPSecurityManager.class, ipSecurityManager);
    }

    @Test
    public void testIsAllowedForAnonymous() throws Exception {
        // GIVEN
        when(MgnlContext.getUser().getName()).thenReturn(UserManager.ANONYMOUS_USER);
        when(request.getMethod()).thenReturn("GET");

        final URISecurityFilter filter = new URISecurityFilter();

        // WHEN
        final boolean result = filter.isAllowed(request, response);

        // THEN
        assertEquals(false, result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testIsAllowedForOther() throws Exception {
        // GIVEN
        when(MgnlContext.getUser().getName()).thenReturn("AnyAuthenticatedUser");
        when(request.getMethod()).thenReturn("GET");

        final URISecurityFilter filter = new URISecurityFilter();

        // WHEN
        final boolean result = filter.isAllowed(request, response);

        // THEN
        assertEquals(false, result);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
