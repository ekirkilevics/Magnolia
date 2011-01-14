/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.module.cache.filter;

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import info.magnolia.cms.filters.WebContainerResources;
import info.magnolia.cms.filters.WebContainerResourcesImpl;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;


/**
 * Tests for the treatment of request headers by cache filter.
 */
public class CacheHeadersFilterTest extends MgnlTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);
    }

    public void testFilterCacheRequest() throws Exception {
        final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        final FilterChain chain = createStrictMock(FilterChain.class);

        expect(request.getAttribute(EasyMock.<String>anyObject())).andReturn(null).anyTimes();

        response.setHeader("Pragma", "");
        response.setHeader("Cache-Control", "max-age=86400, public");
        response.setDateHeader(eq("Expires"), anyLong());
        chain.doFilter(request, response);

        replay(request, response, chain);
        CacheHeadersFilter filter = new CacheHeadersFilter();
        filter.doFilter(request, response, chain);
        verify(request, response, chain);
    }

    public void testFilterNoCacheRequest() throws Exception {
        final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        final FilterChain chain = createStrictMock(FilterChain.class);

        expect(request.getAttribute(EasyMock.<String>anyObject())).andReturn(null).anyTimes();

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
        response.setDateHeader("Expires", 0L);
        chain.doFilter(request, response);

        replay(request, response, chain);
        CacheHeadersFilter filter = new CacheHeadersFilter();
        filter.setNocache(true);
        filter.doFilter(request, response, chain);
        verify(request, response, chain);
    }
}
