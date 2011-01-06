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

import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.browsercachepolicy.FixedDuration;
import info.magnolia.module.cache.browsercachepolicy.Never;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.*;

import junit.framework.TestCase;

/**
 * Tests for the settings of expiration headers cache executor.
 */
public class SetExpirationHeadersTest extends TestCase {

    public void testProcessCacheRequest() throws Exception {
        final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        final FilterChain chain = createStrictMock(FilterChain.class);
        final Cache cache = createStrictMock(Cache.class);
        final CacheConfiguration cacheConfig = new CacheConfiguration();
        final FixedDuration fixed = new FixedDuration();
        fixed.setExpirationMinutes(30);
        cacheConfig.setBrowserCachePolicy(fixed);

        response.setHeader("Pragma", "");
        response.setHeader("Cache-Control", "max-age=1800, public");
        response.setDateHeader(eq("Expires"), anyLong());

        replay(request, response, chain, cache);
        SetExpirationHeaders executor = new SetExpirationHeaders();
        executor.setCacheConfiguration(cacheConfig);
        executor.processCacheRequest(request, response, chain, cache, null);
        verify(request, response, chain, cache);
    }

    public void testProcessNoCacheRequest() throws Exception {
        final HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        final FilterChain chain = createStrictMock(FilterChain.class);
        final Cache cache = createStrictMock(Cache.class);
        final CacheConfiguration cacheConfig = new CacheConfiguration();
        final Never fixed = new Never();
        cacheConfig.setBrowserCachePolicy(fixed);

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
        response.setDateHeader("Expires", 0L);

        replay(request, response, chain, cache);
        SetExpirationHeaders executor = new SetExpirationHeaders();
        executor.setCacheConfiguration(cacheConfig);
        executor.processCacheRequest(request, response, chain, cache, null);
        verify(request, response, chain, cache);
    }

}
