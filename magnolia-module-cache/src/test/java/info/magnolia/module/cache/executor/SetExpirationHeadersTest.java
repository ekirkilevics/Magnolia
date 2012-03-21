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
package info.magnolia.module.cache.executor;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.browsercachepolicy.FixedDuration;
import info.magnolia.module.cache.browsercachepolicy.Never;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

/**
 * Tests for the settings of expiration headers cache executor.
 * @version $Id$
 */
public class SetExpirationHeadersTest {

    @Test
    public void testProcessCacheRequest() throws Exception {
        // GIVEN
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        final Cache cache = mock(Cache.class);
        final CacheConfiguration cacheConfig = new CacheConfiguration();
        final FixedDuration fixed = new FixedDuration();
        fixed.setExpirationMinutes(30);
        cacheConfig.setBrowserCachePolicy(fixed);

        SetExpirationHeaders executor = new SetExpirationHeaders();
        executor.setCacheConfiguration(cacheConfig);

        // WHEN
        executor.processCacheRequest(request, response, chain, cache, null);

        // THEN
        verify(response).setHeader("Pragma", "");
        verify(response).setHeader(eq("Cache-Control"), startsWith("max-age="));
        verify(response).setDateHeader(eq("Expires"), anyLong());
    }

    @Test
    public void testProcessNoCacheRequest() throws Exception {
        // GIVEN
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        final Cache cache = mock(Cache.class);
        final CacheConfiguration cacheConfig = new CacheConfiguration();
        final Never fixed = new Never();
        cacheConfig.setBrowserCachePolicy(fixed);

        SetExpirationHeaders executor = new SetExpirationHeaders();
        executor.setCacheConfiguration(cacheConfig);

        // WHEN
        executor.processCacheRequest(request, response, chain, cache, null);

        // THEN
        verify(response).setHeader("Pragma", "no-cache");
        verify(response).setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
        verify(response).setDateHeader("Expires", 0L);
    }
}
